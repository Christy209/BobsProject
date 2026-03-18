package Bill;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import Utilities.DateUtils;
import Utilities.ErrorCapture;
import Utilities.ExcelUtils;
import Utilities.RowData;
import Utilities.Validation;
import Utilities.WindowHandle;

public class ImportAndInwardBills {
	private static WebDriver driver;
	private WebDriverWait wait;
	private static final int DEFAULT_RETRY = 3;
	private static final long RETRY_SLEEP_MS = 700;

	    @SuppressWarnings("static-access")
		public ImportAndInwardBills(WebDriver driver) {
	        this.driver = driver;
	        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10)); 
	    }
	    	    
	    public Map<String, String> execute(RowData inputData, RowData id,RowData vr, String sheetName, int i, String excelPath) throws Exception {
	        String mainWindowHandle = driver.getWindowHandle();
	        
	        Map<String, String> result = new HashMap<>();
	        String errorMsg  = "";
		    JavascriptExecutor js = (JavascriptExecutor) driver;

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
	   
	    }catch (Exception e) {
	        logWindowHandlesAndTitles();
            throw new RuntimeException("Failed to switch to CoreServer/FINW frames: " + e.getMessage(), e);
	    }
	            String funCode = inputData.getByIndex(2);
	            WindowHandle.selectDropdownIfValuePresent(driver, wait, By.id("funcCode"), funCode);
	            
	            if(funCode.equalsIgnoreCase("G - Lodge")) {
	            
	            	WindowHandle.setValueWithJS(driver, waitForVisibility(By.id("billType"), 8), inputData.getByIndex(3));
	 	            WebElement yesRadio = driver.findElement(By.id("underDc"));
	 	            js.executeScript("arguments[0].click();", yesRadio);
	 	        	WindowHandle.setValueWithJS(driver, waitForVisibility(By.id("dcNo"), 8), inputData.getByIndex(4));  
		            
	            }else {
	            	 WindowHandle.setValueWithJS(driver,wait.until(ExpectedConditions.presenceOfElementLocated(By.id("billId"))),vr.getByHeader("LABELTEXT_BILLID"));   	           
	            }	
	            
	            String nextBillId = vr.getByHeader("LABELTEXT_BILLID");
	            if (nextBillId != null && !nextBillId.trim().isEmpty()
	                    && (funCode.equalsIgnoreCase("K - Delink/Crystallization")
	                        || funCode.equalsIgnoreCase("O - Recovery") 
	                        || funCode.equalsIgnoreCase("N - Dishonor")
	                        || funCode.equalsIgnoreCase("E - Amend Bill")
	                        ||funCode.equalsIgnoreCase("M - Modify")
	                        ||funCode.equalsIgnoreCase("X - Cancel")
	                        ||funCode.equalsIgnoreCase("I - Inquire")
	                        ||funCode.equalsIgnoreCase("R - Realize")
	                        ||funCode.equalsIgnoreCase("U - Interest Run")
	                        ||funCode.equalsIgnoreCase("Z - Close Bill")
	                        ||funCode.equalsIgnoreCase("Y - Unclose Bill"))) 
	            {


               WindowHandle.setValueWithJS(driver,wait.until(ExpectedConditions.presenceOfElementLocated(By.id("billId"))),nextBillId);
	                System.out.println("BillID overwritten with LABELTEXT_BILLID " + nextBillId);	               	                
	            }
	            
	            retryingClick(By.id("Accept"));
	               		    
    		    errorMsg = ErrorCapture.checkApplicationErrors(driver);
                if (errorMsg != null && !errorMsg.trim().isEmpty()) {
                	result.put("errorMsg", errorMsg);
                    return result;
                }
	            
       //--------------------------------U - Interest Run"---------------------------------------//         
                if (funCode.equalsIgnoreCase("U - Interest Run")) {
                    String interest = inputData.getByIndex(3);
    	            WindowHandle.selectDropdownIfValuePresent(driver, wait, By.id("tranTypeSubType"), interest);

                 WindowHandle.slowDown(2);
                 retryingClick(By.id("fbmtran"));
 	   		  clickSubmitAndHandlePopup(mainWindowHandle,excelPath,sheetName,i,result);
              return result;
          }
	    //-----------------------------------Amend Bill---------------------------------------//
	        	
		    	if (funCode.equalsIgnoreCase("E - Amend Bill")) {
                 
                    	 WindowHandle.slowDown(1);
                    	 proceedToEventSection(driver, wait, inputData);
                    	  clickSubmitAndHandlePopup(mainWindowHandle,excelPath,sheetName,i,result);
  	                    return result;
                      }
		    	
		    	
	  //----------------------------F - Free Delivery-------------------------//
		    	if (funCode.equalsIgnoreCase("F - Free Delivery")) {
	                 
               	 WindowHandle.slowDown(1);
                 clickSubmitAndHandlePopup(mainWindowHandle,excelPath,sheetName,i,result);
                 return result;
             }
  //-----------------------------------Amend Bill---------------------------------------//
	        	
		    	if (funCode.equalsIgnoreCase("X - Cancel")) {
                 
                    	 WindowHandle.slowDown(1);
                    	  clickSubmitAndHandlePopup(mainWindowHandle,excelPath,sheetName,i,result);
  	                    return result;
                      }
	  
		    	//-----------------------------------Protest------------------------------------------------------------------------------//
		    	if (funCode.equalsIgnoreCase("Q - Protest")) {
            
                    	 WindowHandle.slowDown(1);                    	
                    	  clickSubmitAndHandlePopup(mainWindowHandle,excelPath,sheetName,i,result);
  	                    return result;
                      }
		          
		 	//------------------------Realize---------------------------------------------------------//
		    	
		    	if (funCode.equalsIgnoreCase("R - Realize"))
		    	{
		    		  WindowHandle.setValueWithJS(driver, waitForVisibility(By.id("realAcctId"), 8), inputData.getByIndex(3));
		    		  fillFromEventOnwards(driver, wait, inputData, excelPath, sheetName, i);
	               	 WindowHandle.slowDown(1);
	                 clickSubmitAndHandlePopup(mainWindowHandle,excelPath,sheetName,i,result);
	                 return result;
             }
		    	
	  //--------------------------------Delink Details----------------------------------------------------------------------------//
		    	
		    	if (funCode.equalsIgnoreCase("K - Delink/Crystallization")) {

		    			    WindowHandle.slowDown(1);
		    			    
		    				WindowHandle.setValueWithJS(driver, waitForVisibility(By.id("DelinkAccId"), 8), inputData.getByIndex(3));
		    				WindowHandle.setValueWithJS(driver, waitForVisibility(By.id("RealiseAccId"), 8), inputData.getByIndex(4)); 
		    				
                            proceedToEventSection(driver, wait, inputData);
                            clickSubmitAndHandlePopup(mainWindowHandle,excelPath,sheetName,i,result);
                            return result;
                       }
		//--------------------------------------Dishonor--------------------------------------------------------//
		    	
		    	if (funCode.equalsIgnoreCase("N - Dishonor")) {
                   
                    	  WindowHandle.slowDown(1);
                    	  proceedToEventSection(driver, wait, inputData);
                    	  clickSubmitAndHandlePopup(mainWindowHandle,excelPath,sheetName,i,result);
  	                      return result;
                      }
	  
    //-------------------------------------Verify---------------------------------------------------------------------//
		    	 if("V - Verify".equalsIgnoreCase(inputData.getByIndex(2))) {
                     Map<String, String> appData = getApplicationData(vr, driver, wait);
                     try {
                         Validation.validateData(inputData.getHeaderMap(), appData);

                     } catch (AssertionError ae) {
                         Thread.sleep(500);
                        appData = getApplicationData(vr, driver, wait);
                         Validation.validateData(inputData.getHeaderMap(), appData); // if fails again it will throw

                     }
                     clickSubmitAndHandlePopup(mainWindowHandle,excelPath,sheetName,i,result);
	                    return result;
                 }
    //----------------------------------Modify--------------------------------------------------------------------//
		    	
		    	if (funCode.equalsIgnoreCase("M - Modify")) {
	                   
               	 WindowHandle.slowDown(1);
                 clickSubmitAndHandlePopup(mainWindowHandle,excelPath,sheetName,i,result);
                 return result;
             }
    //----------------------------------Recovery-----------------------------------------------------------------------------//
		    	
		    	if (funCode.equalsIgnoreCase("O - Recovery")) {
	                   
	               	WindowHandle.slowDown(1);
	               	proceedToEventSection(driver, wait, inputData);
	           	    WindowHandle.slowDown(1);
	                clickSubmitAndHandlePopup(mainWindowHandle,excelPath,sheetName,i,result);
                  return result;
             }

		//-------------------------------------------------------close bill---------------------------------//
		    	
		    	if (funCode.equalsIgnoreCase("Z - Close Bill")) {
	                   
	               	 WindowHandle.slowDown(1);
	               	proceedToEventSection(driver, wait, inputData);
	           	 WindowHandle.slowDown(1);
	             clickSubmitAndHandlePopup(mainWindowHandle,excelPath,sheetName,i,result);
                 return result;
             }
		    	//----------------------------close bill---------------------------------//
		    	if (funCode.equalsIgnoreCase("Y - Unclose Bill")) {
	                   
	               	WindowHandle.slowDown(1);
	               proceedToEventSection(driver, wait, inputData);
	           	   WindowHandle.slowDown(1);
	              clickSubmitAndHandlePopup(mainWindowHandle,excelPath,sheetName,i,result);
                  return result;
             }

