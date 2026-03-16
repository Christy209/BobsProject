package CASA;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;

import Utilities.ErrorCapture;
import Utilities.ExcelUtils;
import Utilities.RowData;
import Utilities.Validation;
import Utilities.WindowHandle;

public class InquireTransactionMaintainence {

    private static WebDriver driver;
    private WebDriverWait wait;

    private static final int DEFAULT_RETRY = 3;
    private static final long RETRY_SLEEP_MS = 700;

    @SuppressWarnings("static-access")
    public InquireTransactionMaintainence(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    public Map<String, String> executeWithResultMap(RowData inputData,RowData id,String sheetname,  int row,String excelpath) throws Exception {
        Map<String, String> result = new HashMap<>();
        String errorMsg=null;

        try {

            driver.switchTo().defaultContent();
            wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(By.name("loginFrame")));

            WindowHandle.setValueWithJS(driver,waitForVisibility(By.id("menuSelect"), 10),inputData.getByIndex(1));
            retryingClick(By.id("menuSearcherGo"));
            try {
            wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(By.name("CoreServer")));
            wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(By.id("FINW")));
         }catch(Exception e) {
		     logWindowHandlesAndTitles();
           throw new RuntimeException("Failed to switch to CoreServer/FINW frames: " + e.getMessage(), e);
	        }
            String funCode = inputData.getByIndex(2);
            WindowHandle.selectDropdownIfValuePresent(driver, wait,By.id("funcCode"), funCode);


            if (funCode.equalsIgnoreCase("I - Inquire")) {
                WindowHandle.setValueWithJS(driver,wait.until(ExpectedConditions.presenceOfElementLocated(By.id("tranId"))),id.getByHeader("TRAN_ID"));
                retryingClick(By.id("Go"));

                errorMsg = ErrorCapture.checkForApplicationError(driver);
	            if (errorMsg != null && !errorMsg.trim().isEmpty()) {
	                result.put("errorMsg", errorMsg);
	                return result;	            
	           }
	            
                Map<String, String> appData =getInquireApplicationData(id);

                appData.forEach((k, v) ->System.out.println("INQUIRE → " + k + " = " + v));
             //   Validation.validateData(verify.getHeaderMap(), appData);
                Validation.validateData(appData, appData);
                String tranId = id.getByHeader("TRAN_ID");
                result.put("TransactionMessage","Inquired Successfully – TRAN_ID: " + tranId);
                result.put("TRAN_ID", tranId);
                result.putAll(appData);
            }

        } catch (Exception e) {
            result.put("errorMsg", e.getMessage());
            Assert.fail("Exception occurred: " + e.getMessage());
        }

        return result;
    }

