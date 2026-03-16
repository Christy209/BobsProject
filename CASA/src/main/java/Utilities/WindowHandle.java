	
package Utilities;

import java.time.Duration;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.UnhandledAlertException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.openqa.selenium.JavascriptExecutor;

public class WindowHandle {

    public static boolean handlePopupIfExists(WebDriver driver) throws Exception {
        //String mainWindow = driver.getWindowHandle(); // Store the main window handle

        String mainWindowHandle = driver.getWindowHandle();
        System.out.println("Main Window: " + mainWindowHandle);

        // Wait for new window to open
        slowDown(2);  // Use explicit wait in real tests

        // Get all window handles
        Set<String> allWindows = driver.getWindowHandles();
        System.out.println("Total windows: " + allWindows.size());

        // Switch to new window
        for (String window : allWindows) {
            if (!window.equals(mainWindowHandle)) {
                driver.switchTo().window(window);
                System.out.println("Switched to new window: " + window);
                break;
            }
        }

        System.out.println("Current Window Handle after switch: " + driver.getWindowHandle());
		return false;

    }
    public static void listAllFrames(WebDriver driver) {
        List<WebElement> frames = driver.findElements(By.tagName("frame"));
        System.out.println("Total frames: " + frames.size());
        for (int i = 0; i < frames.size(); i++) {
            driver.switchTo().defaultContent();
            driver.switchTo().frame(i);
            System.out.println("Frame " + i + " HTML snippet: " 
                + driver.getPageSource().substring(0, 200));
        }
        driver.switchTo().defaultContent();
    }

  
    public static boolean HandlePopupIfExists(WebDriver driver) {
        String mainWindow = driver.getWindowHandle();
        Set<String> allWindows = driver.getWindowHandles();

        for (String handle : allWindows) {
            if (!handle.equals(mainWindow)) {
                driver.switchTo().window(handle);
                return true; // popup handled
            }
        }

        return false; // no popup
    }
    
    public static boolean handlePopupAndClickAccept(WebDriver driver, int timeoutSeconds) {
        String mainWindow = driver.getWindowHandle();
        Set<String> existingWindows = driver.getWindowHandles();

        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));

            // Wait for a new window (popup) to appear
            wait.until(d -> driver.getWindowHandles().size() > existingWindows.size());

            // Switch to the new window
            Set<String> allWindows = driver.getWindowHandles();
            for (String handle : allWindows) {
                if (!existingWindows.contains(handle)) {
                    driver.switchTo().window(handle);
                    break;
                }
            }

            // Wait for Accept button to be clickable
            WebElement acceptButton = wait.until(
                    ExpectedConditions.elementToBeClickable(By.id("accept"))
            );

            // Scroll into view and click
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", acceptButton);
            acceptButton.click();

            // Close popup and switch back
            driver.close();
            driver.switchTo().window(mainWindow);

            System.out.println("Popup handled and Accept clicked.");
            return true;
        } catch (TimeoutException te) {
            System.out.println("No popup appeared or Accept button not clickable.");
        } catch (Exception e) {
            System.out.println("Error handling popup: " + e.getMessage());
        }

        // Ensure switch back to main window
        driver.switchTo().window(mainWindow);
        return false;
    }
    
    public static void validateAlreadyLoggedIn(
            WebDriver driver,
            String logFile
    ) {
        try {
            // Small wait for Finacle response
            Thread.sleep(2000);

            String pageSource = driver.getPageSource();

            if (pageSource.toLowerCase().contains("already logged in")) {

                String errorMsg = "User is already logged in.";
                System.err.println("❌ Login Failed: " + errorMsg);
                TestResultLogger.log(logFile, "❌ Login Failed: " + errorMsg);

                // Stop execution immediately
                Assert.fail("Finacle Login Error: " + errorMsg);
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    public static boolean HandlePopupAndClickAccept12(WebDriver driver, Duration timeout) {
        String mainWindow = driver.getWindowHandle();
        Set<String> allWindows = driver.getWindowHandles();

        // First, check for a separate popup window
        if (allWindows.size() > 1) {
            for (String handle : allWindows) {
                if (!handle.equals(mainWindow)) {
                    driver.switchTo().window(handle);
                    try {
                        WebDriverWait wait = new WebDriverWait(driver, timeout);
                        WebElement acceptBtn = wait.until(ExpectedConditions.elementToBeClickable(
                                By.xpath("//input[@id='accept' or @value='Accept']")));
                        ((JavascriptExecutor) driver).executeScript(
                                "arguments[0].scrollIntoView(true); arguments[0].click();", acceptBtn);
                        System.out.println("Popup window detected and Accept clicked.");
                        return true;
                    } catch (Exception e) {
                        System.out.println("Popup window found but Accept button not clickable: " + e.getMessage());
                        return false;
                    } finally {
                        driver.close(); // Close popup
                        driver.switchTo().window(mainWindow);
                    }
                }
            }
        }

        // Second, check for in-page modal or iframe
        try {
            WebDriverWait wait = new WebDriverWait(driver, timeout);
            // adjust the xpath if your modal is inside an iframe
            WebElement acceptBtn = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//input[@id='accept' or @value='Accept']")));
            ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].scrollIntoView(true); arguments[0].click();", acceptBtn);
            System.out.println("In-page modal detected and Accept clicked.");
            return true;
        } catch (Exception e) {
            System.out.println("No popup or in-page Accept button found.");
            return false;
        }
    }

    public static boolean HandlePopupAndClickAccept11(WebDriver driver, Duration timeout) {
        String mainWindow = driver.getWindowHandle();
        Set<String> allWindows = driver.getWindowHandles();

       try { // Check if a popup window exists
        if (allWindows.size() <= 1) {
            System.out.println("No popup appeared.");
            return false;
        }
       }catch(Exception e) {
        // Iterate through all windows to find the popup
        for (String handle : allWindows) {
            if (!handle.equals(mainWindow)) {
                driver.switchTo().window(handle);
                try {
                    WebDriverWait wait = new WebDriverWait(driver, timeout);
                    WebElement acceptBtn = wait.until(ExpectedConditions.elementToBeClickable(
                            By.xpath("//input[@id='accept' or @value='Accept']")));
                    ((JavascriptExecutor) driver).executeScript(
                            "arguments[0].scrollIntoView(true); arguments[0].click();", acceptBtn);
                    Thread.sleep(500);
                    System.out.println("Popup detected and Accept clicked.");
                } catch (Exception ie) {
                    System.out.println("Popup detected but Accept button not found or clickable: " + e.getMessage());
                } finally {
                   // driver.close(); // Close popup
                    driver.switchTo().window(mainWindow); // Return to main window
                }
            }
                return true;
            }
        }

        return false; // No popup found
    }

    public static boolean clickAcceptButton(WebDriver driver) {
        try {
        	
        	 String mainWindow = driver.getWindowHandle();
             Set<String> allWindows = driver.getWindowHandles();

             for (String handle : allWindows) {
                 if (!handle.equals(mainWindow)) {
                     driver.switchTo().window(handle);
                     return true; // popup handled
                 }
         
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

            // Wait until button is present in DOM
            WebElement acceptBtn = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//input[@value='Accept']")));

            // Remove overlay if present
            ((JavascriptExecutor) driver).executeScript(
                "var overlay = document.getElementById('overlay'); if(overlay){overlay.remove();}"
            );

            // Scroll into view and click using JS
            ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView(true); arguments[0].click();", acceptBtn
            );

            System.out.println("Accept button clicked successfully.");
          
            return true;
             }
        } catch (Exception e) {
            System.out.println("Failed to click Accept: " + e.getMessage());
            
        }
		return false;
    }

    public static boolean HandlePopupAndClickAccept1(WebDriver driver, Duration timeout) {
    	 String mainWindow = driver.getWindowHandle();
         Set<String> allWindows = driver.getWindowHandles();

         for (String handle : allWindows) {
             if (!handle.equals(mainWindow)) {
                 driver.switchTo().window(handle);
                 return true; // popup handled
             }
         }

         return false; // no popup
     }
    public static boolean HandlePopupAndClickAccept(WebDriver driver, Duration timeout) {
        String mainWindow = driver.getWindowHandle();
        Set<String> allWindows = driver.getWindowHandles();

        if (allWindows.size() <= 1) {
            return false;
        }
        for (String handle : allWindows) {
            if (!handle.equals(mainWindow)) {
                driver.switchTo().window(handle);

                try {
                    WebDriverWait wait = new WebDriverWait(driver, timeout);
                    WebElement acceptBtn = wait.until(ExpectedConditions.elementToBeClickable(
                            By.xpath("//input[@id='accept' or @value='Accept']")));
                    ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true); arguments[0].click();", acceptBtn);
                    Thread.sleep(500);

                } catch (Exception e) {
                    System.out.println("Accept button not found or clickable: " + e.getMessage());
                    driver.close(); // Close popup if something goes wrong
                    driver.switchTo().window(mainWindow);
                    return false;
                }
                driver.switchTo().window(mainWindow);
                return true; // popup handled successfully
            }
        }
        return false; // no popup found
    }
    
    