//------------------------------------------Accept----------------------------------------------------------------------------------------//		    	
		    	if (funCode.equalsIgnoreCase("A - Accept"))
		    	{	                   
	               	 WindowHandle.slowDown(1);
	                 clickSubmitAndHandlePopup(mainWindowHandle,excelPath,sheetName,i,result);
                     return result;
             }
//----------------------------------------------------General details---------------------------------------------//
    		    	if (!funCode.equalsIgnoreCase("N - Dishonor") 
    		    	        && !funCode.equalsIgnoreCase("V - Verify") 
    		    	        && !funCode.equalsIgnoreCase("Q - Protest") 
    		    	        && !funCode.equalsIgnoreCase("K - Delink/Crystallization")
    		    	        &&!funCode.equalsIgnoreCase("M - Modify")
    		    		&&!funCode.equalsIgnoreCase("X - Cancel")
    		    		&&!funCode.equalsIgnoreCase("I - Inquire")
    		    		&&!funCode.equalsIgnoreCase("E - Amend Bill")
    		    		&&!funCode.equalsIgnoreCase("O - Recovery")
    		    		&&!funCode.equalsIgnoreCase("U - Interest Run")
    		    		&&!funCode.equalsIgnoreCase("F - Free Delivery")
    		    		&&!funCode.equalsIgnoreCase("Z - Close Bill")
    		    		&&!funCode.equalsIgnoreCase("Y - Unclose Bill")
    		    		&&!funCode.equalsIgnoreCase("A - Accept")
    		    		&&!funCode.equalsIgnoreCase("R - Realize"))
    		    	{
    		    		WindowHandle.setValueWithJS(driver, waitForVisibility(By.id("boeAmt"), 8), inputData.getByIndex(5));
    		    		WindowHandle.setValueWithJS(driver, waitForVisibility(By.id("billCountry"), 8), inputData.getByIndex(6));
    		    		WindowHandle.setValueWithJS(driver, waitForVisibility(By.id("otherBankRefNo"), 8), inputData.getByIndex(7));
    		    		retryingClick(By.id("Validate"));
    		    			
	  //-----------------------------------Party Details------------------------------------------------//
    		    		
	           WindowHandle.slowDown(2);
	           retryingClick(By.id("fbmparty"));
	           
 	  //-----------------------------------Tenor Details-------------------------------------------------//
	           
 	   		if (!funCode.equalsIgnoreCase("R - Realize")) {
	    		        WindowHandle.slowDown(2);
	    		        retryingClick(By.id("fbmtenor"));
	    	   		   
	    		        WindowHandle.slowDown(1);       
	    	            String billDate_ui = inputData.getByIndex(8);
	    	            if (billDate_ui != null && !billDate_ui.trim().isEmpty()) {
	    	                WindowHandle.setValueWithJS(driver, wait.until(ExpectedConditions.presenceOfElementLocated(By.id("billDate_ui"))),
	    	                DateUtils.toDDMMYYYY(billDate_ui));
	    	            }
    	   	          
	    	            WindowHandle.slowDown(1);       
	    	            String shpmntDate_ui = inputData.getByIndex(9);
	    	            if (shpmntDate_ui != null && !shpmntDate_ui.trim().isEmpty()) {
	    	                WindowHandle.setValueWithJS(driver, wait.until(ExpectedConditions.presenceOfElementLocated(By.id("shpmntDate_ui"))),
	    	                DateUtils.toDDMMYYYY(shpmntDate_ui));
	    	            }
	    	            
	    	            WindowHandle.slowDown(1);       
	    	            String acceptDate_ui = inputData.getByIndex(10);
	    	            if (acceptDate_ui != null && !acceptDate_ui.trim().isEmpty()) {
	    	                WindowHandle.setValueWithJS(driver, wait.until(ExpectedConditions.presenceOfElementLocated(By.id("acceptDate_ui"))),
	    	                DateUtils.toDDMMYYYY(acceptDate_ui));
	    	            }
	    	            
    	 //-------------------------------------Bill Details---------------------------------------------------//

	    	            WindowHandle.slowDown(2);
	    	             WebElement BillTab = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("miibbill")));
	          	   		 ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true); arguments[0].click();", BillTab);
	         	          WindowHandle.setValueWithJS(driver, wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("carrierCode"))),inputData.getByIndex(11));
	         	             
	         	          WebElement NextPage = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//img[@id='NextPage']")));
	                      NextPage.click();
	                         
	         	          WindowHandle.selectByVisibleText(wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("docStatus"))),inputData.getByIndex(12));
	         	          WindowHandle.setValueWithJS(driver, wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("invoiceAmt"))),inputData.getByIndex(13));

         	         fillFromEventOnwards(driver, wait, inputData, excelPath, sheetName, i);
 	   		}
 	   		
 	   	  clickSubmitAndHandlePopup(mainWindowHandle,excelPath,sheetName,i,result);
          return result;
      }
					return result;
    		    	}
				
	//****************************************fillFromEventOnwards*******************************************************************************************//  
       private void fillFromEventOnwards(WebDriver driver, WebDriverWait wait, RowData inputData, String excelPath, String sheetName, int row) {
         	              
    	   JavascriptExecutor js = (JavascriptExecutor) driver;    
         	                
	      //------------------------------------Event Details-----------------------------------------------------------//
    	   
         	            WindowHandle.slowDown(2);
         	           retryingClick(By.id("fbmevent"));
       	           //   ((JavascriptExecutor) driver).executeScript("document.getElementById('fbmevent').click();");

       	  //-----------------------------------Charge Details-----------------------------------------------------------//
       	              
       	      WindowHandle.slowDown(2);
       	      retryingClick(By.id("tfccharge"));
       		  WindowHandle.setValueWithJS(driver, waitForVisibility(By.id("chargeId"), 8), inputData.getByIndex(14));
  	   	      
       		 retryingClick(By.id("Recalculate"));
  	   	    
         //--------------------------------Transaction Details--------------------------------------------------------//
                WindowHandle.slowDown(2);
                retryingClick(By.id("fbmtran"));

	   		    
	   //-----------------------------------Document Tab-----------------------------------------------------------------//
	   		    
	   		   WindowHandle.slowDown(2);
	   		   retryingClick(By.id("miibdoc"));
	    	   
	   		WindowHandle.setValueWithJS(driver, wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("docNo"))),inputData.getByIndex(15));
	   		WindowHandle.setValueWithJS(driver, wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("original"))),inputData.getByIndex(16));
	   		
	   	   
	   		WebElement nextPage = driver.findElement(By.xpath("//img[@id='docDet_NextPage']"));
	   		nextPage.click();

            WindowHandle.setValueWithJS(driver, wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("docNo"))),inputData.getByIndex(15));
	   		WindowHandle.setValueWithJS(driver, wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("original"))),inputData.getByIndex(16));
	   		
	   		WebElement lowLimitField = driver.findElement(By.id("docDet_LowLimit")); 
	   		js.executeScript("arguments[0].value='';", lowLimitField);
	   		
	   		lowLimitField.sendKeys("2");
	   		lowLimitField.sendKeys(Keys.ENTER);
	   		wait.until(ExpectedConditions.stalenessOf(lowLimitField));
	   		
	   		
	   	    wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("docNo"))); 
	    	WebElement docNo = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("docNo")));
	    	WebElement original = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("original")));
	    	
	    	WindowHandle.setValueWithJS(driver, docNo, inputData.getByIndex(15));
	    	WindowHandle.setValueWithJS(driver, original, inputData.getByIndex(16));
	    	
	   	   driver.findElement(By.id("Validate")).click();


	    }
	
  //********************************************Verification & Validation *****************************************************************************************//    
       private static Map<String, String> getApplicationData(RowData vr,WebDriver driver, WebDriverWait wait) throws Exception {
          Map<String, String> appData = new HashMap<>();
          
           String funCode = vr.getByIndex(2);      
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
		        try {
		            String rawDate = ExcelUtils.getTextOrValue(driver, wait, "shpmntDate_ui", "id");
		            rawDate = rawDate.trim(); 
		            String normalizedDate = "";
		            if (rawDate.matches("\\d{2}-\\d{2}-\\d{4}")) {
		                normalizedDate = rawDate;
		            } 
		            else if (rawDate.matches("\\d{2}-[a-zA-Z]{3}-\\d{4}")) {
		                SimpleDateFormat uiFormat = new SimpleDateFormat("dd-MMM-yyyy"); 
		                SimpleDateFormat targetFormat = new SimpleDateFormat("dd-MM-yyyy"); 
		                normalizedDate = targetFormat.format(uiFormat.parse(rawDate));
		            } 
		            else {
		                normalizedDate = rawDate;
		            }
		            appData.put("shpmntDate_ui", normalizedDate);
		            System.out.println("✅ Final Date used for comparison: " + normalizedDate);

		        } catch (Exception e) {
		            appData.put("shpmntDate_ui", "Date Error: " + e.getMessage());
		        }
		        appData.put("acceptDate_ui", ExcelUtils.getTextOrValue(driver, wait, "acceptDate_ui","id"));
		        
		        retryingClick(By.id("miibbill"));
		      
		        
		        appData.put("carrierCode", ExcelUtils.getTextOrValue(driver, wait, "carrierCode","id"));
		        WebElement NextPage = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//img[@id='NextPage']")));
		        NextPage.click();
		        appData.put("docStatus", ExcelUtils.getTextOrValue(driver, wait, "docStatus","id"));
		        appData.put("invoiceAmt", ExcelUtils.getTextOrValue(driver, wait, "invoiceAmt","id"));
		        if (
		        	    !funCode.equalsIgnoreCase("Z - Close Bill") &&
		        	    !funCode.equalsIgnoreCase("Y - Unclose Bill")
		        	) {   
		        	
		        retryingClick(By.id("fbmevent"));	
		        retryingClick(By.id("tfccharge"));		      
		        appData.put("chargeId", ExcelUtils.getTextOrValue(driver, wait, "chargeId","id"));		        
		        retryingClick(By.id("fbmtran"));		        
		        retryingClick(By.id("miibdoc"));

		        appData.put("docNo", ExcelUtils.getTextOrValue(driver, wait, "docNo","id"));
		        appData.put("original", ExcelUtils.getTextOrValue(driver, wait, "original","id"));
		       
		        if (!funCode.equalsIgnoreCase("K - Delink/Crystallization") && !funCode.equalsIgnoreCase("U - Interest Run")) {
		            appData.put("DelinkAccId", ExcelUtils.getTextOrValue(driver, wait, "DelinkAccId","id"));
		            appData.put("RealiseAccId", ExcelUtils.getTextOrValue(driver, wait, "RealiseAccId","id"));
		        }
		        }
		        
	        return appData;
	    }
	   
 //*************************************** clickSubmitAndHandlePopup***********************************************************************************************//     
       private Map<String, String> clickSubmitAndHandlePopup(String mainWindowHandle, String excelPath, String sheetName, int i, Map<String, String> result) {
    	   String labelText = "";
    	   try {
                retryingClick(By.id("Submit"));

                try { 
                	driver.switchTo().alert().accept();
                	} catch (Exception ignore)
                {
                		
                }
                boolean popupHandled = false;
                try {
                	popupHandled = clickAcceptRobust(mainWindowHandle, 3); 
                	} catch (Exception ignored) 
                {
                		
                }

                if (!popupHandled) {
                    try {
                        Set<String> handles = driver.getWindowHandles();
                        if (handles.size() > 1) {
                            popupHandled = WindowHandle.HandlePopupAndClickAccept(driver, Duration.ofSeconds(6));
                        }
                    } catch (Exception ignored) {}
                }
                try {
                	logWindowHandlesAndTitles(); 
                	} catch (Exception ignored) 
                {
                		
                }

                Set<String> allHandles = driver.getWindowHandles();
                System.out.println("[DEBUG] Total windows after submit: " + allHandles.size());
                if (allHandles.size() > 1) {
                    for (String handle : allHandles) {
                        if (!handle.equals(mainWindowHandle)) {
                            try {
                                driver.switchTo().window(handle);
                                String title = driver.getTitle();
                                System.out.println("[DEBUG] Closing popup window: " + title);
                                driver.close();
                            } catch (Exception e) {
                                System.out.println("[DEBUG] Failed to close window: " + e.getMessage());
                            }
                        }
                    }
                    driver.switchTo().window(mainWindowHandle);
                }

                driver.switchTo().defaultContent();

                try {
                    WindowHandle.ValidationFrame(driver);
                    WebElement label = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//label[@id='compField']")));
    	            labelText = label.getText().trim();

    	            if (labelText == null || labelText.isEmpty()) {
    	                System.out.println("[WARN] Documentary Credit No label is empty!");
    	            } else {
    	                Pattern pattern = Pattern.compile("\\b[A-Z0-9]+_\\d+\\b", Pattern.CASE_INSENSITIVE);
    	                Matcher matcher = pattern.matcher(labelText);
    	                if (matcher.find()) {
    	                    String DCNo = matcher.group();
    	                    ExcelUtils.updateExcel(excelPath, sheetName, i, "LABELTEXT_BILLID", DCNo);
    	                    System.out.println("[INFO] ✅ Bill ID updated to Excel: " + DCNo);
    	               }
    	            }

                } catch (Exception idException) {
                    String appError = ErrorCapture.checkForApplicationErrors(driver);
                    if (appError != null && !appError.isEmpty()) {
                        result.put("errorMsg", appError);
                    } else {
                        result.put("errorMsg", "ID capture failed: " + idException.getMessage());
                    }
                }

            } catch (Exception e) {
                throw new RuntimeException("Failed to click Submit: " + e.getMessage(), e);
            }
    	   result.put("labelText", labelText);
            return result;
        }

