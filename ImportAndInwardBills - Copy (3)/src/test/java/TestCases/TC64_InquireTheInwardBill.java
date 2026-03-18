package TestCases;

import java.time.Duration;
import java.util.Map;
import java.util.Set;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import Base.DriverManager;
import Bill.InquireImportBills;
import Utilities.ExcelUtils;
import Utilities.LogOut;
import Utilities.Login1;
import Utilities.RowData;
import Utilities.TestResultLogger;
import Utilities.WindowHandle;

public class TC64_InquireTheInwardBill {
    public WebDriver driver;
    private WebDriverWait wait;
    
    private static final String EXCEL_PATH = System.getProperty("user.dir") + "/Resource/MIIB.xlsx";
    private static final String SHEET_NAME = "TC64";

    @BeforeClass
    public void setup() throws Exception {
        driver = DriverManager.getDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        ExcelUtils.loadExcel(EXCEL_PATH);
    }

    @Test
    public void runTest() throws Exception {
        String logFile = TestResultLogger.createLogFile("TC64_InquireTheInwardBill");

        Login1 login = new Login1();

        try {
            RowData initialData = ExcelUtils.getRowAsRowData("TC59", 1);
            RowData InquireData = ExcelUtils.getRowAsRowData(SHEET_NAME, 1); // Changed from Realize to InquireData

            login.First();
            WindowHandle.validateAlreadyLoggedIn(driver, logFile);

            Thread.sleep(2000); // Wait for page to fully load
            InquireImportBills inquire = new InquireImportBills(driver);

            Map<String, String> inquireResult = inquire.executeWithResultMap(InquireData, initialData, SHEET_NAME, 1, EXCEL_PATH);
            
            if (inquireResult.containsKey("errorMsg")) {
                String errorMsg = inquireResult.get("errorMsg");
                TestResultLogger.log(logFile, "❌ Error: " + errorMsg);
                Assert.fail("Test failed: " + errorMsg);
            }
            
            String dcNo = inquireResult.get("LABELTEXT_BILLID");
            String inquireLabelText = inquireResult.get("LabelText");
            
            assertDocumentaryCreditCaptured(dcNo, logFile, "Inquiry");
            
            if (inquireLabelText != null && !inquireLabelText.isEmpty()) {
                TestResultLogger.log(logFile, "✅ " + inquireLabelText);
                System.out.println("✅ " + inquireLabelText);
            } else {
                TestResultLogger.log(logFile, "✅ Inquiry completed successfully");
                System.out.println("✅ Inquiry completed successfully");
            }
   
            TestResultLogger.log(logFile, "✅ AppData captured at inquiry time:");
            for (Map.Entry<String, String> entry : inquireResult.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                if (value != null && !value.isEmpty()) {
                    TestResultLogger.log(logFile, key + " = " + value);
                }
            }

        } catch (AssertionError ae) {
            String errorMsg = "❌ Assertion Failure: " + ae.getMessage();
            System.err.println(errorMsg);
            TestResultLogger.log(logFile, errorMsg);
            throw ae;
        } catch (Exception e) {
            String errorMsg = "❌ Exception Occurred: " + e.getMessage();
            System.err.println(errorMsg);
            TestResultLogger.log(logFile, errorMsg);
            throw e;
        } finally {
            try {
                // ----- ADDED: Extra cleanup before logout -----
                Thread.sleep(1000);
                // Close any remaining popup windows
                String mainWindow = driver.getWindowHandle();
                Set<String> allWindows = driver.getWindowHandles();
                for (String window : allWindows) {
                    if (!window.equals(mainWindow)) {
                        try {
                            driver.switchTo().window(window);
                            driver.close();
                        } catch (Exception e) {
                            // Ignore
                        }
                    }
                }
                driver.switchTo().window(mainWindow);
                LogOut.performLogout(driver, wait);
            } catch (Exception e) {
                System.err.println("⚠️ Logout failed at end: " + e.getMessage());
            }
        }
    }
    
    private void assertDocumentaryCreditCaptured(String dcNo, String logFile, String phase) throws Exception {
        if (dcNo == null || dcNo.trim().isEmpty()) {
            String errorMsg = "❌ Error:  Inward Bill No not captured from UI during " + phase + " phase.";
            System.err.println(errorMsg);
            TestResultLogger.log(logFile, errorMsg);
            Assert.fail(errorMsg);
        } else {
            System.out.println("✅  Inward Bill No captured during " + phase + ": " + dcNo);
            TestResultLogger.log(logFile, "✅ Inward Bill No: " + dcNo);
        }
    }
}