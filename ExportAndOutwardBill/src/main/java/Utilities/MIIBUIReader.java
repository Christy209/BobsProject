package Utilities;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class MIIBUIReader {

    public static String getBillId(WebDriver driver) {
        String rawText = driver.findElement(
            By.xpath("//td[text()='Bill ID']/following-sibling::td")
        ).getText().trim();

        // Extract only first value before space (Bill ID)
        if (rawText.contains(" ")) {
            return rawText.split("\\s+")[0].trim();
        } else {
            return rawText;
        }
    }

    public static String getBillType(WebDriver driver) {
        String rawText = driver.findElement(
            By.xpath("//td[text()='Bill Type']/following-sibling::td")
        ).getText().trim();

        // Extract only first part before any space (Bill Type code)
        if (rawText.contains(" ")) {
            return rawText.split("\\s+")[0].trim();
        } else {
            return rawText;
        }
    }
}
