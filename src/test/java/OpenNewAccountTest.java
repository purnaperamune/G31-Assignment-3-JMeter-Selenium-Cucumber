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
}