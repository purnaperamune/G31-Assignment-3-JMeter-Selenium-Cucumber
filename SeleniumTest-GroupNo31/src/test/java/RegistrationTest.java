import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

// Owner: Member 3 – User Registration tests
// Requirement: REQ-01 User Registration
// Test cases: TC-REG-01 to TC-REG-08

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
        testSetId = 6864
)

// This class extends BaseTest
// Opens a new Chrome window on the ParaBank home page before every test
// Closes the browser after every test
public class RegistrationTest extends BaseTest {

    // This method runs before each test
    // Every registration test starts on the registration page.
    @BeforeEach
    public void open_RegistrationPage() {
        // By.linkText finds a link by its exact visible text
        driver.findElement(By.linkText("Register")).click();
        // Wait until the registration form has loaded
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("customerForm")));
    }
    // HELPER METHODS

    // Types a value into every registration field.
    // The username and both passwords are passed in, so each test can choose them.
    private void populate_customerRegistrationDetails(String username, String password, String confirmPassword) {
        // By.id finds an element by its unique id attribute
        driver.findElement(By.id("customer.firstName")).sendKeys("Roshni");
        driver.findElement(By.id("customer.lastName")).sendKeys("Chandrasekaran");
        driver.findElement(By.id("customer.address.street")).sendKeys("82 Campbell");
        driver.findElement(By.id("customer.address.city")).sendKeys("Hawthorn East");
        driver.findElement(By.id("customer.address.state")).sendKeys("Victoria");
        driver.findElement(By.id("customer.address.zipCode")).sendKeys("3000");
        driver.findElement(By.id("customer.phoneNumber")).sendKeys("0412039000");
        driver.findElement(By.id("customer.ssn")).sendKeys("124-45-5769");
        driver.findElement(By.id("customer.username")).sendKeys(username);
        driver.findElement(By.id("customer.password")).sendKeys(password);
        driver.findElement(By.id("repeatedPassword")).sendKeys(confirmPassword);
    }

    // Clicks the Register button.
    // By.xpath finds the button with the value "Register" inside the registration form
    private void submit_RegistrationForm() {
        driver.findElement(By.xpath("//form[@id='customerForm']//input[@value='Register']")).click();
    }

    // Returns the red error message shown next to a field.
    // ParaBank gives each error the id "<field id>.errors", for example "customer.firstName.errors"
    private String get_FieldErrorMessage(String fieldId) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(By.id(fieldId + ".errors"))).getText();
    }


    // ---------- Tests for Registration Requirement ----------

    // Test Case 1 (positive): verify the registration page opens and loads all the fields
    @Test
    @SpiraTestCase(testCaseId = 53386)
    @DisplayName("Verify that the registration page loads with the registration form and all the 11 required fields")
    public void valid_LoadRegistrationForm_WithAllFields() {
        // Check the browser tab title and the page title using the By.cssSelector
        assertEquals("ParaBank | Register for Free Online Account Access", driver.getTitle());
        assertEquals("Signing up is easy!", driver.findElement(By.cssSelector("#rightPanel h1.title")).getText());
        // The "findElements" will return a list of every match - All the fields in the form are collected.
        List<WebElement> textFields = driver.findElements(By.cssSelector("#customerForm input[type='text'], #customerForm input[type='password']"));
        assertEquals(11, textFields.size(), "Form should have 11 input fields");
        // allMatch checks that every field in the list is visible on screen
        assertTrue(textFields.stream().allMatch(WebElement::isDisplayed), "All fields should be visible");
    }

    // Test Case 2 (positive): verify that an account is registered successfully with given valid details and allows the user to log in
    @Test
    @SpiraTestCase(testCaseId = 53406)
    @DisplayName("Verify that registering with valid details creates an account and allows the user to log in")
    public void valid_RegistrationAndLogin_WithValidDetails() {
        // Adding the current time would make the username unique during each run
        String unique_username = "user" + System.currentTimeMillis();
        populate_customerRegistrationDetails(unique_username, TEST_PASSWORD, TEST_PASSWORD);
        submit_RegistrationForm();

        // To wait for the success message before checking the page
        wait.until(ExpectedConditions.textToBePresentInElementLocated(
                By.id("rightPanel"), "Your account was created successfully"));

        // assertAll: runs all the checks and reports all failures together
        assertAll("Successful registration",
                () -> assertEquals("Welcome " + unique_username, driver.findElement(By.className("title")).getText()),
                () -> assertTrue(driver.findElement(By.xpath("//div[@id='rightPanel']/p")).getText()
                        .contains("You are now logged in.")),
                () -> assertEquals("Welcome Roshni Chandrasekaran",
                        driver.findElement(By.cssSelector("#leftPanel p.smallText")).getText()),
                // A "Log Out" link displayed shows that the user is currently logged in
                () -> assertTrue(driver.findElement(By.linkText("Log Out")).isDisplayed())
        );
    }

    // Test Case 3 (positive): verify that a newly registered customer is linked with a bank account
    @Test
    @SpiraTestCase(testCaseId = 53407)
    @DisplayName("Verify that a newly registered customer has at least one bank account on their Accounts Overview page")
    public void valid_NewRegisteredCustomer_LinkedWithBankAccount() {
        // To register a new customer
        String unique_username = "user" + System.currentTimeMillis();
        populate_customerRegistrationDetails(unique_username, TEST_PASSWORD, TEST_PASSWORD);
        submit_RegistrationForm();
        wait.until(ExpectedConditions.textToBePresentInElementLocated(
                By.id("rightPanel"), "Your account was created successfully"));

        // By.partialLinkText is used to match a specific link text
        driver.findElement(By.partialLinkText("Accounts Overview")).click();
        // To wait for the accounts table to display at least one account link
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#accountTable tbody a")));

        // To collect all the account links from the table
        List<WebElement> accountLinks = driver.findElements(By.cssSelector("#accountTable tbody a"));
        assertFalse(accountLinks.isEmpty(), "New customer should have at least one account");
        // Account number should contain only numbers because "\\d+" means "one or more digits"
        assertTrue(accountLinks.get(0).getText().matches("\\d+"), "Account number should be numeric");
    }

    // Test Case 4 (positive): verify that registration is successful even if the optional phone number field is left empty
    @Test
    @SpiraTestCase(testCaseId = 53409)
    @DisplayName("Verify that a customer can register even if the optional phone number is left empty")
    public void valid_RegisterCustomer_WithNoOptionalPhoneNumber() {
        String unique_username = "user" + System.currentTimeMillis();
        populate_customerRegistrationDetails(unique_username, TEST_PASSWORD, TEST_PASSWORD);

        // To remove the phone number as it was already populated by the helper method
        WebElement phoneField = driver.findElement(By.id("customer.phoneNumber"));
        phoneField.clear();
        assertEquals("", phoneField.getDomProperty("value"), "Phone field should be empty before submitting");

        submit_RegistrationForm();

        // Registration should be successful without a phone number
        wait.until(ExpectedConditions.textToBePresentInElementLocated(
                By.id("rightPanel"), "Your account was created successfully"));
        assertEquals("Welcome " + unique_username, driver.findElement(By.className("title")).getText());
        // If an empty list is returned then there is no error message for the phone field
        assertTrue(driver.findElements(By.id("customer.phoneNumber.errors")).isEmpty(),
                "No errors should be displayed for the empty phone number");
    }

    // Test Case 5 (negative): verify that when trying to submit an empty form, an error message is displayed for every required field
    @Test
    @SpiraTestCase(testCaseId = 53412)
    @DisplayName("Verify that submitting an empty registration form displays an error message for each required field")
    public void invalid_RegisterCustomer_WithNoDetails() {
        // To click register button keeping the form empty
        submit_RegistrationForm();

        // To retrieve the error message next to each of the 10 required fields and check it matches the expected text
        assertAll("Required field errors",
                () -> assertEquals("First name is required.", get_FieldErrorMessage("customer.firstName")),
                () -> assertEquals("Last name is required.", get_FieldErrorMessage("customer.lastName")),
                () -> assertEquals("Address is required.", get_FieldErrorMessage("customer.address.street")),
                () -> assertEquals("City is required.", get_FieldErrorMessage("customer.address.city")),
                () -> assertEquals("State is required.", get_FieldErrorMessage("customer.address.state")),
                () -> assertEquals("Zip Code is required.", get_FieldErrorMessage("customer.address.zipCode")),
                () -> assertEquals("Social Security Number is required.", get_FieldErrorMessage("customer.ssn")),
                () -> assertEquals("Username is required.", get_FieldErrorMessage("customer.username")),
                () -> assertEquals("Password is required.", get_FieldErrorMessage("customer.password")),
                () -> assertEquals("Password confirmation is required.", get_FieldErrorMessage("repeatedPassword"))
        );
        // If there is no "log Out" link then the user did not get registered or logged in
        assertTrue(driver.findElements(By.linkText("Log Out")).isEmpty(), "User should not be able to register");
    }

    // Test Case 6 (negative): verify that registration is not successful when the confirm password and the entered password do not match
    @Test
    @SpiraTestCase(testCaseId = 53413)
    @DisplayName("Verify that registration is not successful when the confirm password does not match the entered password")
    public void invalid_RegisterCustomer_WithIncorrectConfirmPassword() {
        // A different password is entered in the Confirm password field
        String unique_username = "user" + System.currentTimeMillis();
        populate_customerRegistrationDetails(unique_username, TEST_PASSWORD, "NomatchRoshni@235");
        submit_RegistrationForm();

        // Check the error message shown next to the Confirm password field
        assertEquals("Passwords did not match.", get_FieldErrorMessage("repeatedPassword"));
        // The welcome page should not be displayed, because registration was not successful
        assertNotEquals("Welcome", driver.findElement(By.className("title")).getText().split(" ")[0],
                "The welcome page should not be displayed");
    }

    // Test Case 7 (negative): verify that registration is not successful when the username already exists
    @Test
    @SpiraTestCase(testCaseId = 53414)
    @DisplayName("Verify that registration is not successful when the username already exists")
    public void invalid_RegisterCustomer_WithDuplicateUsername() {
        // First: A new customer is registered with a unique username, then logged out
        String existing_username = "user" + System.currentTimeMillis();
        populate_customerRegistrationDetails(existing_username, TEST_PASSWORD, TEST_PASSWORD);
        submit_RegistrationForm();
        wait.until(ExpectedConditions.textToBePresentInElementLocated(
                By.id("rightPanel"), "Your account was created successfully"));
        driver.findElement(By.linkText("Log Out")).click();
        // By.name finds an element by its name attribute
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));

        // Last: Register again using the same username
        open_RegistrationPage();
        populate_customerRegistrationDetails(existing_username, TEST_PASSWORD, TEST_PASSWORD);
        submit_RegistrationForm();

        // Check if error message is shown next to the Username field
        assertEquals("This username already exists.", get_FieldErrorMessage("customer.username"));
        // By.tagName finds an element by its HTML tag, and "body" is the whole page
        assertTrue(driver.findElement(By.tagName("body")).getText().contains("Signing up is easy!"),
                "Registration should fail for a duplicate username, so the user should stay on the registration page and not proceed");
    }

    // Test Case 8 (boundary): verify whether a customer can register with a short password of 1 character
    @Test
    @SpiraTestCase(testCaseId = 53486)
    @DisplayName("Verify that a customer can register with a password of only 1 character")
    public void boundary_RegisterCustomer_WithShortestPassword() {
        String unique_username = "user" + System.currentTimeMillis();
        // "r" as the shortest password
        populate_customerRegistrationDetails(unique_username, "r", "r");
        submit_RegistrationForm();

        // ParaBank has no stated minimum length, so registration is expected to be successful
        wait.until(ExpectedConditions.textToBePresentInElementLocated(
                By.id("rightPanel"), "Your account was created successfully"));
        // If an empty list is returned, there is no error message for the password field
        assertTrue(driver.findElements(By.id("customer.password.errors")).isEmpty(),
                "No error should be displayed regarding the password");
        assertTrue(driver.findElement(By.linkText("Log Out")).isDisplayed(), "The customer should get logged in");
    }
}