//********************************************closeAllPopupsAndReturnToMain*********************************************************************************//       
       public void closeAllPopupsAndReturnToMain() {
    	    try {

    	        String mainWindow = driver.getWindowHandle();
    	        System.out.println("[DEBUG] Main window handle: " + mainWindow);

    	        Set<String> allWindows = driver.getWindowHandles();
    	        System.out.println("[DEBUG] Total windows: " + allWindows.size());
   
    	        for (String window : allWindows) {
    	            if (!window.equals(mainWindow)) {
    	                System.out.println("[DEBUG] Switching to and closing window: " + window);
    	                driver.switchTo().window(window);
    	                System.out.println("[DEBUG]   Title: " + driver.getTitle());
    	                driver.close();
    	            }
    	        }
 
    	        driver.switchTo().window(mainWindow);
    	        System.out.println("[DEBUG] Back to main window. Title: " + driver.getTitle());
    	        try {
    	            driver.switchTo().defaultContent();
    	            try {
    	                wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt("loginFrame"));
    	                System.out.println("[DEBUG] Switched to loginFrame");
    	            } catch (Exception e1) {
    	                System.out.println("[DEBUG] Could not switch to loginFrame: " + e1.getMessage());
    	            }
    	            try {
    	                wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt("CoreServer"));
    	                System.out.println("[DEBUG] Switched to CoreServer");
    	            } catch (Exception e2) {
    	                System.out.println("[DEBUG] Could not switch to CoreServer: " + e2.getMessage());
    	            }
    
    	            try {
    	                wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt("FINW"));
    	                System.out.println("[DEBUG] Switched to FINW");
    	            } catch (Exception e3) {
    	                System.out.println("[DEBUG] Could not switch to FINW: " + e3.getMessage());
    	            }
    	        } catch (Exception e) {
    	            System.out.println("[DEBUG] Frame switching failed: " + e.getMessage());
    	        }
    	        
    	    } catch (Exception e) {
    	        System.out.println("⚠️ Error in closeAllPopupsAndReturnToMain: " + e.getMessage());
    	        e.printStackTrace();
    	    }
    	}
  
 //***************************************************** logWindowHandlesAndTitles************************************************************************//     
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

 //************************************captureFinacleTabError***********************************************************************************************//
       public static String captureFinacleTabError(WebDriver driver) {
   	     try {
    	         WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
    	         driver.switchTo().defaultContent();
    	         wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt("FINW"));
    	         List<WebElement> errorLink = driver.findElements(By.id("errordetails"));
    	         if (!errorLink.isEmpty() && errorLink.get(0).isDisplayed()) {
    	             errorLink.get(0).click();  // open error panel
    	             List<WebElement> errorTexts = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.xpath("//*[contains(text(),'E') and contains(text(),'-')]")));
    	            StringBuilder sb = new StringBuilder();
    	             for (WebElement e : errorTexts) {
    	                 String txt = e.getText().trim();
    	                 if (!txt.isEmpty()) sb.append(txt).append(" | ");
    	             }
    	             return sb.toString();
    	         }

    	     } catch (Exception ignored) {}
    	  return null;

    	 }

     	
