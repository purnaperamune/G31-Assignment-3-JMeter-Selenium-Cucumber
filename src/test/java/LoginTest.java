import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.List;

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

    // HELPER METHODS

    // This helper method returns the error message shown on the page.
    private String get_ErrorMessage() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#rightPanel p.error")))
                .getText();
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

    // Test Case 2 (positive): verify that after logging in, at least one of the customer's accounts and its balance are listed on the Accounts Overview page
    @Test
    @SpiraTestCase(testCaseId = 53471)
    @DisplayName("Verify that after the customer logs in, the Accounts Overview page lists at least one of the customer's accounts with a balance")
    public void valid_AfterLogin_AccountWithBalanceDisplayed() {
        login(unique_username, TEST_PASSWORD);
        // To wait until at least one account link is listed in the accounts table
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#accountTable tbody a")));

        // To find every table row that contains an account link
        List<WebElement> account_rows = driver.findElements(By.xpath("//table[@id='accountTable']/tbody/tr[td/a]"));
        assertFalse(account_rows.isEmpty(), "The table should have at-least one account row");

        // To get the balance from the second cell (td) of the first account row
        String account_balance = account_rows.get(0).findElements(By.tagName("td")).get(1).getText();
        assertTrue(account_balance.startsWith("$"), "The balance should be displayed in dollars");
    }

    // Test Case 3 (negative): verify that login is not successful with an incorrect password
    @Test
    @SpiraTestCase(testCaseId = 53472)
    @DisplayName("Verify that login is not successful when the password is incorrect")
    public void invalid_Login_WithIncorrectPassword() {
        // Try to log in with the correct username but a wrong password
        login(unique_username, "WrongPass@1");

        // ParaBank shows an "Error!" heading and a verification error message
        assertEquals("Error!", driver.findElement(By.cssSelector("#rightPanel h1.title")).getText());
        assertEquals("The username and password could not be verified.", get_ErrorMessage());
    }

    // Test Case 4 (negative): verify that login is not successful with a username that is not registered
    @Test
    @SpiraTestCase(testCaseId = 53473)
    @DisplayName("Verify that login is not successful with a username that is not registered before")
    public void invalid_Login_WithUsernameNotRegistered() {
        // Adding the current time makes sure this username has never been registered
        login("rose" + System.currentTimeMillis(), TEST_PASSWORD);

        assertEquals("The username and password could not be verified.", get_ErrorMessage());
        // If an empty list is returned, there is no "Log Out" link, so the customer is not logged in
        assertTrue(driver.findElements(By.linkText("Log Out")).isEmpty(), "The customer should not get logged in");
    }

    // Test Case 5 (negative): verify that login is not successful when both the username and password are left empty
    @Test
    @SpiraTestCase(testCaseId = 53474)
    @DisplayName("Verify that login is not successful when no username or password is entered")
    public void invalid_Login_WithNoCredentialsEntered() {
        // To click Log In without entering a username or password
        login("", "");

        assertEquals("Please enter a username and password.", get_ErrorMessage());
        // The browser tab title should not change to the Accounts Overview page
        assertNotEquals("ParaBank | Accounts Overview", driver.getTitle(),
                "The Accounts Overview page should not be displayed");
    }

    // Test Case 6 (negative): verify that login is not successful when the username is left empty and only the password is entered
    @Test
    @SpiraTestCase(testCaseId = 53475)
    @DisplayName("Verify that login is not successful when the username is not entered and only the password is entered")
    public void invalid_Login_WithNoUsername() {
        // To click Log In with an empty username and the correct password
        login("", TEST_PASSWORD);

        assertEquals("Please enter a username and password.", get_ErrorMessage());
        // If an empty list is returned, there is no "Log Out" link, so the customer is not logged in
        assertTrue(driver.findElements(By.linkText("Log Out")).isEmpty(), "The customer should not get logged in");
    }

    // Test Case 7 (negative): verify that login is not successful when the password is left empty and only the username is entered
    @Test
    @SpiraTestCase(testCaseId = 53476)
    @DisplayName("Verify that login is not successful when the password is not entered and only the username is entered")
    public void invalid_Login_WithNoPassword() {
        // To click Log In with the correct username and an empty password
        login(unique_username, "");

        assertEquals("Please enter a username and password.", get_ErrorMessage());
        // The login is not successful, so the page address should not change to the Accounts Overview page
        assertFalse(driver.getCurrentUrl().contains("overview.htm"), "The Accounts Overview page should not be displayed");
    }

    // Test Case 8 (negative): verify that login is not successful when the username is entered in the wrong letter case
    @Test
    @SpiraTestCase(testCaseId = 53477)
    @DisplayName("Verify that login is not successful when the registered username is entered in the wrong letter case")
    public void invalid_Login_WithUsernameInWrongCase() {
        // For example "user1760000000000" becomes "USER1760000000000", so the username is the same but in capital letters
        String wrongCase_username = unique_username.toUpperCase();
        // To make sure the test data is really different from the registered username
        assertNotEquals(unique_username, wrongCase_username, "The username should differ only by letter case");

        login(wrongCase_username, TEST_PASSWORD);
        assertEquals("The username and password could not be verified.", get_ErrorMessage());
    }

}
