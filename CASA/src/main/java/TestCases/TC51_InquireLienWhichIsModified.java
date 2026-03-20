package TestCases;

import java.time.Duration;
import java.util.Map;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import Base.DriverManager1;
import CASA.InquireLien;
import Utilities.ExcelUtils;
import Utilities.LogOut;
import Utilities.Login1;
import Utilities.RowData;
import Utilities.TestResultLogger;
import Utilities.WindowHandle;


public class TC51_InquireLienWhichIsModified {
    public WebDriver driver;
	private WebDriverWait wait;
	
    private static final String EXCEL_PATH = System.getProperty("user.dir") + "/Resource/CASA.xlsx";
    private static final String SHEET_NAME = "TC51";

    @BeforeClass
    public void setup() throws Exception {
        driver = DriverManager1.getDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        ExcelUtils.loadExcel(EXCEL_PATH);
    }

    @Test
    public void runTest() throws Exception {

        int maxRetry = 1;
        int attempt = 0;
        boolean testPassed = false;

        String logFile = TestResultLogger
                .createLogFile("TC51_InquireLienWhichIsModified");

        while (attempt <= maxRetry && !testPassed) {

            String labelText = null;

            try {

           

                Login1 login = new Login1();
                driver.switchTo().defaultContent();
                RowData id = ExcelUtils.getRowAsRowData("TC01", 1);
                RowData cl = ExcelUtils.getRowAsRowData("TC50", 1);
                RowData vl = ExcelUtils.getRowAsRowData(SHEET_NAME, 1);

                login.First();
                
                WindowHandle.validateAlreadyLoggedIn(driver, logFile);           
                InquireLien verify = new InquireLien(driver);
  
                // ---------------- POST ----------------//
                Map<String, String> verifyResult = verify.executeWithResultMap(vl,id,vl,cl,SHEET_NAME,1, EXCEL_PATH);

                if (verifyResult.containsKey("errorMsg")) {
                    String errorMsg = verifyResult.get("errorMsg");
                    TestResultLogger.log(logFile, "❌ Error: " + errorMsg);
                    Assert.fail("Test failed: " + errorMsg);
                } else {
                    TestResultLogger.log(logFile, "✅ AppData captured at inquiry time:");

                    for (Map.Entry<String, String> entry : verifyResult.entrySet()) {
                        String key = entry.getKey();
                        String value = entry.getValue();
                        TestResultLogger.log(logFile, key + " = " + value);
                    }
                }
                testPassed = true;   // ✅ VERY IMPORTANT
            } catch (AssertionError ae) {

                    if (labelText == null || labelText.trim().isEmpty()) {
                        String errorMsg = "❌ Assertion Failure: " + ae.getMessage();
                        System.err.println(errorMsg);
                        TestResultLogger.log(logFile, errorMsg);
                        Assert.fail(errorMsg);

                    } 
            } catch (Exception e) {

                String errorMsg = "❌ Exception Occurred: " + e.getMessage();
                System.err.println(errorMsg);
                TestResultLogger.log(logFile, errorMsg);

                if (e.getMessage() != null &&
                        (e.getMessage().contains("Unable to get browser")
                        || e.getMessage().contains("Session ID is null")
                        || e.getMessage().contains("no such window")
                        || e.getMessage().contains("disconnected"))) {

                    attempt++;

                    if (attempt > maxRetry) {
                        Assert.fail("Test failed after retry: " + errorMsg);
                    }

                } else {
                    Assert.fail(errorMsg);
                }

            } finally {

                try {
                    LogOut.performLogout(driver, wait);
                } catch (Exception ignored) {}
            }
        }
    }
}