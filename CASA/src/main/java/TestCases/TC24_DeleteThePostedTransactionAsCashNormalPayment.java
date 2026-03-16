package TestCases;

import java.time.Duration;
import java.util.Map;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import Base.DriverManager1;
import CASA.TransactionMaintainence;
import Utilities.ExcelUtils;
import Utilities.LogOut;
import Utilities.Login1;
import Utilities.RowData;
import Utilities.TestResultLogger;
import Utilities.WindowHandle;


public class TC24_DeleteThePostedTransactionAsCashNormalPayment {
	public WebDriver driver;
	private WebDriverWait wait;
	
    private static final String EXCEL_PATH = System.getProperty("user.dir") + "/Resource/CASA.xlsx";
    private static final String SHEET_NAME = "TC24";

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
                .createLogFile("TC24_DeleteThePostedTransactionAsCashNormalPayment");

        while (attempt <= maxRetry && !testPassed) {

            try {

              
                Login1 login = new Login1();
                driver.switchTo().defaultContent();
                RowData id = ExcelUtils.getRowAsRowData("TC21", 1);
                RowData deleteData = ExcelUtils.getRowAsRowData(SHEET_NAME, 1);

                login.First();
                WindowHandle.validateAlreadyLoggedIn(driver, logFile);

                TransactionMaintainence tran =
                        new TransactionMaintainence(driver);

                Map<String, String> result =
                        tran.execute(deleteData, id, id,
                                SHEET_NAME, 1, EXCEL_PATH);

                // ---------------- RESULT HANDLING ----------------//
                if (result.containsKey("errorMsg")) {

                    String errorMsg = result.get("errorMsg");
                    TestResultLogger.log(logFile,
                            "⚠️ Error Received: " + errorMsg);

                    // ✅ EXPECTED BUSINESS ERROR → PASS
                    if (errorMsg.contains("E4698")) {

                        TestResultLogger.log(logFile,
                                "✅ Expected business error E4698. Test PASSED.");

                        testPassed = true;
                        break;
                    }

                    // ❌ Unexpected business error
                    throw new RuntimeException("Unexpected error: " + errorMsg);

                } else {

                    // ❌ Delete succeeded when it should NOT
                    String labelText = result.get("TransactionMessage");

                    TestResultLogger.log(logFile,
                            "❌ Delete succeeded unexpectedly: " + labelText);

                    throw new RuntimeException(
                            "Delete succeeded but E4698 was expected");
                }

            } catch (Exception e) {

                String errorMsg = "❌ Exception Occurred: " + e.getMessage();
                System.err.println(errorMsg);
                TestResultLogger.log(logFile, errorMsg);

                // Retry only for browser crash
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