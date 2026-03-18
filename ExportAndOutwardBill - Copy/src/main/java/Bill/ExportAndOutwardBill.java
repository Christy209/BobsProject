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
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import Utilities.ErrorCapture;
import Utilities.ExcelUtils;
import Utilities.RowData;
import Utilities.Validation;
import Utilities.WindowHandle;

public class ExportAndOutwardBill{
	
	private static WebDriver driver;
	private WebDriverWait wait;
	private static final int DEFAULT_RETRY = 3;
	private static final long RETRY_SLEEP_MS = 700;

	    @SuppressWarnings("static-access")
		public ExportAndOutwardBill(WebDriver driver) {
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
	
		     }catch(Exception e) {
			     logWindowHandlesAndTitles();
	            throw new RuntimeException("Failed to switch to CoreServer/FINW frames: " + e.getMessage(), e);
		        }
		  
	            String funCode = inputData.getByIndex(2);
	            WindowHandle.selectDropdownIfValuePresent(driver, wait, By.id("funcCode"), funCode);

	            // -------------------- Bill ID Handling --------------------
	            String billIdField = "billId";
	            if (funCode.equalsIgnoreCase("G - Lodge")) {
	            	
	            	  WindowHandle.setValueWithJS(driver, waitForVisibility(By.id("billType"), 8), inputData.getByIndex(4));
	                  WindowHandle.setValueWithJS(driver, waitForVisibility(By.id("billCcy"), 8), inputData.getByIndex(5));	
	                  WebElement yesRadio = driver.findElement(By.id("underDc"));
	                  js.executeScript("arguments[0].click();", yesRadio);
	                  WindowHandle.setValueWithJS(driver, waitForVisibility(By.id("dcNo"), 8), inputData.getByIndex(6));	 
	                String initialBillId = inputData.getByIndex(7);
	                WindowHandle.setValueWithJS(driver, wait.until(ExpectedConditions.presenceOfElementLocated(By.id(billIdField))), initialBillId);
	                System.out.println("BillID initially set as " + initialBillId);
	               
	            } else {
	                WindowHandle.setValueWithJS(driver, wait.until(ExpectedConditions.presenceOfElementLocated(By.id(billIdField))), vr.getByHeader("LABELTEXT_BILLID"));
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
	                        ||funCode.equalsIgnoreCase("Y - Unclose Bill")
	                                        )) {


               WindowHandle.setValueWithJS(driver,wait.until(ExpectedConditions.presenceOfElementLocated(By.id("billId"))),nextBillId);
	                System.out.println("BillID overwritten with LABELTEXT_BILLID " + nextBillId);	               	                
	            }
	            
	            retryingClick(By.id("Submit"));
	            
	            errorMsg = ErrorCapture.checkForApplicationError(driver);
	            if (errorMsg != null && !errorMsg.trim().isEmpty()) {
	                result.put("errorMsg", errorMsg);
	                return result;
	            }
	            
//-------------------- -------------------- Realize  -------------------------------------------------------------------//
	            if ("R - Realize".equalsIgnoreCase(funCode)) {
	                    WindowHandle.slowDown(1);
	                    WindowHandle.setValueWithJS(driver, waitForVisibility(By.id("brAcct"), 8), inputData.getByIndex(3));
	                    fillBillTabs(driver, wait, inputData);
	                    clickSubmitAndHandlePopup(mainWindowHandle,excelPath,sheetName,i,result);
	                    return result;
                    }
	            
//---------------------------------Modify----------------------------------------------------------------------------//
	            
	            if (funCode.equalsIgnoreCase("M - Modify")) 
                {
	            	  clickSubmitAndHandlePopup(mainWindowHandle,excelPath,sheetName,i,result);
	                    return result;
                  }
	            
//-------------------------------------Accept---------------------------------------------------------------------------//
	        				   
	            
	                        if (funCode.equalsIgnoreCase("A - Accept")) 
	                        {
							    proceedToTenderSection(driver, wait, inputData);
							    clickSubmitAndHandlePopup(mainWindowHandle,excelPath,sheetName,i,result);
			                    return result;
		                    }

	                        
	                        
//-------------------------------------close Bill----------------------------------------------------------------------------//
	                    	
		                      
	                        if (funCode.equalsIgnoreCase("Z - Close Bill")) 
	                        	{
	                        	  clickSubmitAndHandlePopup(mainWindowHandle,excelPath,sheetName,i,result);
	      	                    return result;
	                          }
        
	                        
 //---------------------------------Unclose Bill----------------------------------------------------------------------------//
	                    	                    	
	                    	                     
	                        
	                        if (funCode.equalsIgnoreCase("Y - Unclose Bill")) 
	                        	{
	                        	  clickSubmitAndHandlePopup(mainWindowHandle,excelPath,sheetName,i,result);
	      	                    return result;
	                          }
	                        
