import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class SmokeTest extends BaseTest {

    @Test
    public void parabankHomePageLoads() {
        assertTrue(driver.getTitle().contains("ParaBank"));
    }
}
