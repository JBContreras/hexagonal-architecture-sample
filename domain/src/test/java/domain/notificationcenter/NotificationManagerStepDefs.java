package domain.notificationcenter;

import cucumber.api.java.Before;
import cucumber.api.java.en.But;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import domain.filemanager.mock.MockFile;
import domain.filemanager.mock.MockFileEventNotification;
import domain.filemanager.spi.FileEventNotification;
import domain.notificationcenter.core.NotificationCenterServiceImpl;
import domain.notificationcenter.core.NotificationServiceConfigurationImpl;
import domain.notificationcenter.mock.MockEventNotificationServiceOne;
import domain.notificationcenter.mock.MockEventNotificationServiceTwo;
import domain.notificationcenter.mock.MockInMemoryNotificationSetting;
import domain.notificationcenter.mock.MockInMemoryServiceConfiguration;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class NotificationManagerStepDefs {
    private NotificationCenterServiceImpl notificationManagerService;
    private MockEventNotificationServiceOne mockNotificationServiceOne;
    private MockEventNotificationServiceTwo mockNotificationServiceTwo;
    private MockInMemoryNotificationSetting mockInMemoryUserEventSettings;
    private MockInMemoryServiceConfiguration mockInMemoryUserServiceConfigurationSettings;

    @Before
    public void setUp() throws Exception {
        mockNotificationServiceOne = new MockEventNotificationServiceOne();
        mockNotificationServiceTwo = new MockEventNotificationServiceTwo();
        mockInMemoryUserEventSettings = new MockInMemoryNotificationSetting();
        mockInMemoryUserServiceConfigurationSettings = new MockInMemoryServiceConfiguration();
        notificationManagerService = new NotificationCenterServiceImpl(
                Arrays.asList(mockNotificationServiceOne, mockNotificationServiceTwo),
                mockInMemoryUserEventSettings,
                mockInMemoryUserServiceConfigurationSettings);
    }

    @Given("^'(.*)' set '(.*)' notification setting to '(.*)' for '(.*)'$")
    public void user_set_notificationTypes_notification_setting_to_trueOrFalse_for_selectedServices(String userId, List<FileEventNotification.Type> types,  boolean isEnable, List<String> servicesId) throws Throwable {
        notificationManagerService.setUserNotificationSetting(userId, servicesId, types, isEnable);
    }

    @When("^'(.*)' receive '(.*)' notification$")
    public void user_receive_notificationTypes_notification(String userId, List<FileEventNotification.Type> types) throws Throwable {
        types.stream().forEach(type ->
                        notificationManagerService.sendNotification(
                                type,
                                userId,
                                Collections.singletonList(new MockFile("NewFile", userId)),
                                Collections.emptyMap())
        );
    }

    @Then("^'(.*)' has '(.*)' notification through all services$")
    public void user_has_notificationTypes_notification(String userId, List<FileEventNotification.Type> types) throws Throwable {
        assertThat(mockNotificationServiceOne.getMockFileEventHandlerList()).hasSize(types.size());
        assertThat(mockNotificationServiceOne.getMockFileEventHandlerList().get(0).getUserId()).isEqualTo(userId);
        assertThat(mockNotificationServiceOne.getMockFileEventHandlerList().get(0).getType()).isIn(types);
        assertThat(mockNotificationServiceTwo.getMockFileEventHandlerList()).hasSize(types.size());
        assertThat(mockNotificationServiceTwo.getMockFileEventHandlerList().get(0).getUserId()).isEqualTo(userId);
        assertThat(mockNotificationServiceTwo.getMockFileEventHandlerList().get(0).getType()).isIn(types);
    }

    @Then("^'(.*)' has '(.*)' notification from '(.*)'$")
    public void user_has_notificationTypes_notification_from_selectedServices(String userId, List<FileEventNotification.Type> types, List<String> servicesId) throws Throwable {
        servicesId.stream().forEach(serviceId -> user_has_notificationTypes_notification(userId, types, serviceId));
    }

    public void user_has_notificationTypes_notification(String userId, List<FileEventNotification.Type> types, String serviceId) {
        if ("ServiceOne".equals(serviceId)) {
            assertThat(mockNotificationServiceOne.getMockFileEventHandlerList()).hasSize(types.size());
            assertThat(mockNotificationServiceOne.getMockFileEventHandlerList().get(0).getUserId()).isEqualTo(userId);
            assertThat(mockNotificationServiceOne.getMockFileEventHandlerList()).hasSize(types.size());
            assertThat(mockNotificationServiceOne.getMockFileEventHandlerList().get(0).getUserId()).isEqualTo(userId);
            assertThat(mockNotificationServiceOne.getMockFileEventHandlerList().get(0).getType()).isIn(types);
        }
        else if ("ServiceTwo".equals(serviceId)) {
            assertThat(mockNotificationServiceTwo.getMockFileEventHandlerList()).hasSize(types.size());
            assertThat(mockNotificationServiceTwo.getMockFileEventHandlerList().get(0).getUserId()).isEqualTo(userId);
            assertThat(mockNotificationServiceTwo.getMockFileEventHandlerList()).hasSize(types.size());
            assertThat(mockNotificationServiceTwo.getMockFileEventHandlerList().get(0).getUserId()).isEqualTo(userId);
            assertThat(mockNotificationServiceTwo.getMockFileEventHandlerList().get(0).getType()).isIn(types);
        }
        else {
            throw new RuntimeException("Unknown Service:" + serviceId);
        }
    }

    @But("^'(.*)' has not '(.*)' notification from '(.*)'$")
    public void user_has_not_notificationTypes_notification_from_selectedServices(String userId, List<FileEventNotification.Type> types, List<String> servicesId) throws Throwable {
        servicesId.stream().forEach(serviceId -> user_has_not_notificationTypes_notification(userId, types, serviceId));
    }

    public void user_has_not_notificationTypes_notification(String userId, List<FileEventNotification.Type> types, String serviceId) {
        if ("ServiceOne".equals(serviceId)) {
            List<FileEventNotification.Type> typesSave = mockNotificationServiceOne.getMockFileEventHandlerList().stream()
                    .map(MockFileEventNotification::getType)
                    .collect(Collectors.toList());
            assertThat(typesSave).isNotIn(types);
        }
        else if ("ServiceTwo".equals(serviceId)) {
            List<FileEventNotification.Type> typesSave = mockNotificationServiceTwo.getMockFileEventHandlerList().stream()
                    .map(MockFileEventNotification::getType)
                    .collect(Collectors.toList());
            assertThat(typesSave).isNotIn(types);
        } else {
            throw new RuntimeException("Unknown Service:" + serviceId);
        }
    }

    @Then("^'(.*)' set '(.*)' notification setting to '(.*)' for '(.*)', '(.*)' is throw$")
    public void user_set_type_notification_setting_to_trueOrFalse_for_NotImplementedService_UnknownNotificationServiceException_is_throw(String userId, List<FileEventNotification.Type> types,  boolean isEnable, List<String> servicesId, String throwClassName) throws Throwable {
        // Setup
        Exception exception = null;

        // Exercise
        try {
            notificationManagerService.setUserNotificationSetting(userId, servicesId, types, isEnable);
        } catch (Exception e) {
            exception = e;
        }

        // Verify
        assertThat(exception).isNotNull();
        assertThat(exception.getClass().getSimpleName()).isEqualTo(throwClassName);
    }

    @Given("^'(.*)' configure '(.*)' with '(.*)'='(.*)'$")
    public void user_configure_service_with_key_value(String userId, String serviceId, String key, String value) throws Throwable {
        notificationManagerService.setUserServiceConfiguration(userId, new NotificationServiceConfigurationImpl(serviceId, Collections.singletonMap(key, value)));
    }

    @Then("^'(.*)' receive '(.*)' notification, '(.*)' is throw$")
    public void user_receive_notification_Exception_is_throw(String userId, List<FileEventNotification.Type> types, String throwClassName) throws Throwable {
        // Setup
        Exception exception = null;

        // Exercise
        try {
            types.stream().forEach(type ->
                            notificationManagerService.sendNotification(
                                    type,
                                    userId,
                                    Collections.singletonList(new MockFile("NewFile", userId)),
                                    Collections.emptyMap())
            );
        } catch (Exception e) {
            exception = e;
        }

        // Verify
        assertThat(exception).isNotNull();
        assertThat(exception.getClass().getSimpleName()).isEqualTo(throwClassName);
    }
}