//-------------------------------------Amend Bill----------------------------------------------------------------------------//
	                    	
							                      
	                        if (funCode.equalsIgnoreCase("E - Amend Bill")) 
	                        	{
	                        	proceedToEventSection(driver, wait, inputData);
	                        	  clickSubmitAndHandlePopup(mainWindowHandle,excelPath,sheetName,i,result);
	      	                    return result;
	                          }

//------------------------------------Free Delivery----------------------------------------------------------------------------//
	                    	
	                        if (funCode.equalsIgnoreCase("F - Free Delivery")) 
                        	{
	                        	  clickSubmitAndHandlePopup(mainWindowHandle,excelPath,sheetName,i,result);
	      	                    return result;
	                          }

	                        
//------------------------------------Verify----------------------------------------------------------------------------//

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

//------------------------------------Dishonor----------------------------------------------------------------------------//
                     
	                        if (funCode.equalsIgnoreCase("N - Dishonor"))
	                        {
	                            
	                       	 WindowHandle.slowDown(1);
	                       	 proceedToEventSection(driver, wait, inputData);
	                         clickSubmitAndHandlePopup(mainWindowHandle,excelPath,sheetName,i,result);
	 	                    return result;
	                     }


//--------------------------------------------Tabs----------------------------------------------------------------------------//
	                      
	                     //*******************General Details**********************//   

	                        if (!funCode.equalsIgnoreCase("V - Verify")
	                        	&& !funCode.equalsIgnoreCase("E - Amend Bill") 
	    		    	        && !funCode.equalsIgnoreCase("A - Accept") 
	    		    	        && !funCode.equalsIgnoreCase("K - Delink/Crystallization")
	    		    	        && !funCode.equalsIgnoreCase("F - Free Delivery")
	    		    	        && !funCode.equalsIgnoreCase("M - Modify")
	    		    	        && !funCode.equalsIgnoreCase("N - Dishonor") 
	    		    	        && !funCode.equalsIgnoreCase("X - Cancel")
	    		    	        && !funCode.equalsIgnoreCase("I - Inquire")
	    		    	        && !funCode.equalsIgnoreCase("Z - Close Bill") 
	    		    	        && !funCode.equalsIgnoreCase("Y - Unclose Bill")
	    		    	        && !funCode.equalsIgnoreCase("Q - Protest")
	    		    	        ){            
	           
	                        	WindowHandle.setValueWithJS(driver, waitForVisibility(By.id("sAccName"), 8), inputData.getByIndex(8));	
	                        	WindowHandle.setValueWithJS(driver, waitForVisibility(By.id("sAddr1"), 8), inputData.getByIndex(9));	
	                        	WindowHandle.setValueWithJS(driver, waitForVisibility(By.id("boeAmt"), 8), inputData.getByIndex(10));	
	                        	WindowHandle.setValueWithJS(driver, waitForVisibility(By.id("billCountry"), 8), inputData.getByIndex(11));	
	                        	WindowHandle.setValueWithJS(driver, waitForVisibility(By.id("otherBankRefNo"), 8), inputData.getByIndex(12));	
	                        	 retryingClick(By.id("Validate"));
	           

	          //*******************Party Details**********************//

	                WindowHandle.slowDown(2);
	                retryingClick(By.id("fbmparty"));
	                
	                WindowHandle.setValueWithJS(driver, waitForVisibility(By.id("Dcity"), 8), inputData.getByIndex(12));	
                	WindowHandle.setValueWithJS(driver, waitForVisibility(By.id("Dstate"), 8), inputData.getByIndex(13));	
                	WindowHandle.setValueWithJS(driver, waitForVisibility(By.id("Dcntry"), 8), inputData.getByIndex(14));	
                	retryingClick(By.id("Validate"));
                	
                	 //*******************Tender Details**********************//
	                WindowHandle.slowDown(2);
	                WebElement tenderTab = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("fbmtenor")));
	                js.executeScript("arguments[0].scrollIntoView(true); arguments[0].click();", tenderTab);
	                WindowHandle.selectByVisibleText(wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("dueDateInd"))), inputData.getByIndex(15));
	                WindowHandle.setValueWithJS(driver, wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("billDate_ui"))), inputData.getByIndex(16));
	                WindowHandle.setValueWithJS(driver, wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("shpmntDate_ui"))), inputData.getByIndex(16));
	                WindowHandle.setValueWithJS(driver, wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("acceptDate_ui"))), inputData.getByIndex(16));

	                //*******************Bill Details**********************//
	                WindowHandle.slowDown(2);
	                js.executeScript("document.getElementById('fbmbill').click();");
	                WindowHandle.setValueWithJS(driver, wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("carrierCode"))), inputData.getByIndex(18));
	                WebElement NextPage = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//img[@id='NextPage']")));
	                NextPage.click();
	                WindowHandle.selectByVisibleText(wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("docStatus"))), inputData.getByIndex(19));
	                WindowHandle.setValueWithJS(driver, wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("invoiceAmt"))), inputData.getByIndex(20));


	                fillBillTabs(driver, wait, inputData);
	                
	                clickSubmitAndHandlePopup(mainWindowHandle,excelPath,sheetName,i,result);
	                
                    return result;
                }
	                        return result;
	    }

