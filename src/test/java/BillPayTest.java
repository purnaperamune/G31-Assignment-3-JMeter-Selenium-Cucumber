import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

// Owner: Member 4 – Bill Pay tests
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

    @Test
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
}