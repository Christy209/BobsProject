package Utilities;

import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.apache.poi.xwpf.usermodel.*;

public class WordLogger {

    private XWPFDocument document;
    private String filePath;
    @SuppressWarnings("unused")
	private String testCaseName;

    public WordLogger(String testCaseName) {
        this.testCaseName = testCaseName;
        document = new XWPFDocument();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
        String timestamp = LocalDateTime.now().format(formatter);

        filePath = System.getProperty("user.dir") + "/TestLogs/" + testCaseName + "_" + timestamp + ".docx";
    }

    public void log(String message, String colorHex) {
        XWPFParagraph paragraph = document.createParagraph();
        XWPFRun run = paragraph.createRun();
        run.setText(LocalDateTime.now() + " - " + message);
        run.setColor(colorHex);
    }

    public void logSuccess(String message) {
        log("🟢 PASSED: " + message, "00AA00"); // Green
    }

    public void logError(String message) {
        log("❌ FAILED: " + message, "FF0000"); // Red
    }

    public void logInfo(String message) {
        log("ℹ️ INFO: " + message, "000000"); // Black
    }

    public void save() {
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            document.write(fos);
            document.close();
            System.out.println("✅ Word log saved: " + filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getFilePath() {
        return filePath;
    }
}
