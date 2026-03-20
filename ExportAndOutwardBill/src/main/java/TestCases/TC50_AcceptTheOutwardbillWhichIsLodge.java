package TestCases;

import java.time.Duration;
import java.util.Map;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import Base.DriverManager;
import Bill.ExportAndOutwardBill;
import Utilities.ExcelUtils;
import Utilities.LogOut;
import Utilities.Login1;
import Utilities.RowData;
import Utilities.TestResultLogger;
import Utilities.WindowHandle;

public class TC50_AcceptTheOutwardbillWhichIsLodge {
    public WebDriver driver;
    private WebDriverWait wait;
    private static final String EXCEL_PATH = System.getProperty("user.dir") + "/Resource/MEOB.xlsx";
    private static final String SHEET_NAME = "TC50";

    @BeforeClass
    public void setup() throws Exception {
        driver = DriverManager.getDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        ExcelUtils.loadExcel(EXCEL_PATH);
    }

    @Test
    public void runTest() throws Exception {
        String logFile = TestResultLogger.createLogFile("TC50_AcceptTheOutwardbillWhichIsLodge");
        String labelText =null;
        String VlabelText =null;
        Login1 login = new Login1();
        driver.switchTo().defaultContent();

        try {
            RowData initialData = ExcelUtils.getRowAsRowData("TC49", 1);
            RowData Realize = ExcelUtils.getRowAsRowData(SHEET_NAME, 1);
            RowData VerifyData = ExcelUtils.getRowAsRowData(SHEET_NAME, 2); // Row 2 = Invoke

  //****************************************Accept*********************************************************************************************//
            login.First();
            WindowHandle.validateAlreadyLoggedIn(driver, logFile);
            
            ExportAndOutwardBill FrwConNo = new ExportAndOutwardBill(driver);

            // ---------------- Step 2: Mark Invoke ----------------
            Map<String, String> markResult = FrwConNo.execute(Realize, initialData,initialData, SHEET_NAME, 1, EXCEL_PATH);
            if (markResult.containsKey("errorMsg")) {
                String errorMsg = markResult.get("errorMsg");
                TestResultLogger.log(logFile, "❌ Error: " + errorMsg);
               Assert.fail("Test failed: " + errorMsg);
            } else {
                 labelText = markResult.get("labelText");
                System.out.println(labelText);
                TestResultLogger.log(logFile,labelText);
            }

            LogOut.performLogout(driver, wait);
            
 //***********************************************verify****************************************************************************************//           
            login.Second();
            WindowHandle.validateAlreadyLoggedIn(driver, logFile);

            ExcelUtils.loadExcel(EXCEL_PATH);
            Realize = ExcelUtils.getRowAsRowData(SHEET_NAME, 1);

            Map<String, String> Verifyresult = FrwConNo.execute(VerifyData, Realize,Realize, SHEET_NAME, 2, EXCEL_PATH);
            if (Verifyresult.containsKey("errorMsg")) {
                String errorMsg = Verifyresult.get("errorMsg");
               TestResultLogger.log(logFile, "❌ Error: " + errorMsg);
               Assert.fail("Test failed: " + errorMsg);
            } else {
                 VlabelText = Verifyresult.get("labelText");
                System.out.println(VlabelText);
                TestResultLogger.log(logFile,labelText);
            }

        } catch (AssertionError ae) {

            if (labelText == null || labelText.trim().isEmpty() ||
            	VlabelText == null || VlabelText.trim().isEmpty()) {
                String errorMsg = "❌ Assertion Failure: " + ae.getMessage();
                System.err.println(errorMsg);
                TestResultLogger.log(logFile, errorMsg);
                Assert.fail(errorMsg);
            }
        } catch (Exception e) {
            String errorMsg = "❌ Exception Occurred: " + e.getMessage();
            System.err.println(errorMsg);
            TestResultLogger.log(logFile, errorMsg);
        } finally {
            try {
                LogOut.performLogout(driver, wait);
            } catch (Exception e) {
                System.err.println("⚠️ Logout failed at end: " + e.getMessage());
            }
        }
    }
}
