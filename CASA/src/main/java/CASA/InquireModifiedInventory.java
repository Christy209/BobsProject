package CASA;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;

import Utilities.ErrorCapture;
import Utilities.ExcelUtils;
import Utilities.RowData;
import Utilities.Validation;
import Utilities.WindowHandle;
 
public class InquireModifiedInventory {

	 private static WebDriver driver;
	 private WebDriverWait wait;
	 
		private static final int DEFAULT_RETRY = 3;
		private static final long RETRY_SLEEP_MS = 700;
 
	    @SuppressWarnings("static-access")
		public InquireModifiedInventory(WebDriver driver) {
	        this.driver = driver;
	        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10)); 

	    }

	    @SuppressWarnings("unused")
		public Map<String, String> executeWithResultMap(RowData inputData, RowData verify, String sheetname, int row, String excelpath) throws Exception {
	        String mainWindowHandle = driver.getWindowHandle();
	        Map<String, String> result = new HashMap<>();
			String guaranteeNo = "";
	        String errorMsg  = "";

 
	        try {

	            driver.switchTo().defaultContent();
	            wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(By.name("loginFrame")));

	        } catch (Exception e) {
	            System.out.println("⚠️ Warning: could not switch to loginFrame before menuSelect: " + e.getMessage());
	        }

            WindowHandle.setValueWithJS(driver,wait.until(ExpectedConditions.presenceOfElementLocated(By.id("menuSelect"))),inputData.getByIndex(1));
            WebElement searchButton = driver.findElement(By.id("menuSearcherGo"));
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", searchButton);
 
	        try {
	            wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(By.name("CoreServer")));
	            wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(By.id("FINW")));

	            String FunCode = inputData.getByIndex(2);

	            if (FunCode.equalsIgnoreCase("A - Add")) {
	 	    	   WindowHandle.selectByVisibleText(wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("funcCode"))),inputData.getByIndex(2));    
	 	    	   }else {
	 	    		   WindowHandle.selectByVisibleText(wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("funcCode"))),inputData.getByIndex(2));    
	 	    		   WindowHandle.setValueWithJS(driver, waitForVisibility(By.id("invtTranId"), 8), verify.getByHeader("Tran_Id"));
	 	    	   }
	 	           retryingClick(By.id("Go"));
	            
	 	          errorMsg = ErrorCapture.checkForApplicationError(driver);
		            if (errorMsg != null && !errorMsg.trim().isEmpty()) {
		                result.put("errorMsg", errorMsg);
		                return result;	            
		           }
 
	            if(FunCode.equalsIgnoreCase("I - Inquire")){
	                 Map<String, String> appData = getInquireApplicationData(verify, driver, wait, sheetname, row, excelpath);
	                 Validation.validateData(inputData.getHeaderMap(), appData);  
	                 String Tran_id = verify.getByHeader("Tran_id");
	                 String successMsg = "Inquired Successfully – Documentary Credit No: " + Tran_id;
	                 result.put("LabelText", successMsg);   // 👈 THIS FIXES NULL
	                 result.put("Tran_id", Tran_id);            // keep DC No
	                 result.putAll(appData);

	             }
 
	        } catch (Exception e) {
	        	String exceptionMsg = "Exception occurred: " + e.getMessage();
	            result.put("errorMsg", errorMsg);
	            Assert.fail(exceptionMsg);
	        }

			return result;
 
	    }  

	    private static Map<String, String> getInquireApplicationData(RowData vr,WebDriver driver, WebDriverWait wait, String sheetname, int row, String excelpath) throws Exception {

	    	  Map<String, String> appData = new HashMap<>();
		       String funCode = vr.getByIndex(2);      
		       System.out.println(funCode);

		       WebElement label1 = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//label[@cmpfldcount='cmpfld8']")));
		       String uiValue1 = label1.getText().trim();
		       String excelValue1 = vr.getByIndex(3).trim();

		       System.out.println("UI Value (cmpfld8): " + uiValue1);
		       System.out.println("Excel Value (Index 3): " + excelValue1);

		       if (!uiValue1.equalsIgnoreCase(excelValue1)) {
		           throw new AssertionError("Mismatch in cmpfld8. Expected: "
		                   + excelValue1 + " but Found: " + uiValue1);
		       }

		       WebElement label2 = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//label[@cmpfldcount='cmpfld19']")));
		       String uiValue2 = label2.getText().trim();
		       String excelValue2 = vr.getByIndex(5).trim();

		       System.out.println("UI Value (cmpfld19): " + uiValue2);
		       System.out.println("Excel Value (Index 5): " + excelValue2);

		       if (!uiValue2.equalsIgnoreCase(excelValue2)) {
		           throw new AssertionError("Mismatch in cmpfld19. Expected: "
		                   + excelValue2 + " but Found: " + uiValue2);
		       }

		       WebElement label3 = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//label[@cmpfldcount='cmpfld13']")));
		       String value3 = label3.getText().trim();
		       System.out.println("Captured UI  Value of From Location code: " + value3);

		       WebElement label4 = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//label[@cmpfldcount='cmpfld26']")));
		       String value4 = label4.getText().trim();
		       System.out.println("Captured UI of To Location Code Value: " + value4);
		       System.out.println("✅ All validations of comparing excel and UI value Captured are  completed successfully.");
		       
		       appData.put("invtClass", ExcelUtils.getTextOrValue(driver, wait, "invtClass", "id"));
	           appData.put("invtType", ExcelUtils.getTextOrValue(driver, wait, "invtType", "id"));
	           appData.put("invtSrlAlpha", ExcelUtils.getTextOrValue(driver, wait, "invtSrlAlpha", "id"));
	           appData.put("invtBeginSrlNum", ExcelUtils.getTextOrValue(driver, wait, "invtBeginSrlNum", "id"));
	           appData.put("invtEndSrlNum", ExcelUtils.getTextOrValue(driver, wait, "invtEndSrlNum", "id"));
	           
	           retryingClick(By.id("sLnk1"));
	           
			   return appData;
		  }
	  //------------------------------------------waitForVisibility---------------------------------------------------------//

	    private static WebElement waitForVisibility(By locator, int timeoutSeconds) {
	    WebDriverWait localWait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
	    return localWait.until(ExpectedConditions.visibilityOfElementLocated(locator));
	    }

	    //--------------------------------------------------retryingClick------------------------------------------------------//
	    private static void retryingClick(By locator) {
	    for (int attempt = 1; attempt <= DEFAULT_RETRY; attempt++) {
	       try {
	           WebElement el = waitForVisibility(locator, 8);
	           try {
	               el.click();
	           } catch (Exception e) {
	               ((JavascriptExecutor) driver).executeScript("arguments[0].click();", el);
	           }
	           return;
	       } catch (StaleElementReferenceException | NoSuchElementException e) {
	           if (attempt == DEFAULT_RETRY) throw e;
	           try { 
	          	 Thread.sleep(RETRY_SLEEP_MS);
	          	 } catch (InterruptedException ignored) {
	          		 
	          	 }
	       }
	    }
	    }
	    }