//**************************************waitForVisibility************************************************************************************************//
    	 private static WebElement waitForVisibility(By locator, int timeoutSeconds) {
    	     WebDriverWait localWait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
    	     return localWait.until(ExpectedConditions.visibilityOfElementLocated(locator));

    	 }

 //*************************************retryingClick*******************************************************************************************************//   	 
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
    	             {}

    	         }

    	     }

    	 }

 //****************************************clickAcceptRobust****************************************************************************************************//   	 
    	 private boolean clickAcceptRobust(String mainWindowHandle, int timeoutSeconds) {
    	     String originalWindow = mainWindowHandle;
    	     try {
    	         try {
    	             driver.switchTo().alert().accept();
    	             System.out.println("[FAST] Alert accepted.");
    	             return true;
    	         } catch (Exception ignore) {
    	        	 
    	         }

    	         Set<String> windows = driver.getWindowHandles();
    	         for (String w : windows) {
    	             if (!w.equals(originalWindow)) {
    	                 try {
    	                     driver.switchTo().window(w);
    	                     // Quick JS click: search common selectors and click first match
    	                     String script = "(function(){" +
    	                             "var sel=['#accept','input[id*=\\'accept\\']','input[value*=\\'Accept\\']','input[value*=\\'OK\\']','button','a'];" +
    	                             "for(var s=0;s<sel.length;s++){var nodes=document.querySelectorAll(sel[s]);" +
    	                             "for(var i=0;i<nodes.length;i++){var n=nodes[i];var txt=(n.innerText||n.value||'').toLowerCase();" +
    	                             "if(txt.indexOf('accept')!==-1||txt.indexOf('ok')!==-1||txt.indexOf('yes')!==-1||n.id&&n.id.toLowerCase().indexOf('accept')!==-1){" +
    	                             "try{n.click();return true;}catch(e){try{var ev=document.createEvent('MouseEvents');ev.initEvent('click',true,true);n.dispatchEvent(ev);return true;}catch(e2){}}}}}" +
    	                             "return false;})()";

    	                     Object clicked = ((JavascriptExecutor) driver).executeScript(script);
    	                     if (clicked instanceof Boolean && (Boolean) clicked) {
    	                         try { driver.close();
    	                         } catch (Exception ignore) {
    	                        	 
    	                         }
    	                         driver.switchTo().window(originalWindow);
    	                         System.out.println("[FAST] Clicked Accept in popup window.");
    	                         return true;

    	                     }

    	                 } catch (Exception exWin) {


    	                 } finally {

    	                     try { 
    	                    	 driver.switchTo().window(originalWindow); 
    	                    	 } catch (Exception ignore) {
    	                    		 
    	                    	 }

    	                 }

    	             }

    	         }

    	         driver.switchTo().window(originalWindow);
    	         String recursiveClickScript =
    	             "(function clickInDoc(d){" +
    	             "try{var sel=['#accept','input[id*=\\'accept\\']','input[value*=\\'Accept\\']','input[value*=\\'OK\\']','button','a'];" +
    	             "for(var s=0;s<sel.length;s++){var nodes=d.querySelectorAll(sel[s]);for(var i=0;i<nodes.length;i++){var n=nodes[i];var txt=(n.innerText||n.value||'').toLowerCase();" +
    	             "if(txt.indexOf('accept')!==-1||txt.indexOf('ok')!==-1||txt.indexOf('yes')!==-1||n.id&&n.id.toLowerCase().indexOf('accept')!==-1){try{n.click();return true;}catch(e){try{var ev=document.createEvent('MouseEvents');ev.initEvent('click',true,true);n.dispatchEvent(ev);return true;}catch(e2){}}}}}" +
    	             "if(d.frames&&d.frames.length){for(var k=0;k<d.frames.length;k++){try{var fr=d.frames[k];if(fr&&fr.document){var res=clickInDoc(fr.document);if(res) return true;}}catch(e){} }}" +
    	             "}catch(e){}return false;})(document);";

    	         Object clickedMain = ((JavascriptExecutor) driver).executeScript(recursiveClickScript);
    	         if (clickedMain instanceof Boolean && (Boolean) clickedMain) {
    	             System.out.println("[FAST] Clicked Accept in main document or frame.");
    	             return true;

    	         }

    	         int tries = Math.max(1, timeoutSeconds * 2); // try ~0.5s intervals
    	         for (int i = 0; i < tries; i++) {
    	             try {
    	                 Object again = ((JavascriptExecutor) driver).executeScript(recursiveClickScript);
    	                 if (again instanceof Boolean && (Boolean) again) {
    	                     System.out.println("[FAST] Clicked Accept on retry " + i);
    	                     return true;

    	                 }

    	             } catch (Exception ex) {
    	             }

    	             try { 
    	            	 Thread.sleep(250); 
    	            	 } catch (InterruptedException ie) { 
    	            		 Thread.currentThread().interrupt(); 
    	            		 }

    	         }

    	         try {
    	             WebElement body = driver.findElement(By.tagName("body"));
    	             body.sendKeys(org.openqa.selenium.Keys.ENTER);
    	             System.out.println("[FAST] Sent ENTER to body as fallback.");
    	             return true;

    	         } catch (Exception e) {

    	         }



    	     } catch (Exception e) {
    	         System.out.println("[WARN] clickAcceptRobust encountered: " + e.getMessage());

    	     } finally {

    	         try { 
    	        	 driver.switchTo().window(originalWindow);
    	        	 } catch (Exception ignore) {
    	        		 
    	        	 }

    	     }

    	     return false;

    	 }

 //**********************************************closePopupWindow*******************************************************************************************//  	 
    	 public static void closePopupWindow(WebDriver driver) {
    	     String mainWindow = driver.getWindowHandle();
    	     Set<String> allWindows = driver.getWindowHandles();

    	     for (String window : allWindows) {
    	         if (!window.equals(mainWindow)) {
    	             driver.switchTo().window(window);

    	             try {
    	                 WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    	                 WebElement closeButton = wait.until(ExpectedConditions.presenceOfElementLocated(
    	                     By.xpath("//img[contains(@title, 'Close') or contains(@alt, 'Close') or contains(@src, 'close')]")));

    	                 ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", closeButton);
    	                 Thread.sleep(500);
    	                 ((JavascriptExecutor) driver).executeScript("arguments[0].click();", closeButton);

    	                 System.out.println("✅ Popup window closed successfully using JS click.");
    	             } catch (Exception e) {
    	                 System.out.println("⚠️ JS click failed, closing window directly: " + e.getMessage());
    	                 driver.close(); // Fallback
    	             }

    	             driver.switchTo().window(mainWindow);
    	             break;
    	         }
    	     }
    	 }
	            
//**************************************************proceedToEventSection****************************************************************************************//	           
	    private void proceedToEventSection(WebDriver driver, WebDriverWait wait, RowData inputData) {
	        try {
	            WindowHandle.slowDown(2);
	            retryingClick(By.id("fbmevent"));
 	          
 	             WindowHandle.slowDown(2);
 	            retryingClick(By.id("fbmtran"));
 	    	 
	        } catch (Exception e) {
	            System.out.println("⚠️ Could not proceed to Tender section: " + e.getMessage());
	        }
	    }
	    
}