package CASA;


import java.time.Duration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import Utilities.ActionResolver;
import Utilities.DateUtils;
import Utilities.ErrorCapture;
import Utilities.ExcelUtils;
import Utilities.RowData;
import Utilities.Validation;
import Utilities.WindowHandle;

public class TransactionMaintainence {
		private static WebDriver driver;
		private WebDriverWait wait;
		
		private static final int DEFAULT_RETRY = 3;
		private static final long RETRY_SLEEP_MS = 700;

	@SuppressWarnings("static-access")
	public TransactionMaintainence(WebDriver driver){
			     this.driver = driver;
			        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10)); 
			    }
		    	    
 @SuppressWarnings("unused")
public Map<String, String> execute(RowData inputData, RowData no, RowData vr,String sheetName, int i, String excelPath) throws Exception{
	            String mainWindowHandle = driver.getWindowHandle();
		        Map<String, String> result = new HashMap<>();
		        JavascriptExecutor js = (JavascriptExecutor) driver;
		        String errorMsg = "";
		        
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
	   
		    }catch (Exception e) {
		    	
		        logWindowHandlesAndTitles();
	            throw new RuntimeException("Failed to switch to CoreServer/FINW frames: " + e.getMessage(), e);
		    }
	    
		        String funCode = inputData.getByIndex(2);
		        WindowHandle.selectDropdownIfValuePresent(driver, wait, By.id("funcCode"), funCode);
		        String tranTypeSubType = inputData.getByIndex(3);

		        if (funCode.equalsIgnoreCase("A - Add") &&
		                (
		                    tranTypeSubType.equalsIgnoreCase("C/NP - Cash/Normal Payment") ||
		                    tranTypeSubType.equalsIgnoreCase("C/NR - Cash/Normal Receipt") ||
		                    tranTypeSubType.equalsIgnoreCase("C/PI - Cross CCY Payment Implicit") ||
		                    tranTypeSubType.equalsIgnoreCase("C/RI - Cross CCY Receipt Implicit")||
		                    tranTypeSubType.equalsIgnoreCase("T/BI - Bank Induced"))) {

		            WindowHandle.selectDropdownIfValuePresent(driver, wait, By.id("tranTypeSubType"), tranTypeSubType);
		            System.out.println("tranTypeSubType = " + tranTypeSubType);

		        } else {
		            WindowHandle.setValueWithJS(driver, wait.until(ExpectedConditions.presenceOfElementLocated(By.id("tranId"))), no.getByHeader("TRAN_ID"));
		        }


		        	 retryingClick(By.id("Go"));
		        	 
		        	  errorMsg = ErrorCapture.checkForApplicationError(driver);
		                if (errorMsg != null && !errorMsg.trim().isEmpty()) {
		                	result.put("errorMsg", errorMsg);
		                    return result;
		                }
		        
		        
		        
//-------------------------------- ----------------------------- Delete -------------------------------------------------------------------------//
		        if (funCode.equalsIgnoreCase("D - Delete")) {
		        	WindowHandle.handlePopupIfExists(driver);
		        	 clickSubmitAndHandlePopup(inputData,mainWindowHandle,excelPath,sheetName,i,result);
	                 return result;
		        }


