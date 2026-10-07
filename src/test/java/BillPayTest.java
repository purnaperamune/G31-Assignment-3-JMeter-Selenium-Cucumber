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
}