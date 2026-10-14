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
 * Loan Request tests for ParaBank.
 * Owner: Member 5 (Thashi Godahewa)
 * Requirement: As a registered customer, I want to submit a loan request with an amount
 * and down payment, so that I can find out whether the loan is approved or denied.
 *
 * Each test registers a fresh user in @BeforeEach, so no test depends on data left over
 * from earlier runs (ParaBank's database can be reset at any time).
 */
public class LoanRequestTest extends BaseTest {

    private static final String APPROVED_MESSAGE = "Congratulations, your loan has been approved.";

    // ---------- Setup ----------

    @BeforeEach
    public void goToRequestLoanPage() {
        registerNewUser(); // from BaseTest: creates a unique user and leaves them logged in
        driver.findElement(By.linkText("Request Loan")).click();
        wait.until(d -> !new Select(d.findElement(By.id("fromAccountId"))).getOptions().isEmpty());
    }

    // ---------- Helpers ----------

    private void applyForLoan(String amount, String downPayment) {
        driver.findElement(By.id("amount")).clear();
        driver.findElement(By.id("amount")).sendKeys(amount);
        driver.findElement(By.id("downPayment")).clear();
        driver.findElement(By.id("downPayment")).sendKeys(downPayment);
        driver.findElement(By.cssSelector("input[value='Apply Now']")).click();
    }

    /** Waits for the loan result panel and returns the status text (Approved / Denied). */
    private String waitForLoanStatus() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("requestLoanResult")));
        wait.until(d -> !d.findElement(By.id("loanStatus")).getText().isBlank());
        return driver.findElement(By.id("loanStatus")).getText();
    }

    /** Waits until either the error panel or the result panel is shown after a submit. */
    private void waitForAnyOutcome() {
        wait.until(d -> d.findElement(By.id("requestLoanError")).isDisplayed()
                || d.findElement(By.id("requestLoanResult")).isDisplayed());
    }

    // ---------- Tests ----------

    @Test
    @DisplayName("LR-01 Request Loan page loads with amount, down payment and a funding account")
    public void pageLoadsWithLoanForm() {
        List<WebElement> fromAccounts = driver.findElement(By.id("fromAccountId"))
                .findElements(By.tagName("option"));

        assertAll("Request Loan form",
                () -> assertEquals("Apply for a Loan",
                        driver.findElement(By.cssSelector("#requestLoanForm .title")).getText()),
                () -> assertTrue(driver.findElement(By.id("amount")).isDisplayed(),
                        "Loan amount field should be visible"),
                () -> assertTrue(driver.findElement(By.id("downPayment")).isDisplayed(),
                        "Down payment field should be visible"),
                () -> assertFalse(fromAccounts.isEmpty(), "At least one funding account should be listed")
        );
    }

    @Test
    @DisplayName("LR-02 A small loan with an affordable down payment is approved")
    public void smallLoanIsApproved() {
        applyForLoan("100", "100");
        String status = waitForLoanStatus();

        if (status.equalsIgnoreCase("Denied")) {
            String reason = driver.findElement(
                    By.cssSelector("#loanRequestDenied .error")
            ).getText();

            System.out.println("Loan status: " + status);
            System.out.println("Loan denial reason: " + reason);
        }

        String heading = driver.findElement(By.cssSelector("#requestLoanResult .title")).getText();
        String message = driver.findElement(By.xpath("//div[@id='loanRequestApproved']/p[1]")).getText();
        String newAccountId = driver.findElement(By.id("newAccountId")).getText();

        assertAll("Approved loan result",
                () -> assertEquals("Loan Request Processed", heading),
                () -> assertEquals("Approved", status),
                () -> assertEquals(APPROVED_MESSAGE, message),
                () -> assertTrue(newAccountId.matches("\\d+"), "New loan account number should be numeric"),
                () -> assertFalse(driver.findElement(By.id("loanRequestDenied")).isDisplayed(),
                        "Denied section should be hidden for an approved loan")
        );
    }

    @Test
    @DisplayName("LR-03 A loan is denied when the down payment exceeds the available balance")
    public void loanDeniedWhenDownPaymentTooHigh() {
        applyForLoan("1000", "100000");
        String status = waitForLoanStatus();

        WebElement deniedMessage = driver.findElement(By.cssSelector("#loanRequestDenied .error"));

        assertAll("Denied loan result",
                () -> assertEquals("Denied", status),
                () -> assertTrue(deniedMessage.isDisplayed(), "A denial reason should be shown"),
                () -> assertFalse(deniedMessage.getText().isBlank(), "The denial reason should not be empty"),
                () -> assertFalse(driver.findElement(By.id("loanRequestApproved")).isDisplayed(),
                        "Approved section should be hidden for a denied loan")
        );
    }

    @Test
    @DisplayName("LR-04 Submitting the loan form with empty fields does not approve a loan")
    public void blankLoanRequestIsNotApproved() {
        driver.findElement(By.cssSelector("input[value='Apply Now']")).click();
        waitForAnyOutcome();

        WebElement error = driver.findElement(By.id("requestLoanError"));

        assertAll("Blank loan request",
                () -> assertFalse(driver.findElement(By.id("loanRequestApproved")).isDisplayed(),
                        "A loan must not be approved when no amount is entered"),
                // Once you have seen the real ParaBank message, replace this with assertEquals on that text.
                () -> assertTrue(error.isDisplayed(), "An error message should be shown"),
                () -> assertFalse(error.getText().isBlank(), "The error message should not be empty")
        );
    }

    @Test
    @DisplayName("LR-05 A non-numeric loan amount is not approved")
    public void nonNumericAmountIsNotApproved() {
        applyForLoan("abc", "10");
        waitForAnyOutcome();

        WebElement error = driver.findElement(By.id("requestLoanError"));

        assertAll("Non-numeric amount",
                () -> assertFalse(driver.findElement(By.id("loanRequestApproved")).isDisplayed(),
                        "A loan must not be approved for a non-numeric amount"),
                () -> assertTrue(error.isDisplayed(), "An error message should be shown"),
                () -> assertFalse(error.getText().isBlank(), "The error message should not be empty")
        );
    }

    @Test
    @DisplayName("LR-06 An approved loan account appears in Accounts Overview")
    public void approvedLoanAppearsInAccountsOverview() {
        applyForLoan("100", "10");
        assertEquals("Approved", waitForLoanStatus(), "Loan must be approved before checking the overview");
        String newAccountId = driver.findElement(By.id("newAccountId")).getText();

        driver.findElement(By.partialLinkText("Accounts Overview")).click();
        WebElement accountLink = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.linkText(newAccountId)));

        assertNotNull(accountLink);
        assertEquals(newAccountId, accountLink.getText());
    }
}
