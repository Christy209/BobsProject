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

public class TC71_FreeDeliveryTheLodgedInwardBill {
	public WebDriver driver;
    private WebDriverWait wait;
    private static final String EXCEL_PATH = System.getProperty("user.dir") + "/Resource/MIIB.xlsx";
    private static final String SHEET_NAME = "TC71";

    @BeforeClass
    public void setup() throws Exception {
        driver = DriverManager.getDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        ExcelUtils.loadExcel(EXCEL_PATH);
    }

    @Test
    public void runTest() throws Exception {
        String logFile = TestResultLogger.createLogFile("TC71_FreeDeliveryTheLodgedInwardBill");
        String labelText=null;
        String VlabelText=null;
        Login1 login = new Login1();

        try {
        	RowData id = ExcelUtils.getRowAsRowData("TC70", 1);
            RowData fwdcno = ExcelUtils.getRowAsRowData(SHEET_NAME, 1);
            RowData VerifyData = ExcelUtils.getRowAsRowData(SHEET_NAME,2);

            // ---------------- Step 1: Login ----------------//
            login.First();
            WindowHandle.validateAlreadyLoggedIn(driver, logFile);
            ImportAndInwardBills FrwConNo = new ImportAndInwardBills(driver);

            // ---------------- Step 2: Mark Invoke ----------------//
            Map<String, String> result = FrwConNo.execute(fwdcno,id,id,SHEET_NAME, 1, EXCEL_PATH);
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
	        fwdcno = ExcelUtils.getRowAsRowData(SHEET_NAME, 1);
	          
	       
	        Map<String, String> Verifyresult = FrwConNo.execute(VerifyData,fwdcno,fwdcno,SHEET_NAME, 2, EXCEL_PATH);	        
	            if (Verifyresult.containsKey("errorMsg")) {
	                String errorMsg = Verifyresult.get("errorMsg");
	               TestResultLogger.log(logFile, "❌ Error: " + errorMsg);
	               Assert.fail("Test failed: " + errorMsg);
	            } else {
	            	VlabelText = Verifyresult.get("labelText");
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
                System.out.println("✅ Logout performed successfully at end.");
            } catch (Exception e) {
                System.err.println("⚠️ Logout failed at end: " + e.getMessage());
                TestResultLogger.log(logFile, "⚠️ Logout failed at end: " + e.getMessage());
            }
        }
        
}
    
}