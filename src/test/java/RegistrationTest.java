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
// Test cases: TC-REG-01 to TC-REG-07
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
    

}