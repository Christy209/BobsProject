package Utilities;

import java.time.Duration;
import java.util.Set;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class LookupUtils {

    /**
     * Selects a value from a lookup field (popup or iframe)
     *
     * @param driver       WebDriver instance
     * @param lookupBtnId  The ID of the lookup button on main page
     * @param value        The value to select (exact text)
     * @param frameId      The ID of the iframe opened by the lookup (if any). Pass null if popup window instead
     */
    public static void selectLookupValue(WebDriver driver, String lookupBtnId, String value, String frameId) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        // 1️⃣ Click the lookup button
        WebElement lookupBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id(lookupBtnId)));
        lookupBtn.click();

        // 2️⃣ Switch to iframe or new window
        if (frameId != null && !frameId.isEmpty()) {
            wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(By.id(frameId)));
        } else {
            String mainWindow = driver.getWindowHandle();
            Set<String> allWindows = driver.getWindowHandles();
            for (String w : allWindows) {
                if (!w.equals(mainWindow)) {
                    driver.switchTo().window(w);
                    break;
                }
            }
        }

        // 3️⃣ Select the value from table/grid
        WebElement option = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//td[text()='" + value + "']")));
        option.click();

        // 4️⃣ Switch back to main content or window
        if (frameId != null && !frameId.isEmpty()) {
            driver.switchTo().defaultContent();
        } else {
            String mainWindow = driver.getWindowHandles().iterator().next(); // main window
            driver.switchTo().window(mainWindow);
        }
    }
}
