package TestCases;
 
import java.time.Duration;
import java.util.Map;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import Base.DriverManager1;
import CASA.InquireModifiedInventory;
import Utilities.ExcelUtils;
import Utilities.LogOut;
import Utilities.Login1;
import Utilities.RowData;
import Utilities.TestResultLogger;
import Utilities.WindowHandle;

 
public class TC11_InquireTheModifiedInventoryMoment {
     private WebDriver driver;
    private WebDriverWait wait;

    private static final String EXCEL_PATH = System.getProperty("user.dir") + "/Resource/CASA.xlsx";
    private static final String SHEET_NAME = "TC11";
 
    @BeforeClass
    public void setup() throws Exception {
        driver = DriverManager1.getDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(12));
        ExcelUtils.loadExcel(EXCEL_PATH);
    }
 
    @Test
    public void runTest() throws Exception {
        String logFile = TestResultLogger.createLogFile("TC11_InquireTheModifiedInventoryMoment");
        Login1 login = new Login1();
        driver.switchTo().defaultContent();
 
        try {
            RowData initialData = ExcelUtils.getRowAsRowData("TC10", 1); // Initial row
            RowData verifyData = ExcelUtils.getRowAsRowData(SHEET_NAME, 1); // Row 2 = Verify
 
            // ---------------- Step 1: Login as Maker ----------------

            login.Fourth();
            WindowHandle.validateAlreadyLoggedIn(driver, logFile);  
            InquireModifiedInventory id = new InquireModifiedInventory(driver);
           Map<String, String> result = id.executeWithResultMap(verifyData, initialData, SHEET_NAME,1, EXCEL_PATH);
            if (result.containsKey("errorMsg")) {
                String errorMsg = result.get("errorMsg");
                TestResultLogger.log(logFile, "❌ Error: " + errorMsg);
                Assert.fail("Test failed: " + errorMsg);
            } else {

                TestResultLogger.log(logFile, "✅ AppData captured at inquiry time:");
                for (Map.Entry<String, String> entry : result.entrySet()) {
                    String key = entry.getKey();
                    String value = entry.getValue();
                    TestResultLogger.log(logFile, key + " = " + value);
                }
            }
        }catch (Exception e) {
            String errorMsg = "❌ Exception Occurred: " + e.getMessage();
            System.err.println(errorMsg);
            TestResultLogger.log(logFile, errorMsg);

            throw e;   // 🔥 VERY IMPORTANT – rethrow original exception
        

        } finally {

            try {
               LogOut.performLogout(driver, wait);
            } catch (Exception e) {
                System.err.println("⚠️ Logout failed at end: " + e.getMessage());
                TestResultLogger.log(logFile, "⚠️ Logout failed at end: " + e.getMessage());

            }

        }

    }
 
    

}

 