//    public static boolean HandlePopupAndClickAccept(WebDriver driver, Duration timeout) {
//        String mainWindow = driver.getWindowHandle();
//        Set<String> allWindows = driver.getWindowHandles();
//
//        if (allWindows.size() <= 1) {
//            return false; // no popup window, return immediately
//        }
//
//        for (String handle : allWindows) {
//            if (!handle.equals(mainWindow)) {
//                driver.switchTo().window(handle);
//                try {
//                    // Use short wait for Accept button
//                    WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(3));
//                    WebElement acceptBtn = shortWait.until(ExpectedConditions.elementToBeClickable(
//                            By.xpath("//input[@id='accept' or @value='Accept']")));
//                    ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true); arguments[0].click();", acceptBtn);
//                    Thread.sleep(500);
//                   
//                } catch (Exception e) {
//                    System.out.println("Accept button not found or clickable: " + e.getMessage());
//                }
//                driver.switchTo().window(mainWindow);
//                return true; // handled popup
//            }
//        }
//        return false; // no popup found
//    }


	public static boolean HandlePopupIfExists(WebDriver driver, Duration timeout) {
		 String mainWindow = driver.getWindowHandle();
         Set<String> allWindows = driver.getWindowHandles();

         for (String handle : allWindows) {
             if (!handle.equals(mainWindow)) {
                 driver.switchTo().window(handle);
                 return true; // popup handled
             }
         }
         return false; 
	}
    public static boolean handleAlertIfPresent(WebDriver driver ) {
        try {
            WebDriverWait alertWait = new WebDriverWait(driver, Duration.ofSeconds(2)); 
            Alert alert = alertWait.until(ExpectedConditions.alertIsPresent());
           
            System.out.println("⚠️ Alert Message: " + alert.getText());
            alert.accept(); // Dismiss the alert
        } catch (TimeoutException ignored) {
            // No alert means we can continue
        }
		return false;
    }
   
public static void selectByVisibleText(WebElement element, String visibleText) {
   try {
       Select dropdown = new Select(element);
       dropdown.selectByVisibleText(visibleText);
   } catch (NoSuchElementException e) {
       System.out.println("Option '" + visibleText + "' not found in dropdown.");
   } catch (Exception e) {
       System.out.println("Error selecting from dropdown: " + e.getMessage());
   }
}
public static void selectDropdownIfValuePresent(WebDriver driver, WebDriverWait wait, By locator, String value) {
try {
       WebElement dropdown = wait.until(ExpectedConditions.elementToBeClickable(locator));
       Select select = new Select(dropdown);

       if (value != null && !value.trim().isEmpty()) {
           select.selectByVisibleText(value.trim());
       } else {
           try {
               // Try to select default/empty option
               select.selectByVisibleText("--Select--");
           } catch (Exception e) {
           }
       }

   } catch (Exception e) {
       System.out.println("❌ Error handling dropdown " + locator + ": " + e.getMessage());
   }
}

    public static void slowDown(int seconds) {
        try {
            Thread.sleep(seconds * 1000L); // Convert to milliseconds
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("⚠️ Slow down interrupted: " + e.getMessage());
        }
    }
    
    public static void selectDropdownOrInput(WebDriver driver, WebDriverWait wait, By locator, String value) {
        try {
            WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
            String tag = element.getTagName();

            if (value != null && !value.trim().isEmpty()) {
                if ("select".equalsIgnoreCase(tag)) {
                    // Handle dropdown
                    Select select = new Select(element);
                    select.selectByVisibleText(value.trim());
                    System.out.println("✅ Selected from dropdown: " + value.trim());
                } else {
                    // Handle input field
                    element.clear();
                    element.sendKeys(value.trim());
                    System.out.println("✅ Entered in input: " + value.trim());
                }
            } else {
                System.out.println("⚠️ No value provided for " + locator.toString());
            }
        } catch (Exception e) {
            System.out.println("❌ Error handling element " + locator + ": " + e.getMessage());
        }
    }

    public static void goToMainFrame(WebDriverWait wait) {
try {
wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(By.name("loginFrame")));
wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(By.name("CRMServer")));
    wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(By.id("DataAreaFrm")));
    wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(By.id("tempFrm")));
    wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(By.id("tabContentFrm")));
    wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(By.id("userArea")));
    wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(By.id("IFrmtab0")));
    wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(By.id("formDispFrame")));
   
} catch (Exception ed) {
ed.printStackTrace();
}

}
    public static String Window(WebDriver driver) throws Exception {
        String mainWindowHandle = driver.getWindowHandle();
        System.out.println("Main Window: " + mainWindowHandle);

        // Wait briefly for popup
        Thread.sleep(2000);  // Replace with WebDriverWait if needed

        Set<String> allWindows = driver.getWindowHandles();
        System.out.println("Total windows: " + allWindows.size());

        for (String window : allWindows) {
            if (!window.equals(mainWindowHandle)) {
                driver.switchTo().window(window);
                System.out.println("Switched to new window: " + window);
                return window;
            }
        }

        // If no new window found, stay on main
        driver.switchTo().window(mainWindowHandle);
        return mainWindowHandle;
    }
    public static void ValidationFrame(WebDriver driver) {
   driver.switchTo().defaultContent(); // or use driver.switchTo().frame(0); if no name
   driver.switchTo().frame("loginFrame");
   driver.switchTo().frame("CoreServer");
   driver.switchTo().frame("FINW");

}
    public static void action(WebDriver driver) {
    Actions actions = new Actions(driver);
        actions.sendKeys(Keys.PAGE_DOWN).perform();

}
    public static String extract(String fullText) {
        if (fullText == null || fullText.isBlank()) {
            return "";
        }
        // Split based on whitespace, limit = 2 to keep only the first token
        String[] parts = fullText.trim().split("\\s+", 2);
        return parts.length > 0 ? parts[0] : "";
    }
    public static String getValueByLabel(WebDriver driver, String labelText) {
        // Adjust this if the field is inside an iframe
        // driver.switchTo().frame("FINW"); // only if needed

        String xpath = "//td[normalize-space(text())='" + labelText + "']" +
                       "/following-sibling::td//span[contains(@class,'display-text')]";

        WebElement el = new WebDriverWait(driver, Duration.ofSeconds(10))
            .until(ExpectedConditions.visibilityOfElementLocated(By.xpath(xpath)));
        return el.getText().trim();
    }
    public static String getLabelledValue(WebDriver driver, String labelText) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
       
        // 2️⃣ Build and use a reliable following-sibling XPath
        String xpath = "//td[normalize-space()='" + labelText + "']"
                     + "/following-sibling::td[1]//*[contains(@class,'display-text') or normalize-space(text())]";
        WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(xpath)));
        String text = el.getText().trim();
       
        // 🛑 Switch back to the main document context
        driver.switchTo().defaultContent();
       
        return text;
    }
    
    public static void UIError(WebDriver driver, WebDriverWait wait) {
 
        // Wait for any spinner or overlay to disappear
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(".loading-overlay")));

        WindowHandle.ValidationFrame(driver);
       
        String alertMsg = "";
        By err = By.cssSelector("table.errortableborder tr.alert td");
        List<WebElement> elems = driver.findElements(err);
        if (!elems.isEmpty()) {
            String msg = wait.until(ExpectedConditions.visibilityOfElementLocated(err)).getText().trim();
            Assert.fail("❌ UI Error: " + msg);
        }

        SharedContext.alertMessage = alertMsg;
}
    public static void Go(WebDriver driver, WebDriverWait wait) {

    WindowHandle.ValidationFrame(driver);
    String alertMsg = "";
        By errorLocator = By.cssSelector("table.errortableborder tr.alert td a[onmouseover]");
        List<WebElement> elems = driver.findElements(errorLocator);
        if (!elems.isEmpty()) {
            String msg = wait.until(ExpectedConditions.visibilityOfElementLocated(errorLocator)).getText().trim();
            Assert.fail("❌ UI Error: " + msg);
        }
        SharedContext.alertMessage = alertMsg;
    }