//------------------------------------fillbilll]tabs-------------------------------------------------------------------//
   public static void fillBillTabs(WebDriver driver, WebDriverWait wait, RowData inputData) {
                  
                    try {
          
                    //*******************Event Details**********************//
     		    	 WindowHandle.slowDown(2);
     		    	retryingClick(By.id("fbmevent"));
     		    	
     		   //*******************Charge Details**********************//

      		    	  WindowHandle.slowDown(2);
      		    	retryingClick(By.id("tfccharge"));
      	          
      		    //*******************Transaction Details**********************//

     		    	     WindowHandle.slowDown(2);
     		    	    retryingClick(By.id("fbmtran"));
     		    	    
     		 //*******************OutwardMessage Details**********************//

    		    	     WindowHandle.slowDown(2);
    		    	     retryingClick(By.id("tfcmsg"));
    		    	   
    		    }catch(Exception e) {
    		    	System.out.println("");
    		    }     
	    }
	  
 //--------------------------------------- verification & validation---------------------------------------------------//
   private static Map<String, String> getApplicationData(RowData vr,WebDriver driver, WebDriverWait wait) throws Exception {
       Map<String, String> appData = new HashMap<>();
       String funCode = vr.getByIndex(2);      
       System.out.println(funCode);
 
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
	        	if (billIdMatcher.find())
	        	{
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

	        		   retryingClick(By.id("fbmparty"));
		 
		        
	        		   appData = navigateThroughTabsAndCaptureData(driver, wait, appData);
	        		}
	        	
	        	if ("A - Accept".equalsIgnoreCase(funCode) || "E - Amend Bill".equalsIgnoreCase(funCode))
	        	{
	        	    captureBillTabsData(driver, wait, appData);
	        	}

	        	return appData;

	    }
  
//---------------------------------- navigateThroughTabsAndCaptureData----------------------------------------------//       
  public static Map<String, String> navigateThroughTabsAndCaptureData(WebDriver driver, WebDriverWait wait, Map<String, String> appData) {
	    	   
	    	   retryingClick(By.id("fbmtenor"));        
	    	   captureBillTabsData(driver, wait, appData);
	    	   return appData;
	       }      
  
  //--------------------------------captureBillTabsData---------------------------------------------------------------//
      private static void captureBillTabsData(WebDriver driver, WebDriverWait wait, Map<String, String> appData) {
	        
	        appData.put("billDate_ui", ExcelUtils.getTextOrValue(driver, wait, "billDate_ui","id"));
	        try {
	            String rawDate = ExcelUtils.getTextOrValue(driver, wait, "shpmntDate_ui", "id");
	            SimpleDateFormat uiFormat = new SimpleDateFormat("dd-MM-yyyy");
	            SimpleDateFormat excelFormat = new SimpleDateFormat("dd-MMM-yyyy");	            
	            String normalizedDate = excelFormat.format(uiFormat.parse(rawDate));
	            appData.put("shpmntDate_ui", normalizedDate);
	        } catch (Exception e) {
	            appData.put("shpmntDate_ui", "Date Error");
	        }

	        retryingClick(By.id("fbmbill"));
	        appData.put("carrierCode", ExcelUtils.getTextOrValue(driver, wait, "carrierCode","id"));
	        WebElement NextPage = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//img[@id='NextPage']")));
	        NextPage.click();
	        appData.put("docStatus", ExcelUtils.getTextOrValue(driver, wait, "docStatus","id"));
	        appData.put("invoiceAmt", ExcelUtils.getTextOrValue(driver, wait, "invoiceAmt","id"));
       	
	        retryingClick(By.id("fbmevent"));  
	        
	        retryingClick(By.id("tfccharge"));  
	        
	        retryingClick(By.id("fbmtran"));  
	        
	        WindowHandle.slowDown(2);
	        retryingClick(By.id("tfcmsg"));  
    	
        	}

 //-----------------------------------clickSubmitAndHandlePopup--------------------------------------------------------//
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
            for (String handle : allHandles) {
                if (!handle.equals(mainWindowHandle)) {
                    driver.switchTo().window(handle);
                    driver.close(); // Close lingering "Fetch" popups
                }
            }
            driver.switchTo().window(mainWindowHandle);
            driver.switchTo().defaultContent(); 

            try {
                WindowHandle.ValidationFrame(driver);
                WebElement label = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//label[@id='compField']")));
                labelText = label.getText().trim();

                if (labelText == null || labelText.isEmpty()) {
                    System.out.println("[WARN] Documentary Credit No label is empty!");
                } else {
                
                	   String DCNo = labelText.replaceAll("[^0-9]", "");
                        ExcelUtils.updateExcel(excelPath, sheetName, i, "LABELTEXT_BILLID", DCNo);
                        result.put("labelText", labelText);
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

        return result;
    }

//------------------------------------logWindowHandlesAndTitles---------------------------------------------------------//
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
         {
        		 
         }

     } catch (Exception e) {

         System.out.println("[DIAG] Failed to enumerate window handles: " + e.getMessage());

     }

 }

 //------------------------------------captureFinacleTabError----------------------------------------------------------//
 public static String captureFinacleTabError(WebDriver driver) {

     try {
         WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
         driver.switchTo().defaultContent();
         wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt("FINW"));
         List<WebElement> errorLink = driver.findElements(By.id("errordetails"));
         if (!errorLink.isEmpty() && errorLink.get(0).isDisplayed()) {
             errorLink.get(0).click();
             List<WebElement> errorTexts = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.xpath("//*[contains(text(),'E') and contains(text(),'-')]")));
            StringBuilder sb = new StringBuilder();
             for (WebElement e : errorTexts) {
                 String txt = e.getText().trim();
                 if (!txt.isEmpty()) sb.append(txt).append(" | ");
             }
             return sb.toString();
         }
     } catch (Exception ignored) {
    	 
     }
  return null;
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

