package stepdefinitions;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

public class TransferFundsSteps {

    private WebDriverWait wait;

    // Accounts manually verified for this ParaBank test user
    private static final String FROM_ACCOUNT = "14676";
    private static final String TO_ACCOUNT = "13677";

    // Values captured for defect-revealing scenarios
    private double zeroBalanceBefore;
    private double zeroBalanceAfter;
    private boolean zeroTransferCompleted;

    private double negativeBalanceBefore;
    private double negativeBalanceAfter;
    private boolean negativeTransferCompleted;

    private double overBalanceBefore;
    private double overBalanceAfter;
    private boolean overBalanceTransferCompleted;

    private double sameAccountBalanceBefore;
    private double sameAccountBalanceAfter;
    private boolean sameAccountTransferCompleted;


    private WebDriverWait getWait() {

        if (wait == null) {
            wait = new WebDriverWait(
                    Hooks.driver,
                    Duration.ofSeconds(15)
            );
        }

        return wait;
    }



    // BACKGROUND check
    @And("I am on the Transfer Funds page")
    public void iAmOnTheTransferFundsPage() {

        openTransferFundsPage();

        assertTrue(
                Hooks.driver.findElement(
                        By.id("amount")
                ).isDisplayed(),
                "Transfer Funds page was not displayed."
        );
    }


    @And("at least two accounts are available for transfer")
    public void atLeastTwoAccountsAreAvailableForTransfer() {

        getWait().until(driver -> {

            Select fromAccounts =
                    new Select(
                            driver.findElement(
                                    By.id("fromAccountId")
                            )
                    );

            Select toAccounts =
                    new Select(
                            driver.findElement(
                                    By.id("toAccountId")
                            )
                    );

            boolean sourceExists =
                    fromAccounts.getOptions()
                            .stream()
                            .anyMatch(option ->
                                    option.getText()
                                            .trim()
                                            .equals(FROM_ACCOUNT)
                            );

            boolean destinationExists =
                    toAccounts.getOptions()
                            .stream()
                            .anyMatch(option ->
                                    option.getText()
                                            .trim()
                                            .equals(TO_ACCOUNT)
                            );

            return sourceExists && destinationExists;
        });


        Select fromAccounts =
                new Select(
                        Hooks.driver.findElement(
                                By.id("fromAccountId")
                        )
                );

        Select toAccounts =
                new Select(
                        Hooks.driver.findElement(
                                By.id("toAccountId")
                        )
                );


        assertAll(

                () -> assertTrue(
                        fromAccounts.getOptions().size() >= 2,
                        "At least two source accounts are required."
                ),

                () -> assertTrue(
                        toAccounts.getOptions().size() >= 2,
                        "At least two destination accounts are required."
                )
        );
    }


    // VALID TRANSFERS
    // Includes integer and decimal amounts
    @When("I transfer {string} from the first account to the second account")
    public void iTransferValidAmount(String amount) {

        enterTransferDetails(
                amount,
                FROM_ACCOUNT,
                TO_ACCOUNT
        );

        submitTransfer();
    }


    @Then("I should see that the transfer was completed successfully")
    public void iShouldSeeThatTheTransferWasCompletedSuccessfully() {

        assertTransferComplete();
    }


    // NON-NUMERIC INVALID INPUT
    @When("I attempt to transfer {string} from the first account to the second account")
    public void iAttemptToTransferInvalidAmount(String amount) {

        enterTransferDetails(
                amount,
                FROM_ACCOUNT,
                TO_ACCOUNT
        );

        submitTransfer();
    }


    // BLANK AMOUNT
    @When("I attempt to transfer a blank amount")
    public void iAttemptToTransferABlankAmount() {

        enterTransferDetails(
                "",
                FROM_ACCOUNT,
                TO_ACCOUNT
        );

        submitTransfer();
    }


    // Used by both "abc" and blank amount scenarios
    @Then("the transfer should not be completed")
    public void theTransferShouldNotBeCompleted() {

        waitForTransferOutcome();

        boolean successDisplayed =
                isTransferCompleteVisible();

        boolean errorDisplayed =
                isErrorVisible();

        assertAll(

                () -> assertFalse(
                        successDisplayed,
                        "The invalid transfer was incorrectly completed."
                ),

                () -> assertTrue(
                        errorDisplayed,
                        "ParaBank did not display the expected error."
                )
        );
    }


    @And("I should see an internal transfer error")
    public void iShouldSeeAnInternalTransferError() {

        WebElement rightPanel =
                Hooks.driver.findElement(
                        By.id("rightPanel")
                );

        assertTrue(
                rightPanel.getText().contains(
                        "An internal error has occurred and has been logged."
                ),
                "Expected internal transfer error was not displayed."
        );
    }


    // DEFECT 1 - ZERO AMOUNT
    @When("I attempt to transfer a zero amount")
    public void iAttemptToTransferAZeroAmount() {

        zeroBalanceBefore =
                getSourceAccountBalance();

        openTransferFundsPage();

        enterTransferDetails(
                "0",
                FROM_ACCOUNT,
                TO_ACCOUNT
        );

        submitTransfer();

        waitForTransferOutcome();

        zeroTransferCompleted =
                isTransferCompleteVisible();

        zeroBalanceAfter =
                getSourceAccountBalance();
    }


