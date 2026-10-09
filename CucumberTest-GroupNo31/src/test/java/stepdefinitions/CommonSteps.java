package stepdefinitions;

import io.cucumber.java.en.Given;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class CommonSteps {

    private static final String BASE_URL =
            "https://parabank.parasoft.com/parabank/index.htm";

    /*
     * Getting created everytime because the database resets time to time.
     */
    private static String username;
    private static String password;

    private static String sourceAccountNumber;
    private static String destinationAccountNumber;


    @Given("I am logged in as a ParaBank customer")
    public void iAmLoggedInAsAParaBankCustomer() {

        createFreshCustomer();

        captureInitialAccount();

        createSecondAccount();

        assertAll(

                () -> assertNotNull(
                        sourceAccountNumber,
                        "Source account was not created."
                ),

                () -> assertNotNull(
                        destinationAccountNumber,
                        "Destination account was not created."
                ),

                () -> assertNotEquals(
                        sourceAccountNumber,
                        destinationAccountNumber,
                        "Source and destination accounts must be different."
                )
        );
    }


    // CREATE A NEW USER
    private void createFreshCustomer() {

        WebDriverWait wait =
                new WebDriverWait(
                        Hooks.driver,
                        Duration.ofSeconds(15)
                );


        /*
         * Creating a unique username every run.
         */
        username =
                "PurnaMP_" +
                        UUID.randomUUID()
                                .toString()
                                .replace("-", "")
                                .substring(0, 12);

        password =
                "Test12345";


        Hooks.driver.get(
                BASE_URL
        );


        wait.until(
                ExpectedConditions.elementToBeClickable(
                        By.linkText("Register")
                )
        ).click();


        wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.name("customer.firstName")
                )
        );


        setField(
                "customer.firstName",
                "Automation"
        );

        setField(
                "customer.lastName",
                "Tester"
        );

        setField(
                "customer.address.street",
                "1 Test Street"
        );

        setField(
                "customer.address.city",
                "Melbourne"
        );

        setField(
                "customer.address.state",
                "VIC"
        );

        setField(
                "customer.address.zipCode",
                "3000"
        );

        setField(
                "customer.phoneNumber",
                "0400000000"
        );

        setField(
                "customer.ssn",
                "123456789"
        );

        setField(
                "customer.username",
                username
        );

        setField(
                "customer.password",
                password
        );

        setField(
                "repeatedPassword",
                password
        );


        wait.until(
                ExpectedConditions.elementToBeClickable(
                        By.cssSelector(
                                "input[value='Register']"
                        )
                )
        ).click();


        /*
         * Successful registration automatically logs the user in.
         */
        wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.linkText("Log Out")
                )
        );


        assertTrue(
                Hooks.driver.findElement(
                        By.linkText("Log Out")
                ).isDisplayed(),
                "Fresh ParaBank customer was not created successfully."
        );
    }


    // CAPTURE INITIAL ACCOUNT
    private void captureInitialAccount() {

        WebDriverWait wait =
                new WebDriverWait(
                        Hooks.driver,
                        Duration.ofSeconds(15)
                );


        wait.until(
                ExpectedConditions.elementToBeClickable(
                        By.linkText("Accounts Overview")
                )
        ).click();


        wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.id("accountTable")
                )
        );


        WebElement firstAccount =
                wait.until(
                        ExpectedConditions.visibilityOfElementLocated(
                                By.cssSelector(
                                        "#accountTable tbody tr td a"
                                )
                        )
                );


        sourceAccountNumber =
                firstAccount.getText()
                        .trim();


        assertFalse(
                sourceAccountNumber.isBlank(),
                "Initial account number could not be detected."
        );
    }


    // AUTOMATICALLY CREATE SECOND ACCOUNT
    private void createSecondAccount() {

        WebDriverWait wait =
                new WebDriverWait(
                        Hooks.driver,
                        Duration.ofSeconds(15)
                );


        wait.until(
                ExpectedConditions.elementToBeClickable(
                        By.linkText("Open New Account")
                )
        ).click();


        /*
         * Wait for account type selector.
         */
        WebElement accountTypeElement =
                wait.until(
                        ExpectedConditions.visibilityOfElementLocated(
                                By.id("type")
                        )
                );


        Select accountType =
                new Select(
                        accountTypeElement
                );


        /*
         * Use SAVINGS simply to make the second account
         * clearly different from the initial account.
         */
        accountType.selectByVisibleText(
                "SAVINGS"
        );


        /*
         * Wait for funding-account dropdown to load.
         */
        wait.until(driver -> {

            Select fromAccounts =
                    new Select(
                            driver.findElement(
                                    By.id("fromAccountId")
                            )
                    );

            return !fromAccounts.getOptions()
                    .isEmpty();
        });


        Select fromAccount =
                new Select(
                        Hooks.driver.findElement(
                                By.id("fromAccountId")
                        )
                );


        /*
         * Select the account ParaBank created during registration.
         */
        boolean initialAccountAvailable =
                fromAccount.getOptions()
                        .stream()
                        .anyMatch(option ->
                                option.getText()
                                        .trim()
                                        .equals(
                                                sourceAccountNumber
                                        )
                        );


        assertTrue(
                initialAccountAvailable,
                "Initial account was not available when opening the second account."
        );


        fromAccount.selectByVisibleText(
                sourceAccountNumber
        );


        wait.until(
                ExpectedConditions.elementToBeClickable(
                        By.cssSelector(
                                "input[value='Open New Account']"
                        )
                )
        ).click();


        /*
         * ParaBank displays the newly created account number
         * using id=newAccountId.
         */
        WebElement newAccount =
                wait.until(
                        ExpectedConditions.visibilityOfElementLocated(
                                By.id("newAccountId")
                        )
                );


        destinationAccountNumber =
                newAccount.getText()
                        .trim();


        assertFalse(
                destinationAccountNumber.isBlank(),
                "Second account number could not be detected."
        );
    }


    // SHARED FIELD HELPER
    private void setField(
            String name,
            String value) {

        WebDriverWait wait =
                new WebDriverWait(
                        Hooks.driver,
                        Duration.ofSeconds(15)
                );


        WebElement field =
                wait.until(
                        ExpectedConditions.visibilityOfElementLocated(
                                By.name(name)
                        )
                );


        field.clear();

        field.sendKeys(
                value
        );
    }

    // GETTERS USED BY OTHER STEP CLASSES
    public static String getSourceAccountNumber() {

        return sourceAccountNumber;
    }


    public static String getDestinationAccountNumber() {

        return destinationAccountNumber;
    }


    public static String getUsername() {

        return username;
    }
}