//------------------------------------ ----------------------------- Add -------------------------------------------------------------------------//
		        if (!funCode.equalsIgnoreCase("D - Delete") && !funCode.equalsIgnoreCase("M - Modify") && !funCode.equalsIgnoreCase("I - Inquire") 
		        		&& !funCode.equalsIgnoreCase("V - Verify")&& !funCode.equalsIgnoreCase("P - Post")) {
		           
		 
		            	String excelFlag = inputData.getByIndex(4);
		            	
		     //--------------------------Condition D/C----------------------------------------------------------//
		            	WebElement debitRadio = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("pTranType")));
		            	boolean isDebitSelected = debitRadio.isSelected();
		            	System.out.println("Is Debit Selected: " + isDebitSelected);
		            	System.out.println("Excel Flag: " + excelFlag);

		            	if ("Y".equalsIgnoreCase(excelFlag) && !isDebitSelected) {
		            	    System.out.println("➡️ Changing to Debit");
		            	    debitRadio.click();
		            	} else {
		            	    System.out.println("ℹ️ No change required.");
		            	}

		             
		                
		                WindowHandle.setValueWithJS(driver, waitForVisibility(By.id("acctId"), 5), inputData.getByIndex(5));
		                WindowHandle.setValueWithJS(driver, waitForVisibility(By.id("refCrncy"), 5), inputData.getByIndex(6));
		                WindowHandle.setValueWithJS(driver, waitForVisibility(By.id("refAmt"), 5), inputData.getByIndex(7));
		                WindowHandle.setValueWithJS(driver, waitForVisibility(By.id("tranParticularsCode"), 5), inputData.getByIndex(8));
		                WindowHandle.setValueWithJS(driver, waitForVisibility(By.id("tranParticular"), 5), inputData.getByIndex(9));
	                
		                String valueDate = inputData.getByIndex(10);
		                if (valueDate != null && !valueDate.trim().isEmpty()) {
		                    WebElement dateField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("valueDate_ui")));
		                    dateField.clear();
		                    String formattedDate = DateUtils.toDDMMYYYY(valueDate);
		                    ((JavascriptExecutor) driver).executeScript(
		                        "arguments[0].value='" + formattedDate + "';", dateField);
		                    dateField.sendKeys(Keys.TAB);
		                }

		                //Thread.sleep(1000);
		                WindowHandle.slowDown(2);

		                // ================= DEBUG PRINT ================= //
		                System.out.println("----- FIRST LEG VALUES -----");
		                System.out.println("acctId = " + getFreshValue(driver, wait, By.id("acctId")));
		                System.out.println("refCrncy = " + getFreshValue(driver, wait, By.id("refCrncy")));
		                System.out.println("refAmt = " + getFreshValue(driver, wait, By.id("refAmt")));
		                System.out.println("tranParticularsCode = " + getFreshValue(driver, wait, By.id("tranParticularsCode")));
		                System.out.println("tranParticular = " + getFreshValue(driver, wait, By.id("tranParticular")));
		                System.out.println("valueDate = " + getFreshValue(driver, wait, By.id("valueDate_ui")));