    @Then("the zero-value transfer should be rejected")
    public void theZeroValueTransferShouldBeRejected() {

        assertAll(

                () -> assertFalse(
                        zeroTransferCompleted,

                        "DEFECT FOUND: ParaBank accepted a zero-value " +
                                "transfer instead of rejecting it."
                ),

                () -> assertEquals(
                        zeroBalanceBefore,
                        zeroBalanceAfter,
                        0.01,

                        "DEFECT FOUND: Source account balance changed " +
                                "during the zero-value transfer. " +
                                "Balance before = " +
                                zeroBalanceBefore +
                                ", balance after = " +
                                zeroBalanceAfter
                )
        );
    }


    // DEFECT 2 - NEGATIVE AMOUNT
    @When("I attempt to transfer the negative amount {string}")
    public void iAttemptToTransferTheNegativeAmount(
            String amount) {

        negativeBalanceBefore =
                getSourceAccountBalance();

        openTransferFundsPage();

        enterTransferDetails(
                amount,
                FROM_ACCOUNT,
                TO_ACCOUNT
        );

        submitTransfer();

        waitForTransferOutcome();

        negativeTransferCompleted =
                isTransferCompleteVisible();

        negativeBalanceAfter =
                getSourceAccountBalance();
    }


    @Then("the negative-value transfer should be rejected")
    public void theNegativeValueTransferShouldBeRejected() {

        assertAll(

                () -> assertFalse(
                        negativeTransferCompleted,

                        "DEFECT FOUND: ParaBank accepted a negative " +
                                "transfer amount instead of rejecting it."
                ),

                () -> assertEquals(
                        negativeBalanceBefore,
                        negativeBalanceAfter,
                        0.01,

                        "DEFECT FOUND: The source account balance changed " +
                                "after a negative transfer. " +
                                "Balance before = " +
                                negativeBalanceBefore +
                                ", balance after = " +
                                negativeBalanceAfter
                )
        );
    }


    // DEFECT 3 - AMOUNT GREATER THAN BALANCE
    @When("I attempt to transfer more than the source account balance")
    public void iAttemptToTransferMoreThanTheSourceAccountBalance() {

        overBalanceBefore =
                getSourceAccountBalance();

        /*
         * Helps to generate an amount that is definitely greater than
         * the current balance.
         */
        double excessiveAmount =
                Math.abs(overBalanceBefore) + 1000.00;

        openTransferFundsPage();

        String amount =
                String.format(
                        Locale.US,
                        "%.2f",
                        excessiveAmount
                );

        enterTransferDetails(
                amount,
                FROM_ACCOUNT,
                TO_ACCOUNT
        );

        submitTransfer();

        waitForTransferOutcome();

        overBalanceTransferCompleted =
                isTransferCompleteVisible();

        overBalanceAfter =
                getSourceAccountBalance();
    }


    @Then("the over-balance transfer should be rejected")
    public void theOverBalanceTransferShouldBeRejected() {

        assertAll(

                () -> assertFalse(
                        overBalanceTransferCompleted,

                        "DEFECT FOUND: ParaBank allowed a transfer " +
                                "greater than the source account balance."
                ),

                () -> assertEquals(
                        overBalanceBefore,
                        overBalanceAfter,
                        0.01,

                        "DEFECT FOUND: The source balance changed after " +
                                "an over-balance transfer. " +
                                "Balance before = " +
                                overBalanceBefore +
                                ", balance after = " +
                                overBalanceAfter
                )
        );
    }


    // DEFECT 4 - SAME SOURCE AND DESTINATION ACCOUNT
    @When("I attempt to transfer {string} from an account to the same account")
    public void iAttemptToTransferFromAnAccountToTheSameAccount(
            String amount) {

        sameAccountBalanceBefore =
                getSourceAccountBalance();

        openTransferFundsPage();

        /*
         * Use FROM_ACCOUNT as both sender and receiver.
         */
        enterTransferDetails(
                amount,
                FROM_ACCOUNT,
                FROM_ACCOUNT
        );

        submitTransfer();

        waitForTransferOutcome();

        sameAccountTransferCompleted =
                isTransferCompleteVisible();

        sameAccountBalanceAfter =
                getSourceAccountBalance();
    }


    @Then("the same-account transfer should be rejected")
    public void theSameAccountTransferShouldBeRejected() {

        assertAll(

                () -> assertFalse(
                        sameAccountTransferCompleted,

                        "DEFECT FOUND: ParaBank allowed a transfer " +
                                "where the source and destination accounts " +
                                "were the same."
                ),

                () -> assertEquals(
                        sameAccountBalanceBefore,
                        sameAccountBalanceAfter,
                        0.01,

                        "DEFECT FOUND: The account balance changed during " +
                                "a same-account transfer. " +
                                "Balance before = " +
                                sameAccountBalanceBefore +
                                ", balance after = " +
                                sameAccountBalanceAfter
                )
        );
    }