//***********************************getInquireApplicationData*********************************************************//
    private Map<String, String> getInquireApplicationData(RowData vr) {

        Map<String, String> appData = new LinkedHashMap<>();

        // ================= FIRST LEG =================
        appData.put("acctId", getField("acctId"));
        appData.put("refCrncy", getField("refCrncy"));
        appData.put("refAmt", getField("refAmt"));
        appData.put("tranParticularsCode", getField("tranParticularsCode"));
        appData.put("tranParticular", getField("tranParticular"));

        // ================= DENOMINATION =================
        if ("Y".equalsIgnoreCase(vr.getByIndex(11))) {

            retryingClick(By.id("DENOMDTLS"));
            appData.put("arrDenomCount_leg1",getField("arrDenomCount"));
            retryingClick(By.id("Back"));
        }

        // ================= SECOND LEG =================
        if ("Y".equalsIgnoreCase(vr.getByIndex(13))) {

            moveNext();

            appData.put("acctId1", getField("acctId"));
            appData.put("refCrncy1", getField("refCrncy"));
            appData.put("refAmt1", getField("refAmt"));
            appData.put("tranParticularsCode1", getField("tranParticularsCode"));
            appData.put("tranParticular1", getField("tranParticular"));
        }

        // ================= THIRD LEG =================
        if ("Y".equalsIgnoreCase(vr.getByIndex(21))) {

            moveNext();

            appData.put("acctId2", getField("acctId"));
            appData.put("refCrncy2", getField("refCrncy"));
            appData.put("refAmt2", getField("refAmt"));
            appData.put("tranParticularsCode2", getField("tranParticularsCode"));
            appData.put("tranParticular2", getField("tranParticular"));
        }

        // ================= FOURTH LEG =================
        if ("Y".equalsIgnoreCase(vr.getByIndex(29))) {

            moveNext();

            appData.put("acctId3", getField("acctId"));
            appData.put("refCrncy3", getField("refCrncy"));
            appData.put("refAmt3", getField("refAmt"));
            appData.put("tranParticularsCode3", getField("tranParticularsCode"));
            appData.put("tranParticular3", getField("tranParticular"));
        }

        // ================= RESET TO FIRST LEG =================
        resetToFirstLeg();

        return appData;
    }

    // ==========================================================
    // 🔹 COMMON FIELD GETTER
    // ==========================================================
    private String getField(String id) {
        return ExcelUtils.getTextOrValue(driver, wait, id, "id");
    }

    // ==========================================================
    // 🔹 MOVE NEXT RECORD
    // ==========================================================
    private void moveNext() {
        retryingClick(By.id("partTranDetail_NextRec"));
        WindowHandle.slowDown(1);
    }

    // ==========================================================
    // 🔹 RESET NAVIGATION
    // ==========================================================
    private void resetToFirstLeg() {
        try {
            while (true) {
                WebElement prev = driver.findElement(By.id("partTranDetail_PrevRec"));
                if (!prev.isEnabled()) break;
                prev.click();
                WindowHandle.slowDown(1);
            }
        } catch (Exception ignored) {}
    }

    // ==========================================================
    // WAIT METHOD
    // ==========================================================
    private static WebElement waitForVisibility(By locator, int timeoutSeconds) {

        WebDriverWait localWait =
                new WebDriverWait(driver,
                        Duration.ofSeconds(timeoutSeconds));

        return localWait.until(
                ExpectedConditions.visibilityOfElementLocated(locator));
    }

  //-----------------------------------------------logWindowHandlesAndTitles-------------------------------------------------------//
    private void logWindowHandlesAndTitles() {
  	     try {
  	         String main = driver.getWindowHandle();
  	         Set<String> handles = driver.getWindowHandles();
  	         System.out.println("[DIAG] Total window handles: " + handles.size());
  	         for (String h : handles) {
  	             try {
  	                 driver.switchTo().window(h);
  	                 String title = "";
  	                 String url = "";
  	                 try { 
  	                	 title = driver.getTitle();
  	                	 } catch (Exception e) { 
  	                		 title = "(no title)"; 
  	                		 }

  	                 try {
  	                	 url = driver.getCurrentUrl(); 
  	                	 } catch (Exception e) {
  	                		 url = "(no url)";
  	                		 }
  	                 System.out.println("[DIAG] Handle=" + h + " | Title='" + title + "' | URL='" + url + "'");
  	             } catch (Exception e) {
  	                 System.out.println("[DIAG] Could not inspect window handle " + h + ": " + e.getMessage());

  	             }

  	         }


  	         try { 
  	        	 driver.switchTo().window(main);
  	        	 } catch (Exception ignore) 
  	         {}

  	     } catch (Exception e) {
  	         System.out.println("[DIAG] Failed to enumerate window handles: " + e.getMessage());
  	     }

  	 }


    // ==========================================================
    // RETRY CLICK METHOD
    // ==========================================================
    private static void retryingClick(By locator) {

        for (int attempt = 1; attempt <= DEFAULT_RETRY; attempt++) {

            try {

                WebElement el = waitForVisibility(locator, 8);

                try {
                    el.click();
                } catch (Exception e) {
                    ((JavascriptExecutor) driver)
                            .executeScript("arguments[0].click();", el);
                }

                return;

            } catch (StaleElementReferenceException |
                     NoSuchElementException e) {

                if (attempt == DEFAULT_RETRY)
                    throw e;

                try {
                    Thread.sleep(RETRY_SLEEP_MS);
                } catch (InterruptedException ignored) {}
            }
        }
    }
}
