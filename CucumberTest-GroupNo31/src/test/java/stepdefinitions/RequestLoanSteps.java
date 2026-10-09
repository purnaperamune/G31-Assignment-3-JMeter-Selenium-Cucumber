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
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

public class RequestLoanSteps {

    private WebDriverWait wait;

    // Defect-result flags
    private boolean negativeDownPaymentApproved;
    private boolean greaterThanLoanDownPaymentApproved;


    // DYNAMIC SOURCE ACCOUNT
    private String sourceAccount() {

        return CommonSteps.getSourceAccountNumber();
    }


    private WebDriverWait getWait() {

        if (wait == null) {

            wait = new WebDriverWait(
                    Hooks.driver,
                    Duration.ofSeconds(15)
            );
        }

        return wait;
    }


    // BACKGROUND
    @And("I am on the Request Loan page")
    public void iAmOnTheRequestLoanPage() {

        openRequestLoanPage();

        assertAll(

                () -> assertTrue(
                        Hooks.driver.findElement(
                                By.id("amount")
                        ).isDisplayed(),
                        "Loan Amount field was not displayed."
                ),

                () -> assertTrue(
                        Hooks.driver.findElement(
                                By.id("downPayment")
                        ).isDisplayed(),
                        "Down Payment field was not displayed."
                )
        );
    }


