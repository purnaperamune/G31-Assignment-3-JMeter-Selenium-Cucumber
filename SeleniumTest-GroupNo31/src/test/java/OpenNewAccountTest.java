import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import com.inflectra.spiratest.addons.junitextension.*;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

// Owner: Member 4 – Open New Account tests
// Requirement: Open New Account
// Test cases: TC:53392 to TC:53398

// @ExtendWith turns on the Spira extension so each test result is sent to Spira automatically
@ExtendWith(SpiraExtension.class)
@SpiraTestConfiguration(
        url = "https://rmit.spiraservice.net",
        login = "S4143272",
        rssToken = "{4B9371F7-82E4-4134-980E-8FC6BF544028}",
        projectId = 1203,
        releaseId = -1,
        testSetId = -1
)

// This class extends BaseTest
// Opens a new Chrome window on the ParaBank home page before every test
// Closes the browser after every test
public class OpenNewAccountTest extends BaseTest {

    // This method runs before each test
    // A new customer is registered so every test starts with a fresh account and a known balance.
    // This also keeps the tests working even if ParaBank resets its database and removes old users.
    @BeforeEach
    public void goToOpenNewAccountPage() {
        registerNewUser();
        driver.findElement(By.linkText("Open New Account")).click();
        waitForFundingAccounts();
    }

    // HELPER METHODS