//--------------------------------------Checking the denomation tab--------------------------------------------------------//
		                		                
		                String denomFlag = inputData.getByIndex(11);
		                if ("Y".equalsIgnoreCase(denomFlag))
		                {
		                	System.out.println("➡️ Opening Denomination Details");

		                WebElement denomButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("DENOMDTLS")));
		                denomButton.click();

		                try {
		                	
		                	String rawValue = inputData.getByIndex(12);
		                	System.out.println("Denomination Raw Value: [" + rawValue + "]");
		                  WindowHandle.setValueWithJS(driver, waitForVisibility(By.id("arrDenomCount"), 8), inputData.getByIndex(12));

		                	} catch (Exception e) {
		                    System.err.println("Error setting denomCount: " + e.getMessage());
		                }

		                WebElement OK = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("OK")));
		                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", OK);
		                
		                }else {
		                	System.out.println("ℹ️ Denomination skipped (Flag = " + denomFlag + ")");
		                }

      
	//----------------------------------Add tab---------------------------------------------------------------------------//	                
		                String addFlag = inputData.getByIndex(13);
		                if ("Y".equalsIgnoreCase(addFlag)) {

		       try {
		                	WebElement addBtn = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("partTranDetail_AddNew")));
		                	js.executeScript("arguments[0].click();", addBtn);		               
		                    try {
		                        Alert alert = driver.switchTo().alert();
		                        System.out.println("⚠️ ALERT MESSAGE: " + alert.getText());
		                        alert.accept();
		                       
		                    } catch (NoAlertPresentException e) {
		                        System.out.println("No alert after first leg.");
		                    }
		                    
		                    if (WindowHandle.handleAlertIfPresent(driver)) {
		                        throw new RuntimeException("Add rejected by Finacle: Mandatory field missing");
		                    }
		                    
		                //-----------Record exists or not--------------------------------------//    
		                    boolean secondLegCreated = false;

		                    try {
		                        secondLegCreated = wait.until(d -> {
		                            String val = d.findElement(By.id("partTranDetail_LowLimit")).getAttribute("value");
		                            return "2".equals(val);
		                        });
		                    } catch (Exception e) {
		                        System.out.println("Second leg creation wait timed out.");
		                    }

		                    String currentRecCnt = driver.findElement(By.id("partTranDetail_LowLimit"))
		                                                  .getAttribute("value");

		                    System.out.println("Current recCnt: " + currentRecCnt);

		                    if (!"2".equals(currentRecCnt)) {
		                        throw new RuntimeException("❌ Second leg was NOT created by Finacle.");
		                    }

		//-------------------------Condition to check D / C---------------------------------------------------//
		                    
		                    excelFlag = inputData.getByIndex(14);
		                    WebElement partTranTypeElement = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("pTranType")));
		                    String currentValue = partTranTypeElement.getAttribute("value");

		                    System.out.println("Current Value: " + currentValue);
		                    System.out.println("Excel Flag: " + excelFlag);

		                    if ("Y".equalsIgnoreCase(excelFlag) || "D".equalsIgnoreCase(currentValue)) {
		                        System.out.println("➡️ Changing Debit to Credit");
		                        selectPartTranType(driver, wait, "C");
		                    } else {
		                        System.out.println("ℹ️ No change required.");
		                    }
                    
		                    WindowHandle.setValueWithJS(driver, waitForVisibility(By.id("acctId"), 5), inputData.getByIndex(15));
			                WindowHandle.setValueWithJS(driver, waitForVisibility(By.id("refCrncy"), 5), inputData.getByIndex(16));
			                WindowHandle.setValueWithJS(driver, waitForVisibility(By.id("refAmt"), 5), inputData.getByIndex(17));
			                WindowHandle.setValueWithJS(driver, waitForVisibility(By.id("tranParticularsCode"), 5), inputData.getByIndex(18));
			                WindowHandle.setValueWithJS(driver, waitForVisibility(By.id("tranParticular"), 5), inputData.getByIndex(19));
			      
			                 valueDate = inputData.getByIndex(10);
			                if (valueDate != null && !valueDate.trim().isEmpty()) {
			                    WebElement dateField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("valueDate_ui")));
			                    dateField.clear();
			                    String formattedDate = DateUtils.toDDMMYYYY(valueDate);
			                    ((JavascriptExecutor) driver).executeScript(
			                        "arguments[0].value='" + formattedDate + "';", dateField);
			                    dateField.sendKeys(Keys.TAB);
			                }
				       }catch(Exception e) {
				    	   
				       }
		                }
//**************************************Debit**************************************************************************************************//		       
		       
		       String debitflag2  = inputData.getByIndex(21);
               if ("Y".equalsIgnoreCase(debitflag2)) {

      try {
               	WebElement addBtn = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("partTranDetail_AddNew")));
               	js.executeScript("arguments[0].click();", addBtn);

                   try {
                       Alert alert = driver.switchTo().alert();
                       System.out.println("⚠️ ALERT MESSAGE: " + alert.getText());
                       alert.accept();
                   } catch (NoAlertPresentException e) {
                       System.out.println("No alert after first leg.");
                   }
                   
                   if (WindowHandle.handleAlertIfPresent(driver)) {
                       throw new RuntimeException("Add rejected by Finacle: Mandatory field missing");
                   }
                   
               //-----------Record exists or not--------------------------------------//    
                   boolean secondLegCreated = false;

                   try {
                       secondLegCreated = wait.until(d -> {
                           String val = d.findElement(By.id("partTranDetail_LowLimit")).getAttribute("value");
                           return "3".equals(val);
                       });
                   } catch (Exception e) {
                       System.out.println("Third leg creation wait timed out.");
                   }

                   String currentRecCnt = driver.findElement(By.id("partTranDetail_LowLimit"))
                                                 .getAttribute("value");

                   System.out.println("Current recCnt: " + currentRecCnt);

                   if (!"3".equals(currentRecCnt)) {
                       throw new RuntimeException("❌ Second leg was NOT created by Finacle.");
                   }

