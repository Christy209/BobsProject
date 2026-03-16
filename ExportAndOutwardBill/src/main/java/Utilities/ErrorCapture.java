package Utilities;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ErrorCapture {

    private static final Logger logger = LogManager.getLogger(ErrorCapture.class);
    static WebDriver driver;
    private static final List<String> EXPECTED_ERRORS =
            Arrays.asList("E4221");
    // ------------------- Method 1 -------------------
    public static void checkForError() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));

        try {
            WebElement errorElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("tr.alert td")));
            String errorText = errorElement.getText().trim();
            logger.error("Error Found: " + errorText);
            Assert.fail("Test failed due to application error: " + errorText);

        } catch (Exception e) {
            logger.info("No error message found, proceeding with test.");
        }
    }

    // ------------------- Method 2 -------------------
    public static String checkForApplicationErrors(WebDriver driver) {
        WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(2));
        WebDriverWait longWait = new WebDriverWait(driver, Duration.ofSeconds(5));

        try {
            List<By> errorLocators = Arrays.asList(
                    By.cssSelector("tr.alert td"),
                    By.xpath("//a[contains(text(), '-') and contains(@onclick,'fnSelectField')]"),
                    By.id("anc1"),
                    By.xpath("//span[contains(@class,'error') or contains(@id,'error') or contains(text(),'Error')]")
            );

            for (By locator : errorLocators) {
                try {
                    WebElement errorElement = shortWait.until(ExpectedConditions.visibilityOfElementLocated(locator));
                    String errorText = errorElement.getText().trim();
                    if (errorText.isEmpty()) {
                        errorText = errorElement.getAttribute("innerText").trim();
                    }
                    if (!errorText.isEmpty()) {
                        logger.error("❌ Error Found: " + errorText);
                        return errorText;
                    }
                } catch (TimeoutException ignore) {}
            }

            try {
                longWait.until(ExpectedConditions.or(
                        ExpectedConditions.presenceOfElementLocated(By.id("Submit")),
                        ExpectedConditions.presenceOfElementLocated(By.xpath("//input[@value='Submit' or @value='Ok']"))
                ));
                logger.info("✅ Navigation successful — next page loaded after action.");
            } catch (TimeoutException e) {
                String additionalError = "";
                try {
                    WebElement msg = driver.findElement(By.xpath("//tr[contains(@class,'alert')]/td | //span[contains(@class,'error') or contains(@id,'error')]"));
                    additionalError = msg.getText().trim();
                    if (additionalError.isEmpty()) {
                        additionalError = msg.getAttribute("innerText").trim();
                    }
                } catch (Exception ignored2) {}

                String failMsg = additionalError.isEmpty() ? "Page did not load properly after action." : additionalError;
                logger.error("❌ " + failMsg);
                return failMsg;
            }

            logger.info("✅ No error message found, proceeding with test.");

        } catch (Exception e) {
            String msg = "⚠️ Unexpected issue while checking for errors: " + e.getMessage();
            logger.error(msg, e);
            return msg;
        }

        return null;
    }

    // ------------------- Method 3 -------------------
    public static String checkApplicationErrors(WebDriver driver) {
        WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(2));
        WebDriverWait normalWait = new WebDriverWait(driver, Duration.ofSeconds(6));
        String detectedError = null;

        try {
            List<By> errorLocators = Arrays.asList(
                    By.cssSelector("tr.alert td"),
                    By.xpath("//a[contains(text(), '-') and contains(@onclick,'fnSelectField')]"),
                    By.id("anc1"),
                    By.xpath("//span[contains(@class,'error') or contains(@id,'error') or contains(text(),'Error')]")
            );

            for (By locator : errorLocators) {
                try {
                    WebElement errorElement = shortWait.until(ExpectedConditions.visibilityOfElementLocated(locator));
                    String errorText = errorElement.getText().trim();
                    if (errorText.isEmpty()) {
                        errorText = errorElement.getAttribute("innerText").trim();
                    }
                    if (!errorText.isEmpty()) {
                        detectedError = errorText;
                        logger.warn("⚠️ Error message detected: " + errorText);
                        break;
                    }
                } catch (TimeoutException ignore) {}
            }

            boolean pageLoaded = false;
            try {
                normalWait.until(ExpectedConditions.or(
                        ExpectedConditions.presenceOfElementLocated(By.id("Submit")),
                        ExpectedConditions.presenceOfElementLocated(By.xpath("//input[@value='Submit' or @value='Ok']"))
                ));
                pageLoaded = true;
            } catch (TimeoutException e) {
                if (detectedError == null) {
                    logger.info("⏳ Finacle slow — waiting extra 10 seconds...");
                    WebDriverWait extraWait = new WebDriverWait(driver, Duration.ofSeconds(10));
                    try {
                        extraWait.until(ExpectedConditions.or(
                                ExpectedConditions.presenceOfElementLocated(By.id("Submit")),
                                ExpectedConditions.presenceOfElementLocated(By.xpath("//input[@value='Submit' or @value='Ok']"))
                        ));
                        pageLoaded = true;
                        logger.info("✅ Page loaded after extra delay.");
                    } catch (TimeoutException e2) {
                        pageLoaded = false;
                    }
                }
            }
            if (detectedError != null && !pageLoaded) {

                // ✅ Check if this is an expected business error
                for (String expected : EXPECTED_ERRORS) {
                    if (detectedError.contains(expected)) {
                        logger.warn("⚠️ Expected business error detected: " + detectedError);
                        return detectedError; // let test case decide
                    }
                }

                // ❌ Genuine failure
                logger.error("❌ Error found and page stuck — failing.");
                return detectedError;
            }

            if (detectedError != null && !pageLoaded) {
                logger.error("❌ Error found and page stuck — failing.");
                return detectedError;
            } else if (detectedError != null && pageLoaded) {
                logger.warn("⚠️ Error found but page loaded — continuing test.");
                return null;
            } else if (detectedError == null && !pageLoaded) {
                logger.error("❌ No visible error, but page didn't load even after waiting.");
                return "Page stuck or unresponsive — Finacle timeout.";
            } else {
                logger.info("✅ No error and page loaded successfully — continuing fast.");
                return null;
            }

        } catch (Exception e) {
            String msg = "⚠️ Unexpected issue while checking for errors: " + e.getMessage();
            logger.error(msg, e);
            return msg;
        }
    }
    public static void printBillDetails(WebDriver driver) {
        try {
            // Read Bill ID and Bill Type from UI
            String uiBillId = MIIBUIReader.getBillId(driver);
            String uiBillType = MIIBUIReader.getBillType(driver);

            // Log them
            if (uiBillId != null && !uiBillId.isEmpty()) {
                System.out.println("🔹 UI Bill ID   : " + uiBillId);
            } else {
                System.out.println("⚠️ UI Bill ID not found.");
            }

            if (uiBillType != null && !uiBillType.isEmpty()) {
                System.out.println("🔹 UI Bill Type : " + uiBillType);
            } else {
                System.out.println("⚠️ UI Bill Type not found.");
            }

        } catch (Exception e) {
            System.out.println("⚠️ Failed to read Bill details: " + e.getMessage());
        }
    }

    // ------------------- Method 4 -------------------
    public static String CheckingUIErrors(WebDriver driver) {
        WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(2));
        WebDriverWait normalWait = new WebDriverWait(driver, Duration.ofSeconds(6));
        String detectedError = null;
        boolean pageLoaded = false;

        try {
            List<By> messageLocators = Arrays.asList(
                    By.cssSelector("tr.alert td"),
                    By.xpath("//a[contains(text(), '-') and contains(@onclick,'fnSelectField')]"),
                    By.id("anc1"),
                    By.xpath("//span[contains(@class,'error') or contains(@id,'error') or contains(text(),'Error')]")
            );

            for (By locator : messageLocators) {
                try {
                    WebElement msgElement = shortWait.until(ExpectedConditions.visibilityOfElementLocated(locator));
                    String msgText = msgElement.getText().trim();
                    if (msgText.isEmpty()) msgText = msgElement.getAttribute("innerText").trim();
                    if (!msgText.isEmpty()) {
                        detectedError = msgText;
                        logger.info("🔹 Message detected: " + detectedError);
                        break;
                    }
                } catch (TimeoutException ignore) {}
            }

            try {
                normalWait.until(ExpectedConditions.or(
                        ExpectedConditions.presenceOfElementLocated(By.id("Submit")),
                        ExpectedConditions.presenceOfElementLocated(By.xpath("//input[@value='Submit' or @value='Ok']"))
                ));
                pageLoaded = true;
            } catch (TimeoutException e) {
                pageLoaded = false;
            }

            if (detectedError != null && !pageLoaded) {
                logger.error("❌ Error detected and page stuck — failing test.");
                return detectedError;
            } else {
                logger.info("✅ Either no error or page is moving — continuing test.");
                return null;
            }

        } catch (Exception e) {
            String msg = "⚠️ Unexpected issue while checking for errors: " + e.getMessage();
            logger.error(msg, e);
            return msg;
        }
    }
    public static String checkForApplicationError(WebDriver driver) {

        try {

            List<By> errorLocators = Arrays.asList(

                    // Existing ones
                    By.cssSelector("tr.alert td"),
                    By.xpath("//a[contains(text(), '-') and contains(@onclick,'fnSelectField')]"),
                    By.id("anc1"),
                    By.xpath("//span[contains(@class,'error') or contains(@id,'error') or contains(text(),'Error')]"),

                    // FINACLE TAB ERROR SECTION
                    By.xpath("//*[contains(text(),'Error Details')]/ancestor::table"),
                    By.xpath("//*[contains(text(),'This tab contains errors')]"),
                    By.xpath("//*[contains(text(),'Error Details')]/ancestor::table//td[contains(@style,'#FF') or contains(@color,'red') or contains(text(),'error') or contains(text(),'Error')]"),
                    By.xpath("//td[contains(text(),'Amendment Details')]/following-sibling::td[contains(text(),'error') or contains(text(),'Error')]"),
                    By.xpath("//td[contains(text(),'This tab contains errors')]"),
                    By.xpath("//td[contains(text(),'contains errors')]"),
                    By.xpath("//td[contains(text(),'error') and not(contains(text(),'Error Details'))]"),
                    By.xpath("//tr[@class='alert']//td[contains(., 'nothing to verify or cancel')]"),
                    By.xpath("//tr[contains(@class,'alert')]//a[contains(text(),'E4221')]"),
                    By.xpath("//tr[contains(@class,'alert')]//a[contains(text(),'G69')]")

            );

            JavascriptExecutor js = (JavascriptExecutor) driver;

            String foundError = null;

            for (By locator : errorLocators) {
                try {
                    String errorText = null;

                    if (locator.toString().startsWith("By.cssSelector") || locator.toString().startsWith("By.id")) {
                        List<WebElement> elements = driver.findElements(locator);
                        for (WebElement el : elements) {
                            if (el.isDisplayed()) {
                                errorText = el.getText().trim();
                                if (errorText.isEmpty()) {
                                    errorText = el.getAttribute("innerText");
                                    if (errorText != null) errorText = errorText.trim();
                                }
                            }
                        }
                    } else {
                        String xpath = locator.toString().replace("By.xpath: ", "");
                        String script =
                                "var xpath = arguments[0];" +
                                "var result = document.evaluate(xpath, document, null, XPathResult.ORDERED_NODE_SNAPSHOT_TYPE, null);" +
                                "for (var i = 0; i < result.snapshotLength; i++) {" +
                                "  var el = result.snapshotItem(i);" +
                                "  if (el && el.offsetParent !== null) {" +
                                "    var txt = el.innerText.trim();" +
                                "    if (txt) return txt;" +
                                "  }" +
                                "}" +
                                "return null;";

                        errorText = (String) js.executeScript(script, xpath);
                    }

                    if (errorText != null && !errorText.trim().isEmpty()) {
                        foundError = errorText;   // 🔥 STORE BUT DO NOT RETURN YET
                        logger.warn("⚠️ Error detected on page: " + foundError);
                        break;
                    }

                } catch (Exception ignored) {}
            }


         // ===== FINACLE ENTERPRISE NAVIGATION =====
            try {

                WebDriverWait navWait = new WebDriverWait(driver, Duration.ofSeconds(12));

                navWait.until(driver1 -> {

                    // 1️⃣ Business error appeared → stop waiting
                    if (!driver1.findElements(By.xpath("//tr[contains(@class,'alert')]")).isEmpty())
                        return true;

                    if (!driver1.findElements(By.xpath("//span[contains(@class,'error')]")).isEmpty())
                        return true;

                    // 2️⃣ Loading overlay/spinner gone (Finacle rendering finished)
                    List<WebElement> loaders = driver1.findElements(
                            By.xpath("//*[contains(@class,'loading') or contains(@id,'progress')]")
                    );
                    if (!loaders.isEmpty()) return false;

                    // 3️⃣ Any editable field visible → page usable
                    List<WebElement> inputs = driver1.findElements(
                            By.xpath("//input[not(@type='hidden')]")
                    );

                    return inputs.stream().anyMatch(WebElement::isDisplayed);
                });

                // 🔴 Check for business error after load
                List<WebElement> errors = driver.findElements(By.xpath(
                        "//tr[contains(@class,'alert')]//a | //span[contains(@class,'error')]"
                ));

                if (!errors.isEmpty()) {
                    String errorText = errors.get(0).getText().trim();
                    logger.error("❌ Business Error Detected: " + errorText);
                    return errorText;
                }

                logger.info("✅ Screen loaded successfully. Proceeding.");

            } catch (TimeoutException e) {
                logger.error("❌ Application stuck — neither loaded nor error appeared.");
                return "Application stuck after action.";
            }

        } catch (Exception e) {
            String msg = "⚠️ Unexpected issue while checking for errors: " + e.getMessage();
            logger.error(msg, e);
            return msg;
        }
		return null;
    }
    public static String checkForApplicationError11(WebDriver driver) {

//        JavascriptExecutor js = (JavascriptExecutor) driver;
//
//        try {
//            // ⚡ FAST GLOBAL ERROR SCAN (single DOM pass)
//            String script =
//                    "var xpaths = [" +
//                            "'//tr[contains(@class,\"alert\")]//td'," +
//                            "'//a[contains(text(),\"-\") and contains(@onclick,\"fnSelectField\")]'," +
//                            "'//*[@id=\"anc1\"]'," +
//                            "'//span[contains(@class,\"error\") or contains(@id,\"error\") or contains(text(),\"Error\")]'," +
//                            "'//*[contains(text(),\"Error Details\")]/ancestor::table'," +
//                            "'//*[contains(text(),\"This tab contains errors\")]'," +
//                            "'//td[contains(text(),\"contains errors\")]'," +
//                            "'//tr[@class=\"alert\"]//td[contains(.,\"nothing to verify or cancel\")]'," +
//                            "'//tr[contains(@class,\"alert\")]//a[contains(text(),\"E4221\")]'," +
//                            "'//tr[contains(@class,\"alert\")]//a[contains(text(),\"G69\")]'" +
//                    "];" +
//
//                    "for (var i=0;i<xpaths.length;i++){" +
//                    " var result=document.evaluate(xpaths[i],document,null,XPathResult.ORDERED_NODE_SNAPSHOT_TYPE,null);" +
//                    " for (var j=0;j<result.snapshotLength;j++){" +
//                    "   var el=result.snapshotItem(j);" +
//                    "   if(el && el.offsetParent!==null){" +
//                    "     var txt=(el.innerText||'').trim();" +
//                    "     if(txt.length>2) return txt;" +
//                    "   }" +
//                    " }" +
//                    "}" +
//                    "return null;";
//
//            String errorText = (String) js.executeScript(script);
//
//            if (errorText != null) {
//                logger.warn("⚠️ Error detected on page: " + errorText);
//                return errorText;
//            }
//
//            // ===== FINACLE ENTERPRISE LOAD CHECK (FAST) =====
//            WebDriverWait navWait = new WebDriverWait(driver, Duration.ofSeconds(10));
//
//            navWait.until(d -> (Boolean) js.executeScript(
//                    "return (" +
//                            "document.querySelectorAll('tr.alert').length>0 ||" +
//                            "document.querySelectorAll('span.error').length>0 ||" +
//                            "document.querySelectorAll('input:not([type=hidden])').length>0" +
//                            ") && !document.querySelector('[class*=loading],[id*=progress]');"
//            ));
//
//            // 🔴 Post-load error verification (very quick)
//            List<WebElement> errors = driver.findElements(By.xpath(
//                    "//tr[contains(@class,'alert')]//a | //span[contains(@class,'error')]"
//            ));
//
//            if (!errors.isEmpty()) {
//                String msg = errors.get(0).getText().trim();
//                logger.error("❌ Business Error Detected: " + msg);
//                return msg;
//            }
//
//            logger.info("✅ Screen loaded successfully. Proceeding.");
//            return null;
//
//        } catch (TimeoutException e) {
//            logger.error("❌ Application stuck — neither loaded nor error appeared.");
//            return "Application stuck after action.";
//        } catch (Exception e) {
//            String msg = "⚠️ Unexpected issue while checking for errors: " + e.getMessage();
//            logger.error(msg, e);
//            return msg;
//        }
//    }


        try {

            List<By> errorLocators = Arrays.asList(
                    By.cssSelector("tr.alert td"),
                    By.xpath("//a[contains(text(), '-') and contains(@onclick,'fnSelectField')]"),
                    By.id("anc1"),
                    By.xpath("//span[contains(@class,'error') or contains(@id,'error') or contains(text(),'Error')]"),
                    By.xpath("//*[contains(text(),'Error Details')]/ancestor::table"),
                    By.xpath("//*[contains(text(),'This tab contains errors')]"),
                    By.xpath("//*[contains(text(),'Error Details')]/ancestor::table//td[contains(@style,'#FF') or contains(@color,'red') or contains(text(),'error') or contains(text(),'Error')]"),
                    By.xpath("//td[contains(text(),'Amendment Details')]/following-sibling::td[contains(text(),'error') or contains(text(),'Error')]"),
                    By.xpath("//td[contains(text(),'This tab contains errors')]"),
                    By.xpath("//td[contains(text(),'contains errors')]"),
                    By.xpath("//td[contains(text(),'error') and not(contains(text(),'Error Details'))]"),
                    By.xpath("//tr[@class='alert']//td[contains(., 'nothing to verify or cancel')]"),
                    By.xpath("//tr[contains(@class,'alert')]//a[contains(text(),'E4221')]"),
                    By.xpath("//tr[contains(@class,'alert')]//a[contains(text(),'G69')]")
            );

            JavascriptExecutor js = (JavascriptExecutor) driver;
            String foundError = null;

            for (By locator : errorLocators) {
                try {
                    String errorText = null;

                    // 🔹 Faster CSS/ID handling
                    if (locator.toString().startsWith("By.cssSelector") || locator.toString().startsWith("By.id")) {
                        List<WebElement> elements = driver.findElements(locator);

                        for (WebElement el : elements) {
                            errorText = (String) js.executeScript(
                                    "var e=arguments[0];" +
                                    "if(e && e.offsetParent!==null) return (e.innerText||'').trim();" +
                                    "return null;", el);

                            if (errorText != null && !errorText.isEmpty()) break;
                        }

                    } else {
                        // 🔹 Faster XPath handling
                        String xpath = locator.toString().replace("By.xpath: ", "");
                        errorText = (String) js.executeScript(
                                "var r=document.evaluate(arguments[0],document,null,XPathResult.ORDERED_NODE_SNAPSHOT_TYPE,null);" +
                                "for(var i=0;i<r.snapshotLength;i++){" +
                                " var el=r.snapshotItem(i);" +
                                " if(el && el.offsetParent!==null){" +
                                "  var t=(el.innerText||'').trim();" +
                                "  if(t) return t;" +
                                " }" +
                                "}" +
                                "return null;", xpath);
                    }

                    if (errorText != null && !errorText.isEmpty()) {
                        foundError = errorText;
                        logger.warn("⚠️ Error detected on page: " + foundError);
                        break;
                    }

                } catch (Exception ignored) {}
            }

            // ===== FINACLE ENTERPRISE NAVIGATION =====
            try {

                WebDriverWait navWait = new WebDriverWait(driver, Duration.ofSeconds(12));

                navWait.until(driver1 -> {

                    if (!driver1.findElements(By.xpath("//tr[contains(@class,'alert')]")).isEmpty())
                        return true;

                    if (!driver1.findElements(By.xpath("//span[contains(@class,'error')]")).isEmpty())
                        return true;

                    List<WebElement> loaders = driver1.findElements(
                            By.xpath("//*[contains(@class,'loading') or contains(@id,'progress')]")
                    );
                    if (!loaders.isEmpty()) return false;

                    List<WebElement> inputs = driver1.findElements(
                            By.xpath("//input[not(@type='hidden')]")
                    );

                    return inputs.stream().anyMatch(WebElement::isDisplayed);
                });

                List<WebElement> errors = driver.findElements(By.xpath(
                        "//tr[contains(@class,'alert')]//a | //span[contains(@class,'error')]"
                ));

                if (!errors.isEmpty()) {
                    String errorText = errors.get(0).getText().trim();
                    logger.error("❌ Business Error Detected: " + errorText);
                    return errorText;
                }

                logger.info("✅ Screen loaded successfully. Proceeding.");

            } catch (TimeoutException e) {
                logger.error("❌ Application stuck — neither loaded nor error appeared.");
                return "Application stuck after action.";
            }

        } catch (Exception e) {
            String msg = "⚠️ Unexpected issue while checking for errors: " + e.getMessage();
            logger.error(msg, e);
            return msg;
        }

        return null;
    }
}
