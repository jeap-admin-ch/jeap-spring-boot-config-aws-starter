package ch.admin.bit.jeap.config.aws.appconfig.refresh;

import ch.admin.bit.jeap.config.aws.appconfig.client.JeapAppConfigDataChangedListener;
import ch.admin.bit.jeap.config.aws.appconfig.client.JeapAppConfigDataClient;
import ch.admin.bit.jeap.config.aws.appconfig.config.AppConfigPropertySource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.cloud.context.refresh.ContextRefresher;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.ConfigurableEnvironment;

import java.util.List;


/**
 * Refreshes the application context when the configuration of an AppConfig profile changes.
 * <p>
 * The refresher registers itself on the {@link JeapAppConfigDataClient}s only once the application has started.
 * A context refresh must not run while the application context is still being refreshed for the first time: Spring
 * Cloud's {@code ConfigDataContextRefresher} only knows the {@code SpringApplication} once the initial refresh has
 * completed, and fails with a {@link NullPointerException} before that. Changes polled before the registration
 * stay pending in the client and trigger a refresh as soon as the refresher registers.
 */
@Slf4j
@RequiredArgsConstructor
public class AppConfigContextRefresher implements JeapAppConfigDataChangedListener, ApplicationListener<ApplicationStartedEvent> {

    private final ConfigurableEnvironment environment;
    private final ContextRefresher refresher;

    @Override
    public void onApplicationEvent(ApplicationStartedEvent event) {
        registerAsDataChangedListener();
    }

    void registerAsDataChangedListener() {
        // Register this context refresher as the change listener with all config property sources backed by a JeapAppConfigDataClient.
        // Collect the clients first: registering may run a pending refresh, which replaces the property sources.
        List<JeapAppConfigDataClient> clients = environment.getPropertySources().stream()
                .filter(propertySource -> propertySource instanceof AppConfigPropertySource)
                .map(propertySource -> (AppConfigPropertySource<?>) propertySource)
                .filter(appConfigPropertySource -> appConfigPropertySource.getSource() instanceof JeapAppConfigDataClient)
                .map(appConfigPropertySource -> (JeapAppConfigDataClient) appConfigPropertySource.getSource())
                .toList();
        clients.forEach(jeapAppConfigDataClient -> jeapAppConfigDataClient.setConfigDataChangedListener(this));
    }

    @Override
    public void appConfigDataChanged(String profileId) {
        log.info("Starting refresh of spring boot application context to reflect a new configuration from app config profile '{}'.", profileId);
        refresher.refresh();
        log.info("Spring boot application context refresh for app config profile '{}' completed.", profileId);
    }

}
