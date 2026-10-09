package stepdefinitions;

import io.cucumber.java.After;
import io.cucumber.java.Before;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.util.HashMap;
import java.util.Map;

public class Hooks {

    public static WebDriver driver;

    @Before
    public void setUp() {

        ChromeOptions options = new ChromeOptions();

        Map<String, Object> prefs = new HashMap<>();

        // Disable password manager
        prefs.put(
                "credentials_enable_service",
                false
        );

        prefs.put(
                "profile.password_manager_enabled",
                false
        );

        prefs.put(
                "profile.password_manager_leak_detection",
                false
        );

        // Disable address/autofill saving popup
        prefs.put(
                "autofill.profile_enabled",
                false
        );

        prefs.put(
                "autofill.credit_card_enabled",
                false
        );

        // Disable browser notifications
        prefs.put(
                "profile.default_content_setting_values.notifications",
                2
        );

        options.setExperimentalOption(
                "prefs",
                prefs
        );

        // Disable Chrome popup features that may interfere with testing
        options.addArguments(
                "--disable-features=PasswordLeakDetection,AutofillAddressProfileSavePrompt"
        );

        driver = new ChromeDriver(options);

        driver.manage()
                .window()
                .maximize();
    }


    @After
    public void tearDown() {

        if (driver != null) {
            driver.quit();
        }
    }
}