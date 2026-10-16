import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import com.inflectra.spiratest.addons.junitextension.*;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

// Owner: Member 4 – Bill Pay tests
@ExtendWith(SpiraExtension.class)
@SpiraTestConfiguration(
        url = "https://rmit.spiraservice.net",
        login = "S4143272",
        rssToken = "{4B9371F7-82E4-4134-980E-8FC6BF544028}",
        projectId = 1203,
        releaseId = -1,
        testSetId = -1
)
public class BillPayTest extends BaseTest {

    @BeforeEach
    public void goToBillPayPage() {
        registerNewUser();
        driver.findElement(By.linkText("Bill Pay")).click();
        wait.until(d -> new Select(d.findElement(By.name("fromAccountId"))).getOptions().size() > 0);
    }

    private void fillPayeeDetails(String payeeName) {
        driver.findElement(By.name("payee.name")).sendKeys(payeeName);
        driver.findElement(By.name("payee.address.street")).sendKeys("10 Collins Street");
        driver.findElement(By.name("payee.address.city")).sendKeys("Melbourne");
        driver.findElement(By.name("payee.address.state")).sendKeys("VIC");
        driver.findElement(By.name("payee.address.zipCode")).sendKeys("3000");
        driver.findElement(By.name("payee.phoneNumber")).sendKeys("0412345678");
    }

    private void fillPaymentDetails(String account, String verifyAccount, String amount) {
        driver.findElement(By.name("payee.accountNumber")).sendKeys(account);
        driver.findElement(By.name("verifyAccount")).sendKeys(verifyAccount);
        driver.findElement(By.name("amount")).sendKeys(amount);
    }

    private void sendPayment() {
        driver.findElement(By.cssSelector("input[value='Send Payment']")).click();
    }

    private void goToBillPay() {
        driver.findElement(By.linkText("Bill Pay")).click();
        wait.until(d -> new Select(d.findElement(By.name("fromAccountId"))).getOptions().size() > 0);
    }

