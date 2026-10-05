import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

// Owner: Member 4 – Open New Account tests
public class OpenNewAccountTest extends BaseTest {

    @BeforeEach
    public void goToOpenNewAccountPage() {
        registerNewUser();
        driver.findElement(By.linkText("Open New Account")).click();
        waitForFundingAccounts();
    }

    private void waitForFundingAccounts() {
        wait.until(d -> new Select(d.findElement(By.id("fromAccountId"))).getOptions().size() > 0);
    }
    private String openAccount(String accountType) {
        new Select(driver.findElement(By.id("type"))).selectByVisibleText(accountType);
        driver.findElement(By.cssSelector("input[type='button'][value='Open New Account']")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("openAccountResult")));
        wait.until(d -> !d.findElement(By.id("newAccountId")).getText().isBlank());
        return driver.findElement(By.id("newAccountId")).getText();
    }
    @Test
    @DisplayName("Open New Account page loads with account types and a funding account")
    public void pageLoadsWithDropdownOptions() {
        assertEquals("ParaBank | Open Account", driver.getTitle());

        List<String> accountTypes = new Select(driver.findElement(By.id("type")))
                .getOptions().stream().map(WebElement::getText).toList();
        assertTrue(accountTypes.contains("CHECKING"), "CHECKING should be an option");
        assertTrue(accountTypes.contains("SAVINGS"), "SAVINGS should be an option");

        List<WebElement> fundingAccounts = driver.findElement(By.id("fromAccountId"))
                .findElements(By.tagName("option"));
        assertFalse(fundingAccounts.isEmpty(), "At least one funding account should be listed");
    }
    @Test
    @DisplayName("Opening a CHECKING account shows a success message and a new account number")
    public void openCheckingAccount() {
        String newAccountId = openAccount("CHECKING");

        WebElement result = driver.findElement(By.id("openAccountResult"));
        assertAll("Checking account result",
                () -> assertTrue(result.isDisplayed()),
                () -> assertEquals("Account Opened!", result.findElement(By.className("title")).getText()),
                () -> assertTrue(newAccountId.matches("\\d+"), "Account number should be numeric")
        );
    }
    @Test
    @DisplayName("Opening a SAVINGS account shows the confirmation and hides the form")
    public void openSavingsAccount() {
        String newAccountId = openAccount("SAVINGS");

        String message = driver.findElement(By.xpath("//div[@id='openAccountResult']/p[1]")).getText();
        assertEquals("Congratulations, your account is now open.", message);
        assertFalse(driver.findElement(By.id("openAccountForm")).isDisplayed(), "Form should be hidden after opening");
        assertNotNull(newAccountId);
    }
}