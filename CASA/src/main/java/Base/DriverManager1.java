package Base;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import io.github.bonigarcia.wdm.WebDriverManager;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.Properties;
import java.util.Set;

public class DriverManager1 {

    private static final ThreadLocal<WebDriver> driverThreadLocal = new ThreadLocal<>();
    private static final ThreadLocal<WebDriverWait> waitThreadLocal = new ThreadLocal<>();

    static String projectPath = System.getProperty("user.dir");

    private DriverManager1() {}

    /* =====================================================
                     DRIVER INITIALIZATION
       ===================================================== */

    public static WebDriver getDriver() {

        if (driverThreadLocal.get() == null) {

            String executionMode = System.getProperty("executionMode", "normal");

            String browserProperty = System.getProperty("browser");
            String browser = (browserProperty == null || browserProperty.trim().isEmpty())
                    ? "chrome"
                    : browserProperty.trim().toLowerCase();

            boolean isHeadless = executionMode.equalsIgnoreCase("headless");

            System.out.println("🔥 Browser received from Maven: " + browser);
            System.out.println("🔥 Execution Mode: " + executionMode);

            WebDriver driver;

            switch (browser) {

                case "chrome":
                    driver = initializeChrome(isHeadless);
                    break;

                case "edge":
                    driver = initializeEdge(isHeadless);
                    break;

                case "firefox":
                    driver = initializeFirefox(isHeadless);
                    break;

                default:
                    System.out.println("⚠️ Unknown browser. Launching Chrome.");
                    driver = initializeChrome(isHeadless);
                    break;
            }

            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));

            if (!isHeadless) {
                driver.manage().window().maximize();
            }

            driverThreadLocal.set(driver);
            waitThreadLocal.set(new WebDriverWait(driver, Duration.ofSeconds(20)));

            System.out.println("✅ WebDriver initialized for thread: " + Thread.currentThread().getId());
        }

        return driverThreadLocal.get();
    }

    /* =====================================================
                        CHROME
       ===================================================== */

    private static WebDriver initializeChrome(boolean isHeadless) {

        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();

        if (isHeadless) {

            System.out.println("🔧 Starting Chrome HEADLESS");

            options.addArguments("--headless=new");
            options.addArguments("--window-size=1920,1080");
            options.addArguments("--disable-gpu");
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");

        } else {

            System.out.println("🔧 Starting Chrome NORMAL");

        }

        options.addArguments("--disable-blink-features=AutomationControlled");

        return new ChromeDriver(options);
    }

    /* =====================================================
                        EDGE
       ===================================================== */

    private static WebDriver initializeEdge(boolean isHeadless) {

        WebDriverManager.edgedriver().setup();
        EdgeOptions options = new EdgeOptions();

        if (isHeadless) {

            System.out.println("🔧 Starting Edge HEADLESS");

            options.addArguments("--headless=new");
            options.addArguments("--window-size=1920,1080");
            options.addArguments("--disable-gpu");
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");

        } else {

            System.out.println("🔧 Starting Edge NORMAL");

        }

        options.addArguments("--disable-blink-features=AutomationControlled");

        return new EdgeDriver(options);
    }

    /* =====================================================
                        FIREFOX
       ===================================================== */

    private static WebDriver initializeFirefox(boolean isHeadless) {

        WebDriverManager.firefoxdriver().setup();
        FirefoxOptions options = new FirefoxOptions();

        if (isHeadless) {

            System.out.println("🔧 Starting Firefox HEADLESS");

            options.addArguments("--headless");

        } else {

            System.out.println("🔧 Starting Firefox NORMAL");

        }

        return new FirefoxDriver(options);
    }

    public static WebDriverWait getWait() {

        if (waitThreadLocal.get() == null) {
            getDriver();
        }

        return waitThreadLocal.get();
    }

    /* =====================================================
                         LOGIN
       ===================================================== */

    public static void login(String userID, String password) {

        WebDriver driver = getDriver();
        WebDriverWait wait = getWait();

        driver.get(getProperty("url"));

        stabilize();

        try {

            wait.until(ExpectedConditions.elementToBeClickable(By.id("details-button"))).click();
            wait.until(ExpectedConditions.elementToBeClickable(By.id("proceed-link"))).click();

        } catch (Exception e) {

            System.out.println("SSL page not shown");
        }

        wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt("loginFrame"));

        driver.findElement(By.id("usertxt")).sendKeys(userID);
        driver.findElement(By.id("passtxt")).sendKeys(password);
        driver.findElement(By.id("Submit")).click();

        driver.switchTo().defaultContent();

        stabilize();

        selectionsolution();
    }

    /* =====================================================
                     SOLUTION SELECT
       ===================================================== */

    public static void selectionsolution() {

        try {

            WebDriver driver = getDriver();
            WebDriverWait wait = getWait();

            stabilize();

            driver.switchTo().frame(0);

            WebElement dropdownElement =
                    wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("appSelect")));

            Select dropdown = new Select(dropdownElement);

            dropdown.selectByValue("CoreServer");

            try {

                WebDriverWait alertWait = new WebDriverWait(driver, Duration.ofSeconds(5));

                Alert alert = alertWait.until(ExpectedConditions.alertIsPresent());

                alert.accept();

            } catch (TimeoutException e) {

                System.out.println("No alert shown");
            }

            driver.switchTo().defaultContent();

            stabilize();

        } catch (Exception e) {

            System.out.println("Solution selection error: " + e.getMessage());
        }
    }

    /* =====================================================
                     STABILITY ENGINE
       ===================================================== */

    public static void stabilize() {

        ensureSingleWindow();

        waitForPageLoad();
    }

    private static void waitForPageLoad() {

        new WebDriverWait(getDriver(), Duration.ofSeconds(30))
                .until(d ->
                        ((JavascriptExecutor) d)
                                .executeScript("return document.readyState")
                                .equals("complete"));
    }

    private static void ensureSingleWindow() {

        WebDriver driver = getDriver();

        Set<String> windows = driver.getWindowHandles();

        if (windows.size() > 1) {

            String current = driver.getWindowHandle();

            for (String w : windows) {

                if (!w.equals(current)) {

                    driver.switchTo().window(w);

                    break;
                }
            }
        }
    }

    /* =====================================================
                         CONFIG
       ===================================================== */

    public static String getProperty(String key) {

        try {

            FileInputStream configFile =
                    new FileInputStream(projectPath + "/Resource/config.properties");

            Properties propertyObj = new Properties();

            propertyObj.load(configFile);

            return propertyObj.getProperty(key);

        } catch (IOException e) {

            e.printStackTrace();

            return null;
        }
    }

    /* =====================================================
                    DRIVER CLEANUP
       ===================================================== */

    public static void quitDriver() {

        WebDriver driver = driverThreadLocal.get();

        if (driver != null) {

            try {

                driver.quit();

                System.out.println("Driver closed for thread: " + Thread.currentThread().getId());

            } catch (Exception e) {

                System.err.println("Error quitting driver: " + e.getMessage());

            } finally {

                driverThreadLocal.remove();
                waitThreadLocal.remove();
            }
        }
    }

	
}