//-------------------------Condition to check D / C---------------------------------------------------//
                   
                   excelFlag = inputData.getByIndex(22);
                   WebElement partTranTypeElement = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("pTranType")));
                   String currentValue = partTranTypeElement.getAttribute("value");

                   System.out.println("Current Value: " + currentValue);
                   System.out.println("Excel Flag: " + excelFlag);

                   if ("Y".equalsIgnoreCase(excelFlag) || "C".equalsIgnoreCase(currentValue)) {
                       System.out.println("➡️ Changing Credit to Debit");
                       selectPartTranType(driver, wait, "D");
                   } else {
                       System.out.println("ℹ️ No change required.");
                   }
           
                   WindowHandle.setValueWithJS(driver, waitForVisibility(By.id("acctId"), 5), inputData.getByIndex(23));
	                WindowHandle.setValueWithJS(driver, waitForVisibility(By.id("refCrncy"), 5), inputData.getByIndex(24));
	                WindowHandle.setValueWithJS(driver, waitForVisibility(By.id("refAmt"), 5), inputData.getByIndex(25));
	                WindowHandle.setValueWithJS(driver, waitForVisibility(By.id("tranParticularsCode"), 5), inputData.getByIndex(26));
	                WindowHandle.setValueWithJS(driver, waitForVisibility(By.id("tranParticular"), 5), inputData.getByIndex(27));
	      
	                 valueDate = inputData.getByIndex(28);
	                if (valueDate != null && !valueDate.trim().isEmpty()) {
	                    WebElement dateField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("valueDate_ui")));
	                    dateField.clear();
	                    String formattedDate = DateUtils.toDDMMYYYY(valueDate);
	                    ((JavascriptExecutor) driver).executeScript("arguments[0].value='" + formattedDate + "';", dateField);
	                    dateField.sendKeys(Keys.TAB);
	                }
		       }catch(Exception e) {
		    	   
		       }
               }
    //**************************************Credit**************************************************************************************************//		       
      
      String creditflag2  = inputData.getByIndex(29);
      if ("Y".equalsIgnoreCase(creditflag2)) {

try {
      	WebElement addBtn = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("partTranDetail_AddNew")));
      	js.executeScript("arguments[0].click();", addBtn);

          try {
              Alert alert = driver.switchTo().alert();
              System.out.println("⚠️ ALERT MESSAGE: " + alert.getText());
              alert.accept();
              Thread.sleep(1500);
          } catch (NoAlertPresentException e) {
              System.out.println("No alert after first leg.");
          }
          
          if (WindowHandle.handleAlertIfPresent(driver)) {
              throw new RuntimeException("Add rejected by Finacle: Mandatory field missing");
          }
          
      //-----------Record exists or not--------------------------------------//    
          boolean secondLegCreated = false;

          try {
              secondLegCreated = wait.until(d -> {
                  String val = d.findElement(By.id("partTranDetail_LowLimit")).getAttribute("value");
                  return "4".equals(val);
              });
          } catch (Exception e) {
              System.out.println("Third leg creation wait timed out.");
          }

          String currentRecCnt = driver.findElement(By.id("partTranDetail_LowLimit"))
                                        .getAttribute("value");

          System.out.println("Current recCnt: " + currentRecCnt);

          if (!"4".equals(currentRecCnt)) {
              throw new RuntimeException("❌ Second leg was NOT created by Finacle.");
          }

