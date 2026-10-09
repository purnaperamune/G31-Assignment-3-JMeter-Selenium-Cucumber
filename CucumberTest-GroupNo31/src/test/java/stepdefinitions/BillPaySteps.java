package stepdefinitions;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
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

public class BillPaySteps {

    private WebDriverWait wait;

    // Defect test values
    private double zeroBalanceBefore;
    private double zeroBalanceAfter;
    private boolean zeroPaymentCompleted;

    private double negativeBalanceBefore;
    private double negativeBalanceAfter;
    private boolean negativePaymentCompleted;

    private double overBalanceBefore;
    private double overBalanceAfter;
    private boolean overBalancePaymentCompleted;


    // DYNAMIC ACCOUNT
    private String sourceAccount() {

        return CommonSteps
                .getSourceAccountNumber();
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
    @And("I am on the Bill Pay page")
    public void iAmOnTheBillPayPage() {

        openBillPayPage();

        assertTrue(
                Hooks.driver.findElement(
                        By.name("payee.name")
                ).isDisplayed(),
                "Bill Pay page was not displayed."
        );
    }


    @And("a valid source account is available for bill payment")
    public void aValidSourceAccountIsAvailableForBillPayment() {

        getWait().until(driver -> {

            Select accounts =
                    new Select(
                            driver.findElement(
                                    By.name("fromAccountId")
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


        Select accounts =
                new Select(
                        Hooks.driver.findElement(
                                By.name("fromAccountId")
                        )
                );


        boolean sourceExists =
                accounts
                        .getOptions()
                        .stream()
                        .anyMatch(option ->
                                option.getText()
                                        .trim()
                                        .equals(
                                                sourceAccount()
                                        )
                        );


        assertTrue(
                sourceExists,
                "Dynamic source account was not available for Bill Pay."
        );


        accounts.selectByVisibleText(
                sourceAccount()
        );
    }


    // PAYEE DETAILS
    @Given("I have entered valid payee contact details")
    public void iHaveEnteredValidPayeeContactDetails() {

        fillPayeeContactDetails();
    }


    @Given("I have entered valid complete payee details")
    public void iHaveEnteredValidCompletePayeeDetails() {

        fillValidCompletePayeeDetails();
    }


    // SUCCESSFUL BILL PAYMENT
    @When("I submit a bill payment of {string}")
    public void iSubmitABillPaymentOf(
            String amount) {

        enterAmount(
                amount
        );

        submitPayment();
    }


    @Then("I should see that the bill payment was completed successfully")
    public void iShouldSeeThatTheBillPaymentWasCompletedSuccessfully() {

        WebElement heading =
                getWait().until(
                        ExpectedConditions.visibilityOfElementLocated(
                                By.xpath(
                                        "//*[@id='rightPanel']//h1" +
                                                "[normalize-space()='Bill Payment Complete']"
                                )
                        )
                );


        assertEquals(
                "Bill Payment Complete",
                heading.getText()
                        .trim(),
                "Bill Payment Complete heading was not displayed."
        );


        String resultText =
                Hooks.driver.findElement(
                                By.id("rightPanel")
                        )
                        .getText();


        assertTrue(
                resultText.contains(
                        "was successful"
                ),
                "Successful bill-payment details were not displayed."
        );
    }


    // ALL PAYEE DETAILS BLANK
    @When("I submit a bill payment with all payee details blank and amount {string}")
    public void iSubmitBillPaymentWithAllPayeeDetailsBlank(
            String amount) {

        /*
         * Leave all payee fields blank.
         * Only enter the amount.
         */

        enterAmount(
                amount
        );

        selectSourceAccount();

        submitPayment();
    }


    @Then("I should see the required payee field validation errors")
    public void iShouldSeeTheRequiredPayeeFieldValidationErrors() {

        getWait().until(driver ->
                driver.findElement(
                                By.id("rightPanel")
                        )
                        .getText()
                        .contains(
                                "Payee name is required."
                        )
        );


        String pageText =
                Hooks.driver.findElement(
                                By.id("rightPanel")
                        )
                        .getText();


        assertAll(

                () -> assertTrue(
                        pageText.contains(
                                "Payee name is required."
                        ),
                        "Payee name validation message missing."
                ),

                () -> assertTrue(
                        pageText.contains(
                                "Address is required."
                        ),
                        "Address validation message missing."
                ),

                () -> assertTrue(
                        pageText.contains(
                                "City is required."
                        ),
                        "City validation message missing."
                ),

                () -> assertTrue(
                        pageText.contains(
                                "State is required."
                        ),
                        "State validation message missing."
                ),

                () -> assertTrue(
                        pageText.contains(
                                "Zip Code is required."
                        ),
                        "Zip Code validation message missing."
                ),

                () -> assertTrue(
                        pageText.contains(
                                "Phone number is required."
                        ),
                        "Phone number validation message missing."
                ),

                () -> assertTrue(
                        visibleExactTextCount(
                                "Account number is required."
                        ) >= 2,
                        "Both account-number validation messages were not displayed."
                )
        );
    }


    // ACCOUNT NUMBERS BLANK
    @When("I submit a bill payment with blank account numbers and amount {string}")
    public void iSubmitBillPaymentWithBlankAccountNumbers(
            String amount) {

        /*
         * Contact information is already entered by the Given step.
         * Leave account and verify-account blank.
         */

        enterAmount(
                amount
        );

        selectSourceAccount();

        submitPayment();
    }


    @Then("I should see the account number required validation errors")
    public void iShouldSeeTheAccountNumberRequiredValidationErrors() {

        getWait().until(driver ->
                visibleExactTextCount(
                        "Account number is required."
                ) >= 2
        );


        assertTrue(
                visibleExactTextCount(
                        "Account number is required."
                ) >= 2,

                "Expected account-number-required messages were not displayed."
        );
    }


    // BLANK AMOUNT
    @When("I submit a bill payment with a blank amount")
    public void iSubmitABillPaymentWithABlankAmount() {

        enterAmount(
                ""
        );

        selectSourceAccount();

        submitPayment();
    }


    @Then("I should see the amount empty validation message")
    public void iShouldSeeTheAmountEmptyValidationMessage() {

        getWait().until(driver ->
                driver.findElement(
                                By.id("rightPanel")
                        )
                        .getText()
                        .contains(
                                "The amount cannot be empty."
                        )
        );


        assertTrue(
                Hooks.driver.findElement(
                                By.id("rightPanel")
                        )
                        .getText()
                        .contains(
                                "The amount cannot be empty."
                        ),

                "Expected blank-amount validation message was not displayed."
        );
    }


    // NON-NUMERIC AMOUNT
    @When("I submit a bill payment with the non-numeric amount {string}")
    public void iSubmitABillPaymentWithNonNumericAmount(
            String amount) {

        enterAmount(
                amount
        );

        selectSourceAccount();

        submitPayment();
    }


    @Then("I should see the invalid amount validation message")
    public void iShouldSeeTheInvalidAmountValidationMessage() {

        getWait().until(driver ->
                driver.findElement(
                                By.id("rightPanel")
                        )
                        .getText()
                        .contains(
                                "Please enter a valid amount."
                        )
        );


        assertTrue(
                Hooks.driver.findElement(
                                By.id("rightPanel")
                        )
                        .getText()
                        .contains(
                                "Please enter a valid amount."
                        ),

                "Expected invalid-amount validation message was not displayed."
        );
    }


    // DEFECT 1 - ZERO BILL PAYMENT
    @When("I attempt to make a zero-value bill payment")
    public void iAttemptToMakeAZeroValueBillPayment() {

        zeroBalanceBefore =
                getSourceAccountBalance();


        openBillPayPage();

        fillValidCompletePayeeDetails();

        enterAmount(
                "0"
        );

        selectSourceAccount();

        submitPayment();

        waitForBillPayOutcome();


        zeroPaymentCompleted =
                isBillPaymentCompleteVisible();


        zeroBalanceAfter =
                getSourceAccountBalance();
    }


    @Then("the zero-value bill payment should be rejected")
    public void theZeroValueBillPaymentShouldBeRejected() {

        assertAll(

                () -> assertFalse(
                        zeroPaymentCompleted,

                        "DEFECT FOUND: ParaBank accepted a zero-value " +
                                "bill payment instead of rejecting it."
                ),

                () -> assertEquals(
                        zeroBalanceBefore,
                        zeroBalanceAfter,
                        0.01,

                        "Source account balance changed during a zero-value payment. " +
                                "Balance before = " +
                                zeroBalanceBefore +
                                ", balance after = " +
                                zeroBalanceAfter
                )
        );
    }


    // DEFECT 2 - NEGATIVE BILL PAYMENT
    @When("I attempt to make a bill payment of {string}")
    public void iAttemptToMakeABillPaymentOf(
            String amount) {

        negativeBalanceBefore =
                getSourceAccountBalance();


        openBillPayPage();

        fillValidCompletePayeeDetails();

        enterAmount(
                amount
        );

        selectSourceAccount();

        submitPayment();

        waitForBillPayOutcome();


        negativePaymentCompleted =
                isBillPaymentCompleteVisible();


        negativeBalanceAfter =
                getSourceAccountBalance();
    }


    @Then("the negative bill payment should be rejected")
    public void theNegativeBillPaymentShouldBeRejected() {

        assertAll(

                () -> assertFalse(
                        negativePaymentCompleted,

                        "DEFECT FOUND: ParaBank accepted a negative " +
                                "bill-payment amount instead of rejecting it."
                ),

                () -> assertEquals(
                        negativeBalanceBefore,
                        negativeBalanceAfter,
                        0.01,

                        "DEFECT FOUND: Source account balance changed after " +
                                "a negative bill payment. " +
                                "Balance before = " +
                                negativeBalanceBefore +
                                ", balance after = " +
                                negativeBalanceAfter
                )
        );
    }


    // DEFECT 3 - PAYMENT GREATER THAN BALANCE
    @When("I attempt to make a bill payment greater than the source account balance")
    public void iAttemptToMakeABillPaymentGreaterThanTheSourceAccountBalance() {

        overBalanceBefore =
                getSourceAccountBalance();


        /*
         * Dynamically generate an amount guaranteed to be
         * greater than the current balance.
         */
        double excessiveAmount =
                Math.abs(
                        overBalanceBefore
                ) + 1000.00;


        openBillPayPage();

        fillValidCompletePayeeDetails();


        String amount =
                String.format(
                        Locale.US,
                        "%.2f",
                        excessiveAmount
                );


        enterAmount(
                amount
        );

        selectSourceAccount();

        submitPayment();

        waitForBillPayOutcome();


        overBalancePaymentCompleted =
                isBillPaymentCompleteVisible();


        overBalanceAfter =
                getSourceAccountBalance();
    }


    @Then("the over-balance bill payment should be rejected")
    public void theOverBalanceBillPaymentShouldBeRejected() {

        assertAll(

                () -> assertFalse(
                        overBalancePaymentCompleted,

                        "DEFECT FOUND: ParaBank allowed a bill payment " +
                                "greater than the available source-account balance."
                ),

                () -> assertEquals(
                        overBalanceBefore,
                        overBalanceAfter,
                        0.01,

                        "DEFECT FOUND: Source account balance changed after " +
                                "an over-balance bill payment. " +
                                "Balance before = " +
                                overBalanceBefore +
                                ", balance after = " +
                                overBalanceAfter
                )
        );
    }


    // HELPER - OPEN BILL PAY
    private void openBillPayPage() {

        getWait().until(
                ExpectedConditions.elementToBeClickable(
                        By.linkText(
                                "Bill Pay"
                        )
                )
        ).click();


        getWait().until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.name(
                                "payee.name"
                        )
                )
        );


        /*
         * Wait until the dynamically generated account
         * appears in the Bill Pay account dropdown.
         */
        getWait().until(driver -> {

            Select accounts =
                    new Select(
                            driver.findElement(
                                    By.name(
                                            "fromAccountId"
                                    )
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


        selectSourceAccount();
    }


    // HELPER - PAYEE CONTACT DETAILS
    private void fillPayeeContactDetails() {

        setField(
                "payee.name",
                "Mobitel"
        );

        setField(
                "payee.address.street",
                "9 Ashted Rd"
        );

        setField(
                "payee.address.city",
                "Box Hill"
        );

        setField(
                "payee.address.state",
                "VIC"
        );

        setField(
                "payee.address.zipCode",
                "3128"
        );

        setField(
                "payee.phoneNumber",
                "0468478050"
        );
    }


    // HELPER - ACCOUNT DETAILS
    private void fillPayeeAccountDetails() {

        setField(
                "payee.accountNumber",
                "12345"
        );

        setField(
                "verifyAccount",
                "12345"
        );
    }


    private void fillValidCompletePayeeDetails() {

        fillPayeeContactDetails();

        fillPayeeAccountDetails();
    }


    // HELPER - AMOUNT
    private void enterAmount(
            String amount) {

        setField(
                "amount",
                amount
        );
    }


    // HELPER - SOURCE ACCOUNT
    private void selectSourceAccount() {

        Select accounts =
                new Select(
                        getWait().until(
                                ExpectedConditions.visibilityOfElementLocated(
                                        By.name(
                                                "fromAccountId"
                                        )
                                )
                        )
                );


        accounts.selectByVisibleText(
                sourceAccount()
        );
    }

    // HELPER - SET FIELD
    private void setField(
            String name,
            String value) {

        WebElement field =
                getWait().until(
                        ExpectedConditions.visibilityOfElementLocated(
                                By.name(
                                        name
                                )
                        )
                );


        field.clear();

        field.sendKeys(
                value
        );
    }


    // HELPER - SUBMIT
    private void submitPayment() {

        WebElement button =
                getWait().until(driver -> {

                    var buttons =
                            driver.findElements(
                                    By.xpath(
                                            "//input[@value='Send Payment']" +
                                                    " | " +
                                                    "//button[normalize-space()='Send Payment']"
                                    )
                            );

                    return buttons.stream()
                            .filter(WebElement::isDisplayed)
                            .filter(WebElement::isEnabled)
                            .findFirst()
                            .orElse(null);
                });


        assertNotNull(
                button,
                "Visible Send Payment button was not found."
        );


        try {

            button.click();

        } catch (Exception e) {

            /*
             * Fallback if Chrome/Selenium reports that the
             * visible control cannot be clicked normally.
             */
            ((org.openqa.selenium.JavascriptExecutor) Hooks.driver)
                    .executeScript(
                            "arguments[0].click();",
                            button
                    );
        }
    }


    // HELPER - PAYMENT RESULT
    private boolean isBillPaymentCompleteVisible() {

        return Hooks.driver
                .findElements(
                        By.xpath(
                                "//*[@id='rightPanel']//h1" +
                                        "[normalize-space()='Bill Payment Complete']"
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
                                "//*[@id='rightPanel']//h1" +
                                        "[normalize-space()='Error!']"
                        )
                )
                .stream()
                .anyMatch(
                        WebElement::isDisplayed
                );
    }


    private void waitForBillPayOutcome() {

        getWait().until(driver ->

                isBillPaymentCompleteVisible()
                        ||
                        isErrorVisible()
        );
    }


    // HELPER - BALANCE
    private double getSourceAccountBalance() {

        getWait().until(
                ExpectedConditions.elementToBeClickable(
                        By.linkText(
                                "Accounts Overview"
                        )
                )
        ).click();


        getWait().until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.id(
                                "accountTable"
                        )
                )
        );


        WebElement row =
                getWait().until(
                        ExpectedConditions.visibilityOfElementLocated(
                                By.xpath(
                                        "//table[@id='accountTable']" +
                                                "//tr[td/a[normalize-space()='" +
                                                sourceAccount() +
                                                "']]"
                                )
                        )
                );


        String balanceText =
                row.findElements(
                                By.tagName(
                                        "td"
                                )
                        )
                        .get(1)
                        .getText();


        return parseMoney(
                balanceText
        );
    }


    // HELPER - VALIDATION MESSAGE COUNT
    private int visibleExactTextCount(
            String text) {

        return (int) Hooks.driver
                .findElements(
                        By.xpath(
                                "//*[normalize-space()=\"" +
                                        text +
                                        "\"]"
                        )
                )
                .stream()
                .filter(
                        WebElement::isDisplayed
                )
                .count();
    }


    // HELPER - MONEY
    private double parseMoney(
            String value) {

        String cleaned =
                value
                        .replace(
                                "$",
                                ""
                        )
                        .replace(
                                ",",
                                ""
                        )
                        .trim();


        /*
         * Handle accounting-style negative numbers:
         * ($100.00)
         */
        if (cleaned.startsWith("(")
                &&
                cleaned.endsWith(")")) {

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