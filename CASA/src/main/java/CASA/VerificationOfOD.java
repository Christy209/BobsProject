package CASA;


import java.text.SimpleDateFormat;
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
import Utilities.ErrorCapture;
import Utilities.ExcelUtils;
import Utilities.RowData;
import Utilities.Validation;
import Utilities.WindowHandle;

public class VerificationOfOD {
	private static WebDriver driver;
	private WebDriverWait wait;
		
	private static final int DEFAULT_RETRY = 3;
	private static final long RETRY_SLEEP_MS = 700;

@SuppressWarnings("static-access")
public VerificationOfOD(WebDriver driver) {
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

    	   WindowHandle.selectByVisibleText(wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("verifyCancel"))),inputData.getByIndex(2));
           WindowHandle.setValueWithJS(driver, waitForVisibility(By.id("tempForacid"), 8), id.getByHeader("Created_AccountID"));
           
           retryingClick(By.id("Accept"));
           
	    	   errorMsg = ErrorCapture.checkForApplicationError(driver);
	            if (errorMsg != null && !errorMsg.trim().isEmpty()) {
	                result.put("errorMsg", errorMsg);
	                return result;	            
	           }
	       
            

	  //----------------------------------------Verify--------------------------------------------------------------//
      if("V - Verify".equalsIgnoreCase(inputData.getByIndex(2))) {
          Map<String, String> appData = getApplicationData(vr, driver, wait);
          try {
              Validation.validateData(inputData.getHeaderMap(), appData);

          } catch (AssertionError ae) {
              Thread.sleep(500);
             appData = getApplicationData(vr, driver, wait);
              Validation.validateData(inputData.getHeaderMap(), appData); // if fails again it will throw

          }
          clickSubmitAndHandlePopup(mainWindowHandle,excelPath,sheetName,i,result, errorMsg);
             return result;
      }
		return result;
}
	
  //**************************************Verification & Validation Methods**************************************************//
	   private static Map<String, String> getApplicationData(RowData inputData, WebDriver driver, WebDriverWait wait) throws Exception {
	   Map<String, String> appData = new HashMap<>();

			System.out.println("Loaded Excel Data: " + appData);
	    String funCode = inputData.getByIndex(2);
         System.out.println(funCode);

       //********************************General details tab**************************************************************************//
         
         appData.put("pbPsFlg", ExcelUtils.getTextOrValue(driver, wait, "pbPsFlg", "id"));
         appData.put("modeOfOperCode", ExcelUtils.getTextOrValue(driver, wait, "modeOfOperCode", "id"));
         
         String freqOption = inputData.getByIndex(4);
         if (freqOption != null && !freqOption.equalsIgnoreCase("N - None")) {
             appData.put("pbPsFreqType", ExcelUtils.getTextOrValue(driver, wait, "pbPsFreqType", "id"));
             appData.put("pbPsFreqWeek", ExcelUtils.getTextOrValue(driver, wait, "pbPsFreqWeek", "id"));
             appData.put("pbPsFreqDay", ExcelUtils.getTextOrValue(driver, wait, "pbPsFreqDay", "id"));
             appData.put("pbPsFreqStartDD", ExcelUtils.getTextOrValue(driver, wait, "pbPsFreqStartDD", "id"));
             appData.put("pbPsFreqHldyStat", ExcelUtils.getTextOrValue(driver, wait, "pbPsFreqHldyStat", "id"));
             appData.put("pbPsFreqCalBase", ExcelUtils.getTextOrValue(driver, wait, "pbPsFreqCalBase", "id"));
             appData.put("despatchMode", ExcelUtils.getTextOrValue(driver, wait, "despatchMode", "id"));
         } else {
             System.out.println("Skipping frequency-related fields.");
         }
         
         //************************Interest Details Tab*************************************************//
		  try {
			  retryingClick(By.id("generaldetails2"));
			    appData.put("intDrAcctFlg", ExcelUtils.getTextOrValue(driver, wait, "intDrAcctFlg", "id"));
		         appData.put("nextIntDrCalcDt_ui", ExcelUtils.getTextOrValue(driver, wait, "nextIntDrCalcDt_ui", "id"));
		  } catch (Exception e) {
			  System.out.println("⚠️ Warning: Could not retrieve interest details: " + e.getMessage());
		  }
		//***************************Scheme tab**************************************************************//
		    try {    
		    WindowHandle.slowDown(2);
		         retryingClick(By.id("sbschemedetails"));

		         String nomineeFlg = "";

		         List<WebElement> nomineeRadios = driver.findElements(
		                 By.name("sbschemedetails.availNomFlg"));

		         for (WebElement radio : nomineeRadios) {
		             if (radio.isSelected()) {
		                 nomineeFlg = radio.getAttribute("value"); // This gives Y or N
		                 break;
		             }
		         }

		         System.out.println("UI Nominee Flag: " + nomineeFlg);
		         appData.put("availNomFlg", nomineeFlg); 
		    }catch (Exception e) {
				 System.out.println("Scheme tab error : " + e.getMessage());
			 }
		    appData.put("acctHlthCode", ExcelUtils.getTextOrValue(driver, wait, "acctHlthCode", "id"));

		         
		    //*************************** Nominee details tab **************************************************************//
	         retryingClick(By.id("nominationdetails"));

	         try {

	             // ================= FIRST NOMINEE =================//
	             appData.put("regValue", ExcelUtils.getTextOrValue(driver, wait, "regValue", "id"));
	             appData.put("cifId1", ExcelUtils.getTextOrValue(driver, wait, "cifId", "id"));
	             appData.put("nomName1", ExcelUtils.getTextOrValue(driver, wait, "nomName", "id"));
	             appData.put("nomAddrLine11", ExcelUtils.getTextOrValue(driver, wait, "nomAddrLine1", "id"));
	             appData.put("relation1", ExcelUtils.getTextOrValue(driver, wait, "relation", "id"));
	             appData.put("nomStateCode1", ExcelUtils.getTextOrValue(driver, wait, "nomStateCode", "id"));
	             appData.put("nomCityCode1", ExcelUtils.getTextOrValue(driver, wait, "nomCityCode", "id"));
	             appData.put("nomCntryCode1", ExcelUtils.getTextOrValue(driver, wait, "nomCntryCode", "id"));
	             appData.put("nomPcnt1", ExcelUtils.getTextOrValue(driver, wait, "nomPcnt", "id"));
	             appData.put("nomPostalCode1", ExcelUtils.getTextOrValue(driver, wait, "nomPostalCode", "id"));

	             // ================= MINOR NOMINEE FLAG =================//
	             String minorFlag = "";

	             List<WebElement> minorRadios = driver.findElements(
	                     By.name("nominationdetails.nomMinorFlg"));

	             for (WebElement radio : minorRadios) {
	                 if (radio.isSelected()) {
	                     minorFlag = radio.getAttribute("value");   // Y or N
	                     break;
	                 }
	             }

	             appData.put("nomMinorFlg", minorFlag);

	             // ================= GUARDIAN DETAILS =================//
	             appData.put("grdnName", ExcelUtils.getTextOrValue(driver, wait, "grdnName", "id"));
	             appData.put("grdnCode", ExcelUtils.getTextOrValue(driver, wait, "grdnCode", "id"));
	             appData.put("grdnAddrLine1", ExcelUtils.getTextOrValue(driver, wait, "grdnAddrLine1", "id"));
	             appData.put("grdnAddrLine2", ExcelUtils.getTextOrValue(driver, wait, "grdnAddrLine2", "id"));
	             appData.put("grdnAddrLine3", ExcelUtils.getTextOrValue(driver, wait, "grdnAddrLine3", "id"));
	             appData.put("grdnCityCode", ExcelUtils.getTextOrValue(driver, wait, "grdnCityCode", "id"));
	             appData.put("grdnStateCode", ExcelUtils.getTextOrValue(driver, wait, "grdnStateCode", "id"));
	             appData.put("grdnCntryCode", ExcelUtils.getTextOrValue(driver, wait, "grdnCntryCode", "id"));
	             appData.put("grdnPostalCode", ExcelUtils.getTextOrValue(driver, wait, "grdnPostalCode", "id"));

	             // ================= SECOND NOMINEE (IF PRESENT) =================//
	             try {

	            	 retryingClick(By.xpath("//input[@id='nomDetail_NextRec']")); // Click to add second nominee details

	                 appData.put("cifId2", ExcelUtils.getTextOrValue(driver, wait, "cifId", "id"));
	                 appData.put("nomName2", ExcelUtils.getTextOrValue(driver, wait, "nomName", "id"));
	                 appData.put("nomAddrLine12", ExcelUtils.getTextOrValue(driver, wait, "nomAddrLine1", "id"));
	                 appData.put("relation2", ExcelUtils.getTextOrValue(driver, wait, "relation", "id"));
	                 appData.put("nomStateCode2", ExcelUtils.getTextOrValue(driver, wait, "nomStateCode", "id"));
	                 appData.put("nomCityCode2", ExcelUtils.getTextOrValue(driver, wait, "nomCityCode", "id"));
	                 appData.put("nomCntryCode2", ExcelUtils.getTextOrValue(driver, wait, "nomCntryCode", "id"));
	                 appData.put("nomPcnt2", ExcelUtils.getTextOrValue(driver, wait, "nomPcnt", "id"));
	                 appData.put("nomPostalCode2", ExcelUtils.getTextOrValue(driver, wait, "nomPostalCode", "id"));

	             } catch (Exception e) {
	                 System.out.println("Second nominee not present.");
	             }

	         } catch (Exception e) {
	             System.out.println("Nominee tab error : " + e.getMessage());
	         }
	       //************************* Relative details tab ******************************************************//
	         retryingClick(By.id("relatedpartydetails"));

	         try {

	             // ================= FIRST RELATION =================//
	             appData.put("relnType1", ExcelUtils.getTextOrValue(driver, wait, "relnType", "id"));
	             appData.put("relnCode1", ExcelUtils.getTextOrValue(driver, wait, "relnCode", "id"));
	             appData.put("custTitle1", ExcelUtils.getTextOrValue(driver, wait, "custTitle", "id"));
	             appData.put("custName1", ExcelUtils.getTextOrValue(driver, wait, "custName", "id"));
	             appData.put("custAddrLine11", ExcelUtils.getTextOrValue(driver, wait, "custAddrLine1", "id"));

	             // ================= SECOND RELATION (IF PRESENT) =================//
	             try {

	                 retryingClick(By.xpath("//*[@id='relParty_NextRec']"));  
	                 // Use correct Next button ID for related party if different

	                 appData.put("relnType2", ExcelUtils.getTextOrValue(driver, wait, "relnType", "id"));
	                 appData.put("relnCode2", ExcelUtils.getTextOrValue(driver, wait, "relnCode", "id"));
	                 appData.put("custTitle2", ExcelUtils.getTextOrValue(driver, wait, "custTitle", "id"));
	                 appData.put("custName2", ExcelUtils.getTextOrValue(driver, wait, "custName", "id"));
	                 appData.put("custAddrLine12", ExcelUtils.getTextOrValue(driver, wait, "custAddrLine1", "id"));

	             } catch (Exception e) {
	                 System.out.println("Second relation not present.");
	             }

	         } catch (Exception e) {
	             System.out.println("Relationship tab Error : " + e.getMessage());
	         }
	  //*************************Document details tab******************************************************//
	        retryingClick(By.id("documentdetails"));
	        
			appData.put("docCode", ExcelUtils.getTextOrValue(driver, wait, "docCode", "id"));
			appData.put("docScanFlg", ExcelUtils.getTextOrValue(driver, wait, "docScanFlg", "id"));
		
					
			//***************************** FFD Tab ***********************************************************//
			retryingClick(By.id("sbffdparameters"));

			try {

			    // ================= BASIC FFD FIELDS =================//
			    appData.put("schmCode", ExcelUtils.getTextOrValue(driver, wait, "schmCode", "id"));
			    appData.put("swpDepFreqMnths", ExcelUtils.getTextOrValue(driver, wait, "swpDepFreqMnths", "id"));
			    appData.put("swpDepFreqDay", ExcelUtils.getTextOrValue(driver, wait, "swpDepFreqDay", "id"));
			    appData.put("swpFreqType", ExcelUtils.getTextOrValue(driver, wait, "swpFreqType", "id"));
			    appData.put("swpFreqStartDD", ExcelUtils.getTextOrValue(driver, wait, "swpFreqStartDD", "id"));
			    appData.put("swpHldyStat", ExcelUtils.getTextOrValue(driver, wait, "swpHldyStat", "id"));
			    appData.put("swpFreqCalBase", ExcelUtils.getTextOrValue(driver, wait, "swpFreqCalBase", "id"));
			    appData.put("repayInstr", ExcelUtils.getTextOrValue(driver, wait, "repayInstr", "id"));

			    // ================= AUTO SWEEP FLAG (Y/N) =================//
			    String autoSweepFlag = "";

			    List<WebElement> autoSweepRadios = driver.findElements(
			            By.name("sbffdparameters.autSwpFlg"));

			    for (WebElement radio : autoSweepRadios) {
			        if (radio.isSelected()) {
			            autoSweepFlag = radio.getAttribute("value");   // Y or N
			            break;
			        }
			    }

			    appData.put("autSwpFlg", autoSweepFlag);
			 // ================= PRINT RECEIPT FLAG (Y/N) =================//
			    String printReceiptFlag = "";
			    List<WebElement> printReceiptRadios = driver.findElements(
			            By.name("sbffdparameters.printReceipt"));

			    for (WebElement radio : printReceiptRadios) {
			        if (radio.isSelected()) {
			            printReceiptFlag = radio.getAttribute("value");   // Y or N
			            break;
			        }
			    }

			    appData.put("printReceipt", printReceiptFlag);


			    // ================= SAFE CUSTODY FLAG (Y/N) =================//
			    String safeCustodyFlag = "";
			    List<WebElement> safeCustodyRadios = driver.findElements(
			            By.name("sbffdparameters.safeCstdy"));

			    for (WebElement radio : safeCustodyRadios) {
			        if (radio.isSelected()) {
			            safeCustodyFlag = radio.getAttribute("value");   // Y or N
			            break;
			        }
			    }

			    appData.put("safeCustody", safeCustodyFlag);

			} catch (Exception e) {
			    System.out.println("FFD Tab Verification Error: " + e.getMessage());
			}
		         
			//****************************Int Slabs************************************************************************************//
		      try {
		    	  WindowHandle.slowDown(1);
		    	  retryingClick(By.id("linttmacct"));
		    	  
	        appData.put("tenorOfSlabInDays", ExcelUtils.getTextOrValue(driver, wait, "tenorOfSlabInDays", "id"));
	        appData.put("tenorOfSlabInMnths", ExcelUtils.getTextOrValue(driver, wait, "tenorOfSlabInMnths", "id"));

	      //*********************************Accont Limits tab******************************************************//
	    	
			retryingClick(By.id("acctlmt"));
			

			appData.put("documentDate_ui", ExcelUtils.getTextOrValue(driver, wait, "documentDate_ui", "id"));
			  try {
		            String rawDateUI = ExcelUtils.getTextOrValue(driver, wait, "expiryDate_ui", "id");
		            SimpleDateFormat uiFormat = new SimpleDateFormat("dd-MM-yyyy");
		            SimpleDateFormat excelFormat = new SimpleDateFormat("dd-MMM-yyyy");
		            String formattedDate = excelFormat.format(uiFormat.parse(rawDateUI));
		            appData.put("expiryDate_ui", formattedDate);
		        } catch (Exception e) {
		            appData.put("expiryDate_ui", "Date Error");
		        }
			appData.put("drawingPowerInd", ExcelUtils.getTextOrValue(driver, wait, "drawingPowerInd", "id"));
		
	//*************************Miscodes tab******************************************************//
			retryingClick(By.id("miscodes"));
			
			appData.put("sectCode", ExcelUtils.getTextOrValue(driver, wait, "sectCode", "id"));
			appData.put("subSectCode", ExcelUtils.getTextOrValue(driver, wait, "subSectCode", "id"));
			appData.put("purpAdv", ExcelUtils.getTextOrValue(driver, wait, "purpAdv", "id"));
			appData.put("modeAdv", ExcelUtils.getTextOrValue(driver, wait, "modeAdv", "id"));
			appData.put("typeAdv", ExcelUtils.getTextOrValue(driver, wait, "typeAdv", "id"));
			appData.put("natAdv", ExcelUtils.getTextOrValue(driver, wait, "natAdv", "id"));
			
		      } catch (Exception e) {
		    	  System.out.println("Account limits or Miscodes tab error : " + e.getMessage());
		      }
		return appData;
	   }
	   
	 //----------------------------------WaitForVisibility-----------------------------------------------------//   
	   private static WebElement waitForVisibility(By locator, int timeoutSeconds) {
	       WebDriverWait localWait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
	       return localWait.until(ExpectedConditions.visibilityOfElementLocated(locator));
	   }

	 //----------------------------------retryingClick-----------------------------------------------------------//
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
	               } catch (InterruptedException ignored) {}
	           }
	       }
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
 //******************************************Submit and Handle Popup*************************************************//
		  
	   private Map<String, String> clickSubmitAndHandlePopup(String mainWindowHandle,String excelPath,String sheetName,int rowIndex,Map<String, String> result,String menu) {
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
		        WindowHandle.ValidationFrame(driver);
		            try {
		            	  WebElement acctLabel = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//label[@id='AcctNum']")));
		            	     labelText = acctLabel.getText().trim();	            	     
		            	    String formattedText = "Verified A/c. ID : " + labelText;
		                if (!labelText.isEmpty()) {
		                    String accountNumber = labelText.replaceAll("[^0-9]", "");
		                    ExcelUtils.updateExcel(excelPath, sheetName, rowIndex,"Created_AccountID", accountNumber);
		                    System.out.println("✅ Account Number Captured: " + accountNumber);

		                    result.put("Created_AccountID", accountNumber);
		                    result.put("labelText",formattedText );
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
	//******************************************Close All Popups and Return to Main***********************************************//
	      
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
	     

	//******************************************Capture Finacle Tab Error*************************************************************//
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


	//******************************************Close Popup Window*************************************************************//
	  	 public static void closePopupWindow(WebDriver driver) {
	 	     String mainWindow = driver.getWindowHandle();
	 	     Set<String> allWindows = driver.getWindowHandles();

	 	     for (String window : allWindows) {
	 	         if (!window.equals(mainWindow)) {
	 	             driver.switchTo().window(window);

	 	             try {
	 	                 WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
	 	                 WebElement closeButton = wait.until(ExpectedConditions.presenceOfElementLocated(
	 	                     By.xpath("//img[contains(@title, 'Close') or contains(@alt, 'Close') or contains(@src, 'close')]")
	 	                 ));

	 	                 // Scroll and click via JavaScript
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
	  }     