//    
//     WindowHandle.ValidationFrame(driver);
//        By locator = By.cssSelector("table.errortableborder tr.alert td a[onmouseover]");
//        List<WebElement> elems = driver.findElements(locator);
//
//        if (!elems.isEmpty()) {
//            String actual = wait.until(ExpectedConditions.visibilityOfElementLocated(locator))
//                                 .getText().trim();
//
//            if (expected != null && !expected.isEmpty() && actual.equalsIgnoreCase(expected)) {
//                System.out.println("✅ Expected UI error occurred: " + actual);
//                return; // Treat as pass
//            }
//
//            Assert.fail("❌ UI Error: '" + actual + "' — expected: '" + expected + "'");
//        } else if (expected != null && !expected.isEmpty()) {
//            Assert.fail("❌ Expected UI error '" + expected + "' did not occur");
//        }
//
    
//    public void handleExcelSecurityPopup() throws Exception {
//        Robot robot = new Robot();
//        Thread.sleep(2000); // wait for popup
//
//        // Press Left arrow to select "Yes"
//        robot.keyPress(KeyEvent.VK_LEFT);
//        robot.keyRelease(KeyEvent.VK_LEFT);
//        Thread.sleep(500);
//
//        // Press Enter to confirm
//        robot.keyPress(KeyEvent.VK_ENTER);
//        robot.keyRelease(KeyEvent.VK_ENTER);
//    }
    public static boolean handleIEDownloadPopup(String action) {
        try {
            Robot robot = new Robot();
            robot.setAutoDelay(500);

            // Bring popup to focus (ALT + N works for IE download bar)
            robot.keyPress(KeyEvent.VK_ALT);
            robot.keyPress(KeyEvent.VK_N);
            robot.keyRelease(KeyEvent.VK_N);
            robot.keyRelease(KeyEvent.VK_ALT);
            Thread.sleep(1000);

            action = action.toUpperCase();

            if (action.equals("OPEN")) {
                // Shortcut key for OPEN is 'O'
                robot.keyPress(KeyEvent.VK_ALT);
                robot.keyPress(KeyEvent.VK_O);
                robot.keyRelease(KeyEvent.VK_O);
                robot.keyRelease(KeyEvent.VK_ALT);
            } else if (action.equals("SAVE")) {
                robot.keyPress(KeyEvent.VK_ALT);
                robot.keyPress(KeyEvent.VK_S);
                robot.keyRelease(KeyEvent.VK_S);
                robot.keyRelease(KeyEvent.VK_ALT);
            } else if (action.equals("CANCEL")) {
                robot.keyPress(KeyEvent.VK_ESCAPE);
                robot.keyRelease(KeyEvent.VK_ESCAPE);
            } else {
                return false;
            }

            Thread.sleep(1000); // wait for action to complete
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
//    public static void handleExcelSecurityPopup() {
//        try {
//            Robot robot = new Robot();
//            Thread.sleep(2000); // wait for popup to appear
//
//            // Move selection to "Yes"
//            robot.keyPress(KeyEvent.VK_LEFT);
//            robot.keyRelease(KeyEvent.VK_LEFT);
//            Thread.sleep(500);
//
//            // Press Enter to confirm
//            robot.keyPress(KeyEvent.VK_ENTER);
//            robot.keyRelease(KeyEvent.VK_ENTER);
//
//            System.out.println("✅ Excel security popup handled successfully.");
//
//        } catch (Exception e) {
//            System.out.println("⚠ Failed to handle Excel security popup: " + e.getMessage());
//        }
//    }
    public static void clickNoOnExcelPopup() {
        try {
            Robot robot = new Robot();
            robot.setAutoDelay(500); // Delay between keystrokes

            int maxRetries = 10; // max number of popups to handle
            for (int i = 0; i < maxRetries; i++) {
                Thread.sleep(1000); // wait for popup to appear

                // Move selection from "Yes" to "No"
                robot.keyPress(KeyEvent.VK_RIGHT);
                robot.keyRelease(KeyEvent.VK_RIGHT);

                Thread.sleep(300); // wait for move to register

                // Press ENTER to confirm "No"
                robot.keyPress(KeyEvent.VK_ENTER);
                robot.keyRelease(KeyEvent.VK_ENTER);

                System.out.println("✅ Excel popup handled: Clicked NO (attempt " + (i + 1) + ")");
            }

            System.out.println("✅ All Excel popups attempted");

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("❌ Failed to handle Excel popup");
        }
    }

    public static void handleExcelFormatPopup() {
        try {
            Robot robot = new Robot();
            robot.setAutoDelay(800); // small delay between actions

            // ALT + Y to click "Yes" on format/extension popup
            robot.keyPress(KeyEvent.VK_ALT);
            robot.keyPress(KeyEvent.VK_Y);
            robot.keyRelease(KeyEvent.VK_Y);
            robot.keyRelease(KeyEvent.VK_ALT);

            // Press ENTER as a fallback
            robot.keyPress(KeyEvent.VK_ENTER);
            robot.keyRelease(KeyEvent.VK_ENTER);

            System.out.println("✅ Excel format/extension popup handled (Yes clicked)");

        } catch (AWTException e) {
            System.out.println("⚠️ Error handling Excel format popup: " + e.getMessage());
        }
    }

    public static void handleRecoverWebpagePopup() {
        try {
            Robot robot = new Robot();
            robot.setAutoDelay(200);

            // Send ALT + C  (this closes the yellow bar if it exists)
            robot.keyPress(KeyEvent.VK_ALT);
            robot.keyPress(KeyEvent.VK_C);
            robot.keyRelease(KeyEvent.VK_C);
            robot.keyRelease(KeyEvent.VK_ALT);

            System.out.println("✅ Tried to close Recover Webpage bar (ALT+C)");
        } catch (AWTException e) {
            System.out.println("⚠️ Could not send keys for Recover Webpage");
        }
    }

    // Excel “The file format & extension don’t match”  → Yes (Alt+Y)
    public static void handleExcelFormatMismatchPopup() {
        try {
            // ✅ Force Excel window to the front
            focusExcelWindow();
            Thread.sleep(500);

            // ✅ Send ALT+Y for format mismatch popup
            Robot robot = new Robot();
            robot.setAutoDelay(300);

            // ALT+Y is a shortcut for "Yes" in Excel's warning
            robot.keyPress(KeyEvent.VK_ALT);
            robot.keyPress(KeyEvent.VK_Y);
            robot.keyRelease(KeyEvent.VK_Y);
            robot.keyRelease(KeyEvent.VK_ALT);

            System.out.println("✅ ALT+Y sent for format mismatch");

            // ✅ Wait & send ENTER as fallback if ALT+Y didn’t work
            Thread.sleep(800);
            robot.keyPress(KeyEvent.VK_ENTER);
            robot.keyRelease(KeyEvent.VK_ENTER);
            System.out.println("✅ ENTER sent as fallback for format mismatch popup");

        } catch (Exception e) {
            System.out.println("⚠️ Failed to handle Excel format mismatch popup");
            e.printStackTrace();
        }
    }
    public static boolean handleIEDownloadPopup(String action, Runnable afterPopupAction) {
        try {
            // Handle popup logic
            System.out.println("Handling popup with action: " + action);
            
            // ✅ Popup handled successfully
            if (afterPopupAction != null) {
                afterPopupAction.run(); // 🔄 Call main method again
            }
            return true;
        } catch (Exception e) {
            System.out.println("⚠ Failed to handle popup: " + e.getMessage());
            return false;
        }
    }

    public static void closeRecoverWebpage() {
        try {
            Robot r = new Robot();
            r.setAutoDelay(150);
            r.keyPress(KeyEvent.VK_ALT);
            r.keyPress(KeyEvent.VK_C);
            r.keyRelease(KeyEvent.VK_C);
            r.keyRelease(KeyEvent.VK_ALT);
            System.out.println("✅ Sent ALT+C for Recover Webpage");
        } catch (AWTException e) {
            e.printStackTrace();
        }
    }

    /** Click “Yes” on the Excel format/extension warning (ALT+Y) */
    public static void confirmExcelFormatMismatch() {
        try {
            Robot r = new Robot();
            r.setAutoDelay(150);
            r.keyPress(KeyEvent.VK_ALT);
            r.keyPress(KeyEvent.VK_Y);
            r.keyRelease(KeyEvent.VK_Y);
            r.keyRelease(KeyEvent.VK_ALT);
            System.out.println("✅ Sent ALT+Y for Excel Format Mismatch");
        } catch (AWTException e) {
            e.printStackTrace();
        }
    }
    // Excel “Problems During Load” dialog → OK (Enter)
    public static void handleExcelProblemsDuringLoad() {
        try {
            Robot robot = new Robot();
            robot.setAutoDelay(200);

            // Press ENTER (OK)
            robot.keyPress(KeyEvent.VK_ENTER);
            robot.keyRelease(KeyEvent.VK_ENTER);

            System.out.println("✅ Tried to close Excel Problems During Load (ENTER)");
        } catch (AWTException e) {
            System.out.println("⚠️ Could not send keys for Excel Problems During Load");
        }
    }
    public static void handleExcelSecurityPopup() {
        try {
            focusExcelWindow();
            Thread.sleep(500);

            Robot robot = new Robot();
            robot.setAutoDelay(300);

            // Press ALT+A to click "Enable Content" (shortcut in Excel security popup)
            robot.keyPress(KeyEvent.VK_ALT);
            robot.keyPress(KeyEvent.VK_A);
            robot.keyRelease(KeyEvent.VK_A);
            robot.keyRelease(KeyEvent.VK_ALT);

            System.out.println("✅ Clicked 'Yes' on Excel security popup");

        } catch (Exception e) {
            System.out.println("⚠️ Failed to handle Excel security popup");
            e.printStackTrace();
        }
    }

    public static void handleAllExcelPopups() {
        try {
            Robot robot = new Robot();
            robot.setAutoDelay(800);

            // 1️⃣ First Popup: File format mismatch -> Yes
            robot.keyPress(KeyEvent.VK_ALT);
            robot.keyPress(KeyEvent.VK_Y);
            robot.keyRelease(KeyEvent.VK_Y);
            robot.keyRelease(KeyEvent.VK_ALT);
            robot.keyPress(KeyEvent.VK_ENTER); // fallback
            robot.keyRelease(KeyEvent.VK_ENTER);
            System.out.println("✅ Clicked 'Yes' on first Excel popup");
            Thread.sleep(2000); // wait for next popup

            // 2️⃣ Second Popup: Problems During Load -> OK
            robot.keyPress(KeyEvent.VK_ENTER);
            robot.keyRelease(KeyEvent.VK_ENTER);
            System.out.println("✅ Clicked 'OK' on Problems During Load popup");
            Thread.sleep(1000); // wait for possible third popup

            // 3️⃣ Optional Third Popup: Another Yes/No -> Yes
            robot.keyPress(KeyEvent.VK_ENTER);
            robot.keyRelease(KeyEvent.VK_ENTER);
            System.out.println("✅ Clicked 'Yes' on final popup (if appeared)");

        } catch (Exception e) {
            System.out.println("⚠️ Error handling Excel popups: " + e.getMessage());
        }
    }
//    public static boolean isExcelRunning() {
//        try {
//            return ProcessHandle.allProcesses()
//                    .anyMatch(p -> p.info().command().isPresent() &&
//                            p.info().command().get().toLowerCase().contains("excel.exe"));
//        } catch (Exception e) {
//            return false;
//        }
//    }
    public static void focusExcelWindow() {
        try {
            // Brings Excel to front using cmd (Windows only)
            Runtime.getRuntime().exec("cmd /c nircmd win activate ititle \"Excel\"");
            Thread.sleep(700);
        } catch (Exception e) {
            System.out.println("⚠️ Could not focus Excel window");
        }
    }
    public static void handleExcelPopupsAndClose(WebDriver driver, String reportName) {
        try {
            // 1️⃣ Focus Excel window first
            focusExcelWindow();
            System.out.println("✅ Excel focused");

            // 2️⃣ Handle format mismatch popup (ALT+Y)
            handleExcelFormatMismatchPopup();
            System.out.println("✅ Format mismatch popup handled");

            // 3️⃣ Handle security popup (Enable Editing / Enable Content)
            handleExcelSecurityPopup();
            System.out.println("✅ Excel security popup handled");

            // 4️⃣ Wait briefly so Excel is stable before screenshot
            Thread.sleep(2000);

            // 5️⃣ Capture screenshot
            //WordReportUtil.captureScreenshotToWord(driver, "PrintQueue", "After_PrintScreen", "SUCCESS");
            System.out.println("✅ Screenshot captured after Excel opened");

            // 6️⃣ Close Excel safely
            ensureExcelClosed();
            System.out.println("✅ Excel closed successfully");

        } catch (Exception e) {
            System.err.println("❌ Error while handling Excel popups: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void ensureExcelClosed() {
        try {
            ProcessBuilder pb = new ProcessBuilder("taskkill", "/IM", "excel.exe", "/F");
            Process p = pb.start();
            p.waitFor();
            System.out.println("✅ Closed any running Excel instance before opening a new one");
        } catch (Exception e) {
            System.out.println("⚠️ Could not close Excel: " + e.getMessage());
        }
    
    }
    public static void navigateToDlyRptMenu(WebDriver driver, WebDriverWait wait, String reportName) {
        try {
            System.out.println("🔄 Navigating back to DLYRPT menu for report: " + reportName);

            driver.switchTo().defaultContent();
            driver.switchTo().frame("loginFrame");
            WindowHandle.slowDown(3);

            // Locate and click DLYRPT menu again
            WebElement menuSearchBox = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("menuSelect")));
            ((JavascriptExecutor) driver).executeScript("arguments[0].value='DLYRPT';", menuSearchBox);

            WebElement searchButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("menuSearcherGo")));
            searchButton.click();

            try {
                Alert alert = wait.until(ExpectedConditions.alertIsPresent());
                System.out.println("ℹ️ Popup text: " + alert.getText());
                alert.accept();
                System.out.println("✅ Alert handled successfully.");
            } catch (TimeoutException te) {
                System.out.println("ℹ️ No alert appeared while navigating to DLYRPT.");
            }

            // Switch to core + FINW frames
            wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt("CoreServer"));
            wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(By.id("FINW")));

            // Refill report name field
            WebElement reportField = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("ModuleName")));
            String jsReport = "var el = arguments[0];" +
                    "el.value='" + reportName + "';" +
                    "var evt1 = document.createEvent('HTMLEvents'); evt1.initEvent('input', true, false); el.dispatchEvent(evt1);" +
                    "var evt2 = document.createEvent('HTMLEvents'); evt2.initEvent('change', true, false); el.dispatchEvent(evt2);";
            ((JavascriptExecutor) driver).executeScript(jsReport, reportField);

            // Click "Go" button to reload report screen
            ((JavascriptExecutor) driver).executeScript("document.querySelector(\"input[value='Go']\").click();");

            System.out.println("✅ DLYRPT menu reloaded successfully for " + reportName);

        } catch (Exception e) {
            System.out.println("❌ Failed to navigate back to DLYRPT menu: " + e.getMessage());
            // Optional: capture screenshot for debugging
            //WordReportUtil.captureScreenshotToWord(driver, "DLYRPT", "NavigateToDlyRpt_Failed", "ERROR");
        }
    }

        public static void switchToFrames(WebDriver driver, WebDriverWait wait, String... frameNames) {
            driver.switchTo().defaultContent(); // always start fresh
            for (String frameName : frameNames) {
                int retries = 0;
                while (retries < 5) { // retry up to 5 times
                    try {
                        wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(By.name(frameName)));
                        System.out.println("✔ Switched to frame: " + frameName);
                        break;
                    } catch (Exception e) {
                        retries++;
                        System.out.println("⚠ Retry " + retries + " for frame: " + frameName);
                        try { Thread.sleep(1000); } catch (InterruptedException ie) {}
                        if (retries == 5) {
                            throw new RuntimeException("❌ Could not switch to frame: " + frameName);
                        }
                    }
                }
            }
        

}
        public static void clickSubmitButtonIE(WebDriver driver) {
            JavascriptExecutor js = (JavascriptExecutor) driver;
            try {
                // ✅ Option 1: Call onclick function directly
                js.executeScript("selectedUser();");
                System.out.println("Submit button triggered via JS function call.");
                return;
            } catch (Exception e1) {
                System.out.println("JS function call failed, trying JS click...");
            }

            try {
                // ✅ Option 2: Force click via JS
                WebElement submitButton = driver.findElement(By.id("submitBtn"));
                js.executeScript("arguments[0].click();", submitButton);
                System.out.println("Submit button clicked via JS.");
                return;
            } catch (Exception e2) {
                System.out.println("JS click failed, trying Robot key press...");
            }

            try {
                // ✅ Option 3: Robot ENTER key
                java.awt.Robot robot = new java.awt.Robot();
                robot.keyPress(java.awt.event.KeyEvent.VK_ENTER);
                robot.keyRelease(java.awt.event.KeyEvent.VK_ENTER);
                System.out.println("Submit button triggered via Robot ENTER key.");
            } catch (Exception e3) {
                System.err.println("All methods failed to click submit button: " + e3.getMessage());
            }
        }
			        public static void handleLookupMain(WebDriver driver, WebDriverWait wait,
			                By lookupButtonLocator, String value) throws Exception {
			String mainWindow = driver.getWindowHandle();
			
			// 1️⃣ Click the lookup button
			WebElement lookupBtn = wait.until(ExpectedConditions.elementToBeClickable(lookupButtonLocator));
			lookupBtn.click();
			
			// 2️⃣ Wait for new window (popup) to appear
			Set<String> allWindows = driver.getWindowHandles();
			for (int i = 0; i < 10; i++) { // ~10 sec max wait
			if (allWindows.size() > 1) break;
			Thread.sleep(1000);
			allWindows = driver.getWindowHandles();
			}
			
			// 3️⃣ Switch to the popup window
			for (String win : allWindows) {
			if (!win.equals(mainWindow)) {
			driver.switchTo().window(win);
			break;
			}
			}
			
			System.out.println("Switched to lookup popup for value: " + value);
			
			// 4️⃣ Enter search value
			WebElement searchBox = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("FilterParam1")));
			searchBox.clear();
			searchBox.sendKeys(value);
			
			// 5️⃣ Click Submit
			WebElement submitBtn = wait.until(ExpectedConditions.elementToBeClickable(By.name("Submit")));
			submitBtn.click();
			
			// 6️⃣ Wait & double-click first matching result
			WebElement firstResult = wait.until(ExpectedConditions.visibilityOfElementLocated(
			By.xpath("//font[@class='fntColData' and text()='" + value + "']")));
			new Actions(driver).doubleClick(firstResult).perform();
			
			// 7️⃣ Switch back to main window
			driver.switchTo().window(mainWindow);
			WindowHandle.goToMainFrame(wait);
			
			System.out.println("✅ Lookup filled successfully: " + value);
			}
			
