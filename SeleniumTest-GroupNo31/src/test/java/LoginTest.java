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
// Test cases: TC-LOG-01 to TC-LOG-12

import com.inflectra.spiratest.addons.junitextension.SpiraTestCase;
import com.inflectra.spiratest.addons.junitextension.SpiraTestConfiguration;

@SpiraTestConfiguration (
//following are REQUIRED
        url = "https://rmit.spiraservice.net",
        login = "S4143272",
        rssToken = "{4B9371F7-82E4-4134-980E-8FC6BF544028}",
        projectId = 1203,
//following are OPTIONAL
        releaseId = 2977,
        testSetId = 6865
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

    // This helper method registers a customer with a unique last name and SSN, then logs out.
    // Every other test customer has the same name and SSN, so a unique customer makes sure
    // the Forgot login info page finds only this customer.
    private String register_UniqueCustomer(String last_name, String ssn) {
        String new_username = "rose" + System.currentTimeMillis();
        driver.findElement(By.linkText("Register")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("customer.firstName")));

        driver.findElement(By.id("customer.firstName")).sendKeys("Roshni");
        driver.findElement(By.id("customer.lastName")).sendKeys(last_name);
        driver.findElement(By.id("customer.address.street")).sendKeys("1 Test Street");
        driver.findElement(By.id("customer.address.city")).sendKeys("Melbourne");
        driver.findElement(By.id("customer.address.state")).sendKeys("VIC");
        driver.findElement(By.id("customer.address.zipCode")).sendKeys("3000");
        driver.findElement(By.id("customer.ssn")).sendKeys(ssn);
        driver.findElement(By.id("customer.username")).sendKeys(new_username);
        driver.findElement(By.id("customer.password")).sendKeys(TEST_PASSWORD);
        driver.findElement(By.id("repeatedPassword")).sendKeys(TEST_PASSWORD);
        driver.findElement(By.cssSelector("input[value='Register']")).click();
        wait.until(ExpectedConditions.textToBePresentInElementLocated(
                By.id("rightPanel"), "Your account was created successfully"));

        // To log out, so the test can use the Forgot login info page as a logged-out customer
        driver.findElement(By.linkText("Log Out")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));
        return new_username;
    }

    // This helper method opens the Forgot login info page, fills in the customer's details and submits the form.
    private void submit_ForgotLoginForm(String last_name, String ssn) {
        // By.partialLinkText finds the "Forgot login info?" link using only part of its text
        driver.findElement(By.partialLinkText("Forgot login")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("lookupForm")));

        driver.findElement(By.id("firstName")).sendKeys("Roshni");
        driver.findElement(By.id("lastName")).sendKeys(last_name);
        driver.findElement(By.id("address.street")).sendKeys("1 Test Street");
        driver.findElement(By.id("address.city")).sendKeys("Melbourne");
        driver.findElement(By.id("address.state")).sendKeys("VIC");
        driver.findElement(By.id("address.zipCode")).sendKeys("3000");
        driver.findElement(By.id("ssn")).sendKeys(ssn);
        driver.findElement(By.xpath("//form[@id='lookupForm']//input[@value='Find My Login Info']")).click();
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

    // Test Case 9 (positive): verify that a logged-in customer can log out and is redirected to the login form
    @Test
    @SpiraTestCase(testCaseId = 53478)
    @DisplayName("Verify that the login form is displayed after a customer logs out")
    public void valid_AfterLogout_LoginFormDisplayed() {
        login(unique_username, TEST_PASSWORD);
        // To wait until the "Log Out" link can be clicked, then click it
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Log Out"))).click();

        // To wait until the username box appears again inside the login panel
        WebElement username_field = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//div[@id='loginPanel']//input[@name='username']")));

        assertTrue(username_field.isDisplayed(), "The login form should be displayed after logging out");
        assertTrue(driver.getCurrentUrl().contains("index.htm"), "The customer should be returned to the home page");
        assertEquals("Customer Login", driver.findElement(By.cssSelector("#leftPanel h2")).getText());
        // If an empty list is returned, there is no "Log Out" link, so the customer is logged out
        assertTrue(driver.findElements(By.linkText("Log Out")).isEmpty(), "The customer should be logged out");
    }

    // Test Case 10 (positive): verify that a customer can recover their username and password by entering the same details used during registration on the Forgot login info page
    @Test
    @SpiraTestCase(testCaseId = 53479)
    @DisplayName("Verify that a customer can recover their username and password by entering their registered details on the Forgot login info page")
    public void valid_RecoverLoginCredentials_WithRegisteredDetails() {
        // To prepare unique details for the registration
        String time_stamp = String.valueOf(System.currentTimeMillis());
        String last_name = "Chandra" + time_stamp;
        String ssn = time_stamp.substring(time_stamp.length() - 9);
        String recovered_username = register_UniqueCustomer(last_name, ssn);

        submit_ForgotLoginForm(last_name, ssn);
        wait.until(ExpectedConditions.textToBePresentInElementLocated(
                By.id("rightPanel"), "Your login information was located successfully"));

        // To read all the text in the recovered login details panel
        String panel_text = driver.findElement(By.id("rightPanel")).getText();
        assertAll("Recovered login details",
                () -> assertEquals("Customer Lookup", driver.findElement(By.className("title")).getText()),
                () -> assertTrue(panel_text.contains("Username: " + recovered_username), "The username should be displayed"),
                () -> assertTrue(panel_text.contains("Password: " + TEST_PASSWORD), "The password should be displayed"),
                // ParaBank also logs the customer in, so the "Log Out" link should be shown
                () -> assertTrue(driver.findElement(By.linkText("Log Out")).isDisplayed(), "The customer should be logged in")
        );
    }

    // Test Case 11 (negative): verify that an error is displayed when the details entered to recover credentials do not match any customer
    @Test
    @SpiraTestCase(testCaseId = 53483)
    @DisplayName("Verify that an error is displayed on the Forgot login info page when the details entered to recover credentials do not match any registered customer")
    public void invalid_RecoverLoginCredentials_WithUnregisteredDetails() {
        // To submit a unique last name and SSN that no customer has registered with
        String time_stamp = String.valueOf(System.currentTimeMillis());
        submit_ForgotLoginForm("Unknown" + time_stamp, time_stamp.substring(time_stamp.length() - 9));

        assertEquals("The customer information provided could not be found.", get_ErrorMessage());
        // If an empty list is returned, there is no "Log Out" link, so the customer is not logged in
        assertTrue(driver.findElements(By.linkText("Log Out")).isEmpty(), "The customer should not get logged in");
    }

    // Test Case 12 (boundary): verify that login is not successful when the password is one character shorter than the correct password
    @Test
    @SpiraTestCase(testCaseId = 53484)
    @DisplayName("Verify that login is not successful when the password is one character shorter than the correct password")
    public void boundary_Login_WithPasswordOneCharacterShort() {
        // "Test@123" becomes "Test@12", so the last character is removed
        String short_password = TEST_PASSWORD.substring(0, TEST_PASSWORD.length() - 1);
        // One character shorter than the correct password
        assertEquals(TEST_PASSWORD.length() - 1, short_password.length(), "The password should be one character shorter");

        login(unique_username, short_password);

        assertEquals("The username and password could not be verified.", get_ErrorMessage());
        // If an empty list is returned, there is no "Log Out" link, so the customer is not logged in
        assertTrue(driver.findElements(By.linkText("Log Out")).isEmpty(), "The customer should not get logged in");
    }

}