//-------------------------Condition to check D / C---------------------------------------------------//
          
          excelFlag = inputData.getByIndex(30);
          WebElement partTranTypeElement = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("pTranType")));
          String currentValue = partTranTypeElement.getAttribute("value");

          System.out.println("Current Value: " + currentValue);
          System.out.println("Excel Flag: " + excelFlag);

          if ("Y".equalsIgnoreCase(excelFlag) || "D".equalsIgnoreCase(currentValue)) {
              System.out.println("➡️ Changing debit to credit");
              selectPartTranType(driver, wait, "C");
          } else {
              System.out.println("ℹ️ No change required.");
          }
  
          WindowHandle.setValueWithJS(driver, waitForVisibility(By.id("acctId"), 5), inputData.getByIndex(31));
           WindowHandle.setValueWithJS(driver, waitForVisibility(By.id("refCrncy"), 5), inputData.getByIndex(32));
           WindowHandle.setValueWithJS(driver, waitForVisibility(By.id("refAmt"), 5), inputData.getByIndex(33));
           WindowHandle.setValueWithJS(driver, waitForVisibility(By.id("tranParticularsCode"), 5), inputData.getByIndex(34));
           WindowHandle.setValueWithJS(driver, waitForVisibility(By.id("tranParticular"), 5), inputData.getByIndex(35));
 
            valueDate = inputData.getByIndex(36);
           if (valueDate != null && !valueDate.trim().isEmpty()) {
               WebElement dateField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("valueDate_ui")));
               dateField.clear();
               String formattedDate = DateUtils.toDDMMYYYY(valueDate);
               ((JavascriptExecutor) driver).executeScript("arguments[0].value='" + formattedDate + "';", dateField);
               dateField.sendKeys(Keys.TAB);
           }
           }catch(Exception e) {
   	   
             }
         }
		                
		                clickSubmitAndHandlePopup(inputData,mainWindowHandle,excelPath,sheetName,i,result);
		                 return result;

 }
//------------------------------------------Post-----------------------------------------------------------------
		        if (funCode.equalsIgnoreCase("P - Post")) {
		        	
		        	 clickSubmitAndHandlePopup(inputData,mainWindowHandle,excelPath,sheetName,i,result);
	                 return result;
		        }
 // ----------------------------- Modify -----------------------------------------------------------------------------------------------------------//
		        if (funCode.equalsIgnoreCase("M - Modify")) {
		        	  WindowHandle.setValueWithJS(driver, waitForVisibility(By.id("refAmt"), 5), inputData.getByIndex(7));
		            String denomFlag = inputData.getByIndex(11);
	                if ("Y".equalsIgnoreCase(denomFlag))
	                {
	                	System.out.println("➡️ Opening Denomination Details");

	                WebElement denomButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("DENOMDTLS")));
	                denomButton.click();

	                try {
	                	
	                	 WindowHandle.setValueWithJS(driver, waitForVisibility(By.id("arrDenomCount"), 5), inputData.getByIndex(12));
	                	} catch (Exception e) {
	                    System.err.println("Error setting denomCount: " + e.getMessage());
	                }

	                WebElement OK = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("OK")));
	                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", OK);
	                
	                }else {
	                	System.out.println("ℹ️ Denomination skipped (Flag = " + denomFlag + ")");
	                }

  
		            clickSubmitAndHandlePopup(inputData,mainWindowHandle,excelPath,sheetName,i,result);
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
                    clickSubmitAndHandlePopup(inputData,mainWindowHandle,excelPath,sheetName,i,result);
	                 return result;
                }        
