package Bill;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import Utilities.ExcelUtils;
import Utilities.RowData;
import Utilities.Validation;
import Utilities.WindowHandle;

public class InquireImportBills {
		private static WebDriver driver;
	    private WebDriverWait wait;
		private static final int DEFAULT_RETRY = 3;
		private static final long RETRY_SLEEP_MS = 700;

	    @SuppressWarnings("static-access")
		public InquireImportBills(WebDriver driver) {
	        this.driver = driver;
	        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10)); 
	    }

		public Map<String, String> executeWithResultMap(RowData inputData, RowData verify, String sheetname, int row, String excelpath) throws Exception {
	        Map<String, String> result = new HashMap<>();
	        
	        String errorMsg  = "";
	        WindowHandle.slowDown(2);
	        JavascriptExecutor js = (JavascriptExecutor) driver;
	        
	        try {
	            driver.switchTo().defaultContent();
	            wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(By.name("loginFrame")));
	        } catch (Exception e) {
	            System.out.println("⚠️ Warning: could not switch to loginFrame before menuSelect: " + e.getMessage());
	        }
	        
	        WindowHandle.setValueWithJS(driver, waitForVisibility(By.id("menuSelect"), 10), inputData.getByIndex(1));
	        retryingClick(By.id("menuSearcherGo"));


	        try {
	            wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(By.name("CoreServer")));
	            wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(By.id("FINW")));

	            String funCode = inputData.getByIndex(2);
	            WindowHandle.selectDropdownIfValuePresent(driver, wait, By.id("funcCode"), funCode);
	          
	            
	            if(funCode.equalsIgnoreCase("G - Lodge")) {
	            
	            	WindowHandle.setValueWithJS(driver, waitForVisibility(By.id("billType"), 8), inputData.getByIndex(3));
	 	            WebElement yesRadio = driver.findElement(By.id("underDc"));
	 	            js.executeScript("arguments[0].click();", yesRadio);
	 	            WindowHandle.setValueWithJS(driver, waitForVisibility(By.id("dcNo"), 8), inputData.getByIndex(4));
	 	          
	            }else {
	            	 WindowHandle.setValueWithJS(driver,wait.until(ExpectedConditions.presenceOfElementLocated(By.id("billId"))),verify.getByHeader("LABELTEXT_BILLID"));   	           
	            }	
	            
	            retryingClick(By.id("Accept"));
	         
	            if(funCode.equalsIgnoreCase("I - Inquire")){
	                 Map<String, String> appData = getInquireApplicationData(verify, driver, wait, sheetname, row, excelpath);
	                 Validation.validateData(inputData.getHeaderMap(), appData);  
	                 String dcNo = verify.getByHeader("LABELTEXT_BILLID");
	                 String successMsg = "Inquired Successfully – Import No: " + dcNo;
	                 result.put("LabelText", successMsg);   
	                 result.put("LABELTEXT_BILLID", dcNo); 
	                 result.putAll(appData);
	             }
	            

	        } catch (Exception e) {
	        	String exceptionMsg = "Exception occurred: " + e.getMessage();
	            result.put("errorMsg", errorMsg);
	            Assert.fail(exceptionMsg);
	        }
			return result;

	       
	    }  
//****************************************Verification & Validation ****************************************************************************************//		  
	    private static Map<String, String> getInquireApplicationData(RowData inputData, WebDriver driver, WebDriverWait wait, String sheetname, int row, String excelpath) throws Exception {
	        Map<String, String> appData = new HashMap<>();
	        
	        String funCode = inputData.getByIndex(2);	        
	        System.out.println("Verify functioncode :" + funCode );
	        
	        JavascriptExecutor js = (JavascriptExecutor) driver;

	        // ========================== BILL TYPE ==========================
	        
	        
	        WebElement billTypeElement = wait.until(ExpectedConditions.presenceOfElementLocated(
	            By.xpath("//td[contains(normalize-space(text()),'Bill Type')]/following-sibling::td[@class='textfielddisplaylabel']")));
	        String rawBillType = (String) js.executeScript("return arguments[0].textContent;", billTypeElement);
	        Pattern billTypePattern = Pattern.compile("\\b[A-Z]{5,7}\\b");
	        Matcher billTypeMatcher = billTypePattern.matcher(rawBillType);
	        String billType = "";
	        if (billTypeMatcher.find()) {
	            billType = billTypeMatcher.group();
	        }

	        appData.put("billType", billType);
	        System.out.println("✅ Captured Bill Type: " + billType);

	        // ========================== BILL ID ==========================
	        
	        
	        WebElement billIdElement = wait.until(ExpectedConditions.presenceOfElementLocated(
	            By.xpath("//td[contains(normalize-space(text()),'Bill ID')]/following-sibling::td[@class='textfielddisplaylabel']")));
	        String rawBillId = (String) js.executeScript("return arguments[0].textContent;", billIdElement);
	        Pattern billIdPattern = Pattern.compile("\\b\\w{2,}\\d{3,}_\\d{4}\\b");
	        Matcher billIdMatcher = billIdPattern.matcher(rawBillId);
	        String billID = "";
	        if (billIdMatcher.find()) {
	            billID = billIdMatcher.group();
	       }
	        appData.put("billId", billID);
	        System.out.println("✅ Captured Bill ID: " + billID);
	        
	        if (
	        	    !funCode.equalsIgnoreCase("N - Dishonor") &&
	        	    !funCode.equalsIgnoreCase("M - Modify")
	        	) {
	        	String boeAmtUI = ExcelUtils.getTextOrValue(driver, wait, "boeAmt", "id").replace(",", "");
	 	        appData.put("boeAmt", boeAmtUI);
	            appData.put("billCountry", ExcelUtils.getTextOrValue(driver, wait, "billCountry","id"));
	            appData.put("otherBankRefNo", ExcelUtils.getTextOrValue(driver, wait, "otherBankRefNo","id"));
	            
	           
	            	retryingClick(By.id("fbmparty"));
		      
	        }
	
	        	retryingClick(By.id("fbmtenor"));		        
		        appData.put("billDate_ui", ExcelUtils.getTextOrValue(driver, wait, "billDate_ui","id"));
		        appData.put("shpmntDate_ui", ExcelUtils.getTextOrValue(driver, wait, "shpmntDate_ui","id"));
		        appData.put("acceptDate_ui", ExcelUtils.getTextOrValue(driver, wait, "acceptDate_ui","id"));
		        
	
		        retryingClick(By.id("miibbill"));		        
		        appData.put("carrierCode", ExcelUtils.getTextOrValue(driver, wait, "carrierCode","id"));
		        WebElement NextPage = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//img[@id='NextPage']")));
		        NextPage.click();
		        appData.put("docStatus", ExcelUtils.getTextOrValue(driver, wait, "docStatus","id"));
		        appData.put("invoiceAmt", ExcelUtils.getTextOrValue(driver, wait, "invoiceAmt","id"));

		        
	        return appData;
	    }
	    
//**************************************waitForVisibility**************************************************************************************************//	    
	    private static WebElement waitForVisibility(By locator, int timeoutSeconds) {
   	     WebDriverWait localWait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
   	     return localWait.until(ExpectedConditions.visibilityOfElementLocated(locator));

   	 }

//*******************************************retryingClick*******************************************************************************************************//	    
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
   	            	 } catch (InterruptedException ignored) 
   	             {
   	            		 
   	             }

   	         }

   	     }

   	 }
}