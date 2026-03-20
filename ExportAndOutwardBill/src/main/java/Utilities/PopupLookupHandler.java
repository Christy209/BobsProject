package Utilities;

import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.Set;

public class PopupLookupHandler {

    /**
     * Handles a lookup popup in IE with recursive frame search, retries, and index-based selection.
     *
     * @param driver      Selenium WebDriver
     * @param searchValue The value to search (e.g., "Kerala")
     * @param resultIndex 0-based index of the result to select
     * @param timeoutSecs Max wait time for elements
     * @param maxRetries  Number of retry attempts if element not found
     */
    public static void handleLookupPopupByIndexWithRetry(WebDriver driver, String searchValue, int resultIndex, int timeoutSecs, int maxRetries) {
        String mainWindow = driver.getWindowHandle();

        // 1. Switch to popup window
        switchToPopupWindow(driver, mainWindow);

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSecs));

        try {
            WebElement searchBox = null;
            int attempt = 0;

            // 2. Retry loop for slow-loading popups
            while (attempt < maxRetries) {
                try {
                    searchBox = findElementInFrames(driver, By.name("FilterParam1"), wait);
                    if (searchBox != null && searchBox.isDisplayed() && searchBox.isEnabled()) {
                        break;
                    }
                } catch (NoSuchElementException | TimeoutException ignored) {
                }
                attempt++;
                Thread.sleep(1000); // wait 1s before retrying
            }

            if (searchBox == null) {
                throw new NoSuchElementException("FilterParam1 not found after " + maxRetries + " attempts");
            }

            // 3. Enter search value
            searchBox.clear();
            searchBox.sendKeys(searchValue);

            // 4. Click Submit
            WebElement submitBtn = findElementInFrames(driver, By.name("Submit"), wait);
            submitBtn.click();

            // 5. Wait for results and select by index
            List<WebElement> results = wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(
                    By.xpath("//font[@class='fntColData']")));

            if (resultIndex >= results.size()) {
                throw new NoSuchElementException("Requested index " + resultIndex + " exceeds results: " + results.size());
            }

            new Actions(driver).doubleClick(results.get(resultIndex)).perform();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted during popup retry wait", e);
        } finally {
            // 6. Return to main window
            driver.switchTo().window(mainWindow);
            driver.switchTo().defaultContent();
        }
    }

    private static void switchToPopupWindow(WebDriver driver, String mainWindow) {
        Set<String> allWindows = driver.getWindowHandles();
        for (String win : allWindows) {
            if (!win.equals(mainWindow)) {
                driver.switchTo().window(win);
                break;
            }
        }
    }

    private static WebElement findElementInFrames(WebDriver driver, By by, WebDriverWait wait) {
        try {
            return wait.until(ExpectedConditions.elementToBeClickable(by));
        } catch (TimeoutException e) {
            List<WebElement> frames = driver.findElements(By.tagName("iframe"));
            for (WebElement frame : frames) {
                driver.switchTo().frame(frame);
                try {
                    return findElementInFrames(driver, by, wait);
                } catch (NoSuchElementException | TimeoutException ignored) {
                }
                driver.switchTo().parentFrame();
            }
            throw new NoSuchElementException("Element " + by + " not found in any frame.");
        }
    }
}
