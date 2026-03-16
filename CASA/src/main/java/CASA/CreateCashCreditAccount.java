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
import Utilities.DateUtils;
import Utilities.ErrorCapture;
import Utilities.ExcelUtils;
import Utilities.RowData;
import Utilities.WindowHandle;


public class CreateCashCreditAccount {
	private static WebDriver driver;
	private WebDriverWait wait;
	
	private static final int DEFAULT_RETRY = 3;
	private static final long RETRY_SLEEP_MS = 700;
	
	  @SuppressWarnings("static-access")
	public CreateCashCreditAccount(WebDriver driver) {
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

	    	   WindowHandle.selectByVisibleText(wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("templateFunction"))),inputData.getByIndex(2));
	           WindowHandle.setValueWithJS(driver, waitForVisibility(By.id("cifId"), 8), inputData.getByIndex(3));
	           WindowHandle.setValueWithJS(driver, waitForVisibility(By.id("schmCode"), 8), inputData.getByIndex(4));	
	           WindowHandle.setValueWithJS(driver, waitForVisibility(By.id("glSubHeadCode"), 8), inputData.getByIndex(5));
	           
	           retryingClick(By.id("Accept"));

		    	   errorMsg = ErrorCapture.checkForApplicationError(driver);
		            if (errorMsg != null && !errorMsg.trim().isEmpty()) {
		                result.put("errorMsg", errorMsg);
		                return result;	            
		           }
		       
	            
           //******************************General Details Tab*************************************************//
	 			
		  try {
              WebElement dropdown = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("pbPsFlg")));
              WindowHandle.selectDropdownWithJS(driver, dropdown, inputData.getByIndex(7));  
              
              WindowHandle.setValueWithJS(driver, waitForVisibility(By.id("modeOfOperCode"),5), inputData.getByIndex(8));
              String freqOption = inputData.getByIndex(7);
              if (freqOption != null && !freqOption.equalsIgnoreCase("N - None")) 
              { 
           	   WindowHandle.setValueWithJS(driver, waitForVisibility(By.id("pbPsFreqType"), 5), inputData.getByIndex(9));
                  WindowHandle.setValueWithJS(driver, waitForVisibility(By.id("pbPsFreqWeek"), 5), inputData.getByIndex(10));	            	                
                  WindowHandle.setValueWithJS(driver, waitForVisibility(By.id("pbPsFreqDay"), 5), inputData.getByIndex(11));
                  WindowHandle.setValueWithJS(driver, waitForVisibility(By.id("pbPsFreqStartDD"), 5), inputData.getByIndex(12));
                  WindowHandle.setValueWithJS(driver, waitForVisibility(By.id("pbPsFreqHldyStat"), 5), inputData.getByIndex(13));
                  WindowHandle.setValueWithJS(driver, waitForVisibility(By.id("pbPsFreqCalBase"), 5), inputData.getByIndex(14));
                  WindowHandle.setValueWithJS(driver, waitForVisibility(By.id("despatchMode"), 5), inputData.getByIndex(15));
           } else {
               System.out.println("Skipping frequency-related fields as 'pbPsFlg' is set to 'N - None'.");
           }
   	   }catch (Exception e) {
   		   System.out.println("General Tab Error " + e.getMessage());
   	   }
            
		  //************************Interest Details Tab*************************************************//
		  try {
			  retryingClick(By.id("generaldetails2"));
			  WindowHandle.selectByVisibleText(wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("intDrAcctFlg"))),inputData.getByIndex(16));
			   WindowHandle.slowDown(1);
               String nextIntDrCalcDt_ui = inputData.getByIndex(17);
               if (nextIntDrCalcDt_ui != null && !nextIntDrCalcDt_ui.trim().isEmpty()) {
                   WindowHandle.setValueWithJS(driver, wait.until(ExpectedConditions.presenceOfElementLocated(By.id("nextIntDrCalcDt_ui"))),
                           DateUtils.toDDMMYYYY(nextIntDrCalcDt_ui));
               }
   	           	        
		  }catch (Exception e) {
			  System.out.println("Interest Details Tab Error " + e.getMessage());
		  }
			 retryingClick(By.id("Validate"));
			  
			//****************************************Scheme Details******************************************************************************************//       
		       try {

		    	    WindowHandle.slowDown(1);
		    	    retryingClick(By.id("sbschemedetails"));

		    	    String excelValue = inputData.getByIndex(18);
		    	    List<WebElement> nomineeRadios = driver.findElements(By.name("sbschemedetails.availNomFlg"));
		    	    String selectedValue = "";

		    	    for (WebElement radio : nomineeRadios) {
		    	        if (radio.isSelected()) {
		    	            selectedValue = radio.getAttribute("value");
		    	            break;
		    	        }
		    	    }

		    	    System.out.println("Currently Selected: " + selectedValue);
		    	    System.out.println("Excel Value: " + excelValue);

		    	    if (selectedValue.equalsIgnoreCase("N")&& excelValue.equalsIgnoreCase("Y")) {
		    	        WebElement yesRadio = driver.findElement(By.xpath("//input[@name='sbschemedetails.availNomFlg' and @value='Y']"));

		    	        if (!yesRadio.isSelected()) {
		    	            yesRadio.click();
		    	            System.out.println("Clicked YES radio button");
		    	        }
		    	    }

		    	    WindowHandle.setValueWithJS(driver, waitForVisibility(By.id("acctHlthCode"), 5), inputData.getByIndex(19));
		    	} catch (Exception e) {
		    	    System.out.println("Scheme Details Error: " + e.getMessage());
		    	}
		       
