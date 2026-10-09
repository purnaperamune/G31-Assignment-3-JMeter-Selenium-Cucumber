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

        // Disable Chrome password manager
        Map<String, Object> prefs = new HashMap<>();

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

        options.setExperimentalOption(
                "prefs",
                prefs
        );

        // Disable breached password warning
        options.addArguments(
                "--disable-features=PasswordLeakDetection"
        );

        driver = new ChromeDriver(options);

        driver.manage().window().maximize();
    }

    @After
    public void tearDown() {

        if (driver != null) {
            driver.quit();
        }
    }
}