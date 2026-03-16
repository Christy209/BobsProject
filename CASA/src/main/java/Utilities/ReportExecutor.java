package Utilities;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class ReportExecutor {

    private WebDriver driver;

    public ReportExecutor(WebDriver driver) {
        this.driver = driver;
    }

    /**
     * Execute report and log result (Success/Error) with screenshot
     */
    public void executeReport(String moduleName, String reportName) {
        try {
            // Try to locate result message element
            WebElement resultMessage = driver.findElement(
                    By.xpath("//div[contains(@class,'result') or contains(text(),'Report generated')]")
            );

            String messageText = resultMessage.getText().trim();

            if (messageText.contains("Report generated successfully")) {
                // ✅ Success
               // WordReportUtil.captureScreenshotToWord(driver, moduleName, reportName, "SUCCESS");
            } else {
                // ⚠ Error message present but not successful
                //WordReportUtil.captureScreenshotToWord(driver, moduleName, reportName, "ERROR");
            }

            // Try clicking OK button if present
            try {
                WebElement okBtn = driver.findElement(By.xpath("//input[@value='Ok' or @type='submit']"));
                okBtn.click();
            } catch (Exception ignored) {
                // No OK button → safe to ignore
            }

        } catch (Exception e) {
            // ❌ If any exception (no message found, popup not handled, etc.)
           // WordReportUtil.captureScreenshotToWord(driver, moduleName, reportName, "ERROR");
        }
    }
}
