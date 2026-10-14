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
}
