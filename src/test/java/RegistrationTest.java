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
}