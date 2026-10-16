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
@ExtendWith(SpiraExtension.class)
@SpiraTestConfiguration(
        url = "https://rmit.spiraservice.net",
        login = "S4143272",
        rssToken = "{4B9371F7-82E4-4134-980E-8FC6BF544028}",
        projectId = 1203,
        releaseId = -1,
        testSetId = -1
)
public class OpenNewAccountTest extends BaseTest {

    @BeforeEach
    public void goToOpenNewAccountPage() {
        registerNewUser();
        driver.findElement(By.linkText("Open New Account")).click();
        waitForFundingAccounts();
    }

    private BigDecimal readBalanceFromOverview(String accountId) {
        driver.findElement(By.partialLinkText("Accounts Overview")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.linkText(accountId)));
        String balanceText = driver.findElement(
                By.xpath("//a[text()='" + accountId + "']/ancestor::tr/td[2]")).getText();
        return new BigDecimal(balanceText.replace("$", "").replace(",", ""));
    }

    private BigDecimal readBalanceFromAccountDetails(String accountId) {
        driver.get("https://parabank.parasoft.com/parabank/activity.htm?id=" + accountId);
        wait.until(d -> !d.findElement(By.id("balance")).getText().isBlank());
        String balanceText = driver.findElement(By.id("balance")).getText();
        return new BigDecimal(balanceText.replace("$", "").replace(",", ""));
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
    @SpiraTestCase(testCaseId = 53392)
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
    @SpiraTestCase(testCaseId = 53393)
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
    @SpiraTestCase(testCaseId = 53394)
    @DisplayName("Opening a SAVINGS account shows the confirmation and hides the form")
    public void openSavingsAccount() {
        String newAccountId = openAccount("SAVINGS");

        String message = driver.findElement(By.xpath("//div[@id='openAccountResult']/p[1]")).getText();
        assertEquals("Congratulations, your account is now open.", message);
        assertFalse(driver.findElement(By.id("openAccountForm")).isDisplayed(), "Form should be hidden after opening");
        assertNotNull(newAccountId);
    }

    @Test
    @SpiraTestCase(testCaseId = 53395)
    @DisplayName("A newly opened account appears in Accounts Overview")
    public void newAccountAppearsInAccountsOverview() {
        String newAccountId = openAccount("SAVINGS");

        driver.findElement(By.partialLinkText("Accounts Overview")).click();
        WebElement accountLink = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.linkText(newAccountId)));
        assertNotNull(accountLink);
        assertEquals(newAccountId, accountLink.getText());
    }

    @Test
    @SpiraTestCase(testCaseId = 53396)
    @DisplayName("The new account number links to that account's details page")
    public void newAccountLinkOpensAccountDetails() {
        String newAccountId = openAccount("CHECKING");

        driver.findElement(By.xpath("//a[@id='newAccountId']")).click();
        wait.until(ExpectedConditions.urlContains("activity.htm?id=" + newAccountId));
        assertTrue(driver.getCurrentUrl().contains("id=" + newAccountId));
    }

    @Test
    @SpiraTestCase(testCaseId = 53398)
    @DisplayName("Opening an account moves the opening deposit from the funding account without changing the total")
    public void openingAccountTransfersDepositFromFundingAccount() {
        String fundingAccount = new Select(driver.findElement(By.id("fromAccountId")))
                .getFirstSelectedOption().getText();
        BigDecimal fundingBefore = readBalanceFromOverview(fundingAccount);

        driver.findElement(By.linkText("Open New Account")).click();
        waitForFundingAccounts();
        String newAccountId = openAccount("SAVINGS");

        BigDecimal fundingAfter = readBalanceFromAccountDetails(fundingAccount);
        BigDecimal newAccountBalance = readBalanceFromAccountDetails(newAccountId);

        assertAll("Opening deposit transfer",
                () -> assertTrue(newAccountBalance.compareTo(BigDecimal.ZERO) > 0,
                        "New account should receive an opening deposit, but its balance was " + newAccountBalance),
                () -> assertEquals(0, fundingBefore.subtract(newAccountBalance).compareTo(fundingAfter),
                        "Funding account should drop by exactly the opening deposit, keeping the total unchanged (before: "
                                + fundingBefore + ", deposit: " + newAccountBalance + ", after: " + fundingAfter + ")")
        );
    }

    @Test
    @SpiraTestCase(testCaseId = 53397)
    @DisplayName("A logged-out user cannot access the Open New Account page")
    public void loggedOutUserCannotOpenAccount() {
        driver.findElement(By.linkText("Log Out")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));

        driver.get("https://parabank.parasoft.com/parabank/openaccount.htm");

        assertTrue(driver.findElements(By.id("type")).isEmpty(),
                "Account type dropdown should not be available when logged out");
        assertTrue(driver.findElements(By.cssSelector("input[value='Open New Account']")).isEmpty(),
                "Open New Account button should not be available when logged out");
        assertTrue(driver.findElement(By.name("username")).isDisplayed(),
                "Login form should be shown instead");
    }
}