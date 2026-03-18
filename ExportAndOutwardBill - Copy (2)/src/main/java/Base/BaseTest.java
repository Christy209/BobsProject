package Base;
 
import java.time.Duration;
import org.apache.logging.log4j.LogManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.BeforeClass;
 
public class BaseTest {
 
    protected WebDriver driver;
    protected WebDriverWait wait;
    public org.apache.logging.log4j.Logger logger;
 
    @BeforeClass
    public void setup() {
 
        driver = DriverManager.getDriver();
        wait = DriverManager.getWait();
        logger = LogManager.getLogger(this.getClass());
 
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(60));
 
        // 🔥 LOGIN FIRST (THIS WAS MISSING)
        DriverManager.login(
                DriverManager.getProperty("username"),
                DriverManager.getProperty("password")
        );
    }
 
    public WebDriver getDriver() { return driver; }
    public WebDriverWait getWait() { return wait; }
}
 
 