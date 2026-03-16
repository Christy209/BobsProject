package Utilities;

import org.apache.poi.util.Units;

import org.apache.poi.xwpf.usermodel.*;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.*;
import java.nio.file.*;
import java.time.Duration;

public class WordReportUtil {

    private static final Path REPORT_PATH =
            Paths.get(System.getProperty("user.dir"), "test-output", "AutomationReports.docx");

    // lock to avoid concurrent writes when tests run in parallel
    private static final Object fileLock = new Object();

    /**
     * Full-page screenshot (waits for document readyState before capture).
     */
    public static void captureScreenshotToWord(WebDriver driver,
                                               String moduleName,
                                               String reportName,
                                               String status) {
        if (driver == null) {
            System.err.println("Driver is null - cannot capture screenshot");
            return;
        }

        try {
            // wait for page to be fully loaded (optional - adjust timeout)
            try {
                new WebDriverWait(driver, Duration.ofSeconds(10))
                        .until(d -> ((JavascriptExecutor) d).executeScript("return document.readyState").equals("complete"));
            } catch (Exception ignored) {}

            // take screenshot into a unique temp file
            Path tmp = Files.createTempFile("screenshot_", ".png");
            File src = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            Files.copy(src.toPath(), tmp, StandardCopyOption.REPLACE_EXISTING);

            addImageToWord(tmp, moduleName, reportName, status);

            Files.deleteIfExists(tmp);
            System.out.println("✅ Screenshot embedded to Word: " + reportName);
        } catch (Exception e) {
            System.err.println("❌ captureScreenshotToWord failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Element-only screenshot (waits for the element visibility).
     */
    public static File captureScreenshot(WebDriver driver) {
        try {
            return ((TakesScreenshot) driver)
                    .getScreenshotAs(OutputType.FILE);
        } catch (Exception e) {
            return null;
        }
    }

    public static void captureElementScreenshotToWord(WebDriver driver,
                                                      By locator,
                                                      String moduleName,
                                                      String reportName,
                                                      String status,
                                                      long timeoutSeconds) {
        if (driver == null) {
            System.err.println("Driver is null - cannot capture screenshot");
            return;
        }

        try {
            WebElement el = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds))
                    .until(ExpectedConditions.visibilityOfElementLocated(locator));

            Path tmp = Files.createTempFile("screenshot_el_", ".png");
            File src = el.getScreenshotAs(OutputType.FILE); // element-level screenshot (Selenium 4+)
            Files.copy(src.toPath(), tmp, StandardCopyOption.REPLACE_EXISTING);

            addImageToWord(tmp, moduleName, reportName, status);

            Files.deleteIfExists(tmp);
            System.out.println("✅ Element screenshot embedded to Word: " + reportName);
        } catch (Exception e) {
            System.err.println("❌ captureElementScreenshotToWord failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    public static void insertImageToWord(String moduleName,
            String stepName,
            File imageFile,
            String status) {
if (imageFile == null || !imageFile.exists()) {
System.err.println("❌ Image file not found: " + imageFile);
return;
}
try {
addImageToWord(imageFile.toPath(), moduleName, stepName, status);
System.out.println("✅ Image inserted into Word: " + stepName);
} catch (Exception e) {
System.err.println("❌ insertImageToWord failed: " + e.getMessage());
e.printStackTrace();
}
}
    /**
     * Core writer - synchronized to protect against parallel writers.
     */
    private static void addImageToWord(Path imagePath, String moduleName, String reportName, String status) {
        synchronized (fileLock) {
            XWPFDocument document = null;
            try {
                // open existing doc or create new
                if (Files.exists(REPORT_PATH)) {
                    try (InputStream in = Files.newInputStream(REPORT_PATH)) {
                        document = new XWPFDocument(in);
                    }
                } else {
                    document = new XWPFDocument();
                }

                // add header text
                XWPFParagraph p = document.createParagraph();
                XWPFRun r = p.createRun();
                r.setBold(true);
                r.setFontSize(12);
                r.setText("Module: " + moduleName);
                r.addBreak();
                r.setText("Report: " + reportName);
                r.addBreak();
                r.setColor(status.equalsIgnoreCase("ERROR") ? "FF0000" : "008000");
                r.setText("Status: " + status);
                r.addBreak();

                // embed image
                try (InputStream pic = Files.newInputStream(imagePath)) {
                    r.addPicture(pic,
                            XWPFDocument.PICTURE_TYPE_PNG,
                            imagePath.getFileName().toString(),
                            Units.toEMU(600),
                            Units.toEMU(350));
                }
                r.addBreak(BreakType.PAGE);

                // write back (atomic replace)
                try (OutputStream out = Files.newOutputStream(REPORT_PATH,
                        StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
                    document.write(out);
                }

            } catch (Exception e) {
                System.err.println("❌ addImageToWord error: " + e.getMessage());
                e.printStackTrace();
            } finally {
                if (document != null) {
                    try { document.close(); } catch (IOException ignored) {}
                }
            }
        }
    }
}