//---------------------- ----------------------------- Delete ---------------------------------------------------------------------------------//
		        
		        if (funCode.equalsIgnoreCase("D - Delete")) {
		        	WindowHandle.handlePopupIfExists(driver);
		        	 clickSubmitAndHandlePopup(inputData,mainWindowHandle,excelPath,sheetName,i,result);
	                 return result;
		            
		        }
		        
		        return result;
		    }

 //********************************************Verification & Validation *****************************************************************************************//    
 private static Map<String, String> getApplicationData(RowData vr,WebDriver driver, WebDriverWait wait) throws Exception {
	  Map<String, String> appData = new LinkedHashMap<>();
    
     String funCode = vr.getByIndex(2);      
     System.out.println("Verify functioncode :" + funCode );
   

     int totalLegs = 1;

     try {
         String recCount = driver.findElement(By.id("partTranDetail_LowLimit"))
                                 .getAttribute("value");
         totalLegs = Integer.parseInt(recCount);
     } catch (Exception e) {
         System.out.println("Could not detect leg count. Defaulting to 1.");
     }

     System.out.println("Total Legs Found: " + totalLegs);

     // ----------- Loop through all legs -----------
     for (int leg = 1; leg <= totalLegs; leg++) {

         if (leg > 1) {
             retryingClick(By.id("partTranDetail_NextRec"));
             WindowHandle.slowDown(1);
         }

         storeLegData(driver, wait, appData, leg);
     }

         String denomFlag = vr.getByIndex(11);
         if ("Y".equalsIgnoreCase(denomFlag))
         {
         	System.out.println("➡️ Opening Denomination Details");

         WebElement denomButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("DENOMDTLS")));
         denomButton.click();

          appData.put("arrDenomCount", ExcelUtils.getTextOrValue(driver, wait, "arrDenomCount","id"));
          
          retryingClick(By.id("Back"));

         }

    
      return appData;
  }
 
//**********************************getFreshValue***********************************************************************************// 
 private String getFreshValue(WebDriver driver, WebDriverWait wait, By locator) {

	    for (int i = 0; i < 3; i++) {
	        try {
	            return wait.until(ExpectedConditions.presenceOfElementLocated(locator))
	                       .getAttribute("value");
	        } catch (StaleElementReferenceException e) {
	            System.out.println("Retrying stale element...");
	        }
	    }
	    return "Unable to fetch value";
	}

//*******************************selectPartTranType**************************************************************************************************//
 
 private static void storeLegData(WebDriver driver,
         WebDriverWait wait,
         Map<String, String> appData,
         int leg) {

System.out.println("➡ Capturing Leg " + leg);

String acctId =
ExcelUtils.getTextOrValue(driver, wait, "acctId", "id");

String refCrncy =
ExcelUtils.getTextOrValue(driver, wait, "refCrncy", "id");

String refAmt =
ExcelUtils.getTextOrValue(driver, wait, "refAmt", "id");

String tranCode =
ExcelUtils.getTextOrValue(driver, wait,
"tranParticularsCode", "id");

String tranParticular =
ExcelUtils.getTextOrValue(driver, wait,
"tranParticular", "id");

String valueDate =
ExcelUtils.getTextOrValue(driver, wait,
"valueDate_ui", "id");

String tranType =
driver.findElement(By.id("pTranType"))
.getAttribute("value");

appData.put("acctId_leg" + leg, acctId);
appData.put("refCrncy_leg" + leg, refCrncy);
appData.put("refAmt_leg" + leg, refAmt);
appData.put("tranParticularsCode_leg" + leg, tranCode);
appData.put("tranParticular_leg" + leg, tranParticular);
appData.put("valueDate_leg" + leg, valueDate);
appData.put("pTranType_leg" + leg, tranType);
}