//			        public static void handleLookupFieldDynamic(WebDriver driver, WebDriverWait wait, String value) throws Exception {
//            try {
//                driver.switchTo().defaultContent();
//
//                // 1️⃣ Find search input anywhere in all frames
//                WebElement searchBox = findElementInFrames(driver, By.name("FilterParam1"));
//                searchBox.clear();
//                searchBox.sendKeys(value);
//
//                // 2️⃣ Find Submit button anywhere in all frames
//                WebElement submitBtn = findElementInFrames(driver, By.name("Submit"));
//                submitBtn.click();
//
//                driver.switchTo().defaultContent();
//
//                // 3️⃣ Find the first result anywhere in frames
//                WebElement firstResult = findElementInFrames(driver, By.xpath("//font[@class='fntColData' and text()='" + value + "']"));
//                new Actions(driver).doubleClick(firstResult).perform();
//
//                driver.switchTo().defaultContent();
//                System.out.println("✅ Lookup filled successfully: " + value);
//
//            } catch (Exception e) {
//                driver.switchTo().defaultContent();
//                System.out.println("❌ Lookup failed for '" + value + "': " + e.getMessage());
//                throw e;
//            }
//        }
//
//        public static WebElement findElementInFrames(WebDriver driver, By locator) throws Exception {
//            return findElementInFrames(driver, locator, 0);
//        }

