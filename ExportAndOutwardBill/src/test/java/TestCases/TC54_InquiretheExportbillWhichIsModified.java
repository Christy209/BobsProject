package TestCases;

import java.time.Duration;
import java.util.Map;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import Base.DriverManager;
import Bill.InquireOfExportBill;
import Utilities.ExcelUtils;
import Utilities.LogOut;
import Utilities.Login1;
import Utilities.RowData;
import Utilities.TestResultLogger;
import Utilities.WindowHandle;

public class TC54_InquiretheExportbillWhichIsModified {
    public WebDriver driver;
    private WebDriverWait wait;
    private static final String EXCEL_PATH = System.getProperty("user.dir") + "/Resource/MEOB.xlsx";
    private static final String SHEET_NAME = "TC54";

    @BeforeClass
    public void setup() throws Exception {
        driver = DriverManager.getDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        ExcelUtils.loadExcel(EXCEL_PATH);
    }

    @Test
    public void runTest() throws Exception {
        String logFile = TestResultLogger.createLogFile("TC54_InquiretheExportbillWhichIsModified");
        Login1 login = new Login1();
        driver.switchTo().defaultContent();

        try {
            RowData initialData = ExcelUtils.getRowAsRowData("TC49", 1);
            RowData Realize = ExcelUtils.getRowAsRowData(SHEET_NAME, 1);

  //----------------------------------------------Inquire-------------------------------------------------------------------------------------------------//          
            login.First();
            WindowHandle.validateAlreadyLoggedIn(driver, logFile);

            InquireOfExportBill FrwConNo = new InquireOfExportBill(driver);
            Map<String, String> verifyResult = FrwConNo.executeWithResultMap(Realize, initialData, SHEET_NAME,2, EXCEL_PATH);
            
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
        } catch (Exception e) {
            String errorMsg = "❌ Exception Occurred: " + e.getMessage();
            System.err.println(errorMsg);
            TestResultLogger.log(logFile, errorMsg);
            Assert.fail(errorMsg);
        } finally {
            // 🔹 Always perform logout at the end
            try {
               LogOut.performLogout(driver, wait);
            } catch (Exception e) {
                System.err.println("⚠️ Logout failed at end: " + e.getMessage());
                TestResultLogger.log(logFile, "⚠️ Logout failed at end: " + e.getMessage());
            }
        }
    }
    
}


