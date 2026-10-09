package stepdefinitions;

import io.cucumber.java.en.Given;

import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class CommonSteps {

    private static final String USERNAME =
            "purna";

    private static final String PASSWORD =
            "purna";

    private static final String BASE_URL =
            "https://parabank.parasoft.com/parabank/index.htm";

    @Given("I am logged in as a ParaBank customer")
    public void iAmLoggedInAsAParaBankCustomer() {

        Hooks.driver.get(BASE_URL);

        WebDriverWait wait =
                new WebDriverWait(
                        Hooks.driver,
                        Duration.ofSeconds(10)
                );

        // Enter username
        wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.name("username")
                )
        ).sendKeys(USERNAME);

        // Enter password
        Hooks.driver.findElement(
                By.name("password")
        ).sendKeys(PASSWORD);

        // Click login
        Hooks.driver.findElement(
                By.cssSelector("input[value='Log In']")
        ).click();

        // "Log Out" is used to confirm successful authentication
        wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.linkText("Log Out")
                )
        );

        assertTrue(
                Hooks.driver.findElement(
                        By.linkText("Log Out")
                ).isDisplayed(),
                "Login was not successful."
        );
    }
}