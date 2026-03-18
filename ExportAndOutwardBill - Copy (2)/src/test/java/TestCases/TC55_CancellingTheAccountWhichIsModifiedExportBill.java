package TestCases;

import java.time.Duration;
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

public class TC55_CancellingTheAccountWhichIsModifiedExportBill {

    private WebDriver driver;
    private WebDriverWait wait;

    private static final String EXCEL_PATH =System.getProperty("user.dir") + "/Resource/MEOB.xlsx";

    @BeforeClass
    public void setup() throws Exception {
        driver = DriverManager.getDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        ExcelUtils.loadExcel(EXCEL_PATH);
    }

    @Test
    public void runTest() {

        String logFile =TestResultLogger.createLogFile("TC55_CancellingTheAccountWhichIsModifiedExportBill");
        try {

            RowData initialData = ExcelUtils.getRowAsRowData("TC49", 1);
            RowData cancelData  = ExcelUtils.getRowAsRowData("TC55", 1);


            Login1 login = new Login1();
            driver.switchTo().defaultContent();
            
            login.First();
            WindowHandle.validateAlreadyLoggedIn(driver, logFile);

            try {
                // ---------- Execute Cancel ----------
                new ExportAndOutwardBill(driver)
                        .execute(cancelData, initialData, initialData,"TC55", 1, EXCEL_PATH);

                // ❌ If execution reaches here → NO E4221 → FAIL
                String failMsg =
                        "❌ TEST FAILED | Expected E4221 but no error was thrown.";

                System.err.println(failMsg);
                TestResultLogger.log(logFile, failMsg);
                Assert.fail(failMsg);

            } catch (AssertionError ae) {

                String errorMsg = ae.getMessage();

                // ✅ ONLY ACCEPTABLE OUTCOME
                if (errorMsg != null && errorMsg.contains("E4221")) {

                    String passMsg =
                            "✅ EXPECTED BUSINESS ERROR RECEIVED – TEST PASSED : " +
                            "E4221 – Nothing to Cancel";

                    System.out.println(passMsg);
                    TestResultLogger.log(logFile, passMsg);
                    return;
                }

                // ❌ Any other assertion
                throw ae;
            }

        } catch (Exception e) {

            String msg = "❌ EXCEPTION OCCURRED: " + e.getMessage();
            System.err.println(msg);
            TestResultLogger.log(logFile, msg);
            Assert.fail(msg);

        } finally {
            try {
                LogOut.performLogout(driver, wait);
                System.out.println("✅ Logout performed successfully at end.");
            } catch (Exception e) {
                String warnMsg =
                        "⚠️ Logout failed at end: " + e.getMessage();
                System.err.println(warnMsg);
                TestResultLogger.log(logFile, warnMsg);
            }
        }
    }
}
