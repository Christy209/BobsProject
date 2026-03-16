package Utilities;

import org.apache.poi.util.Units;
import org.apache.poi.xwpf.usermodel.*;
import org.openqa.selenium.*;
import org.openqa.selenium.io.FileHandler;

import java.io.*;

public class ReportUtils {

    private static final String REPORT_PATH = "C:\\FinacleBanking\\Bhatina_Reports\\resources\\AutomationReport.docx";

    public static void captureReportScreenshot(WebDriver driver, String reportName, String description) {
        try {
            // 1️⃣ Take screenshot
            File src = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            String screenshotFolder = "C:\\FinacleBanking\\Bhatina_Reports\\resources\\screenshots";
            //String screenshotFolder = "C:\\\\FinacleBanking\\\\Bhatina_Reports\\\\resources\\\\AutomationReport.docx";
            File folder = new File(screenshotFolder);
            if (!folder.exists()) folder.mkdirs();

            File screenshotFile = new File(folder, reportName + ".png");
            FileHandler.copy(src, screenshotFile);
            System.out.println("Screenshot saved at: " + screenshotFile.getAbsolutePath());

            // 2️⃣ Load existing report or create new
            XWPFDocument doc;
            File reportFile = new File(REPORT_PATH);
            if (reportFile.exists()) {
                try (FileInputStream fis = new FileInputStream(reportFile)) {
                    doc = new XWPFDocument(fis);
                }
            } else {
                doc = new XWPFDocument();
            }

            // 3️⃣ Add description
            XWPFParagraph paragraph = doc.createParagraph();
            paragraph.setSpacingAfter(200);
            XWPFRun run = paragraph.createRun();
            run.setBold(true);
            run.setFontSize(12);
            run.setText("Report: " + description);
            run.addBreak();

            // 4️⃣ Add screenshot
            try (InputStream pic = new FileInputStream(screenshotFile)) {
                @SuppressWarnings("unused")
				XWPFPicture picture = run.addPicture(
                        pic,
                        Document.PICTURE_TYPE_PNG,
                        screenshotFile.getName(),
                        Units.toEMU(500),
                        Units.toEMU(300)
                );
            }

            run.addBreak(BreakType.PAGE);

            // 5️⃣ Save Word report
            try (FileOutputStream out = new FileOutputStream(REPORT_PATH)) {
                doc.write(out);
            }
            doc.close();

            System.out.println("📸 Screenshot appended to report: " + REPORT_PATH);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("❌ Failed to capture screenshot for report: " + reportName, e);
        }
    }
}