    @And("a valid source account is available for the loan request")
    public void aValidSourceAccountIsAvailableForLoanRequest() {

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
                                    .equals(sourceAccount())
                    );
        });


        Select accounts =
                new Select(
                        Hooks.driver.findElement(
                                By.id("fromAccountId")
                        )
                );


        assertTrue(
                accounts.getOptions()
                        .stream()
                        .anyMatch(option ->
                                option.getText()
                                        .trim()
                                        .equals(sourceAccount())
                        ),

                "Dynamic source account was not available for Request Loan."
        );


        accounts.selectByVisibleText(
                sourceAccount()
        );
    }


    // STANDARD REQUEST
    @When("I request a loan amount of {string} with a down payment of {string}")
    public void iRequestALoanAmountWithADownPayment(
            String loanAmount,
            String downPayment) {

        enterLoanDetails(
                loanAmount,
                downPayment
        );

        submitLoanRequest();

        waitForLoanOutcome();


        /*
         * Used by the negative-down-payment defect scenario.
         */
        if (downPayment.startsWith("-")) {

            negativeDownPaymentApproved =
                    isLoanApproved();
        }
    }


    // BLANK LOAN AMOUNT
    @When("I request a loan with a blank loan amount and down payment {string}")
    public void iRequestALoanWithABlankLoanAmount(
            String downPayment) {

        enterLoanDetails(
                "",
                downPayment
        );

        submitLoanRequest();

        waitForLoanOutcome();
    }


    // BLANK DOWN PAYMENT
    @When("I request a loan amount of {string} with a blank down payment")
    public void iRequestALoanWithABlankDownPayment(
            String loanAmount) {

        enterLoanDetails(
                loanAmount,
                ""
        );

        submitLoanRequest();

        waitForLoanOutcome();
    }


    // VALID APPROVED LOAN
    @Then("the loan request should be processed successfully")
    public void theLoanRequestShouldBeProcessedSuccessfully() {

        WebElement heading =
                getWait().until(
                        ExpectedConditions.visibilityOfElementLocated(
                                By.xpath(
                                        "//*[@id='rightPanel']//h1" +
                                                "[normalize-space()='Loan Request Processed']"
                                )
                        )
                );


        assertEquals(
                "Loan Request Processed",
                heading.getText().trim(),
                "Loan Request Processed heading was not displayed."
        );
    }


    @And("the loan should be approved")
    public void theLoanShouldBeApproved() {

        String resultText =
                Hooks.driver.findElement(
                                By.id("rightPanel")
                        )
                        .getText();


        assertAll(

                () -> assertTrue(
                        resultText.contains("Approved"),
                        "Loan status was not Approved."
                ),

                () -> assertTrue(
                        resultText.contains(
                                "Congratulations, your loan has been approved."
                        ),
                        "Loan approval message was not displayed."
                )
        );
    }


    // INTERNAL ERROR CASES
    @Then("I should see an internal loan request error")
    public void iShouldSeeAnInternalLoanRequestError() {

        WebElement heading =
                getWait().until(
                        ExpectedConditions.visibilityOfElementLocated(
                                By.xpath(
                                        "//*[@id='rightPanel']//h1" +
                                                "[normalize-space()='Error!']"
                                )
                        )
                );


        assertEquals(
                "Error!",
                heading.getText().trim(),
                "Expected Error heading was not displayed."
        );


        String pageText =
                Hooks.driver.findElement(
                                By.id("rightPanel")
                        )
                        .getText();


        assertTrue(
                pageText.contains(
                        "An internal error has occurred and has been logged."
                ),

                "Expected internal loan-request error message was not displayed."
        );
    }


    // NEGATIVE LOAN AMOUNT
    @Then("the negative loan request should be denied")
    public void theNegativeLoanRequestShouldBeDenied() {

        assertTrue(
                isLoanDenied(),
                "Negative loan amount was not denied."
        );
    }


    @And("I should see the invalid loan amount denial message")
    public void iShouldSeeTheInvalidLoanAmountDenialMessage() {

        String pageText =
                Hooks.driver.findElement(
                                By.id("rightPanel")
                        )
                        .getText();


        assertTrue(
                pageText.contains(
                        "We cannot grant a loan in that amount with your available funds."
                ),

                "Expected invalid-loan-amount denial message was not displayed."
        );
    }


    // DEFECT - NEGATIVE DOWN PAYMENT
    @Then("the negative down payment should be rejected")
    public void theNegativeDownPaymentShouldBeRejected() {

        assertFalse(
                negativeDownPaymentApproved,

                "DEFECT FOUND: ParaBank approved a loan request " +
                        "with a negative down payment."
        );
    }


    // DEFECT - DOWN PAYMENT GREATER THAN LOAN AMOUNT
    @When("I request a loan with a down payment greater than the loan amount but within the available balance")
    public void iRequestLoanWithDownPaymentGreaterThanLoanAmount() {

        double balance =
                getSourceAccountBalance();


        /*
         * Choose values dynamically.
         *
         * We need:
         *
         * down payment > loan amount
         * BUT
         * down payment <= available balance
         *
         * This avoids ParaBank rejecting the request merely
         * because the account has insufficient funds.
         */

        double downPayment =
                Math.max(
                        20.00,
                        Math.min(
                                200.00,
                                Math.abs(balance) * 0.40
                        )
                );


        /*
         * Loan amount is half of the down payment,
         * therefore down payment > loan amount.
         */
        double loanAmount =
                downPayment / 2.0;


        openRequestLoanPage();


        enterLoanDetails(
                String.format(
                        Locale.US,
                        "%.2f",
                        loanAmount
                ),

                String.format(
                        Locale.US,
                        "%.2f",
                        downPayment
                )
        );


        submitLoanRequest();

        waitForLoanOutcome();


        greaterThanLoanDownPaymentApproved =
                isLoanApproved();
    }


    @Then("the down payment greater than loan amount should be rejected")
    public void downPaymentGreaterThanLoanAmountShouldBeRejected() {

        assertFalse(
                greaterThanLoanDownPaymentApproved,

                "DEFECT FOUND: ParaBank approved a loan where " +
                        "the down payment was greater than the loan amount."
        );
    }


    // DOWN PAYMENT GREATER THAN ACCOUNT BALANCE
    @When("I request a loan with a down payment greater than the available account balance")
    public void iRequestLoanWithDownPaymentGreaterThanAvailableBalance() {

        double balance =
                getSourceAccountBalance();


        /*
         * Make the down payment definitely greater than
         * the current account balance.
         */
        double downPayment =
                Math.abs(balance) + 1000.00;


        /*
         * Keep loan amount greater than down payment so that
         * this scenario specifically tests insufficient funds,
         * not the down-payment > loan defect.
         */
        double loanAmount =
                downPayment + 1000.00;


        openRequestLoanPage();


        enterLoanDetails(
                String.format(
                        Locale.US,
                        "%.2f",
                        loanAmount
                ),

                String.format(
                        Locale.US,
                        "%.2f",
                        downPayment
                )
        );


        submitLoanRequest();

        waitForLoanOutcome();
    }


    @Then("the loan request should be denied")
    public void theLoanRequestShouldBeDenied() {

        assertTrue(
                isLoanDenied(),
                "Expected loan request to be denied."
        );
    }


    @And("I should see the insufficient funds for down payment message")
    public void iShouldSeeInsufficientFundsForDownPaymentMessage() {

        String pageText =
                Hooks.driver.findElement(
                                By.id("rightPanel")
                        )
                        .getText();


        assertTrue(
                pageText.contains(
                        "You do not have sufficient funds for the given down payment."
                ),

                "Expected insufficient-funds down-payment message was not displayed."
        );
    }


    // HELPER - OPEN REQUEST LOAN
    private void openRequestLoanPage() {

        getWait().until(
                ExpectedConditions.elementToBeClickable(
                        By.linkText("Request Loan")
                )
        ).click();


        getWait().until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.id("amount")
                )
        );


        getWait().until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.id("downPayment")
                )
        );


        /*
         * Wait until the dynamically created source account
         * is loaded into the account dropdown.
         */
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
                                    .equals(sourceAccount())
                    );
        });


        selectSourceAccount();
    }


    // HELPER - ENTER LOAN DETAILS
    private void enterLoanDetails(
            String loanAmount,
            String downPayment) {

        WebElement loanAmountField =
                getWait().until(
                        ExpectedConditions.visibilityOfElementLocated(
                                By.id("amount")
                        )
                );


        loanAmountField.clear();

        loanAmountField.sendKeys(
                loanAmount
        );


        WebElement downPaymentField =
                Hooks.driver.findElement(
                        By.id("downPayment")
                );


        downPaymentField.clear();

        downPaymentField.sendKeys(
                downPayment
        );


        selectSourceAccount();
    }


    // HELPER - SOURCE ACCOUNT
    private void selectSourceAccount() {

        Select accounts =
                new Select(
                        getWait().until(
                                ExpectedConditions.visibilityOfElementLocated(
                                        By.id("fromAccountId")
                                )
                        )
                );


        accounts.selectByVisibleText(
                sourceAccount()
        );
    }


    // HELPER - APPLY NOW
    private void submitLoanRequest() {

        WebElement button =
                getWait().until(driver -> {

                    List<WebElement> buttons =
                            driver.findElements(
                                    By.xpath(
                                            "//input[@value='Apply Now']" +
                                                    " | " +
                                                    "//button[normalize-space()='Apply Now']"
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
                "Visible Apply Now button was not found."
        );


        try {

            button.click();

        } catch (Exception e) {

            ((org.openqa.selenium.JavascriptExecutor)
                    Hooks.driver)
                    .executeScript(
                            "arguments[0].click();",
                            button
                    );
        }
    }


    // HELPER - WAIT FOR RESULT
    private void waitForLoanOutcome() {

        getWait().until(driver -> {

            boolean processed =
                    driver.findElements(
                                    By.xpath(
                                            "//*[@id='rightPanel']//h1" +
                                                    "[normalize-space()='Loan Request Processed']"
                                    )
                            )
                            .stream()
                            .anyMatch(WebElement::isDisplayed);


            boolean error =
                    driver.findElements(
                                    By.xpath(
                                            "//*[@id='rightPanel']//h1" +
                                                    "[normalize-space()='Error!']"
                                    )
                            )
                            .stream()
                            .anyMatch(WebElement::isDisplayed);


            return processed || error;
        });
    }


    // HELPER - APPROVED / DENIED
    private boolean isLoanApproved() {

        if (!isLoanProcessed()) {
            return false;
        }


        String text =
                Hooks.driver.findElement(
                                By.id("rightPanel")
                        )
                        .getText();


        return text.contains("Approved");
    }


    private boolean isLoanDenied() {

        if (!isLoanProcessed()) {
            return false;
        }


        String text =
                Hooks.driver.findElement(
                                By.id("rightPanel")
                        )
                        .getText();


        return text.contains("Denied");
    }


    private boolean isLoanProcessed() {

        return Hooks.driver.findElements(
                        By.xpath(
                                "//*[@id='rightPanel']//h1" +
                                        "[normalize-space()='Loan Request Processed']"
                        )
                )
                .stream()
                .anyMatch(WebElement::isDisplayed);
    }


    // HELPER - CURRENT SOURCE BALANCE
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
                                        "//table[@id='accountTable']" +
                                                "//tr[td/a[normalize-space()='" +
                                                sourceAccount() +
                                                "']]"
                                )
                        )
                );


        String balanceText =
                row.findElements(
                                By.tagName("td")
                        )
                        .get(1)
                        .getText();


        return parseMoney(
                balanceText
        );
    }


    // HELPER - MONEY
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