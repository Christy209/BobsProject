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

public class TC72_RecoverAFreeDeliveredBillInwardBill {

    public WebDriver driver;
    private WebDriverWait wait;

    private static final String EXCEL_PATH =System.getProperty("user.dir") + "/Resource/MIIB.xlsx";
    private static final String SHEET_NAME = "TC72";
    private static final String EXPECTED_BUSINESS_ERROR = "E3493";

    @BeforeClass
    public void setup() throws Exception {
        driver = DriverManager.getDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        ExcelUtils.loadExcel(EXCEL_PATH);
    }

    @Test
    public void runTest() {

        String logFile =TestResultLogger.createLogFile("TC72_RecoverAFreeDeliveredBillInwardBill");
        Login1 login = new Login1();

        try {
            RowData id = ExcelUtils.getRowAsRowData("TC71", 1);
            RowData fwdcno = ExcelUtils.getRowAsRowData(SHEET_NAME, 1);

            login.First();
            WindowHandle.validateAlreadyLoggedIn(driver, logFile);

            ImportAndInwardBills inwardBill =new ImportAndInwardBills(driver);
            Map<String, String> result =inwardBill.execute(fwdcno,id,id,SHEET_NAME,1,EXCEL_PATH);
            if (result.containsKey("errorMsg")) {
                String errorMsg = result.get("errorMsg");
                if (errorMsg.contains(EXPECTED_BUSINESS_ERROR)) {
                    System.out.println("✅ Expected Business Error:");
                    System.out.println("➡️ " + errorMsg);
                    TestResultLogger.log(logFile,"✅ Expected business error occurred: " + errorMsg);
                    return;
                

                } else {
                    TestResultLogger.log(logFile,"❌ Unexpected error occurred: " + errorMsg);
                    Assert.fail("Unexpected error occurred: " + errorMsg);
                }

            } else {
                TestResultLogger.log(logFile,"❌ Test Failed – Expected business error but transaction succeeded");
                Assert.fail("Expected business error but transaction succeeded");
            }

        } catch (Exception e) {
            String errorMsg ="❌ Exception occurred: " + e.getMessage();
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