//********************************** selectPartTranType***************************************************************//
 public static void selectPartTranType(WebDriver driver, WebDriverWait wait, String type) {
	    int attempts = 0;

	    while (attempts < 3) {
	        try {
	            String value = type.equalsIgnoreCase("D") ? "D" : "C";
	            By locator = By.xpath("//input[@name='tm.pTranType' and @value='" + value + "']");
	            WebElement radio = wait.until(ExpectedConditions.elementToBeClickable(locator));
	            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", radio);
	            if (!radio.isSelected()) {
	                radio.click();  // REAL click
	            }
	            Thread.sleep(500);
	            System.out.println("✅ Part Tran Type selected: " +
	                    (value.equals("D") ? "Debit" : "Credit"));
	            return; // success

	        } catch (StaleElementReferenceException e) {
	            System.out.println("⚠ Stale element while selecting PartTranType. Retrying...");
	            attempts++;
	        } catch (Exception e) {
	            throw new RuntimeException("❌ Failed to select Part Transaction Type: " + type, e);
	        }
	    }
	    throw new RuntimeException("❌ Could not select Part Transaction Type after retries.");
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
//**************************************waitForVisibility************************************************************************************************//
private static WebElement waitForVisibility(By locator, int timeoutSeconds) {
    WebDriverWait localWait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
    return localWait.until(ExpectedConditions.visibilityOfElementLocated(locator));

}

//*************************************retryingClick*******************************************************************************************************//   	 
private static void retryingClick(By locator) {
    for (int attempt = 1; attempt <= DEFAULT_RETRY; attempt++) {
        try {
            WebElement el = waitForVisibility(locator,5);
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
//*************************************** clickSubmitAndHandlePopup **********************************************//

private Map<String, String> clickSubmitAndHandlePopup(RowData vr,String mainWindowHandle,String excelPath,String sheetName, int i,Map<String, String> result) {

  String fullMessage = "";
  String action;
  String testCaseName = sheetName;
  String funCode = vr.getByIndex(2);

  boolean isPostTC =testCaseName.toUpperCase().startsWith("TC30");

  boolean isVerifyStep = "V - VERIFY".equalsIgnoreCase(funCode);

  try {
      if (isPostTC) {
          retryingClick(By.id("Post"));
          System.out.println("Clicked POST for " + testCaseName);

      } else if ("P - Post".equalsIgnoreCase(funCode)
              || "D - Delete".equalsIgnoreCase(funCode)
              || "V - Verify".equalsIgnoreCase(funCode)) {

          retryingClick(By.id("Submit"));
          System.out.println("Clicked SUBMIT for " + funCode);

      } else {
          retryingClick(By.id("Save"));
          System.out.println("Clicked SAVE for " + testCaseName);
      }

  
      try {
          Alert alert = wait.until(ExpectedConditions.alertIsPresent());
          String alertText = alert.getText();
          System.out.println("⚠️ Alert detected: " + alertText);
          alert.accept(); // or alert.dismiss(); depending on desired behavior

          if (alertText != null && alertText.toLowerCase().contains("enter a value in the field")) {
              String finacleError = captureFinacleTabError(driver);
              if (finacleError != null && !finacleError.trim().isEmpty()) {
                  System.out.println("⚠️ Finacle tab error: " + finacleError);
                  result.put("errorMsg", alertText + " -> " + finacleError);
              } else {
                  result.put("errorMsg", alertText);
              }
              return result;
          }
      } catch (Exception ie) {
          System.out.println("ℹ️ No alert present.");
      }
  }catch (Exception e) {
      String appError = ErrorCapture.checkForApplicationError(driver);
      result.put("errorMsg",
              (appError != null && !appError.isEmpty())
                      ? appError
                      : "Submit failed: " + e.getMessage());
      return result;
  }

  // Handle alert
  try {
      driver.switchTo().alert().accept();
  } catch (Exception ignored) {}

  // Handle popup
  boolean popupHandled = false;
  try {
      popupHandled = clickAcceptRobust(mainWindowHandle, 3);
  } catch (Exception ignored) {}

  if (!popupHandled) {
      try {
          if (driver.getWindowHandles().size() > 1) {
              popupHandled = WindowHandle.HandlePopupAndClickAccept(driver, Duration.ofSeconds(2));
          }
      } catch (Exception ignored) {}
  }

  try {
      logWindowHandlesAndTitles();
  } catch (Exception ignored) {}

  // Close extra windows
  Set<String> allHandles = driver.getWindowHandles();
  System.out.println("[DEBUG] Total windows after submit: " + allHandles.size());

  if (allHandles.size() > 1) {
      for (String handle : allHandles) {
          if (!handle.equals(mainWindowHandle)) {
              try {
                  driver.switchTo().window(handle);
                  System.out.println("[DEBUG] Closing popup: " + driver.getTitle());
                  driver.close();
              } catch (Exception ignored) {}
          }
      }
      driver.switchTo().window(mainWindowHandle);
  }

  driver.switchTo().defaultContent();

  try {
      WindowHandle.ValidationFrame(driver);

      WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
      String tranId = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("tranId")))
                          .getText().trim();
      String tranDate = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("tranDate")))
                            .getText().trim();

      // ✅ SINGLE SOURCE OF TRUTH FOR ACTION
      action = ActionResolver.getAction(funCode, isPostTC);

      fullMessage = "Transaction ID < " + tranId + " / " + tranDate + " > "
                  + action + " successfully.";

      System.out.println(fullMessage);

      result.put("TransactionMessage", fullMessage);
      result.put("tranId", tranId);
      result.put("TransactionMessage", fullMessage);

      // ✅ Excel update ONLY ONCE
      if (!(isPostTC && isVerifyStep)) {
          ExcelUtils.updateExcel(excelPath, sheetName, i, "TRAN_ID", tranId);
          System.out.println("✅ Excel updated: TRAN_ID = " + tranId);
      } else {
          System.out.println("[INFO] Skipped Excel update (VERIFY step of POST TC)");
      }

  } catch (Exception e) {
      String appError = ErrorCapture.checkForApplicationError(driver);
      result.put("errorMsg",
              (appError != null && !appError.isEmpty())
                      ? appError
                      : "Transaction ID capture failed: " + e.getMessage());
  }

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

//************************************captureFinacleTabError***********************************************************************************************//

public static String captureFinacleTabError(WebDriver driver) {
     try {
         WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(8));

         // Ensure we are back to the main frames hierarchy
         driver.switchTo().defaultContent();
         try {
             wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt("loginFrame"));
         } catch (Exception ignored) {}
         try {
             wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt("CoreServer"));
         } catch (Exception ignored) {}
         try {
             wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt("FINW"));
         } catch (Exception ignored) {}

         // 1) Standard Finacle error-details link (if present)
         List<WebElement> errorLink = driver.findElements(By.id("errordetails"));
         if (!errorLink.isEmpty() && errorLink.get(0).isDisplayed()) {
             try {
                 errorLink.get(0).click();  // open error panel
             } catch (Exception ignored) {}
         }

         // 2) Try multiple common locators for error text
         String[] xpaths = new String[]{
                 "//*[contains(@class,'errortext') or contains(@class,'error')]",
                 "//*[@id='errorlist']//*[text()]",
                 "//*[contains(text(),'E-') or contains(text(),'W-') or contains(text(),'ERROR')]"
         };

         StringBuilder sb = new StringBuilder();
         for (String xp : xpaths) {
             try {
                 List<WebElement> errorTexts =
                         wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.xpath(xp)));
                 for (WebElement e : errorTexts) {
                     String txt = e.getText().trim();
                     if (!txt.isEmpty()) {
                         sb.append(txt).append(" | ");
                     }
                 }
                 if (sb.length() > 0) {
                     break; // we captured something
                 }
             } catch (Exception ignoreEach) {
                 // keep trying other xpaths
             }
         }

         String finalMsg = sb.toString().trim();
         if (!finalMsg.isEmpty()) {
             System.out.println("⚠️ Finacle detailed error: " + finalMsg);
             return finalMsg;
         }

     } catch (Exception e) {
         System.out.println("⚠️ captureFinacleTabError failed: " + e.getMessage());
     }

     return null;
 }

//****************************************clickAcceptRobust****************************************************************************************************// 

@SuppressWarnings("unused")
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
}