    private BigDecimal readBalance(String accountId) {
        driver.findElement(By.partialLinkText("Accounts Overview")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.linkText(accountId)));
        String balanceText = driver.findElement(
                By.xpath("//a[text()='" + accountId + "']/ancestor::tr/td[2]")).getText();
        return new BigDecimal(balanceText.replace("$", "").replace(",", ""));
    }

    private BigDecimal readBalanceFromAccountDetails(String accountId) {
        driver.findElement(By.partialLinkText("Accounts Overview")).click();
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText(accountId))).click();
        wait.until(d -> !d.findElement(By.id("balance")).getText().isBlank());
        String balanceText = driver.findElement(By.id("balance")).getText();
        return new BigDecimal(balanceText.replace("$", "").replace(",", ""));
    }

    @Test
    @SpiraTestCase(testCaseId = 53399)
    @DisplayName("Bill Pay lists the customer's own account as the funding account")
    public void fundingAccountMatchesCustomersAccount() {
        assertEquals("Bill Payment Service",
                driver.findElement(By.cssSelector("#billpayForm .title")).getText());

        String fundingAccount = new Select(driver.findElement(By.name("fromAccountId")))
                .getFirstSelectedOption().getText();

        driver.findElement(By.partialLinkText("Accounts Overview")).click();
        WebElement accountLink = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.linkText(fundingAccount)));

        assertNotNull(accountLink, "Funding account should be one of the customer's accounts");
        assertEquals(fundingAccount, accountLink.getText());
    }

    @Test
    @SpiraTestCase(testCaseId = 53400)
    @DisplayName("A valid bill payment completes and shows the payee, amount and account")
    public void validBillPaymentCompletes() {
        String fromAccount = new Select(driver.findElement(By.name("fromAccountId")))
                .getFirstSelectedOption().getText();

        fillPayeeDetails("Electricity Company");
        fillPaymentDetails("12345", "12345", "50");
        sendPayment();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("billpayResult")));

        assertAll("Bill payment result",
                () -> assertEquals("Bill Payment Complete",
                        driver.findElement(By.cssSelector("#billpayResult .title")).getText()),
                () -> assertEquals("Electricity Company", driver.findElement(By.id("payeeName")).getText()),
                () -> assertTrue(driver.findElement(By.id("amount")).getText().contains("50.00"),
                        "Amount should show 50.00"),
                () -> assertEquals(fromAccount,
                        driver.findElement(By.xpath("//div[@id='billpayResult']//span[@id='fromAccountId']")).getText()),
                () -> assertEquals("ParaBank | Bill Payment Complete", driver.getTitle())
        );
    }

    @Test
    @SpiraTestCase(testCaseId = 53401)
    @DisplayName("Bill payment is blocked when the account numbers do not match")
    public void mismatchedAccountNumbersShowError() {
        fillPayeeDetails("Water Company");
        fillPaymentDetails("12345", "99999", "50");
        sendPayment();

        WebElement mismatchError = driver.findElement(By.id("validationModel-verifyAccount-mismatch"));

        assertTrue(mismatchError.isDisplayed(), "Mismatch error should be shown");
        assertEquals("The account numbers do not match.", mismatchError.getText());
        assertFalse(driver.findElement(By.id("billpayResult")).isDisplayed(),
                "Payment should not be completed");
    }

    @Test
    @SpiraTestCase(testCaseId = 53402)
    @DisplayName("Submitting an empty bill pay form shows required-field errors")
    public void emptyFormShowsRequiredErrors() {
        sendPayment();

        assertAll("Required field errors",
                () -> assertEquals("Payee name is required.",
                        driver.findElement(By.id("validationModel-name")).getText()),
                () -> assertTrue(driver.findElement(By.id("validationModel-address")).isDisplayed()),
                () -> assertTrue(driver.findElement(By.id("validationModel-city")).isDisplayed()),
                () -> assertTrue(driver.findElement(By.id("validationModel-state")).isDisplayed()),
                () -> assertTrue(driver.findElement(By.id("validationModel-zipCode")).isDisplayed()),
                () -> assertTrue(driver.findElement(By.id("validationModel-phoneNumber")).isDisplayed()),
                () -> assertTrue(driver.findElement(By.id("validationModel-account-empty")).isDisplayed()),
                () -> assertEquals("The amount cannot be empty.",
                        driver.findElement(By.id("validationModel-amount-empty")).getText())
        );
        assertFalse(driver.findElement(By.id("billpayResult")).isDisplayed(),
                "Payment should not be completed");
    }

    @Test
    @SpiraTestCase(testCaseId = 53403)
    @DisplayName("Bill payment is blocked when the amount is not a number")
    public void invalidAmountShowsError() {
        fillPayeeDetails("Internet Provider");
        fillPaymentDetails("12345", "12345", "abc");
        sendPayment();

        WebElement amountError = driver.findElement(By.id("validationModel-amount-invalid"));

        assertTrue(amountError.isDisplayed(), "Invalid amount error should be shown");
        assertEquals("Please enter a valid amount.", amountError.getText());
        assertFalse(driver.findElement(By.id("billpayResult")).isDisplayed(),
                "Payment should not be completed");
    }

    @Test
    @SpiraTestCase(testCaseId = 53404)
    @DisplayName("A bill payment reduces the account balance and appears in account activity")
    public void billPaymentReducesBalanceAndAppearsInActivity() {
        String fromAccount = new Select(driver.findElement(By.name("fromAccountId")))
                .getFirstSelectedOption().getText();
        BigDecimal balanceBefore = readBalance(fromAccount);

        goToBillPay();
        fillPayeeDetails("Electricity Company");
        fillPaymentDetails("12345", "12345", "50");
        sendPayment();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("billpayResult")));

        BigDecimal balanceAfter = readBalanceFromAccountDetails(fromAccount);

        assertAll("Bill payment effect on account",
                () -> assertEquals(0, balanceBefore.subtract(new BigDecimal("50")).compareTo(balanceAfter),
                        "Defect: bill payment of $50.00 did not reduce the balance (before: "
                                + balanceBefore + ", after: " + balanceAfter + ")"),
                () -> {
                    try {
                        WebElement paymentRow = wait.until(ExpectedConditions.visibilityOfElementLocated(
                                By.xpath("//table[@id='transactionTable']//tr[td/a[text()='Bill Payment to Electricity Company']]")));
                        assertEquals("$50.00", paymentRow.findElement(By.xpath("./td[3]")).getText());
                    } catch (TimeoutException e) {
                        fail("Defect: 'Bill Payment Complete' was shown but the payment is not recorded in Account Activity");
                    }
                }
        );
    }

    @Test
    @SpiraTestCase(testCaseId = 53405)
    @DisplayName("A negative bill payment amount should be rejected and must not increase the balance")
    public void negativeAmountShouldBeRejected() {
        String fromAccount = new Select(driver.findElement(By.name("fromAccountId")))
                .getFirstSelectedOption().getText();
        BigDecimal balanceBefore = readBalance(fromAccount);

        goToBillPay();
        fillPayeeDetails("Electricity Company");
        fillPaymentDetails("12345", "12345", "-50");
        sendPayment();

        wait.until(d -> d.findElement(By.id("billpayResult")).isDisplayed()
                || d.findElement(By.id("validationModel-amount-invalid")).isDisplayed());
        boolean paymentAccepted = driver.findElement(By.id("billpayResult")).isDisplayed();
        BigDecimal balanceAfter = readBalanceFromAccountDetails(fromAccount);

        assertAll("Negative amount handling",
                () -> assertFalse(paymentAccepted,
                        "Defect: ParaBank accepted a bill payment of -$50.00"),
                () -> assertTrue(balanceAfter.compareTo(balanceBefore) <= 0,
                        "Defect: balance increased from " + balanceBefore + " to " + balanceAfter)
        );
    }
    @Test
    @SpiraTestCase(testCaseId = 53663)
    @DisplayName("A bill payment larger than the balance should be rejected and must not overdraw the account")
    public void paymentExceedingBalanceShouldBeRejected() {
        String fromAccount = new Select(driver.findElement(By.name("fromAccountId")))
                .getFirstSelectedOption().getText();
        BigDecimal balanceBefore = readBalance(fromAccount);
        BigDecimal amount = balanceBefore.add(new BigDecimal("1000"));

        goToBillPay();
        fillPayeeDetails("Electricity Company");
        fillPaymentDetails("12345", "12345", amount.toPlainString());
        sendPayment();

        wait.until(d -> d.findElement(By.id("billpayResult")).isDisplayed()
                || d.findElement(By.id("billpayError")).isDisplayed());
        boolean paymentAccepted = driver.findElement(By.id("billpayResult")).isDisplayed();
        BigDecimal balanceAfter = readBalanceFromAccountDetails(fromAccount);

        assertAll("Payment exceeding balance",
                () -> assertFalse(paymentAccepted,
                        "Defect: ParaBank accepted a bill payment of $" + amount
                                + " which exceeds the balance of $" + balanceBefore),
                () -> assertTrue(balanceAfter.compareTo(BigDecimal.ZERO) >= 0,
                        "Defect: account was overdrawn to " + balanceAfter)
        );
    }

    @Test
    @SpiraTestCase(testCaseId = 53665)
    @DisplayName("Bill payment is blocked when the account number is not a number")
    public void nonNumericAccountNumberShowsError() {
        fillPayeeDetails("Gas Company");
        fillPaymentDetails("abc", "abc", "50");
        sendPayment();

        WebElement accountError = driver.findElement(By.id("validationModel-account-invalid"));
        WebElement verifyError = driver.findElement(By.id("validationModel-verifyAccount-invalid"));

        assertAll("Non-numeric account number",
                () -> assertTrue(accountError.isDisplayed(), "Account # error should be shown"),
                () -> assertEquals("Please enter a valid number.", accountError.getText()),
                () -> assertTrue(verifyError.isDisplayed(), "Verify Account # error should be shown"),
                () -> assertFalse(driver.findElement(By.id("billpayResult")).isDisplayed(),
                        "Payment should not be completed")
        );
    }
}