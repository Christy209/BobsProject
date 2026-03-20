package CASA;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
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

import Utilities.ErrorCapture;
import Utilities.RowData;
import Utilities.Validation;
import Utilities.WindowHandle;
 
public class InquireLien {

	 private static WebDriver driver;
	 private WebDriverWait wait;
	 
		private static final int DEFAULT_RETRY = 3;
		private static final long RETRY_SLEEP_MS = 700;
 
	    @SuppressWarnings("static-access")
		public InquireLien(WebDriver driver) {
	        this.driver = driver;
	        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10)); 

	    }

		public Map<String, String> executeWithResultMap(RowData vr, RowData id,RowData input,RowData cl, String sheetname, int row, String excelpath) throws Exception {
	        Map<String, String> result = new HashMap<>();
	        String errorMsg  = "";

 
	        try {

	            driver.switchTo().defaultContent();
	            wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(By.name("loginFrame")));

	        } catch (Exception e) {
	            System.out.println("⚠️ Warning: could not switch to loginFrame before menuSelect: " + e.getMessage());
	        }

            WindowHandle.setValueWithJS(driver,wait.until(ExpectedConditions.presenceOfElementLocated(By.id("menuSelect"))),vr.getByIndex(1));
            WebElement searchButton = driver.findElement(By.id("menuSearcherGo"));
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", searchButton);
 
	        try {
	            wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(By.name("CoreServer")));
	            wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(By.id("FINW")));

	            String FunCode = vr.getByIndex(2);

	            if (FunCode.equalsIgnoreCase("A - Add")) {
	 	    	   WindowHandle.selectByVisibleText(wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("funcCode"))),vr.getByIndex(2));    
	 	    	   }else {
	 	    		   WindowHandle.selectByVisibleText(wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("funcCode"))),vr.getByIndex(2));    
	 	    		   WindowHandle.setValueWithJS(driver, waitForVisibility(By.id("invtTranId"), 8), id.getByHeader("Tran_Id"));
	 	    	   }
	 	           retryingClick(By.id("Go"));
	            
	 	          errorMsg = ErrorCapture.checkForApplicationError(driver);
		            if (errorMsg != null && !errorMsg.trim().isEmpty()) {
		                result.put("errorMsg", errorMsg);
		                return result;	            
		           }
		            
	            if(FunCode.equalsIgnoreCase("I - Inquire")){
	                 Map<String, String> appData = getInquireApplicationData(vr,id,cl, wait, sheetname, row, excelpath);
	                 Validation.validateData(vr.getHeaderMap(), appData);  
	                 String Accountid = id.getByHeader("Created_AccountID");
	                 String successMsg = "Inquired  Lien AccountId:" + Accountid;
	                 result.put("LabelText", successMsg);   // 👈 THIS FIXES NULL
	                 result.put("Accountid", Accountid);            // keep DC No
	                 result.putAll(appData);

	             }
 
	        } catch (Exception e) {
	        	String exceptionMsg = "Exception occurred: " + e.getMessage();
	            result.put("errorMsg", errorMsg);
	            Assert.fail(exceptionMsg);
	        }

			return result;
 
	    }  

	    private static Map<String, String> getInquireApplicationData(RowData vr,RowData id,RowData cl,WebDriverWait wait, String sheetname, int row, String excelpath) throws Exception {

	    	 Map<String, String> appData = new HashMap<>();
	 	    String funCode = vr.getByIndex(2);
	        System.out.println(funCode);

	        WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//td[@class='textfielddisplaylabel']/span[@class='display-text']")));
	        String fullText = element.getText().trim();
	        System.out.println("✅ Full Value: " + fullText);

	        Pattern pattern = Pattern.compile("^\\d+");   // digits at beginning
	        Matcher matcher = pattern.matcher(fullText);

	        String accountId = "";

	        if (matcher.find()) {
	            accountId = matcher.group();
	        }

	        System.out.println("✅ Extracted Account ID: " + accountId);
	        String createdAccountId = id.getByHeader("Created_AccountID");   // <-- put correct index here
	        createdAccountId = createdAccountId.trim();

	        System.out.println("Excel Account ID: " + createdAccountId);

	        if (accountId.equals(createdAccountId)) {
	            System.out.println("✅ Account ID Matched Successfully");
	        } else {
	            System.out.println("❌ Account ID Mismatch!");
	            System.out.println("Expected: " + createdAccountId);
	            System.out.println("Actual  : " + accountId);
	        }
	  
	        //-------------------------Module type-------------------------------//
	        WebElement lienElement = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//td[@class='textfielddisplaylabel' and contains(.,'ULIEN')]")));
	        String lienText = lienElement.getText().trim();
	        System.out.println("✅ Lien Type: " + lienText);
	        String excelLienValue = vr.getByIndex(6);  // <-- change index if needed
	        excelLienValue = excelLienValue.trim();

	       //*****************************Amount****************************************//
	        WebElement amountElement = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//td[@class='amount']")));
	        String uiAmount = amountElement.getText().replace("\u00A0", "").replace(",", "").trim();
	        System.out.println("💻 UI Amount: " + uiAmount);
	        String excelAmount = vr.getByIndex(4);
	        excelAmount = excelAmount.replace(",", "").trim();

	        System.out.println("📄 Excel Amount: " + excelAmount);

	        BigDecimal uiAmt = new BigDecimal(uiAmount);
	        BigDecimal excelAmt = new BigDecimal(excelAmount);
	        if (uiAmt.compareTo(excelAmt) == 0) {
	     	    System.out.println("✅ Amount Matched Successfully");
	     	} else {
	     	    System.out.println("❌ Amount Mismatch!");
	     	}
	        
	     // ************************ Common Module / Value Validation ************************ //

	        String excelModuleValue = vr.getByIndex(5).trim(); // Excel column 6 (zero-based index 5)
	        System.out.println("📄 Excel Module: '" + excelModuleValue + "'");


	        WebElement moduleElement = wait.until(ExpectedConditions.presenceOfElementLocated(
	            By.xpath("//td[@rowspan='2' and normalize-space()='" + excelModuleValue + "']")  // Only module check
	        ));

	        // ---------------- Step 3: Get UI value and clean it ----------------
	        String uiModule = moduleElement.getText().replace("\u00A0", "").trim(); // removes non-breaking spaces
	        System.out.println("💻 UI Module: '" + uiModule + "'");

	        // ---------------- Step 4: Validate ----------------
	        if (uiModule.equalsIgnoreCase(excelModuleValue)) {
	            System.out.println("✅ Module Type Matched Successfully");
	        } else {
	            System.out.println("❌ Module Mismatch!");
	            System.out.println("Expected (Excel): '" + excelModuleValue + "'");
	            System.out.println("Actual (UI)     : '" + uiModule + "'");
	        }


	     	       
	    // ****************************** Old Lien Amount ******************************************** //

	        try {

	            List<WebElement> oldLienList = driver.findElements(
	                    By.xpath("//td[@rowspan='2' and contains(@class,'amount')]"));

	            if (!oldLienList.isEmpty()) {   // ✅ Only if present (Modification case)

	                WebElement oldLienAmtElement = oldLienList.get(0);

	                String oldLienAmount = oldLienAmtElement.getText()
	                        .replace("\u00A0", "")
	                        .replace(",", "")
	                        .trim();

	                System.out.println("💻 UI Old Lien Amount: " + oldLienAmount);

	                String excelAmount1 = cl.getByIndex(4);
	                excelAmount1 = excelAmount1
	                        .replace(",", "")
	                        .trim();

	                System.out.println("📄 Excel Old Lien Amount: " + excelAmount1);

	                BigDecimal uiAmt1 = new BigDecimal(oldLienAmount);
	                BigDecimal excelAmt1 = new BigDecimal(excelAmount1);

	                if (uiAmt1.compareTo(excelAmt1) == 0) {
	                    System.out.println("✅ Old Lien Amount Matched Successfully");
	                } else {
	                    System.out.println("❌ Old Lien Amount Mismatch!");
	                }

	            } else {
	                // ✅ ADD scenario
	                System.out.println("ℹ Old Lien Amount not present (Add Scenario) - Skipping Validation");
	            }

	        } catch (Exception e) {
	            System.out.println("⚠ Exception while validating Old Lien Amount: " + e.getMessage());
	        }
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

	