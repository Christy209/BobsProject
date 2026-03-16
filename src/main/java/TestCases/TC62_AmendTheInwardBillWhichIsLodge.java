package TestCases;

import java.time.Duration;
import java.util.Map;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import Base.DriverManager;
import Bill.ImportAndInwardBills;
import Utilities.ExcelUtils;
import Utilities.LogOut;
import Utilities.Login1;
import Utilities.RowData;
import Utilities.TestResultLogger;
import Utilities.WindowHandle;

public class TC62_AmendTheInwardBillWhichIsLodge {
    public WebDriver driver;
    private WebDriverWait wait;
    private static final String EXCEL_PATH = System.getProperty("user.dir") + "/Resource/MIIB.xlsx";
    private static final String SHEET_NAME = "TC62";

    @BeforeClass
    public void setup() throws Exception {
        driver = DriverManager.getDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        ExcelUtils.loadExcel(EXCEL_PATH);
    }

    @Test
    public void runTest() throws Exception {
        String logFile = TestResultLogger.createLogFile("TC62_AmendTheInwardBillWhichIsLodge");
        String labelText=null;
        String VlabelText=null;
        Login1 login = new Login1();;

        try {
            RowData initialData = ExcelUtils.getRowAsRowData("TC59", 1);
            RowData realizeData = ExcelUtils.getRowAsRowData(SHEET_NAME, 1);
            RowData verifyData = ExcelUtils.getRowAsRowData(SHEET_NAME, 2);

            login.First();
            WindowHandle.validateAlreadyLoggedIn(driver, logFile);
            ImportAndInwardBills frwConNo = new ImportAndInwardBills(driver);


            Map<String, String> markResult = frwConNo.execute(realizeData, initialData,initialData, SHEET_NAME, 1, EXCEL_PATH);
            logMap(markResult, logFile);

             labelText = markResult.get("LabelText");
            String billId = markResult.get("LABELTEXT_BILLID");

            TestResultLogger.log(logFile, "LabelText: " + (labelText != null ? labelText : "null"));
            TestResultLogger.log(logFile, "BillID: " + (billId != null ? billId : "null"));

            if (markResult.containsKey("errorMsg")) {
                String errorMsg = markResult.get("errorMsg");
                TestResultLogger.log(logFile, "❌ Error: " + errorMsg);
                Assert.fail("Test failed: " + errorMsg);
            }

                LogOut.performLogout(driver, wait);
               
//---------------------------------------- Verify---------------------------------------------------------------------------------------------------------//
            login.Second();
            WindowHandle.validateAlreadyLoggedIn(driver, logFile);

            ExcelUtils.loadExcel(EXCEL_PATH);
            realizeData = ExcelUtils.getRowAsRowData(SHEET_NAME, 1);

            // ---------------- Step 5: Verify ----------------
            Map<String, String> verifyResult = frwConNo.execute(verifyData, realizeData,realizeData, SHEET_NAME, 2, EXCEL_PATH);
            logMap(verifyResult, logFile);

            VlabelText = verifyResult.get("LabelText");
            String verifyBillId = verifyResult.get("LABELTEXT_BILLID");

            TestResultLogger.log(logFile, "Verification LabelText: " + (VlabelText != null ? VlabelText : "null"));
            TestResultLogger.log(logFile, "Verification BillID: " + (verifyBillId != null ? verifyBillId : "null"));

            if (verifyResult.containsKey("errorMsg")) {
                String errorMsg = verifyResult.get("errorMsg");
                TestResultLogger.log(logFile, "❌ Error: " + errorMsg);
                Assert.fail("Test failed: " + errorMsg);
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
            Assert.fail(errorMsg);
        } finally {
            try {
                LogOut.performLogout(driver, wait);
                
            } catch (Exception ignored) {
            	
            }
        }
    }

    // ---------------- Utility Methods ----------------
    private void logMap(Map<String, String> map, String logFile) {
        if (map != null) {
            map.forEach((k, v) -> System.out.println(k + " = " + v));
            map.forEach((k, v) -> TestResultLogger.log(logFile, k + " = " + (v != null ? v : "null")));
        }
    }
}
