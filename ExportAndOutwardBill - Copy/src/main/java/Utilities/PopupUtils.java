package Utilities;

import org.openqa.selenium.Alert;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.TimeoutException;
import java.time.Duration;

public class PopupUtils {

    /**
     * Handles repeated alerts before and after clicking a button.
     * @param driver - WebDriver instance
     * @param clicksBefore - Number of alerts to accept before the main action
     * @param clicksAfter - Number of alerts to accept after the main action
     */
    public static void handleRepeatedAlerts(WebDriver driver, int clicksBefore, int clicksAfter, Runnable mainAction) {
        // Handle alerts before main action
        for (int i = 0; i < clicksBefore; i++) {
            try {
                WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(1));
                Alert alert = wait.until(ExpectedConditions.alertIsPresent());
                System.out.println("⚠️ Pre-action Alert #" + (i + 1) + ": " + alert.getText());
                alert.accept();
            } catch (TimeoutException ignored) {
                break; // No more alerts
            }
        }

        // Execute main action (e.g., clicking a button)
        mainAction.run();

        // Handle alerts after main action
        for (int i = 0; i < clicksAfter; i++) {
            try {
                WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(1));
                Alert alert = wait.until(ExpectedConditions.alertIsPresent());
                System.out.println("⚠️ Post-action Alert #" + (i + 1) + ": " + alert.getText());
                alert.accept();
            } catch (TimeoutException ignored) {
                break; // No more alerts
            }
        }
    }
}
