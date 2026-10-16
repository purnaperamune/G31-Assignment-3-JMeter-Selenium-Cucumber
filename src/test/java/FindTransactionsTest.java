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

    @Test
    @DisplayName("FT-02 Find a transaction by its ID")
    public void findTransactionById() {
        type(By.id("transactionId"), transactionId);
        driver.findElement(By.id("findById")).click();
        waitForResults();

        String rowText = resultRows().get(0).getText();

        assertAll("Search by ID",
                () -> assertEquals(1, resultRows().size(), "Exactly one transaction should match the ID"),
                () -> assertTrue(rowText.contains(transactionDate), "Row should show the transaction date"),
                () -> assertTrue(rowText.contains(NEW_ACCOUNT_AMOUNT), "Row should show the amount")
        );
    }

    @Test
    @DisplayName("FT-03 Find transactions by a single date")
    public void findTransactionsByDate() {
        type(By.id("transactionDate"), transactionDate);
        driver.findElement(By.id("findByDate")).click();
        waitForResults();

        assertAll("Search by date",
                () -> assertFalse(resultRows().isEmpty(), "At least one transaction should be found"),
                () -> assertTrue(resultRows().get(0).getText().contains(transactionDate),
                        "Result should be dated " + transactionDate)
        );
    }

    @Test
    @DisplayName("FT-04 Find transactions by a date range")
    public void findTransactionsByDateRange() {
        type(By.id("fromDate"), transactionDate);
        type(By.id("toDate"), transactionDate);
        driver.findElement(By.id("findByDateRange")).click();
        waitForResults();

        assertFalse(resultRows().isEmpty(), "A range containing the transaction date should return results");
    }

    @Test
    @DisplayName("FT-05 Find transactions by amount")
    public void findTransactionsByAmount() {
        type(By.id("amount"), "100");
        driver.findElement(By.id("findByAmount")).click();
        waitForResults();

        assertAll("Search by amount",
                () -> assertFalse(resultRows().isEmpty(), "At least one transaction should match the amount"),
                () -> assertTrue(resultRows().get(0).getText().contains(NEW_ACCOUNT_AMOUNT),
                        "Result should show the amount " + NEW_ACCOUNT_AMOUNT)
        );
    }

    @Test
    @DisplayName("FT-06 An invalid date format shows an error and no results")
    public void invalidDateFormatShowsError() {
        type(By.id("transactionDate"), "not-a-date");
        driver.findElement(By.id("findByDate")).click();
        waitForAnyOutcome();

        WebElement error = driver.findElement(By.id("transactionDateError"));

        assertAll("Invalid date",
                () -> assertTrue(error.isDisplayed(), "A date error should be shown"),
                () -> assertEquals("Invalid date format", error.getText()),
                () -> assertTrue(resultRows().isEmpty(), "No results should be listed")
        );
    }

    @Test
    @DisplayName("FT-07 A non-existent transaction ID returns no results")
    public void nonExistentTransactionIdReturnsNothing() {
        type(By.id("transactionId"), "999999999");
        driver.findElement(By.id("findById")).click();
        waitForAnyOutcome();

        boolean resultShown = driver.findElement(By.id("resultContainer")).isDisplayed();
        boolean errorShown = driver.findElement(By.id("errorContainer")).isDisplayed();

        assertAll("Unknown transaction ID",
                () -> assertTrue(resultShown || errorShown, "The page should show a result or an error"),
                () -> assertTrue(resultRows().isEmpty(), "No transaction should be listed for an unknown ID")
        );
    }

}