    // HELPER Functions
    private void openTransferFundsPage() {

        getWait().until(
                ExpectedConditions.elementToBeClickable(
                        By.linkText("Transfer Funds")
                )
        ).click();


        getWait().until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.id("amount")
                )
        );


        // Wait until the required account data has loaded
        getWait().until(driver -> {

            Select accounts =
                    new Select(
                            driver.findElement(
                                    By.id("fromAccountId")
                            )
                    );

            return accounts.getOptions()
                    .stream()
                    .anyMatch(option ->
                            option.getText()
                                    .trim()
                                    .equals(FROM_ACCOUNT)
                    );
        });
    }


    private void enterTransferDetails(
            String amount,
            String fromAccountNumber,
            String toAccountNumber) {

        WebElement amountField =
                getWait().until(
                        ExpectedConditions.visibilityOfElementLocated(
                                By.id("amount")
                        )
                );

        amountField.clear();

        // Empty string is intentional for the blank-input test
        amountField.sendKeys(amount);


        Select fromAccount =
                new Select(
                        Hooks.driver.findElement(
                                By.id("fromAccountId")
                        )
                );


        Select toAccount =
                new Select(
                        Hooks.driver.findElement(
                                By.id("toAccountId")
                        )
                );


        fromAccount.selectByVisibleText(
                fromAccountNumber
        );


        toAccount.selectByVisibleText(
                toAccountNumber
        );
    }


    private void submitTransfer() {

        WebElement transferButton =
                getWait().until(
                        ExpectedConditions.elementToBeClickable(
                                By.cssSelector(
                                        "#rightPanel " +
                                                "input[type='submit']" +
                                                "[value='Transfer']"
                                )
                        )
                );

        transferButton.click();
    }


    private void assertTransferComplete() {

        WebElement heading =
                getWait().until(
                        ExpectedConditions.visibilityOfElementLocated(
                                By.xpath(
                                        "//*[@id='rightPanel']//h1" +
                                                "[normalize-space()='Transfer Complete!']"
                                )
                        )
                );


        assertEquals(
                "Transfer Complete!",
                heading.getText().trim(),
                "Transfer completion message was not displayed."
        );


        assertTrue(
                Hooks.driver.findElement(
                        By.id("rightPanel")
                ).getText().contains(
                        "has been transferred"
                ),

                "Transfer confirmation details were not displayed."
        );
    }


    private boolean isTransferCompleteVisible() {

        return Hooks.driver.findElements(
                        By.xpath(
                                "//*[@id='rightPanel']//h1" +
                                        "[normalize-space()='Transfer Complete!']"
                        )
                )
                .stream()
                .anyMatch(
                        WebElement::isDisplayed
                );
    }


    private boolean isErrorVisible() {

        return Hooks.driver.findElements(
                        By.xpath(
                                "//*[@id='rightPanel']//h1" +
                                        "[normalize-space()='Error!']"
                        )
                )
                .stream()
                .anyMatch(
                        WebElement::isDisplayed
                );
    }


    private void waitForTransferOutcome() {

        getWait().until(driver -> {

            boolean success =
                    driver.findElements(
                                    By.xpath(
                                            "//*[@id='rightPanel']//h1" +
                                                    "[normalize-space()='Transfer Complete!']"
                                    )
                            )
                            .stream()
                            .anyMatch(
                                    WebElement::isDisplayed
                            );


            boolean error =
                    driver.findElements(
                                    By.xpath(
                                            "//*[@id='rightPanel']//h1" +
                                                    "[normalize-space()='Error!']"
                                    )
                            )
                            .stream()
                            .anyMatch(
                                    WebElement::isDisplayed
                            );


            return success || error;
        });
    }


    private double getSourceAccountBalance() {

        getWait().until(
                ExpectedConditions.elementToBeClickable(
                        By.linkText("Accounts Overview")
                )
        ).click();


        getWait().until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.id("accountTable")
                )
        );


        WebElement accountRow =
                getWait().until(
                        ExpectedConditions.visibilityOfElementLocated(
                                By.xpath(
                                        "//table[@id='accountTable']" +
                                                "//tr[td/a[normalize-space()='" +
                                                FROM_ACCOUNT +
                                                "']]"
                                )
                        )
                );


        String balanceText =
                accountRow.findElements(
                                By.tagName("td")
                        )
                        .get(1)
                        .getText();


        return parseMoney(
                balanceText
        );
    }


    private double parseMoney(
            String value) {

        String cleaned =
                value
                        .replace("$", "")
                        .replace(",", "")
                        .trim();


        // Handles values such as ($100.00)
        if (cleaned.startsWith("(")
                && cleaned.endsWith(")")) {

            cleaned =
                    "-" +
                            cleaned.substring(
                                    1,
                                    cleaned.length() - 1
                            );
        }


        return Double.parseDouble(
                cleaned
        );
    }
}