package TestCases;


import java.time.Duration;
import java.util.Map;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import Base.DriverManager1;
import CASA.CloseTheAccountWhichIsCreated;
import Utilities.ExcelUtils;
import Utilities.LogOut;
import Utilities.Login1;
import Utilities.RowData;
import Utilities.TestResultLogger;
import Utilities.WindowHandle;


public class TC61_CloseTheCreatedCashCreditAccount {

    private WebDriver driver;
    private WebDriverWait wait;
    private static final String EXCEL_PATH = System.getProperty("user.dir") + "/Resource/CASA.xlsx";
    private static final String SHEET_NAME = "TC61";

    @BeforeClass
    public void setup() throws Exception {
        driver = DriverManager1.getDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        ExcelUtils.loadExcel(EXCEL_PATH);
    }

    @Test
    public void runTest() throws Exception {
        String logFile = TestResultLogger.createLogFile("TC61_CloseTheCreatedCashCreditAccount");
        Login1 login = new Login1();
        driver.switchTo().defaultContent();
        String labelText=null;	
        String VlabelText=null;

        try {
        	  RowData cl = ExcelUtils.getRowAsRowData("TC05", 1);
            RowData id = ExcelUtils.getRowAsRowData(SHEET_NAME, 1);
            RowData tranId = ExcelUtils.getRowAsRowData(SHEET_NAME,2);
            
            // ---------------- Step 1: Login & Create Savings Account ----------------//
               login.First();           
               WindowHandle.validateAlreadyLoggedIn(driver, logFile);           
               CloseTheAccountWhichIsCreated saving = new CloseTheAccountWhichIsCreated(driver);
               Map<String, String> result = saving.execute(id,cl,cl,cl,SHEET_NAME, 1, EXCEL_PATH);
               if (result.containsKey("errorMsg")) {
                   String errorMsg = result.get("errorMsg");
                   TestResultLogger.log(logFile, "❌ Error: " + errorMsg);
                  Assert.fail("Test failed: " + errorMsg);
               } else {
                    labelText = result.get("labelText");
                   System.out.println(labelText);
                   TestResultLogger.log(logFile,labelText);
               }
         
            LogOut.performLogout(driver, wait);           
 
            login.Second();
            WindowHandle.validateAlreadyLoggedIn(driver, logFile);
            ExcelUtils.loadExcel(EXCEL_PATH);
            id = ExcelUtils.getRowAsRowData(SHEET_NAME, 1);

            CloseTheAccountWhichIsCreated verifier = new CloseTheAccountWhichIsCreated(driver);
            Map<String, String> verifyResult = verifier.execute(tranId,id,tranId,cl, SHEET_NAME, 2, EXCEL_PATH);
            if (verifyResult.containsKey("errorMsg")) {
                String errorMsg = verifyResult.get("errorMsg");
                TestResultLogger.log(logFile, "❌ Error: " + errorMsg);
               Assert.fail("Test failed: " + errorMsg);
            } else {
                 VlabelText = verifyResult.get("labelText");
                System.out.println(VlabelText);
                TestResultLogger.log(logFile,VlabelText);
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
            } catch (Exception e) {
                System.err.println("⚠️ Logout failed at end: " + e.getMessage());
            }
        }
    }

    
        }
