package Utilities;

import java.awt.Robot;
import java.awt.event.KeyEvent;

public class PopupHandler {


	    /**
	     * Closes the IE "File Download – Security Warning" popup
	     * @param action "OPEN" to press Enter, "CANCEL" to press Esc
	     */
	    public static void handleIEDownloadPopup(String action) {
	        try {
	            // Wait a little for the popup to appear
	            Thread.sleep(2000);  

	            Robot robot = new Robot();

	            if(action.equalsIgnoreCase("OPEN")) {
	                robot.keyPress(KeyEvent.VK_ENTER);
	                robot.keyRelease(KeyEvent.VK_ENTER);
	            } else if(action.equalsIgnoreCase("CANCEL")) {
	                robot.keyPress(KeyEvent.VK_ESCAPE);
	                robot.keyRelease(KeyEvent.VK_ESCAPE);
	            }

	            // Small delay after pressing key
	            Thread.sleep(500);

	        } catch (Exception e) {
	            System.out.println("Popup handling failed: " + e.getMessage());
	        }
	    }
	}
