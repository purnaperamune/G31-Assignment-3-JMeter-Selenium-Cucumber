import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class BaseTest {
    protected WebDriver driver;
    protected WebDriverWait wait;
    protected static final String BASE_URL = "https://parabank.parasoft.com/parabank/index.htm";
    protected static final String TEST_PASSWORD = "Test@123";

    @BeforeEach
    public void setUp() {
        driver = new ChromeDriver();
        driver.manage().window().maximize();
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.get(BASE_URL);
    }

    protected void login(String username, String password) {
        driver.findElement(By.name("username")).sendKeys(username);
        driver.findElement(By.name("password")).sendKeys(password);
        driver.findElement(By.cssSelector("input[value='Log In']")).click();
    }
    protected String registerNewUser() {
        String username = "user" + System.currentTimeMillis();

        driver.findElement(By.linkText("Register")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("customer.firstName")));

        driver.findElement(By.id("customer.firstName")).sendKeys("Test");
        driver.findElement(By.id("customer.lastName")).sendKeys("User");
        driver.findElement(By.id("customer.address.street")).sendKeys("1 Test Street");
        driver.findElement(By.id("customer.address.city")).sendKeys("Melbourne");
        driver.findElement(By.id("customer.address.state")).sendKeys("VIC");
        driver.findElement(By.id("customer.address.zipCode")).sendKeys("3000");
        driver.findElement(By.id("customer.phoneNumber")).sendKeys("0400000000");
        driver.findElement(By.id("customer.ssn")).sendKeys("123-45-6789");
        driver.findElement(By.id("customer.username")).sendKeys(username);
        driver.findElement(By.id("customer.password")).sendKeys(TEST_PASSWORD);
        driver.findElement(By.id("repeatedPassword")).sendKeys(TEST_PASSWORD);
        driver.findElement(By.cssSelector("input[value='Register']")).click();

        wait.until(ExpectedConditions.textToBePresentInElementLocated(
                By.id("rightPanel"), "Your account was created successfully"));
        return username;
    }

    @AfterEach
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}