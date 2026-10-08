package stepdefinitions;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class GoogleSearchSteps {

    WebDriver driver;

    @Given("I launch chrome browser")
    public void iLaunchChromeBrowser() {

        driver = new ChromeDriver();

        driver.manage().window().maximize();
    }

    @When("I open Google home page")
    public void iOpenGoogleHomePage() {

        driver.get("https://www.google.com/");
    }

    @Then("I check the Google home page title")
    public void iCheckGoogleHomePageTitle() {

        assertTrue(
                driver.getTitle().equals("Google")
        );
    }

    @Then("I enter the search item")
    public void iEnterTheSearchItem() {

        WebElement element =
                driver.findElement(By.name("q"));

        element.sendKeys("Cheese");

        element.submit();
    }

    @Then("Close the browser")
    public void closeTheBrowser() {

        if (driver != null) {
            driver.quit();
        }
    }
}