//********************************************************Nominee details******************************************************************//	       
			     try {  
			    	 
			    	 WindowHandle.slowDown(1);
			    	 retryingClick(By.id("nominationdetails"));
		       
		  
			    	 WindowHandle.setValueWithJS(driver, waitForVisibility(By.id("regValue"), 8), inputData.getByIndex(20));
			    	 WindowHandle.setValueWithJS(driver, waitForVisibility(By.id("cifId"), 8), inputData.getByIndex(21));
			    	 WindowHandle.setValueWithJS(driver, waitForVisibility(By.id("nomName"), 8), inputData.getByIndex(22));
			    	 WindowHandle.setValueWithJS(driver, waitForVisibility(By.id("nomAddrLine1"), 8), inputData.getByIndex(23));
			    	 WindowHandle.setValueWithJS(driver, waitForVisibility(By.id("relation"), 8), inputData.getByIndex(24));
			    	 WindowHandle.setValueWithJS(driver, waitForVisibility(By.id("nomStateCode"), 8), inputData.getByIndex(25));
			    	 WindowHandle.setValueWithJS(driver, waitForVisibility(By.id("nomCityCode"), 8), inputData.getByIndex(26));
			    	 WindowHandle.setValueWithJS(driver, waitForVisibility(By.id("nomCntryCode"), 8), inputData.getByIndex(27));
			    	 WindowHandle.setValueWithJS(driver, waitForVisibility(By.id("nomPcnt"), 8), inputData.getByIndex(28));
			    	
		           //------------------------------minor nominee----------------------------------------------------------------------//
		           
			    	 try {

			    		  String NomineeFlag = inputData.getByIndex(29);
			        	    System.out.println("🔍 Checking SecondNomineeflg: '" + NomineeFlag + "'");

			        	    if (NomineeFlag != null && NomineeFlag.trim().equalsIgnoreCase("Y")) {
			        	        System.out.println("✅ Condition Passed! Adding Second Nominee...");

			    		        WebElement yesRadio = driver.findElement(By.xpath("//input[@name='nominationdetails.nomMinorFlg' and @value='Y']"));
			    		        if (!yesRadio.isSelected()) {
			    		            yesRadio.click();
			    		            System.out.println("Clicked YES for Minor Flag");
			    		        }else {
			    		            System.out.println("Minor Flag in excel is N, no action needed.");
			    		        }
			    		    }
			    		    
			    		    WindowHandle.setValueWithJS(driver, waitForVisibility(By.id("grdnName"), 8), inputData.getByIndex(30));
			    		    WindowHandle.selectByVisibleText(wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("grdnCode"))),inputData.getByIndex(31));
			    		    WindowHandle.setValueWithJS(driver, waitForVisibility(By.id("grdnAddrLine1"), 8), inputData.getByIndex(32));
			    		    WindowHandle.setValueWithJS(driver, waitForVisibility(By.id("grdnAddrLine2"), 8), inputData.getByIndex(33));
			    		    WindowHandle.setValueWithJS(driver, waitForVisibility(By.id("grdnAddrLine3"), 8), inputData.getByIndex(34));
			    		    WindowHandle.setValueWithJS(driver, waitForVisibility(By.id("grdnCityCode"), 8), inputData.getByIndex(35));
			    		    WindowHandle.setValueWithJS(driver, waitForVisibility(By.id("grdnStateCode"), 8), inputData.getByIndex(36));
			    		    WindowHandle.setValueWithJS(driver, waitForVisibility(By.id("grdnCntryCode"), 8), inputData.getByIndex(37));
			    		    WindowHandle.setValueWithJS(driver, waitForVisibility(By.id("grdnPostalCode"), 8), inputData.getByIndex(38));   
			    		    
		           } catch (Exception e) {
		               System.out.println("❌ Error in Minor nominee process: " + e.getMessage());
		               e.printStackTrace();
		           }
			    	 
			   WindowHandle.setValueWithJS(driver, waitForVisibility(By.id("nomPostalCode"), 8), inputData.getByIndex(39));
			    	  
		     //----------------------------------------second nomineee-----------------------------------------------------------//
			    	 
		           try {  
		        	    String secondNomineeFlag = inputData.getByIndex(40);
		        	    System.out.println("🔍 Checking SecondNomineeflg: '" + secondNomineeFlag + "'");

		        	    if (secondNomineeFlag != null && secondNomineeFlag.trim().equalsIgnoreCase("Y")) {
		        	        System.out.println("✅ Condition Passed! Adding Second Nominee...");

		        	        retryingClick(By.xpath("//input[@id='nomDetail_AddNew']"));
		        	        System.out.println("✅ 'Add' button clicked as SecondNomineeFlg is Y.");
		        	        
		        	     WindowHandle.setValueWithJS(driver, waitForVisibility(By.id("cifId"), 8), inputData.getByIndex(41));
		       	    	 WindowHandle.setValueWithJS(driver, waitForVisibility(By.id("nomName"), 8), inputData.getByIndex(42));
		       	    	 WindowHandle.setValueWithJS(driver, waitForVisibility(By.id("nomAddrLine1"), 8), inputData.getByIndex(43));
		       	    	 WindowHandle.setValueWithJS(driver, waitForVisibility(By.id("relation"), 8), inputData.getByIndex(44));
		       	    	 WindowHandle.setValueWithJS(driver, waitForVisibility(By.id("nomStateCode"), 8), inputData.getByIndex(45));
		       	    	 WindowHandle.setValueWithJS(driver, waitForVisibility(By.id("nomCityCode"), 8), inputData.getByIndex(46));
		       	    	 WindowHandle.setValueWithJS(driver, waitForVisibility(By.id("nomCntryCode"), 8), inputData.getByIndex(47));
		       	    	 WindowHandle.setValueWithJS(driver, waitForVisibility(By.id("nomPcnt"), 8), inputData.getByIndex(48));
		       	    	 WindowHandle.setValueWithJS(driver, waitForVisibility(By.id("nomPostalCode"), 8), inputData.getByIndex(49));
		         
		        	    } else {
		        	        System.out.println("⏩ Skipping 'Add' button click as SecondNomineeFlg is not Y.");
		        	    }
		        	} catch (Exception e) {
		        	    System.out.println("❌ Error in second nominee process: " + e.getMessage());
		        	    e.printStackTrace();
		        	}


		        } catch (Exception e) {
		             System.out.println("Nominee tab error : " + e.getMessage());
		         }
			  
			   //*************************************************Relationship tab************************************************************//
		         
			       try {
			    	   WindowHandle.slowDown(1);
			    	   retryingClick(By.id("relatedpartydetails"));
				    	   
			    	   WindowHandle.setValueWithJS(driver, waitForVisibility(By.id("relnCode"), 8), inputData.getByIndex(50));

		              //-------------------------Second Relation---------------------------------------------------------------------//	    	   
			               try {
			            	   
			                   if (inputData.getByIndex(51).equalsIgnoreCase("Y")){

			            	   retryingClick(By.xpath("//input[@id='relParty_AddNew']"));
			            	   
			            	   WindowHandle.slowDown(1);
			            	   WindowHandle.selectByVisibleText(wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("relnType"))),inputData.getByIndex(52));
			            	   WindowHandle.setValueWithJS(driver, waitForVisibility(By.id("relnCode"), 8), inputData.getByIndex(53));            	   
			            	   WindowHandle.setValueWithJS(driver, waitForVisibility(By.id("custTitle"), 8), inputData.getByIndex(54));
			            	   WindowHandle.setValueWithJS(driver, waitForVisibility(By.id("custName"), 8), inputData.getByIndex(55));
			            	   WindowHandle.setValueWithJS(driver, waitForVisibility(By.id("custAddrLine1"), 8), inputData.getByIndex(56));
			               } else {
			            	   System.out.println("⏩ Skipping 'Add' button click as SecondRelatedFlg is not Y.");
			               }
		            	   } catch (Exception e) {
		            		   System.out.println("Error in second Related process:: " + e.getMessage());
		            	   }
		   
			           } catch (Exception e) {
			               System.out.println("Relationship tab Error : " + e.getMessage());
			           }
			             
			     //************************************************Document details tab*******************************************************//
				      try {
				    	  WindowHandle.slowDown(1);
				    	  retryingClick(By.id("documentdetails"));
				       
				    	  WindowHandle.setValueWithJS(driver, waitForVisibility(By.id("docCode"), 8), inputData.getByIndex(57));
				    	  WindowHandle.selectByVisibleText(wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("docScanFlg"))),inputData.getByIndex(58));
				    	  retryingClick(By.id("Validate"));   
				       } catch (Exception e) {
				       	System.out.println("Document details  tab Error " + e.getMessage());
				       }
				      
		 //*************************************************FFD tab*******************************************************//
				      String ffdFlag = inputData.getByIndex(59);
				      System.out.println("FFD Flag from Excel: '" + ffdFlag + "'");
				      if (ffdFlag != null && ffdFlag.trim().equalsIgnoreCase("Y")) {
				    	  System.out.println("✅ FFD Flag is Y, proceeding with FFD tab details.");
				      try {
				    	  
				    	  retryingClick(By.id("sbffdparameters"));

				    	  WindowHandle.setValueWithJS(driver, waitForVisibility(By.id("schmCode"), 8), inputData.getByIndex(60));
				    	 
				    	  try {
				    		    WindowHandle.slowDown(1);
				    		    String excelValue = inputData.getByIndex(61);
				    		    List<WebElement> autoSweepRadios = driver.findElements(
				    		            By.name("sbffdparameters.autSwpFlg"));
				    		    String selectedValue = "";
				    		    for (WebElement radio : autoSweepRadios) {
				    		        if (radio.isSelected()) {
				    		            selectedValue = radio.getAttribute("value");
				    		            break;
				    		        }
				    		    }

				    		    System.out.println("UI Selected Value: " + selectedValue);
				    		    System.out.println("Excel Value: " + excelValue);

				    		    if (selectedValue.equalsIgnoreCase("N") 
				    		            && excelValue.equalsIgnoreCase("Y"))
				    		    {

				    		        WebElement yesRadio = driver.findElement(
				    		                By.xpath("//input[@name='sbffdparameters.autSwpFlg' and @value='Y']"));

				    		        if (!yesRadio.isSelected())
				    		        {
				    		            yesRadio.click();
				    		            System.out.println("Clicked YES for Auto Sweep");
				    		        }
				    		        
				    		    }
				    		    WindowHandle.setValueWithJS(driver, waitForVisibility(By.id("swpDepFreqMnths"), 8), inputData.getByIndex(62));
				    		    WindowHandle.setValueWithJS(driver, waitForVisibility(By.id("swpDepFreqDay"), 8), inputData.getByIndex(63));
				    		    WindowHandle.selectByVisibleText(wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("swpFreqType"))),inputData.getByIndex(64));
				    		    WindowHandle.selectByVisibleText(wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("swpFreqStartDD"))),inputData.getByIndex(65));
				    		    WindowHandle.selectByVisibleText(wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("swpHldyStat"))),inputData.getByIndex(66));
				    		    WindowHandle.selectByVisibleText(wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("swpFreqCalBase"))),inputData.getByIndex(67));
				    		    WindowHandle.setValueWithJS(driver, waitForVisibility(By.id("repayInstr"), 8), inputData.getByIndex(68));	   	    	 

				    		} catch (Exception e) {
				    		    System.out.println("AutoSweep Error: " + e.getMessage());
				    		}
				    	  handleSafeCustodyAndPrintReceipt(driver);
				    	  retryingClick(By.id("Validate"));
				      }catch (Exception e) {
				  	   	System.out.println("FFD Tab Error " + e.getMessage());
					   }
	
	//*************************************************Account limits tab*******************************************************//
				      WindowHandle.slowDown(1);
				      try {
				    	   retryingClick(By.id("acctlmt"));
				       
				    	   WindowHandle.slowDown(1);       
				            String DocDate = inputData.getByIndex(69);
				            if (DocDate != null && !DocDate.trim().isEmpty()) {
				                WindowHandle.setValueWithJS(driver, wait.until(ExpectedConditions.presenceOfElementLocated(By.id("documentDate_ui"))),
				                        DateUtils.toDDMMYYYY(DocDate));
				            }
				            
				            String validFrmDate = inputData.getByIndex(70);
				            if (validFrmDate != null && !validFrmDate.trim().isEmpty()) {
				                WindowHandle.setValueWithJS(driver, wait.until(ExpectedConditions.presenceOfElementLocated(By.id("expiryDate_ui"))),
				                        DateUtils.toDDMMYYYY(validFrmDate));
				            }
					           
						    WindowHandle.slowDown(1);	           
				            WindowHandle.selectDropdownWithJS(driver,wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("drawingPowerInd"))),inputData.getByIndex(71));
				            WindowHandle.handleAlertIfPresent(driver);
				            
				            String limitLevelValue =inputData.getByIndex(72); 

				            if (limitLevelValue != null && limitLevelValue.equalsIgnoreCase("Y")) {
				                System.out.println("Excel value is Y → Clicking A/c Level Interest");
				                WebElement accLevelInterest = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//input[@id='limitLevelInterest' and @value='N']")));
				                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", accLevelInterest);

				                   retryingClick(By.id("miscodes"));
				                
				                   WindowHandle.setValueWithJS(driver, wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("sectCode"))),inputData.getByIndex(73));
						           WindowHandle.setValueWithJS(driver, wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("subSectCode"))),inputData.getByIndex(74));
						           WindowHandle.setValueWithJS(driver, wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("purpAdv"))),inputData.getByIndex(75));
						           WindowHandle.setValueWithJS(driver, wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("modeAdv"))),inputData.getByIndex(76));
						           WindowHandle.setValueWithJS(driver, wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("typeAdv"))),inputData.getByIndex(77));
						           WindowHandle.setValueWithJS(driver, wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("natAdv"))),inputData.getByIndex(78));


				            } else {

				                System.out.println("Excel value is NOT Y → Skipping Limit Level Interest section");

				            }
				    	
				       } catch (Exception e) {
				       	System.out.println("Account Limit Tab Error  " + e.getMessage());
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
					  return result;
				      }
	  
	  //*********************************************Handle Safe Custody and Print Receipt Rule*************************************************//
	  @SuppressWarnings("unused")
	public void handleSafeCustodyAndPrintReceipt(WebDriver driver) {

		    try {

		        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
		        WebElement printReceiptYes = wait.until(ExpectedConditions.presenceOfElementLocated(
		                By.xpath("//input[@name='sbffdparameters.printReceipt' and @value='Y']")));

		        WebElement printReceiptNo = wait.until(ExpectedConditions.presenceOfElementLocated(
		                By.xpath("//input[@name='sbffdparameters.printReceipt' and @value='N']")));

		        // 🔹 Locate Safe Custody radio buttons
		        WebElement safeCustodyYes = wait.until(ExpectedConditions.presenceOfElementLocated(
		                By.xpath("//input[@name='sbffdparameters.safeCstdy' and @value='Y']")));

		        WebElement safeCustodyNo = wait.until(ExpectedConditions.presenceOfElementLocated(
		                By.xpath("//input[@name='sbffdparameters.safeCstdy' and @value='N']")));

		        JavascriptExecutor js = (JavascriptExecutor) driver;


		        if (printReceiptNo.isSelected()) {
		            System.out.println("Print Receipt is NO");
		            if (!safeCustodyYes.isSelected()) {
		                System.out.println("Safe Custody is NOT YES. Setting it to YES...");
		                try {
		                    wait.until(ExpectedConditions.elementToBeClickable(safeCustodyYes)).click();
		                } catch (Exception e) {
		                    js.executeScript("arguments[0].click();", safeCustodyYes);
		                }

		                System.out.println("Safe Custody set to YES successfully");
		            }
		        }

		        // =====================================================
		        // 🔥 Optional: Ensure Valid Combination Always
		        // =====================================================

		        // If Safe Custody is NO AND Print Receipt is NO → Fix it
		        if (safeCustodyNo.isSelected() && printReceiptNo.isSelected()) {

		            System.out.println("Invalid combination detected. Fixing...");

		            try {
		                wait.until(ExpectedConditions.elementToBeClickable(safeCustodyYes)).click();
		            } catch (Exception e) {
		                js.executeScript("arguments[0].click();", safeCustodyYes);
		            }

		            System.out.println("Corrected Safe Custody to YES");
		        }

		    } catch (Exception e) {
		        System.out.println("Error while handling Safe Custody rule: " + e.getMessage());
		        e.printStackTrace();
		    }
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
                   WebElement label = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//label[@id='AcctNum']")));
		            labelText = label.getText().trim();
		            
		            String formattedText = "New A/c. ID : " + labelText;
		            if (labelText != null && !labelText.isEmpty()) {
		                ExcelUtils.updateExcel(excelPath, sheetName, i, "Created_AccountID", labelText);
		                System.out.println("[SUCCESS] ✅ Account No updated to Excel: " + labelText);
		                result.put("labelText", formattedText);
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


