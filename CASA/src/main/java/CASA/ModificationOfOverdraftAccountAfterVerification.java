package CASA;


import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import Utilities.ExcelUtils;
import Utilities.ErrorCapture;
import Utilities.RowData;
import Utilities.WindowHandle;

public class ModificationOfOverdraftAccountAfterVerification {

    private static WebDriver driver;
    private WebDriverWait wait;
    
	private static final int DEFAULT_RETRY = 3;
	private static final long RETRY_SLEEP_MS = 700;
	

    @SuppressWarnings("static-access")
	public ModificationOfOverdraftAccountAfterVerification(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    public Map<String, String> execute(RowData inputData,RowData id,RowData vr,String sheetname, int row, String excelPath) {
	        Map<String, String> result = new HashMap<>();
	        String mainWindowHandle = driver.getWindowHandle();
	        String errorMsg  = "";

		        try {
		            driver.switchTo().defaultContent();
		            wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(By.name("loginFrame")));
		        } catch (Exception e) {
		            System.out.println("⚠️ Warning: could not switch to loginFrame before menuSelect: " + e.getMessage());
		        }
		        
		        WindowHandle.setValueWithJS(driver, waitForVisibility(By.id("menuSelect"),5), inputData.getByIndex(1));
		        retryingClick(By.id("menuSearcherGo"));

		  try {
	            wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(By.name("CoreServer")));
	            wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(By.id("FINW")));
	
		     }catch(Exception e) {
			     logWindowHandlesAndTitles();
	            throw new RuntimeException("Failed to switch to CoreServer/FINW frames: " + e.getMessage(), e);
		        }
		    
            WebElement funCode = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("mode")));
	        funCode.sendKeys(inputData.getByIndex(2));
	        
	        WindowHandle.setValueWithJS(driver, waitForVisibility(By.id("acctNo"), 8), id.getByHeader("Created_AccountID"));	         
	        retryingClick(By.id("Accept"));

	 	   errorMsg = ErrorCapture.checkForApplicationError(driver);
	         if (errorMsg != null && !errorMsg.trim().isEmpty()) {
	             result.put("errorMsg", errorMsg);
	             return result;	            
	        }
	    
        
   
//*************************************************Relationship tab************************************************************//
        
	       try {
	    	   WindowHandle.slowDown(1);
	    	   retryingClick(By.id("relatedpartydetails"));
		    	   
	    	   WindowHandle.setValueWithJS(driver, waitForVisibility(By.id("relnCode"), 8), inputData.getByIndex(32));

             //-------------------------Second Relation---------------------------------------------------------------------//	    	   
	               try {
	            	   
	                   if (inputData.getByIndex(33).equalsIgnoreCase("Y")){

	            	   retryingClick(By.id("relParty_NextRec"));
	            	   
	            	   WindowHandle.slowDown(1);
	            	   WindowHandle.selectByVisibleText(wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("relnType"))),inputData.getByIndex(34));
	            	   WindowHandle.setValueWithJS(driver, waitForVisibility(By.id("relnCode"), 8), inputData.getByIndex(35));            	   
	            	   WindowHandle.setValueWithJS(driver, waitForVisibility(By.id("custTitle"), 8), inputData.getByIndex(36));
	            	   WindowHandle.setValueWithJS(driver, waitForVisibility(By.id("custName"), 8), inputData.getByIndex(37));
	            	   WindowHandle.setValueWithJS(driver, waitForVisibility(By.id("custAddrLine1"), 8), inputData.getByIndex(38));
	               } else {
	            	   System.out.println("⏩ Skipping 'Add' button click as SecondRelatedFlg is not Y.");
	               }
           	   } catch (Exception e) {
           		   System.out.println("Error in second Related process:: " + e.getMessage());
           	   }
  
	           } catch (Exception e) {
	               System.out.println("Relationship tab Error : " + e.getMessage());
	           }
	       
	     //********************************************Miscode Tab Details*********************************************************//	    
		    try {
		    	   WindowHandle.slowDown(1);
		    	   retryingClick(By.id("miscodes"));

		       
		       WindowHandle.setValueWithJS(driver, wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("sectCode"))),inputData.getByIndex(39));
	           WindowHandle.setValueWithJS(driver, wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("subSectCode"))),inputData.getByIndex(40));
	           WindowHandle.setValueWithJS(driver, wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("purpAdv"))),inputData.getByIndex(41));
	           WindowHandle.setValueWithJS(driver, wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("modeAdv"))),inputData.getByIndex(42));
	           WindowHandle.setValueWithJS(driver, wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("typeAdv"))),inputData.getByIndex(43));
	           WindowHandle.setValueWithJS(driver, wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("natAdv"))),inputData.getByIndex(44));

		       
		   } catch (Exception e) {
		   	System.out.println("Miscode Tab  Error " + e.getMessage());
		   }
		    

		 //*********************************************Submit************************************************************************//
		      try {
		    	  WindowHandle.slowDown(2);
		          clickSubmitAndHandlePopup(mainWindowHandle,excelPath,sheetname,row,result);
		            return result;
		      }
		            catch(Exception e) {
				  	  errorMsg = ErrorCapture.checkApplicationErrors(driver);
						if (errorMsg != null && !errorMsg.trim().isEmpty()) {
							result.put("errorMsg", errorMsg);
							return result;
						}
						result.put("errorMsg", "Submit failed: " + e.getMessage());
		            }
			  return result;
    }
		      
		    //******************************************Submit and Handle Popup*************************************************//
			  
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
		                   WebElement resultMsg = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//td[contains(.,'A/c') and contains(.,'modified')]")));
		                   labelText = resultMsg.getText().trim();
		                   System.out.println("[INFO] Result Message: " + labelText);
				            
				            if (labelText != null && !labelText.isEmpty()) {
				            	  String accountNumber = labelText.replaceAll("[^0-9]", "");
				                  System.out.println("[INFO] Extracted Account Number: " + accountNumber);
				                  ExcelUtils.updateExcel(excelPath, sheetName, i, "Created_AccountID", accountNumber);
				                  System.out.println("[SUCCESS] ✅ Account No updated to Excel: " + accountNumber);
				                  result.put("labelText", labelText);
				                  return result;
				            } else {
				                result.put("errorMsg", "Account ID label is empty");
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
		   	                         try { driver.close();
		   	                         } catch (Exception ignore) {
		   	                        	 
		   	                         }
		   	                         driver.switchTo().window(originalWindow);
		   	                         System.out.println("[FAST] Clicked Accept in popup window.");
		   	                         return true;

		   	                     }

		   	                 } catch (Exception exWin) {
		   	                     System.out.println("[FAST] Failed to click Accept in window " + w + ": " + exWin.getMessage());

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


 