    // This helper method reads an account's balance from the Accounts Overview table.
    // BigDecimal is used instead of double because money should be compared exactly.
    private BigDecimal readBalanceFromOverview(String accountId) {
        driver.findElement(By.partialLinkText("Accounts Overview")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.linkText(accountId)));
        // By.xpath finds the row that has the account link, then takes the second cell (the balance)
        String balanceText = driver.findElement(
                By.xpath("//a[text()='" + accountId + "']/ancestor::tr/td[2]")).getText();
        // To remove "$" and "," so the text can be turned into a number, e.g. "$1,515.50" becomes 1515.50
        return new BigDecimal(balanceText.replace("$", "").replace(",", ""));
    }

    // This helper method reads an account's balance from the Account Details page.
    // The page is opened directly using the account number in the URL, so the test gets fresh data.
    private BigDecimal readBalanceFromAccountDetails(String accountId) {
        driver.get("https://parabank.parasoft.com/parabank/activity.htm?id=" + accountId);
        // To wait until the balance is loaded and not empty
        wait.until(d -> !d.findElement(By.id("balance")).getText().isBlank());
        String balanceText = driver.findElement(By.id("balance")).getText();
        return new BigDecimal(balanceText.replace("$", "").replace(",", ""));
    }

    // This helper method waits until the funding account dropdown has loaded.
    // The dropdown is filled using AJAX after the page loads, so the test must wait for at least one option.
    private void waitForFundingAccounts() {
        wait.until(d -> new Select(d.findElement(By.id("fromAccountId"))).getOptions().size() > 0);
    }

    // This helper method opens a new account of the given type (CHECKING or SAVINGS)
    // and returns the new account number shown on the page.
    private String openAccount(String accountType) {
        // Select is used to choose an option from a dropdown by its visible text
        new Select(driver.findElement(By.id("type"))).selectByVisibleText(accountType);
        // By.cssSelector finds the button using its type and value attributes
        driver.findElement(By.cssSelector("input[type='button'][value='Open New Account']")).click();
        // To wait until the result section is shown and the new account number has loaded
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("openAccountResult")));
        wait.until(d -> !d.findElement(By.id("newAccountId")).getText().isBlank());
        return driver.findElement(By.id("newAccountId")).getText();
    }

    // ---------- Tests for Open New Account Requirement ----------

    // Test Case 1 (smoke): verify that the Open New Account page loads with both account types and a funding account
    @Test
    @SpiraTestCase(testCaseId = 53392)
    @DisplayName("Open New Account page loads with account types and a funding account")
    public void pageLoadsWithDropdownOptions() {
        // To check that the correct page has opened using the browser tab title
        assertEquals("ParaBank | Open Account", driver.getTitle());

        // To collect the text of every option in the account type dropdown into a list
        List<String> accountTypes = new Select(driver.findElement(By.id("type")))
                .getOptions().stream().map(WebElement::getText).toList();
        assertTrue(accountTypes.contains("CHECKING"), "CHECKING should be an option");
        assertTrue(accountTypes.contains("SAVINGS"), "SAVINGS should be an option");

        // The customer needs at least one existing account to pay the opening deposit from
        List<WebElement> fundingAccounts = driver.findElement(By.id("fromAccountId"))
                .findElements(By.tagName("option"));
        assertFalse(fundingAccounts.isEmpty(), "At least one funding account should be listed");
    }

    // Test Case 2 (positive): verify that a customer can open a CHECKING account and gets a new account number
    @Test
    @SpiraTestCase(testCaseId = 53393)
    @DisplayName("Opening a CHECKING account shows a success message and a new account number")
    public void openCheckingAccount() {
        String newAccountId = openAccount("CHECKING");

        WebElement result = driver.findElement(By.id("openAccountResult"));
        // assertAll: runs all the checks and reports all failures together
        assertAll("Checking account result",
                () -> assertTrue(result.isDisplayed()),
                () -> assertEquals("Account Opened!", result.findElement(By.className("title")).getText()),
                // "\\d+" means "one or more digits", so the account number should contain only numbers
                () -> assertTrue(newAccountId.matches("\\d+"), "Account number should be numeric")
        );
    }

    // Test Case 3 (positive): verify that a customer can open a SAVINGS account and the form is replaced by the confirmation
    @Test
    @SpiraTestCase(testCaseId = 53394)
    @DisplayName("Opening a SAVINGS account shows the confirmation and hides the form")
    public void openSavingsAccount() {
        String newAccountId = openAccount("SAVINGS");

        // By.xpath finds the first paragraph inside the result section, which holds the confirmation message
        String message = driver.findElement(By.xpath("//div[@id='openAccountResult']/p[1]")).getText();
        assertEquals("Congratulations, your account is now open.", message);
        // The form should be hidden after the account is opened, so it cannot be submitted again
        assertFalse(driver.findElement(By.id("openAccountForm")).isDisplayed(), "Form should be hidden after opening");
        assertNotNull(newAccountId);
    }

    // Test Case 4 (positive): verify that the new account is really saved and is listed in Accounts Overview
    @Test
    @SpiraTestCase(testCaseId = 53395)
    @DisplayName("A newly opened account appears in Accounts Overview")
    public void newAccountAppearsInAccountsOverview() {
        String newAccountId = openAccount("SAVINGS");

        // To go to Accounts Overview and wait until the new account number appears as a link
        driver.findElement(By.partialLinkText("Accounts Overview")).click();
        WebElement accountLink = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.linkText(newAccountId)));
        assertNotNull(accountLink);
        assertEquals(newAccountId, accountLink.getText());
    }

    // Test Case 5 (positive): verify that clicking the new account number opens the correct account details page
    @Test
    @SpiraTestCase(testCaseId = 53396)
    @DisplayName("The new account number links to that account's details page")
    public void newAccountLinkOpensAccountDetails() {
        String newAccountId = openAccount("CHECKING");

        // To click the new account number link on the confirmation page
        driver.findElement(By.xpath("//a[@id='newAccountId']")).click();
        // The page address should contain the new account number, e.g. "activity.htm?id=12345"
        wait.until(ExpectedConditions.urlContains("activity.htm?id=" + newAccountId));
        assertTrue(driver.getCurrentUrl().contains("id=" + newAccountId));
    }

    // Test Case 7 (end-to-end): verify that the opening deposit is moved from the funding account into the new account,
    // so the customer's total money stays the same (money is moved, not created or lost)
    @Test
    @SpiraTestCase(testCaseId = 53398)
    @DisplayName("Opening an account moves the opening deposit from the funding account without changing the total")
    public void openingAccountTransfersDepositFromFundingAccount() {
        // To save the funding account number and its balance before opening the new account
        String fundingAccount = new Select(driver.findElement(By.id("fromAccountId")))
                .getFirstSelectedOption().getText();
        BigDecimal fundingBefore = readBalanceFromOverview(fundingAccount);

        // To go back to the Open New Account page, because reading the balance moved to another page
        driver.findElement(By.linkText("Open New Account")).click();
        waitForFundingAccounts();
        String newAccountId = openAccount("SAVINGS");

        // To read both balances after the new account is opened
        BigDecimal fundingAfter = readBalanceFromAccountDetails(fundingAccount);
        BigDecimal newAccountBalance = readBalanceFromAccountDetails(newAccountId);

        assertAll("Opening deposit transfer",
                // The new account should receive some money as its opening deposit
                () -> assertTrue(newAccountBalance.compareTo(BigDecimal.ZERO) > 0,
                        "New account should receive an opening deposit, but its balance was " + newAccountBalance),
                // The funding account should drop by exactly the deposit amount (before - deposit should equal after)
                () -> assertEquals(0, fundingBefore.subtract(newAccountBalance).compareTo(fundingAfter),
                        "Funding account should drop by exactly the opening deposit, keeping the total unchanged (before: "
                                + fundingBefore + ", deposit: " + newAccountBalance + ", after: " + fundingAfter + ")")
        );
    }

    // Test Case 6 (negative/security): verify that a logged-out user cannot use the Open New Account page,
    // even if they type the page address directly into the browser
    @Test
    @SpiraTestCase(testCaseId = 53397)
    @DisplayName("A logged-out user cannot access the Open New Account page")
    public void loggedOutUserCannotOpenAccount() {
        // To log out and wait until the login form appears
        driver.findElement(By.linkText("Log Out")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));

        // To try opening the Open New Account page directly using its URL
        driver.get("https://parabank.parasoft.com/parabank/openaccount.htm");

        // findElements (plural) returns an empty list instead of throwing an error when nothing is found,
        // so an empty list means the element is not on the page
        assertTrue(driver.findElements(By.id("type")).isEmpty(),
                "Account type dropdown should not be available when logged out");
        assertTrue(driver.findElements(By.cssSelector("input[value='Open New Account']")).isEmpty(),
                "Open New Account button should not be available when logged out");
        // The login form should be shown instead of the account form
        assertTrue(driver.findElement(By.name("username")).isDisplayed(),
                "Login form should be shown instead");
    }
}