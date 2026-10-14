import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Find Transactions tests for ParaBank.
 * Owner: Member 5
 * Requirement: As a registered customer, I want to search my account transactions by ID,
 * date, date range or amount, so that I can review my account activity.
 *
 * A brand-new ParaBank user has no transactions, so @BeforeEach creates one by opening a
 * second account (ParaBank funds it with an initial transfer from the first account), then
 * reads that transaction's ID and date from the Account Activity table. Tests therefore
 * never depend on data left over from earlier runs.
 */
public class FindTransactionsTest extends BaseTest {

    private static final String NEW_ACCOUNT_AMOUNT = "100.00"; // initial funding of the new account

    private String transactionId;   // read from the Account Activity table
    private String transactionDate; // MM-DD-YYYY, read from the Account Activity table

    // ---------- Setup ----------

    @BeforeEach
    public void createTransactionAndOpenFindPage() {
        registerNewUser(); // from BaseTest: creates a unique user and leaves them logged in

        // 1. Create a transaction by opening a second account
        driver.findElement(By.linkText("Open New Account")).click();
        wait.until(d -> !new Select(d.findElement(By.id("type"))).getOptions().isEmpty());
        wait.until(d -> !new Select(d.findElement(By.id("fromAccountId"))).getOptions().isEmpty());
        driver.findElement(By.cssSelector("input[value='Open New Account']")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("openAccountResult")));

        // 2. Read that transaction's ID and date from the first (original) account's activity
        driver.findElement(By.partialLinkText("Accounts Overview")).click();
        // The tables are filled in by JavaScript after the page loads, so wait for the
        // link itself (not just the table) before clicking.
        wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("(//table[@id='accountTable']//a)[1]"))).click();
        // Read the ID and date straight from the first row of the Account Activity table:
        // the ID is the "id=" value in the transaction link's href, the date is the row's first cell.
        By firstTransactionLink = By.xpath("(//table[@id='transactionTable']//tbody//a)[1]");
        WebElement link = wait.until(ExpectedConditions.visibilityOfElementLocated(firstTransactionLink));
        String href = link.getAttribute("href");
        transactionId = href.substring(href.indexOf("id=") + 3);
        transactionDate = driver.findElement(
                By.xpath("(//table[@id='transactionTable']//tbody//a)[1]/ancestor::tr/td[1]")).getText().trim();

        // 3. Open the Find Transactions page and wait for the account list
        driver.findElement(By.linkText("Find Transactions")).click();
        wait.until(d -> !new Select(d.findElement(By.id("accountId"))).getOptions().isEmpty());
    }

    // ---------- Helpers ----------

    private void type(By locator, String text) {
        WebElement field = driver.findElement(locator);
        field.clear();
        field.sendKeys(text);
    }

    private List<WebElement> resultRows() {
        return driver.findElements(By.cssSelector("#transactionTable tbody tr"));
    }

    /** Waits until the results container or any search error is displayed. */
    private void waitForAnyOutcome() {
        // The error <span>s are empty (so not displayed) until the page script sets their text.
        wait.until(d -> d.findElement(By.id("resultContainer")).isDisplayed()
                || d.findElement(By.id("errorContainer")).isDisplayed()
                || !d.findElement(By.id("transactionIdError")).getText().isBlank()
                || !d.findElement(By.id("transactionDateError")).getText().isBlank()
                || !d.findElement(By.id("dateRangeError")).getText().isBlank()
                || !d.findElement(By.id("amountError")).getText().isBlank());
    }

    private void waitForResults() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("resultContainer")));
        wait.until(d -> !resultRows().isEmpty());
    }

    // ---------- Tests ----------

    // Spira: TC:XXX
    @Test
    @DisplayName("FT-01 Find Transactions page loads with all four search options")
    public void pageLoadsWithSearchForms() {
        assertAll("Find Transactions page",
                () -> assertEquals("Find Transactions",
                        driver.findElement(By.cssSelector("#formContainer .title")).getText()),
                () -> assertTrue(driver.findElement(By.id("transactionId")).isDisplayed(), "Transaction ID field"),
                () -> assertTrue(driver.findElement(By.id("transactionDate")).isDisplayed(), "Single date field"),
                () -> assertTrue(driver.findElement(By.id("fromDate")).isDisplayed(), "From date field"),
                () -> assertTrue(driver.findElement(By.id("toDate")).isDisplayed(), "To date field"),
                () -> assertTrue(driver.findElement(By.id("amount")).isDisplayed(), "Amount field"),
                () -> assertFalse(new Select(driver.findElement(By.id("accountId"))).getOptions().isEmpty(),
                        "At least one account should be listed")
        );
    }
}