//        private static WebElement findElementInFrames(WebDriver driver, By locator, int depth) throws Exception {
//            List<WebElement> frames = driver.findElements(By.tagName("iframe"));
//
//            // Try current context first
//            try {
//                return driver.findElement(locator);
//            } catch (NoSuchElementException ignored) {}
//
//            // Search recursively in all frames
//            for (WebElement frame : frames) {
//                try {
//                    driver.switchTo().frame(frame);
//                    WebElement found = findElementInFrames(driver, locator, depth + 1);
//                    if (found != null) {
//                        return found;
//                    }
//                    driver.switchTo().parentFrame();
//                } catch (Exception e) {
//                    driver.switchTo().parentFrame();
//                }
//            }
//
//            if (depth == 0) throw new NoSuchElementException("Element not found: " + locator);
//            return null;
//        }

        @SuppressWarnings("unused")
		private static void printAllFrames(WebDriver driver, String prefix, int depth) {
            try {
                List<WebElement> frames = driver.findElements(By.tagName("iframe"));
                System.out.println(prefix + ">>> Found " + frames.size() + " frames at depth " + depth);

                for (int i = 0; i < frames.size(); i++) {
                    WebElement frame = frames.get(i);
					String name = frame.getAttribute("name");
					String id = frame.getAttribute("id");

                    System.out.println(prefix + "Frame[" + i + "] -> name=" + name + ", id=" + id);

                    driver.switchTo().frame(frame);
                    printAllFrames(driver, prefix + "  ", depth + 1);
                    driver.switchTo().parentFrame();
                }
            } catch (Exception e) {
                System.out.println(prefix + "❌ Error while printing frames: " + e.getMessage());
            }
        }


        public static void setStateOrCityIE(WebDriver driver, String fieldName, String value) {
            try {
                String script = 
                    "var el = document.getElementsByName('" + fieldName + "')[0];" +
                    "if(el){ el.value = '" + value + "';" +
                    "var evt = document.createEvent('HTMLEvents');" +
                    "evt.initEvent('change', true, true);" +
                    "el.dispatchEvent(evt); }";
                ((JavascriptExecutor) driver).executeScript(script);
                System.out.println("✅ IE11 value set: " + fieldName + " = " + value);
            } catch (Exception e) {
                System.out.println("❌ Failed to set IE11 value: " + fieldName + " = " + value);
                e.printStackTrace();
            }
        }


        public static void setValueWithJS(WebDriver driver, WebElement element, String value) {
            String jsScript = 
                "var el = arguments[0];" +
                "el.value = arguments[1];" +
                "var evt1 = document.createEvent('HTMLEvents');" +
                "evt1.initEvent('input', true, false);" +
                "el.dispatchEvent(evt1);" +
                "var evt2 = document.createEvent('HTMLEvents');" +
                "evt2.initEvent('change', true, false);" +
                "el.dispatchEvent(evt2);";
            ((JavascriptExecutor) driver).executeScript(jsScript, element, value);
        }

        public static void selectDropdownWithJS(WebDriver driver, WebElement element, String value) {
            String jsScript =
                "var el = arguments[0];" +
                "for (var i = 0; i < el.options.length; i++) {" +
                "  if (el.options[i].text === arguments[1] || el.options[i].value === arguments[1]) {" +
                "    el.selectedIndex = i;" +
                "    var evt = document.createEvent('HTMLEvents');" +
                "    evt.initEvent('change', true, true);" +
                "    el.dispatchEvent(evt);" +
                "    break;" +
                "  }" +
                "}";
            ((JavascriptExecutor) driver).executeScript(jsScript, element, value);
        }
        public static void safeSelectOrInput(WebDriver driver, WebElement element, String value) {
            try {
                String tagName = element.getTagName();
                if ("select".equalsIgnoreCase(tagName)) {
                    new Select(element).selectByVisibleText(value);
                    System.out.println("✅ Selected from dropdown: " + value);
                } else if ("input".equalsIgnoreCase(tagName)) {
                    ((JavascriptExecutor) driver).executeScript("arguments[0].value = arguments[1];", element, value);
                    System.out.println("✅ Typed into input: " + value);
                } else {
                    System.out.println("⚠ Unsupported element type: " + tagName);
                }
            } catch (Exception e) {
                System.out.println("❌ Error in safeSelectOrInput: " + e.getMessage());
            }
        }
        public static String getAccountId(WebDriver driver) {
            // Possible XPaths where A/c. ID might appear
            String[] xpaths = {
                "//table[@class='innertable']//td[contains(text(),'A/c. ID')]",
                "//td[@class='subhdr' and contains(text(),'A/c. ID')]",
                "//table[@class='tableborder']//td[contains(text(),'A/c. ID')]"
            };

            for (String xpath : xpaths) {
                List<WebElement> elements = driver.findElements(By.xpath(xpath));
                if (!elements.isEmpty()) {
                    String tdText = elements.get(0).getText();
                    System.out.println("Full text found: " + tdText);

                    // Regex to extract just the number
                    Pattern pattern = Pattern.compile("A/c\\. ID[:\\s]*([0-9]+)");
                    Matcher matcher = pattern.matcher(tdText);

                    if (matcher.find()) {
                        return matcher.group(1); // return only the account ID
                    }
}
            }
			return null;
        }
        
        public static void captureErrors(WebDriver driver, int timeoutSeconds) {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));

            try {
                List<WebElement> errorTables = wait.until(
                    ExpectedConditions.visibilityOfAllElementsLocatedBy(By.cssSelector("table.errortableborder"))
                );

                for (WebElement table : errorTables) {
                    String tableText = table.getText().trim();
                    if (!tableText.isEmpty()) {
                        System.out.println("❌ Error Table Message due to:\n" + tableText);

                        try (FileWriter writer = new FileWriter("SubmitErrors.txt", true)) {
                            writer.write(tableText + System.lineSeparator() + System.lineSeparator());
                        } catch (IOException ioe) {
                            ioe.printStackTrace();
                        }
                    }
                }
            } catch (TimeoutException e) {
                System.out.println("✅ No error table found after Submit.");
            }
}
        
        public static void setValueWithJS1(WebDriver driver, WebElement element, String value) {
            JavascriptExecutor js = (JavascriptExecutor) driver;
            js.executeScript("arguments[0].value='';", element); // clear existing value
            js.executeScript("arguments[0].value=arguments[1];", element, value); // set new value
        }
        public static void selectDropdownWithJS1(WebDriver driver, WebElement element, String value) {
            JavascriptExecutor js = (JavascriptExecutor) driver;
            js.executeScript("arguments[0].value='';", element); // clear
            js.executeScript("arguments[0].value=arguments[1];", element, value); // set
            js.executeScript("arguments[0].dispatchEvent(new Event('change'));", element); // trigger change event
        }
        public static void setValueWithJS3(WebDriver driver, WebElement element, String value) {
            JavascriptExecutor js = (JavascriptExecutor) driver;

            // Clear existing
            js.executeScript("arguments[0].value='';", element);

            // Ensure correct date format dd/MM/yyyy
            String formattedValue = value.replace("-", "/"); // 08-08-2025 -> 08/08/2025

            // Set new value + trigger events
            js.executeScript(
                "arguments[0].value = arguments[1]; " +
                "arguments[0].dispatchEvent(new Event('input')); " +
                "arguments[0].dispatchEvent(new Event('change'));",
                element, formattedValue
            );
        }
        public static void safeClick(WebDriver driver, WebDriverWait wait, By locator) {
            for (int i = 0; i < 2; i++) {  // retry max 2 times
                try {
                    WebElement element = wait.until(ExpectedConditions.elementToBeClickable(locator));
                    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
                    return;
                } catch (StaleElementReferenceException e) {
                    System.out.println("⚠️ Retrying stale element: " + locator);
                }
            }
            throw new RuntimeException("❌ Could not click element: " + locator);
        }

        public static void popupop(WebDriver driver, WebDriverWait wait, By locator) {
            try {
                String mainWindow = driver.getWindowHandle();
                Set<String> allWindows = driver.getWindowHandles();

                boolean popupHandled = false;

                // 🔹 1. Try switching to popup window
                for (String handle : allWindows) {
                    if (!handle.equals(mainWindow)) {
                        driver.switchTo().window(handle);
                        System.out.println("🪟 Switched to popup window: " + handle);

                        popupHandled = clickAccept(driver);
                        break;
                    }
                }

                // 🔹 2. If no popup window, try inline dialog in the same window
                if (!popupHandled) {
                    System.out.println("ℹ️ No popup window found. Checking inside same window...");
                    popupHandled = clickAccept(driver);
                }

                // 🔹 3. Switch back to main window
                driver.switchTo().window(mainWindow);

                if (popupHandled)
                    System.out.println("✅ Accept button handled successfully.");
                else
                    System.out.println("⚠️ Accept button not detected in popup.");

            } catch (Exception popupEx) {
                System.out.println("❌ Error while handling popup: " + popupEx.getMessage());
            }
        }

		public static String checkForApplicationErrors(WebDriver driver) {
            WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(2));
            WebDriverWait longWait = new WebDriverWait(driver, Duration.ofSeconds(5));

            String callingClass = Thread.currentThread().getStackTrace()[2].getClassName();
            String simpleClassName = callingClass.substring(callingClass.lastIndexOf('.') + 1);

            String projectDir = System.getProperty("user.dir"); // C:\FinacleBanking\Bhatina_Reports
            String folderPath = projectDir + "\\ErrorLog";
            new File(folderPath).mkdirs();

            try {
                // ✅ Step 0: Handle modal alerts first
                try {
                    Alert alert = driver.switchTo().alert();
                    String alertText = alert.getText().trim();
                    alert.accept(); // close the modal
                    FileUtils.saveTextResult(folderPath, simpleClassName + "_Errors", alertText);
                    System.out.println("❌ Alert Found in " + simpleClassName + ": " + alertText);
                    return alertText;
                } catch (NoAlertPresentException ignore) {
                    // no alert, continue
                }

                // ✅ Step 1: Common error locators
                List<By> errorLocators = Arrays.asList(
                    By.cssSelector("tr.alert td"),
                    By.xpath("//a[contains(text(), '-') and contains(@onclick,'fnSelectField')]"),
                    By.id("anc1"),
                    By.xpath("//span[contains(@class,'error') or contains(@id,'error') or contains(text(),'Error')]"),
                    By.cssSelector("table.errortableborder")
                    
                );

                for (By locator : errorLocators) {
                    try {
                        WebElement errorElement = shortWait.until(
                            ExpectedConditions.visibilityOfElementLocated(locator)
                        );

                        String errorText = errorElement.getText().trim();
                        if (errorText.isEmpty()) {
                            errorText = errorElement.getAttribute("innerText").trim();
                        }

                        if (!errorText.isEmpty()) {
                            FileUtils.saveTextResult(folderPath, simpleClassName + "_Errors", errorText);
                            System.out.println("❌ Error Found in " + simpleClassName + ": " + errorText);
                            return errorText;
                        }
                    } catch (TimeoutException ignore) {
                        // continue if not found
                    }
                }

                // ✅ Step 2: Verify navigation success
                try {
                    longWait.until(ExpectedConditions.or(
                        ExpectedConditions.presenceOfElementLocated(By.id("Submit")),
                        ExpectedConditions.presenceOfElementLocated(By.xpath("//input[@value='Submit' or @value='Ok']"))
                    ));
                    System.out.println("✅ Navigation successful — next page loaded after action.");
                } catch (TimeoutException e) {
                    String failMsg = "Page did not load properly after action.";
                    FileUtils.saveTextResult(folderPath, simpleClassName + "_Errors", failMsg);
                    System.out.println("❌ " + failMsg);
                    return failMsg;
                }

                System.out.println("✅ No error message found, proceeding with test.");

            } catch (UnhandledAlertException e) {
                // ✅ Fallback: in case Selenium throws this before switching to alert
                try {
                    String alertText = driver.switchTo().alert().getText().trim();
                    driver.switchTo().alert().accept();
                    FileUtils.saveTextResult(folderPath, simpleClassName + "_Errors", alertText);
                    System.out.println("❌ Alert Found in " + simpleClassName + ": " + alertText);
                    return alertText;
                } catch (Exception ex) {
                    System.err.println("⚠️ Could not read alert text.");
                }
            } catch (Exception e) {
                String msg = "⚠️ Unexpected issue while checking for errors: " + e.getMessage();
                FileUtils.saveTextResult(folderPath, simpleClassName + "_Errors", msg);
                System.err.println(msg);
                return msg;
            }

            return null; // No errors found
        }
