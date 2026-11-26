package ch.admin.bit.jeap.config.aws.appconfig.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.appconfigdata.AppConfigDataClient;
import software.amazon.awssdk.services.appconfigdata.model.GetLatestConfigurationRequest;
import software.amazon.awssdk.services.appconfigdata.model.GetLatestConfigurationResponse;
import software.amazon.awssdk.services.appconfigdata.model.StartConfigurationSessionRequest;
import software.amazon.awssdk.services.appconfigdata.model.StartConfigurationSessionResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JeapAppConfigDataClientChangeNotificationTest {

    private static final SdkBytes INITIAL = SdkBytes.fromUtf8String("{\"key\":\"value\"}");
    private static final SdkBytes CHANGED = SdkBytes.fromUtf8String("{\"key\":\"new value\"}");
    private static final SdkBytes UNCHANGED = SdkBytes.fromUtf8String("");

    @Mock
    private AppConfigDataClient appConfigDataClient;

    @Mock
    private StartConfigurationSessionResponse startConfigurationSessionResponse;

    @Mock
    private GetLatestConfigurationResponse getLatestConfigurationResponse;

    @Mock
    private JeapAppConfigDataChangedListener listener;

    private JeapAppConfigDataClient jeapAppConfigDataClient;

    @BeforeEach
    void setup() {
        when(appConfigDataClient.startConfigurationSession(any(StartConfigurationSessionRequest.class))).thenReturn(startConfigurationSessionResponse);
        when(appConfigDataClient.getLatestConfiguration(any(GetLatestConfigurationRequest.class))).thenReturn(getLatestConfigurationResponse);
        lenient().when(getLatestConfigurationResponse.contentType()).thenReturn("application/json");
        jeapAppConfigDataClient = new JeapAppConfigDataClient(appConfigDataClient, "appId", "envId", "profileId", false);
    }

    @Test
    void initialLoad_doesNotNotifyListener() {
        when(getLatestConfigurationResponse.configuration()).thenReturn(INITIAL);
        jeapAppConfigDataClient.setConfigDataChangedListener(listener);

        jeapAppConfigDataClient.retrieveCurrentConfiguration();

        verifyNoInteractions(listener);
    }

    @Test
    void changedConfiguration_notifiesListenerOnce() {
        when(getLatestConfigurationResponse.configuration()).thenReturn(INITIAL, CHANGED, UNCHANGED);
        jeapAppConfigDataClient.setConfigDataChangedListener(listener);

        jeapAppConfigDataClient.retrieveCurrentConfiguration();
        jeapAppConfigDataClient.retrieveCurrentConfiguration();
        jeapAppConfigDataClient.retrieveCurrentConfiguration();

        verify(listener, times(1)).appConfigDataChanged("profileId");
    }

    @Test
    void changeLoadedBeforeListenerIsSet_notifiesListenerWhenSet() {
        when(getLatestConfigurationResponse.configuration()).thenReturn(INITIAL, CHANGED);
        jeapAppConfigDataClient.retrieveCurrentConfiguration();
        jeapAppConfigDataClient.retrieveCurrentConfiguration();
        assertThat(jeapAppConfigDataClient.getProperties()).containsEntry("key", "new value");

        jeapAppConfigDataClient.setConfigDataChangedListener(listener);

        verify(listener, times(1)).appConfigDataChanged("profileId");
    }

    @Test
    void noChangeLoaded_settingListenerDoesNotNotify() {
        when(getLatestConfigurationResponse.configuration()).thenReturn(INITIAL, UNCHANGED);
        jeapAppConfigDataClient.retrieveCurrentConfiguration();
        jeapAppConfigDataClient.retrieveCurrentConfiguration();

        jeapAppConfigDataClient.setConfigDataChangedListener(listener);

        verifyNoInteractions(listener);
    }

    @Test
    void listenerFails_notificationRetriedWithNextPollUntilSuccessful() {
        when(getLatestConfigurationResponse.configuration()).thenReturn(INITIAL, CHANGED, UNCHANGED);
        doThrow(new NullPointerException("refresh failed"))
                .doThrow(new IllegalStateException("refresh failed again"))
                .doNothing()
                .when(listener).appConfigDataChanged("profileId");
        jeapAppConfigDataClient.setConfigDataChangedListener(listener);

        jeapAppConfigDataClient.retrieveCurrentConfiguration(); // initial load
        jeapAppConfigDataClient.retrieveCurrentConfiguration(); // change, notification fails
        jeapAppConfigDataClient.retrieveCurrentConfiguration(); // unchanged, retry fails
        jeapAppConfigDataClient.retrieveCurrentConfiguration(); // unchanged, retry succeeds
        jeapAppConfigDataClient.retrieveCurrentConfiguration(); // unchanged, nothing pending

        verify(listener, times(3)).appConfigDataChanged("profileId");
    }
}
