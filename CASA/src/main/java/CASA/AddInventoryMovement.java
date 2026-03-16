package CASA;

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

public class AddInventoryMovement {

	private static WebDriver driver;
	private WebDriverWait wait;
	
	private static final int DEFAULT_RETRY = 3;
	private static final long RETRY_SLEEP_MS = 700;
	
	@SuppressWarnings("static-access")
	public AddInventoryMovement(WebDriver driver) {
	        this.driver = driver;
	        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10)); 
	    }

	  public Map<String, String> execute(RowData inputData, RowData id,RowData vr, String sheetName, int i, String excelPath) throws Exception {
	        String mainWindowHandle = driver.getWindowHandle();
	        Map<String, String> result = new HashMap<>();
	        String errorMsg  = "";


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
		  
		  String functionCode = inputData.getByIndex(2);	        
	    	   if (functionCode.equalsIgnoreCase("A - Add")) {
	    	   WindowHandle.selectByVisibleText(wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("funcCode"))),inputData.getByIndex(2));    
	    	   }else {
	    		   WindowHandle.selectByVisibleText(wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("funcCode"))),inputData.getByIndex(2));    
	    		   WindowHandle.setValueWithJS(driver, waitForVisibility(By.id("invtTranId"), 8), id.getByHeader("Tran_Id"));
	    	   }
	           retryingClick(By.id("Go"));

	    	   errorMsg = ErrorCapture.checkForApplicationError(driver);
	            if (errorMsg != null && !errorMsg.trim().isEmpty()) {
	                result.put("errorMsg", errorMsg);
	                return result;	            
	           }
	       

	       
	       if (!functionCode.equalsIgnoreCase("V - Verify")&&(!functionCode.equalsIgnoreCase("X - Cancel"))){
	//*********************************lOCATIONS***********************************************************//
	       try {
	       WindowHandle.setValueWithJS(driver, waitForVisibility(By.id("invtLocnClassFrom"), 8), inputData.getByIndex(3));
           WindowHandle.setValueWithJS(driver, waitForVisibility(By.id("invtLocnCodeFrom"), 8), inputData.getByIndex(4));
           WindowHandle.setValueWithJS(driver, waitForVisibility(By.id("invtLocnClassTo"), 8), inputData.getByIndex(5));
           WindowHandle.setValueWithJS(driver, waitForVisibility(By.id("invtLocnCodeTo"), 8), inputData.getByIndex(6));
           retryingClick(By.id("Accept"));
	      
	       
 //*********************************Item Details***********************************************************//
	     
	    	   WindowHandle.setValueWithJS(driver, waitForVisibility(By.id("invtClass"), 8), inputData.getByIndex(7));
	           WindowHandle.setValueWithJS(driver, waitForVisibility(By.id("invtType"), 8), inputData.getByIndex(8));
	    	   WindowHandle.setValueWithJS(driver, waitForVisibility(By.id("invtSrlAlpha"), 8), inputData.getByIndex(9));
	           WindowHandle.setValueWithJS(driver, waitForVisibility(By.id("invtBeginSrlNum"), 8), inputData.getByIndex(10));
	           WindowHandle.setValueWithJS(driver, waitForVisibility(By.id("invtEndSrlNum"), 8), inputData.getByIndex(11));
	           retryingClick(By.id("Validate"));
	        
	       }catch(Exception e) {
	    	              System.out.println("⚠️ Warning: Error during item details entry: " + e.getMessage());
	       }
	       
	//*********************************************Submit************************************************************************//
      try {
    	  WindowHandle.slowDown(2);
          clickSubmitAndHandlePopup(mainWindowHandle,excelPath,sheetName,i,result);
            return result;
      }
            catch(Exception e) {
		  	 
				result.put("errorMsg", "Submit failed: " + e.getMessage());
            }

      }
	       
	       //******************************************cancel**********************************************//
	       if("X - Cancel".equalsIgnoreCase(inputData.getByIndex(2))) {
	    	   
	    	   WindowHandle.slowDown(2);
	           clickSubmitAndHandlePopup(mainWindowHandle,excelPath,sheetName,i,result);
	             return result;
	       }
	       
		
//**********************************************Verify*******************************************************************************//	  
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
	 	   return result;
	 		  }
	//**********************************************Verification & validation ******************************************************************//  
	  private static Map<String, String> getApplicationData(RowData vr,WebDriver driver, WebDriverWait wait) throws Exception {
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

    //******************************************Submit and Handle Popup*************************************************//
	  
	  private Map<String, String> clickSubmitAndHandlePopup(String mainWindowHandle, String excelPath, String sheetName, int i, Map<String, String> result) {
   	   String labelText = "";
   	   try {
               retryingClick(By.id("Submit"));

               try { 
               	driver.switchTo().alert().accept();
               	} catch (Exception ignore)
               {}
               boolean popupHandled = false;
               try {
               	popupHandled = clickAcceptRobust(mainWindowHandle, 3); 
               	} catch (Exception ignored) 
               {}

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
               {}

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
            		    String fullText = label.getText().trim();
            		    System.out.println("Full Message: " + fullText);

            		    String transactionId = "";

            		    Pattern pattern = Pattern.compile("Transaction ID.*?(\\d+)");
            		    Matcher matcher = pattern.matcher(fullText);

            		    if (matcher.find()) {
            		        transactionId = matcher.group(1);
            		    }

            		    System.out.println("Extracted Transaction ID: " + transactionId);

            		    if (!transactionId.isEmpty()) {
            		        ExcelUtils.updateExcel(excelPath,sheetName,i,"Tran_Id",transactionId);
            		        System.out.println("[SUCCESS] ✅ Transaction ID updated to Excel: " + transactionId);
            		        result.put("labelText", fullText);
            		        return result;

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

//***********************************closeAllPopupsAndReturnToMain********************************************************//	      
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
      
//*********************************captureFinacleTabError*****************************************************//    
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

//*************************************************clickAcceptRobust******************************************************//
 	 @SuppressWarnings("unused")
	private boolean clickAcceptRobust(String mainWindowHandle, int timeoutSeconds) {
   	     String originalWindow = mainWindowHandle;
   	     try {
   	         // 0) Fast try: browser alert
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

   	                     // ignore and continue

   	                 } finally {

   	                     try { driver.switchTo().window(originalWindow); } catch (Exception ignore) {}

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
