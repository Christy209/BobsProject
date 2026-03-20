package TestCases;

import java.time.Duration;
import java.util.Map;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import Base.DriverManager1;
import CASA.Lien;
import Utilities.ExcelUtils;
import Utilities.LogOut;
import Utilities.Login1;
import Utilities.RowData;
import Utilities.TestResultLogger;
import Utilities.WindowHandle;

public class TC53_CancellingtheLienMomentWhichIsVerified {

    private WebDriver driver;
    private WebDriverWait wait;

    private static final String EXCEL_PATH = System.getProperty("user.dir") + "/Resource/CASA.xlsx";
    private static final String SHEET_NAME = "TC53";

    @BeforeClass
    public void setup() throws Exception {
        driver = DriverManager1.getDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        ExcelUtils.loadExcel(EXCEL_PATH);
    }

    @Test
    public void runTest() throws Exception {
        String logFile = TestResultLogger.createLogFile("TC53_CancellingtheLienMomentWhichIsVerified");
        Login1 login = new Login1();
        driver.switchTo().defaultContent();

        try {
            // ===============================
            // STEP: Cancel Lien
            // ===============================
            login.First();
            WindowHandle.validateAlreadyLoggedIn(driver, logFile);// Perform login
            RowData id = ExcelUtils.getRowAsRowData("TC01",1);
            RowData cl = ExcelUtils.getRowAsRowData(SHEET_NAME,1);
            

            Lien cancelLien = new Lien(driver);
            Map<String, String> result = cancelLien.execute(cl,id,id,id,"TC53",1, EXCEL_PATH);
            if (result.containsKey("errorMsg")) {

                String errorMsg = result.get("errorMsg").trim();
                System.out.println("⚠️ Received Error: " + errorMsg);
                TestResultLogger.log(logFile, "⚠️ Received Error: " + errorMsg);

                if (errorMsg.contains("711") &&
                    errorMsg.contains("There is nothing to cancel")) {

                    String passMsg =
                            "✅ EXPECTED BUSINESS ERROR RECEIVED – TEST PASSED : " + errorMsg;

                    System.out.println(passMsg);
                    TestResultLogger.log(logFile, passMsg);
                    Assert.assertTrue(true);

                } else {

                    String failMsg = "❌ Unexpected error received: " + errorMsg;
                    TestResultLogger.log(logFile, failMsg);
                    Assert.fail(failMsg);
                }

            } else {

                String failMsg = "❌ Expected error 711 was NOT displayed.";
                TestResultLogger.log(logFile, failMsg);
                Assert.fail(failMsg);
            }

        } catch (Exception e) {

            String msg = "❌ EXCEPTION OCCURRED: " + e.getMessage();
            System.err.println(msg);
            TestResultLogger.log(logFile, msg);
            Assert.fail(msg);

        } finally {
            try {
               LogOut.performLogout(driver, wait);
               
            } catch (Exception e) {
                System.err.println("⚠️ Logout failed: " + e.getMessage());
                TestResultLogger.log(logFile, "⚠️ Logout failed: " + e.getMessage());
            }
        }
    }
}