//        public static void logSuccess(WebDriver driver) {
//            WebDriverWait longWait = new WebDriverWait(driver, Duration.ofSeconds(5));
//
//            // 🔹 Get calling class name dynamically
//            String callingClass = Thread.currentThread().getStackTrace()[2].getClassName();
//            String simpleClassName = callingClass.substring(callingClass.lastIndexOf('.') + 1);
//
//            // 🔹 Define SuccessLog path inside your project
//            String projectDir = System.getProperty("user.dir"); // e.g. C:\FinacleBanking\Bhatina_Reports
//            String successFolder = projectDir + "\\SuccessLog";
//
//            // 🔹 Create folder if not exists
//            new File(successFolder).mkdirs();
//
//            try {
//                // Wait for next page or confirmation elements to load
//                longWait.until(ExpectedConditions.or(
//                    ExpectedConditions.presenceOfElementLocated(By.id("Submit")),
//                    ExpectedConditions.presenceOfElementLocated(By.xpath("//input[@value='Submit' or @value='Ok']"))
//                ));
//                
//                String successMsg = "✅ " + simpleClassName + " executed successfully — next page loaded after action.";
//                FileUtils.saveTextResult(successFolder, simpleClassName, successMsg);
//                System.out.println(successMsg);
//
//            } catch (TimeoutException e) {
//            	  WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
//            	 WebElement innerTable = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("table.innertable")));
//                 String tableText = innerTable.getText().trim();
//                 System.out.println(tableText);
//                String failMsg = "⚠️ " + simpleClassName + " — executed successfully.";
//                FileUtils.saveTextResult(successFolder, simpleClassName, failMsg);
//                System.out.println(failMsg);
//            } catch (Exception e) {
//                String msg = "⚠️ Unexpected issue while logging success: " + e.getMessage();
//                FileUtils.saveTextResult(successFolder, simpleClassName, msg);
//                System.err.println(msg);
//            }
//        }
//        public static String checkForSuccessElements(WebDriver driver) {
//            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
//            String callingClass = Thread.currentThread().getStackTrace()[2].getClassName();
//            String simpleClassName = callingClass.substring(callingClass.lastIndexOf('.') + 1);
//            String projectDir = System.getProperty("user.dir");
//            String folderPath = projectDir + "\\SuccessLog";
//            new File(folderPath).mkdirs();
//
//            try {
//                // List of success locators (IDs, XPaths, CSS)
//                List<By> successLocators = Arrays.asList(
//                    By.cssSelector("table.innertable"),
//                    By.xpath("//label[@id='tranId']"),
//                    By.id("Submit"),
//                    By.cssSelector("tr.textfielddisplaylabel1")
//                );
//
//                StringBuilder successText = new StringBuilder();
//
//                for (By locator : successLocators) {
//                    try {
//                        WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
//                        String text = element.getText().trim();
//                        if (text.isEmpty()) {
//                            text = element.getAttribute("innerText").trim();
//                        }
//                        if (!text.isEmpty()) {
//                            successText.append(text).append("\n");
//                        }
//                    } catch (TimeoutException ignore) {
//                        // Element not present, skip
//                    }
//                }
//
//                String finalText = successText.toString().trim();
//                if (!finalText.isEmpty()) {
//                    FileUtils.saveTextResult(folderPath, simpleClassName, finalText);
//                    System.out.println("✅ Success logged for " + simpleClassName + ": " + finalText);
//                    return finalText;
//                } else {
//                    System.out.println("⚠️ No success elements found for " + simpleClassName);
//                }
//
//            } catch (Exception e) {
//                System.err.println("⚠️ Unexpected issue while logging success: " + e.getMessage());
//                FileUtils.saveTextResult(folderPath, simpleClassName, "Unexpected issue: " + e.getMessage());
//                return e.getMessage();
//            }
//
//            return null;
//        }


		public static String checkForSuccessElements(WebDriver driver) {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
            String callingClass = Thread.currentThread().getStackTrace()[2].getClassName();
            String simpleClassName = callingClass.substring(callingClass.lastIndexOf('.') + 1);
            String projectDir = System.getProperty("user.dir");
            String folderPath = projectDir + "\\SuccessLog";
            new File(folderPath).mkdirs();

            try {
                // List of success locators
                List<By> successLocators = Arrays.asList(
                    By.cssSelector("table.innertable"),
                    By.xpath("//label[@id='tranId']"),
                    By.id("Submit"),
                    By.cssSelector("tr.textfielddisplaylabel1")
                );

                Set<String> uniqueText = new LinkedHashSet<>(); // stores only unique lines

                for (By locator : successLocators) {
                    try {
                        WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
                        String text = element.getText().trim();
                        if (text.isEmpty()) {
                            text = element.getAttribute("innerText").trim();
                        }
                        if (!text.isEmpty()) {
                            // Split by line breaks and add each line to Set (removes duplicates)
                            String[] lines = text.split("\\r?\\n");
                            for (String line : lines) {
                                uniqueText.add(line.trim());
                            }
                        }
                    } catch (TimeoutException ignore) {
                        // skip if element not found
                    }
                }

                String finalText = String.join("\n", uniqueText); // join unique lines
                if (!finalText.isEmpty()) {
                    FileUtils.saveTextResult(folderPath, simpleClassName, finalText);
                    System.out.println("✅ Success logged for " + simpleClassName + ":\n" + finalText);
                    return finalText;
                } else {
                    System.out.println("⚠️ No success elements found for " + simpleClassName);
                }

            } catch (Exception e) {
                System.err.println("⚠️ Unexpected issue while logging success: " + e.getMessage());
                FileUtils.saveTextResult(folderPath, simpleClassName, "Unexpected issue: " + e.getMessage());
                return e.getMessage();
            }

            return null;
        }

        public static void handleExpectedMessage(WebDriver driver, String simpleClassName, String message) {
            if (message == null || message.isEmpty()) {
                return; // Nothing to process
            }

            List<String> expectedMessages = Arrays.asList(
                "257 - The account creation is not yet authorized"
                // You can add more expected messages here
            );

            boolean isExpected = expectedMessages.stream()
                    .anyMatch(msg -> message.toLowerCase().contains(msg.toLowerCase()));

            if (isExpected) {
                // ✅ Print in green
                String GREEN = "\u001B[32m";
                String RESET = "\u001B[0m";
                System.out.println(GREEN + "🟢 " + simpleClassName + " PASSED: " + message + RESET);

                // ✅ Save separately
                try {
                    String projectDir = System.getProperty("user.dir");
                    String folderPath = projectDir + "\\ExpectedMessageLog";
                    new File(folderPath).mkdirs();

                    FileUtils.saveTextResult(folderPath, simpleClassName + "_ExpectedMessage", message);
                } catch (Exception e) {
                    System.err.println("⚠️ Failed to save expected message: " + e.getMessage());
                }
            } else {
                // ❌ Not expected, you can log or throw if needed
                System.out.println("⚠️ Message is not in expected list: " + message);
            }
        }

        private static boolean clickAccept(WebDriver driver) {
            WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(5));
            JavascriptExecutor js = (JavascriptExecutor) driver;

            try {
                // 🔹 Try locating Accept button by ID first
                WebElement acceptBtn = null;
                try {
                    acceptBtn = shortWait.until(ExpectedConditions.presenceOfElementLocated(By.id("accept")));
                } catch (Exception e1) {
                    // Try fallback XPath
                    acceptBtn = shortWait.until(ExpectedConditions.presenceOfElementLocated(
                            By.xpath("//input[contains(@value,'Accept') or contains(@id,'accept')]")));
                }

                // 🔹 Bring it into view and click
                js.executeScript("arguments[0].scrollIntoView(true);", acceptBtn);
                WindowHandle.slowDown(1);

                try {
                    js.executeScript("arguments[0].click();", acceptBtn);
                    System.out.println("✅ Accept button clicked via JavaScript.");
                } catch (Exception e2) {
                    System.out.println("⚠️ JS click failed, trying native click...");
                    acceptBtn.click();
                }

                return true;

            } catch (Exception e) {
                System.out.println("⚠️ Accept button not found, using Robot fallback...");
                try {
                    Robot robot = new Robot();
                    robot.setAutoDelay(700);
                    robot.keyPress(KeyEvent.VK_TAB);
                    robot.keyRelease(KeyEvent.VK_TAB);
                    robot.keyPress(KeyEvent.VK_ENTER);
                    robot.keyRelease(KeyEvent.VK_ENTER);
                    System.out.println("🤖 Accept clicked using Robot fallback.");
                    return true;
                } catch (Exception e2) {
                    System.out.println("❌ Robot fallback failed: " + e2.getMessage());
                }
            }

            return false;
        }
        
        public static boolean HandlePopupForAccept(WebDriver driver) {
            String mainWindow = driver.getWindowHandle(); // store main window handle

            try {
            	// WindowHandle.HandlePopupForAccept(driver);
                WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(3)); // short wait
                WebElement acceptButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("accept")));

                // Scroll into view and click using JS to avoid click issues
                ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].scrollIntoView(true); arguments[0].click();", acceptButton
                );

                System.out.println("✅ Popup accepted successfully.");
                
                // Switch back to main window just in case
                driver.switchTo().window(mainWindow);

                return true; // popup was found and handled

            } catch (TimeoutException e) {
                // No popup appeared
                System.out.println("No popup detected, continuing...");
                return false;
            } catch (NoSuchElementException e) {
                // Accept button not found
                System.out.println("Popup accept button not found, continuing...");
                return false;
            } catch (StaleElementReferenceException e) {
                // Element got refreshed before we could click
                System.out.println("Popup appeared but element became stale. Retrying...");
              //  return HandlePopupIfExists(driver,Duration.ofSeconds(5))); // retry once
                return HandlePopupIfExists(driver, Duration.ofSeconds(5)) || HandlePopupIfExists(driver, Duration.ofSeconds(5));

            }
        }


}
