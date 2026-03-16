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

public class TC75_RealizeTheInwardBillWhichIsFreeDelivered {
	public WebDriver driver;
    private WebDriverWait wait;
    private static final String EXCEL_PATH = System.getProperty("user.dir") + "/Resource/MIIB.xlsx";
    private static final String SHEET_NAME = "TC75";
    private static final String EXPECTED_BUSINESS_ERROR ="E4221 - Cannot Realise a Free Delivered Bill";


    @BeforeClass
    public void setup() throws Exception {
        driver = DriverManager.getDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        ExcelUtils.loadExcel(EXCEL_PATH);
    }

    @Test
    public void runTest() throws Exception {
        String logFile = TestResultLogger.createLogFile("TC75_RealizeTheInwardBillWhichIsFreeDelivered");
        String labelText=null;
        Login1 login = new Login1();

        try {
        	RowData id = ExcelUtils.getRowAsRowData("TC71", 1);
            RowData fwdcno = ExcelUtils.getRowAsRowData(SHEET_NAME, 1);


            login.First();
            WindowHandle.validateAlreadyLoggedIn(driver, logFile);
            ImportAndInwardBills FrwConNo = new ImportAndInwardBills(driver);

            // ---------------- Step 2: Mark Invoke ----------------//
            Map<String, String> result = FrwConNo.execute(fwdcno,id,id,SHEET_NAME, 1, EXCEL_PATH);
            if (result.containsKey("errorMsg")) {

                String errorMsg = result.get("errorMsg");

                if (errorMsg.contains(EXPECTED_BUSINESS_ERROR)) {
                    // ✅ Expected business validation – PASS
                    TestResultLogger.log(logFile,
                            "✅ Expected business error occurred: " + errorMsg);
                    System.out.println("✅ Test Passed – Expected business error received");
                    return; // stop further execution, test PASSES
                } else {
                    // ❌ Unexpected error – FAIL
                    TestResultLogger.log(logFile, "❌ Unexpected Error: " + errorMsg);
                    Assert.fail("Unexpected error occurred: " + errorMsg);
                }

            } else {
                labelText = result.get("labelText");
                System.out.println(labelText);
                TestResultLogger.log(logFile, labelText);
            }

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
            Assert.fail(errorMsg);
        } finally {
            try {
            	 LogOut.performLogout(driver, wait);
            } catch (Exception e) {
                System.err.println("⚠️ Logout failed at end: " + e.getMessage());
            }
        }
        
}
    
}