//----------------------------------------clickAcceptRobust------------------------------------------------------------//
 private boolean clickAcceptRobust(String mainWindowHandle, int timeoutSeconds) {
     String originalWindow = mainWindowHandle;
     try {
         try {
             driver.switchTo().alert().accept();
             System.out.println("[FAST] Alert accepted.");
             return true;
         } catch (Exception ignore) {}

         Set<String> windows = driver.getWindowHandles();
         for (String w : windows) {
             if (!w.equals(originalWindow)) {
                 try {
                     driver.switchTo().window(w);
                     String script = "(function(){" +
                             "var sel=['#accept','input[id*=\\'accept\\']','input[value*=\\'Accept\\']','input[value*=\\'OK\\']','button','a'];" +
                             "for(var s=0;s<sel.length;s++){var nodes=document.querySelectorAll(sel[s]);" +
                             "for(var i=0;i<nodes.length;i++){var n=nodes[i];var txt=(n.innerText||n.value||'').toLowerCase();" +
                             "if(txt.indexOf('accept')!==-1||txt.indexOf('ok')!==-1||txt.indexOf('yes')!==-1||n.id&&n.id.toLowerCase().indexOf('accept')!==-1){" +
                             "try{n.click();return true;}catch(e){try{var ev=document.createEvent('MouseEvents');ev.initEvent('click',true,true);n.dispatchEvent(ev);return true;}catch(e2){}}}}}" +
                             "return false;})()";

                     Object clicked = ((JavascriptExecutor) driver).executeScript(script);
                     if (clicked instanceof Boolean && (Boolean) clicked) {
                         try {
                        	 driver.close();
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
                 // ignore
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

 //---------------------------------------closePopupWindow-------------------------------------------------------------//
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

 //---------------------------------------------proceedToTenderSection-------------------------------------------------//
	    private void proceedToTenderSection(WebDriver driver, WebDriverWait wait, RowData inputData) {
	        try {
	            WindowHandle.slowDown(2);
	            retryingClick(By.id("fbmbill"));  
 	            fillBillTabs(driver, wait, inputData);
	        } catch (Exception e) {
	            System.out.println("⚠️ Could not proceed to Tender section: " + e.getMessage());
	        }
	    }

//---------------------------------------proceedToEventSection--------------------------------------------------------------------------------//
		private void proceedToEventSection(WebDriver driver, WebDriverWait wait, RowData inputData) {
	        try {
	            WindowHandle.slowDown(2);
	   		    
	   		 fillBillTabs(driver, wait, inputData);
	        } catch (Exception e) {
	            System.out.println("⚠️ Could not proceed to Tender section: " + e.getMessage());
	        }
	    }
	    
	  
	    }
