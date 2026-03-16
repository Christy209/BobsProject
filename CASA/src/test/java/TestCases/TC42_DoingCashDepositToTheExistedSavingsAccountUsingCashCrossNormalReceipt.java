package TestCases;


import java.time.Duration;
import java.util.Map;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import Base.DriverManager1;
import CASA.CashDeposit;
import Utilities.ExcelUtils;
import Utilities.LogOut;
import Utilities.Login1;
import Utilities.RowData;
import Utilities.TestResultLogger;
import Utilities.WindowHandle;


public class TC42_DoingCashDepositToTheExistedSavingsAccountUsingCashCrossNormalReceipt {

    private WebDriver driver;
    private WebDriverWait wait;
    private static final String EXCEL_PATH = System.getProperty("user.dir") + "/Resource/CASA.xlsx";
    private static final String SHEET_NAME = "TC42";

    @BeforeClass
    public void setup() throws Exception {
        driver = DriverManager1.getDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        ExcelUtils.loadExcel(EXCEL_PATH);
    }

    @Test
    public void runTest() throws Exception {
        String logFile = TestResultLogger.createLogFile("TC42_DoingCashDepositToTheExistedSavingsAccountUsingCashCrossNormalReceipt");
        Login1 login = new Login1();
        driver.switchTo().defaultContent();
        String labelText=null;	


        try {
        	 RowData id = ExcelUtils.getRowAsRowData(SHEET_NAME, 1);

            
            // ---------------- Step 1: Login & Create Savings Account ----------------//
               login.First();           
               WindowHandle.validateAlreadyLoggedIn(driver, logFile);           
               CashDeposit saving = new CashDeposit(driver);
               Map<String, String> result = saving.execute(id,id,id,SHEET_NAME, 1, EXCEL_PATH);
               if (result.containsKey("errorMsg")) {
                   String errorMsg = result.get("errorMsg");
                   TestResultLogger.log(logFile, "❌ Error: " + errorMsg);
                  Assert.fail("Test failed: " + errorMsg);
               } else {
                    labelText = result.get("labelText");
                   System.out.println(labelText);
                   TestResultLogger.log(logFile,labelText);
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
