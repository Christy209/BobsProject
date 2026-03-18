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

public class InquireOfExportBill {
	 private static WebDriver driver;
	    private WebDriverWait wait;
	    
	    private static final int DEFAULT_RETRY = 3;
		private static final long RETRY_SLEEP_MS = 700;

	    public InquireOfExportBill(WebDriver driver) {
	        InquireOfExportBill.driver = driver;
	        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10)); 
	    }
	    
	    @SuppressWarnings("unused")
		public Map<String, String> executeWithResultMap(RowData inputData, RowData verify, String sheetname, int row, String excelpath) throws Exception {
	        String mainWindowHandle = driver.getWindowHandle();
	        Map<String, String> result = new HashMap<>();
			String guaranteeNo = "";
	        String errorMsg  = "";
	        WindowHandle.slowDown(4);

	        WindowHandle.slowDown(2);
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
	            JavascriptExecutor js = (JavascriptExecutor) driver;
	            WindowHandle.selectDropdownIfValuePresent(driver, wait, By.id("funcCode"), funCode);

	            // -------------------- Bill ID Handling --------------------
	            String billIdField = "billId";
	            if (funCode.equalsIgnoreCase("G - Lodge")) {
	                WindowHandle.setValueWithJS(driver, wait.until(ExpectedConditions.presenceOfElementLocated(By.id("billType"))), inputData.getByIndex(4));
	                WindowHandle.setValueWithJS(driver, wait.until(ExpectedConditions.presenceOfElementLocated(By.id("billCcy"))), inputData.getByIndex(5));
	                WebElement yesRadio = driver.findElement(By.id("underDc"));
	                js.executeScript("arguments[0].click();", yesRadio);
	                WindowHandle.setValueWithJS(driver, wait.until(ExpectedConditions.presenceOfElementLocated(By.id("dcNo"))), inputData.getByIndex(6));
	                String initialBillId = inputData.getByIndex(7);
	                WindowHandle.setValueWithJS(driver, wait.until(ExpectedConditions.presenceOfElementLocated(By.id(billIdField))), initialBillId);
	                System.out.println("BillID initially set as " + initialBillId);
	               
	            } else {
	                WindowHandle.setValueWithJS(driver, wait.until(ExpectedConditions.presenceOfElementLocated(By.id(billIdField))), verify.getByHeader("LABELTEXT_BILLID"));
	            }
            

	            WebElement Go = wait.until(ExpectedConditions.elementToBeClickable(By.id("Submit")));
	            js.executeScript("arguments[0].click();", Go);
	            
//-----------------------------------------------Inquire----------------------------------------------------------------------------------------------//
	            if(funCode.equalsIgnoreCase("I - Inquire")){
	                 Map<String, String> appData = getInquireApplicationData(verify, driver, wait, sheetname, row, excelpath);
	                 Validation.validateData(inputData.getHeaderMap(), appData);  
	                 String dcNo = verify.getByHeader("LABELTEXT_BILLID");
	                 String successMsg = "Inquired Successfully – Import No: " + dcNo;
	                 result.put("LabelText", successMsg);   // 👈 THIS FIXES NULL
	                 result.put("LABELTEXT_BILLID", dcNo);            // keep DC No
	                 result.putAll(appData);
	             }
	            

	        } catch (Exception e) {
	        	String exceptionMsg = "Exception occurred: " + e.getMessage();
	            result.put("errorMsg", errorMsg);
	            Assert.fail(exceptionMsg);
	        }
			return result;

	       
	    }  

//------------------------------------------------getInquireApplicationData-----------------------------------------------------------------------------//
	    private static Map<String, String> getInquireApplicationData(RowData vr, WebDriver driver, WebDriverWait wait, String sheetname, int row, String excelpath) throws Exception {

	        Map<String, String> appData = new HashMap<>(); 
	        String funCode = vr.getByIndex(2);
	        System.out.println("Verify functioncode :" + funCode );
        
	        JavascriptExecutor js = (JavascriptExecutor) driver;

	        	
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

	        WebElement billIdElement = wait.until(ExpectedConditions.presenceOfElementLocated(
	        	    By.xpath("//td[contains(normalize-space(text()),'Bill ID')]/following-sibling::td[@class='textfielddisplaylabel']")));
	        	String rawBillId = (String) js.executeScript("return arguments[0].textContent;", billIdElement);
	        	Pattern billIdPattern = Pattern.compile("\\d+");
	        	Matcher billIdMatcher = billIdPattern.matcher(rawBillId);
	        	String billID = "";
	        	if (billIdMatcher.find()) {
	        	    billID = billIdMatcher.group();
	        	}
	        	appData.put("billId", billID);
	        	System.out.println("✅ Captured Bill ID: " + billID);



	        	if ("G - Lodge".equalsIgnoreCase(funCode)) {    
	        appData.put("sAccName", ExcelUtils.getTextOrValue(driver, wait, "sAccName","id"));
	        appData.put("sAddr1", ExcelUtils.getTextOrValue(driver, wait, "sAddr1","id"));
	        String boeAmtUI = ExcelUtils.getTextOrValue(driver, wait, "boeAmt", "id").replace(",", "");
	        appData.put("boeAmt", boeAmtUI);
	        appData.put("billCountry", ExcelUtils.getTextOrValue(driver, wait, "billCountry","id"));

                   retryingClick(By.id("fbmbill"));
	        appData.put("carrierCode", ExcelUtils.getTextOrValue(driver, wait, "carrierCode","id"));
	        WebElement NextPage = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//img[@id='NextPage']")));
	        NextPage.click();
	        appData.put("docStatus", ExcelUtils.getTextOrValue(driver, wait, "docStatus","id"));
	        appData.put("invoiceAmt", ExcelUtils.getTextOrValue(driver, wait, "invoiceAmt","id"));
	        

    }
				return appData;
  				
        	}
	    
//------------------------------waitForVisibility------------------------------------------------------------------------------------------------//
      private static WebElement waitForVisibility(By locator, int timeoutSeconds) {
    	     WebDriverWait localWait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
    	     return localWait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    	 }

  //--------------------------------------------retryingClick-------------------------------------------------------------------------------------//
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

    	             try { Thread.sleep(RETRY_SLEEP_MS); } catch (InterruptedException ignored) {}

    	         }

    	     }

    	 }

}