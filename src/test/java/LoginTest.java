import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.support.ui.ExpectedConditions;

import static org.junit.jupiter.api.Assertions.*;

// Owner: Member 3 – Login tests
// Requirement: REQ-02 User Login
// Test cases: TC-LOG-01 to TC-LOG-11
//

import com.inflectra.spiratest.addons.junitextension.SpiraTestCase;
import com.inflectra.spiratest.addons.junitextension.SpiraTestConfiguration;

@SpiraTestConfiguration (
//following are REQUIRED
        url = "https://rmit.spiraservice.net/",
        login = "S4143272",
        rssToken = "{F88666FD-CCD6-479B-B1D6-858DFF329B23}",
        projectId = 1203,
//following are OPTIONAL
        releaseId = -1,
        testSetId = -1
)

// This class extends BaseTest
// Opens a new Chrome window on the ParaBank home page before every test
// Closes the browser after every test
public class LoginTest extends BaseTest {

    // To store the username of the new customer created before each test
    private String unique_username;

    // This method runs before each test
    // A new customer is registered and then logged out, so every test has a known account.
    // This also keeps the tests working even if ParaBank resets its database and removes old users.
    @BeforeEach
    public void newCustomer_RegisterAndLogout() {
        // registerNewUser() returns the username after registering a customer
        unique_username = registerNewUser();
        // Registering logs the customer in automatically, so log out to get back to the login form
        driver.findElement(By.linkText("Log Out")).click();
        // To wait until the login page appears (By.name finds the username box)
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));
    }

    // ---------- Tests for Login Requirement ----------

    // Test Case 1 (positive): verify that a customer can log in with correct details,
    // and the customer should be redirected to accounts overview page if successfully logged in
    @Test
    @SpiraTestCase(testCaseId = 53415)
    @DisplayName("Verify that a customer can successfully log in with a valid username and password")
    public void valid_Login_WithCorrectCredentials() {
        // login() is a helper in BaseTest that types the username and password and clicks Log In
        login(unique_username, TEST_PASSWORD);
        // Wait until the browser tab title changes to the Accounts Overview page
        wait.until(ExpectedConditions.titleIs("ParaBank | Accounts Overview"));

        // assertAll: runs all the checks and reports all failures together
        assertAll("Successful login",
                () -> assertEquals("Accounts Overview", driver.findElement(By.className("title")).getText()),
                () -> assertTrue(driver.getCurrentUrl().contains("overview.htm")),
                // A "Log Out" link shows that the customer is currently logged in
                () -> assertTrue(driver.findElement(By.linkText("Log Out")).isDisplayed())
        );
        // To check that the username box no longer exists
        assertThrows(NoSuchElementException.class, () -> driver.findElement(By.name("username")),
                "Once the customer is logged in successfully the login form should not be displayed");
    }

}
