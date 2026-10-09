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


    private String sourceAccount() {

        return CommonSteps
                .getSourceAccountNumber();
    }


    private String destinationAccount() {

        return CommonSteps
                .getDestinationAccountNumber();
    }


    private WebDriverWait getWait() {

        if (wait == null) {

            wait =
                    new WebDriverWait(
                            Hooks.driver,
                            Duration.ofSeconds(15)
                    );
        }

        return wait;
    }


    // BACKGROUND
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
                    fromAccounts
                            .getOptions()
                            .stream()
                            .anyMatch(option ->
                                    option.getText()
                                            .trim()
                                            .equals(
                                                    sourceAccount()
                                            )
                            );


            boolean destinationExists =
                    toAccounts
                            .getOptions()
                            .stream()
                            .anyMatch(option ->
                                    option.getText()
                                            .trim()
                                            .equals(
                                                    destinationAccount()
                                            )
                            );


            return sourceExists &&
                    destinationExists;
        });


        assertNotEquals(
                sourceAccount(),
                destinationAccount(),
                "Two different accounts are required."
        );
    }


    // VALID TRANSFER
    @When("I transfer {string} from the first account to the second account")
    public void iTransferValidAmount(
            String amount) {

        enterTransferDetails(
                amount,
                sourceAccount(),
                destinationAccount()
        );

        submitTransfer();
    }


    @Then("I should see that the transfer was completed successfully")
    public void iShouldSeeThatTheTransferWasCompletedSuccessfully() {

        assertTransferComplete();
    }


    // NON-NUMERIC
    @When("I attempt to transfer {string} from the first account to the second account")
    public void iAttemptInvalidAmount(
            String amount) {

        enterTransferDetails(
                amount,
                sourceAccount(),
                destinationAccount()
        );

        submitTransfer();
    }


    // BLANK
    @When("I attempt to transfer a blank amount")
    public void iAttemptBlankAmount() {

        enterTransferDetails(
                "",
                sourceAccount(),
                destinationAccount()
        );

        submitTransfer();
    }


    @Then("the transfer should not be completed")
    public void theTransferShouldNotBeCompleted() {

        waitForTransferOutcome();


        assertFalse(
                isTransferCompleteVisible(),
                "Invalid transfer was incorrectly completed."
        );


        assertTrue(
                isErrorVisible(),
                "Expected transfer error was not displayed."
        );
    }


    @And("I should see an internal transfer error")
    public void iShouldSeeInternalTransferError() {

        assertTrue(
                Hooks.driver
                        .findElement(
                                By.id("rightPanel")
                        )
                        .getText()
                        .contains(
                                "An internal error has occurred and has been logged."
                        ),

                "Expected internal error message was not displayed."
        );
    }


    // ZERO
    @When("I attempt to transfer a zero amount")
    public void iAttemptZeroAmount() {

        zeroBalanceBefore =
                getSourceAccountBalance();


        openTransferFundsPage();


        enterTransferDetails(
                "0",
                sourceAccount(),
                destinationAccount()
        );


        submitTransfer();

        waitForTransferOutcome();


        zeroTransferCompleted =
                isTransferCompleteVisible();


        zeroBalanceAfter =
                getSourceAccountBalance();
    }


    @Then("the zero-value transfer should be rejected")
    public void zeroTransferShouldBeRejected() {

        assertAll(

                () -> assertFalse(
                        zeroTransferCompleted,

                        "DEFECT FOUND: ParaBank accepted a zero-value transfer."
                ),

                () -> assertEquals(
                        zeroBalanceBefore,
                        zeroBalanceAfter,
                        0.01,

                        "Source balance changed during zero-value transfer."
                )
        );
    }


    // NEGATIVE
    @When("I attempt to transfer the negative amount {string}")
    public void iAttemptNegativeAmount(
            String amount) {

        negativeBalanceBefore =
                getSourceAccountBalance();


        openTransferFundsPage();


        enterTransferDetails(
                amount,
                sourceAccount(),
                destinationAccount()
        );


        submitTransfer();

        waitForTransferOutcome();


        negativeTransferCompleted =
                isTransferCompleteVisible();


        negativeBalanceAfter =
                getSourceAccountBalance();
    }


    @Then("the negative-value transfer should be rejected")
    public void negativeTransferShouldBeRejected() {

        assertAll(

                () -> assertFalse(
                        negativeTransferCompleted,

                        "DEFECT FOUND: ParaBank accepted a negative transfer amount."
                ),

                () -> assertEquals(
                        negativeBalanceBefore,
                        negativeBalanceAfter,
                        0.01,

                        "DEFECT FOUND: Source balance changed after negative transfer."
                )
        );
    }


    // OVER BALANCE
    @When("I attempt to transfer more than the source account balance")
    public void iAttemptOverBalanceTransfer() {

        overBalanceBefore =
                getSourceAccountBalance();


        double excessiveAmount =
                Math.abs(
                        overBalanceBefore
                ) + 1000.00;


        openTransferFundsPage();


        String amount =
                String.format(
                        Locale.US,
                        "%.2f",
                        excessiveAmount
                );


        enterTransferDetails(
                amount,
                sourceAccount(),
                destinationAccount()
        );


        submitTransfer();

        waitForTransferOutcome();


        overBalanceTransferCompleted =
                isTransferCompleteVisible();


        overBalanceAfter =
                getSourceAccountBalance();
    }


    @Then("the over-balance transfer should be rejected")
    public void overBalanceTransferShouldBeRejected() {

        assertAll(

                () -> assertFalse(
                        overBalanceTransferCompleted,

                        "DEFECT FOUND: ParaBank allowed a transfer greater than the source balance."
                ),

                () -> assertEquals(
                        overBalanceBefore,
                        overBalanceAfter,
                        0.01,

                        "DEFECT FOUND: Source balance changed after over-balance transfer."
                )
        );
    }


    // SAME ACCOUNT
    @When("I attempt to transfer {string} from an account to the same account")
    public void iAttemptSameAccountTransfer(
            String amount) {

        sameAccountBalanceBefore =
                getSourceAccountBalance();


        openTransferFundsPage();


        enterTransferDetails(
                amount,
                sourceAccount(),
                sourceAccount()
        );


        submitTransfer();

        waitForTransferOutcome();


        sameAccountTransferCompleted =
                isTransferCompleteVisible();


        sameAccountBalanceAfter =
                getSourceAccountBalance();
    }


    @Then("the same-account transfer should be rejected")
    public void sameAccountTransferShouldBeRejected() {

        assertAll(

                () -> assertFalse(
                        sameAccountTransferCompleted,

                        "DEFECT FOUND: ParaBank allowed transfer to the same account."
                ),

                () -> assertEquals(
                        sameAccountBalanceBefore,
                        sameAccountBalanceAfter,
                        0.01,

                        "Account balance changed during same-account transfer."
                )
        );
    }


    // HELPERS
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


        getWait().until(driver -> {

            Select accounts =
                    new Select(
                            driver.findElement(
                                    By.id("fromAccountId")
                            )
                    );


            return accounts
                    .getOptions()
                    .stream()
                    .anyMatch(option ->
                            option.getText()
                                    .trim()
                                    .equals(
                                            sourceAccount()
                                    )
                    );
        });
    }


    private void enterTransferDetails(
            String amount,
            String from,
            String to) {

        WebElement amountField =
                getWait().until(
                        ExpectedConditions.visibilityOfElementLocated(
                                By.id("amount")
                        )
                );


        amountField.clear();

        amountField.sendKeys(
                amount
        );


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
                from
        );


        toAccount.selectByVisibleText(
                to
        );
    }


    private void submitTransfer() {

        getWait().until(
                ExpectedConditions.elementToBeClickable(
                        By.cssSelector(
                                "#rightPanel input[type='submit'][value='Transfer']"
                        )
                )
        ).click();
    }


    private void assertTransferComplete() {

        WebElement heading =
                getWait().until(
                        ExpectedConditions.visibilityOfElementLocated(
                                By.xpath(
                                        "//*[@id='rightPanel']//h1[normalize-space()='Transfer Complete!']"
                                )
                        )
                );


        assertEquals(
                "Transfer Complete!",
                heading.getText()
                        .trim()
        );
    }


    private boolean isTransferCompleteVisible() {

        return Hooks.driver
                .findElements(
                        By.xpath(
                                "//*[@id='rightPanel']//h1[normalize-space()='Transfer Complete!']"
                        )
                )
                .stream()
                .anyMatch(
                        WebElement::isDisplayed
                );
    }


    private boolean isErrorVisible() {

        return Hooks.driver
                .findElements(
                        By.xpath(
                                "//*[@id='rightPanel']//h1[normalize-space()='Error!']"
                        )
                )
                .stream()
                .anyMatch(
                        WebElement::isDisplayed
                );
    }


    private void waitForTransferOutcome() {

        getWait().until(driver ->
                isTransferCompleteVisible() ||
                        isErrorVisible()
        );
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


        WebElement row =
                getWait().until(
                        ExpectedConditions.visibilityOfElementLocated(
                                By.xpath(
                                        "//table[@id='accountTable']//tr[td/a[normalize-space()='" +
                                                sourceAccount() +
                                                "']]"
                                )
                        )
                );


        String balance =
                row.findElements(
                                By.tagName("td")
                        )
                        .get(1)
                        .getText();


        return parseMoney(
                balance
        );
    }


    private double parseMoney(
            String value) {

        String cleaned =
                value
                        .replace("$", "")
                        .replace(",", "")
                        .trim();


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