package Utilities;

import org.testng.*;

import io.qameta.allure.Allure;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

import org.apache.poi.util.Units;
import org.apache.poi.xwpf.usermodel.*;
import org.openqa.selenium.WebDriver;

import java.nio.file.*;

public class TestReportListener implements ITestListener {

    private XWPFDocument document;
    private String outputFolder = "test-output/TestReports";
    private String individualResultFolder = "TestResults";
    private String latestCopyFolder = "test-output/LatestTxtCopy";  
    private String fileName;
    private int passedCount = 0;
    private int failedCount = 0;
    private int skippedCount = 0;
    private long suiteStartTime;
    private int totalTestCount = 0;

    @Override
    public void onStart(ITestContext context) {
        try {
            new File(outputFolder).mkdirs();
            new File(latestCopyFolder).mkdirs();
            suiteStartTime = System.currentTimeMillis();

            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            fileName = outputFolder + "/Functionality_TestResult_" + timestamp + ".docx";
            document = new XWPFDocument();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onTestStart(ITestResult result) {
        totalTestCount++;
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        addIndividualTestResult(result, true);
        passedCount++;
    }

//    @Override
//    public void onTestFailure(ITestResult result) {
//        addIndividualTestResult(result, false);
//        failedCount++;
//    }
    @Override
    public void onTestFailure(ITestResult result) {
        addIndividualTestResult(result, false);
        attachScreenshotToWord(result);   // ✅ HERE
        failedCount++;
    }


    @Override
    public void onTestSkipped(ITestResult result) {
        XWPFParagraph p = document.createParagraph();
        XWPFRun r = p.createRun();
        r.setText("⚠️ SKIPPED: " + result.getMethod().getMethodName());
        r.setFontSize(11);
        r.setColor("FFA500");
        skippedCount++;
        totalTestCount++;
    }

    private void addIndividualTestResult(ITestResult result, boolean passed) {
        try {
            String testCaseName = result.getTestClass().getRealClass().getSimpleName();

            // Header
            XWPFParagraph header = document.createParagraph();
            XWPFRun title = header.createRun();
            title.setText("📄 Test Case: " + testCaseName);
            title.setBold(true);
            title.setFontSize(12);

            // Status
            XWPFParagraph statusPara = document.createParagraph();
            XWPFRun statusRun = statusPara.createRun();
            statusRun.setFontSize(11);
            statusRun.setBold(true);

            Throwable throwable = result.getThrowable();
            boolean isSkippedAfterSuccess = (throwable instanceof SkipException);

            if (isSkippedAfterSuccess) {
                statusRun.setText("✅ SKIPPED AFTER SUCCESS:");
                statusRun.setColor("008000");
            } else if (passed) {
                statusRun.setText("✅ PASSED:");
                statusRun.setColor("008000");
            } else {
                statusRun.setText("❌ FAILED:");
                statusRun.setColor("FF0000");
            }

            // Get latest .txt log for this test
            File folder = new File(individualResultFolder);
            File[] txtFiles = folder.listFiles((dir, name) ->
                    name.toLowerCase().startsWith(testCaseName.toLowerCase()) && name.endsWith(".txt"));
            File latestTxt = null; // declare only once

            if (txtFiles != null && txtFiles.length > 0) {
                Arrays.sort(txtFiles, Comparator.comparingLong(File::lastModified).reversed());
                latestTxt = txtFiles[0]; // assign, do not redeclare

                try (BufferedReader br = new BufferedReader(new FileReader(latestTxt))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        if (line.trim().isEmpty()) continue;

                        XWPFParagraph para = document.createParagraph();
                        XWPFRun run = para.createRun();
                        run.setFontSize(10);

                        if (line.contains("❌") || line.toLowerCase().contains("error") ||
                            line.toLowerCase().contains("failed") || line.toLowerCase().contains("exception")) {
                            run.setColor("FF0000");
                            run.setBold(true);
                        } else if (line.contains("✅") || line.toLowerCase().contains("success")) {
                            run.setColor("008000");
                            run.setBold(true);
                        } else {
                            run.setColor("000000");
                        }

                        run.setText(line);
                    }
                }
            } else {
                XWPFParagraph para = document.createParagraph();
                XWPFRun run = para.createRun();
                run.setFontSize(10);
                run.setColor("808080");
                run.setText("[No log file found for " + testCaseName + "]");
            }

            // ------------------- Attach TXT log to Allure -------------------
//            try {
//                if (latestTxt != null && latestTxt.exists()) {
//                    try (InputStream is = new FileInputStream(latestTxt)) {
//                        Allure.addAttachment(
//                            testCaseName + "_Log", // Name shown in Allure
//                            "text/plain",          // MIME type
//                            is,                    // InputStream of the TXT file
//                            ".txt"                 // File extension
//                        );
//                    }
//                }
//            } catch (Exception e) {
//                System.out.println("❌ Error attaching TXT to Allure for " + testCaseName + ": " + e.getMessage());
//            }
         // ------------------- Attach TXT log to Allure as separate attachment -------------------
            if (latestTxt != null && latestTxt.exists()) {
                try (InputStream is = new FileInputStream(latestTxt)) {
                    Allure.getLifecycle().addAttachment(
                        testCaseName + "_Log.txt",  // Name of attachment in Allure
                        "text/plain",               // MIME type
                        ".txt",                     // File extension
                        is                          // InputStream
                    );
                } catch (Exception e) {
                    System.out.println("❌ Error attaching TXT to Allure for " + testCaseName + ": " + e.getMessage());
                }
            }

            // Add failure / skip reason
            if (throwable != null) {
                XWPFParagraph reasonPara = document.createParagraph();
                XWPFRun reasonRun = reasonPara.createRun();
                reasonRun.setFontSize(10);

                if (isSkippedAfterSuccess) {
                    reasonRun.setColor("008000");
                    reasonRun.setText("↳ Note: ✅ " + throwable.getMessage());
                } else {
                    reasonRun.setColor("FF0000");
                    reasonRun.setBold(true);
                    reasonRun.setText("↳ Reason: ❌ " + throwable.getMessage());
                }
            }

            // Blank line
            document.createParagraph().createRun().addBreak();

        } catch (Exception e) {
            XWPFParagraph errorPara = document.createParagraph();
            XWPFRun r = errorPara.createRun();
            r.setText("❌ Error writing result for " + result.getName() + ": " + e.getMessage());
            r.setColor("FF0000");
            r.setFontSize(10);
        }
    }


    @SuppressWarnings("unused")
	private void copyLatestTxtFileForTest(String testCaseName) {
        try {
            File folder = new File(individualResultFolder);
            File[] files = folder.listFiles((dir, name) ->
                    name.toLowerCase().contains(testCaseName.toLowerCase()) && name.endsWith(".txt"));

            if (files != null && files.length > 0) {
                Arrays.sort(files, Comparator.comparingLong(File::lastModified).reversed());
                File latestFile = files[0];

                Path source = latestFile.toPath();
                Path destination = Paths.get(latestCopyFolder, latestFile.getName());

                Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (Exception e) {
            System.out.println("❌ Error copying latest txt for " + testCaseName + ": " + e.getMessage());
        }
    }

    @Override
    public void onFinish(ITestContext context) {
        try {
            long suiteEndTime = System.currentTimeMillis();
            long totalDurationMs = suiteEndTime - suiteStartTime;
            long minutes = (totalDurationMs / 1000) / 60;
            long seconds = (totalDurationMs / 1000) % 60;

         // Separator before summary
            XWPFParagraph separator1 = document.createParagraph();
            XWPFRun sepRun1 = separator1.createRun();
            sepRun1.setText("============================================================");

            // Suite Summary title
            XWPFParagraph summaryHeader = document.createParagraph();
            XWPFRun headerRun = summaryHeader.createRun();
            headerRun.setText("📊 Suite Summary: " + context.getName());
            headerRun.setBold(true);
            headerRun.addCarriageReturn();

            // TestNG-style summary block
            XWPFParagraph summaryBlock = document.createParagraph();
            XWPFRun blockRun = summaryBlock.createRun();
            blockRun.setText("===============================================");
            blockRun.addCarriageReturn();
            blockRun.setText(context.getName());
            blockRun.addCarriageReturn();
            blockRun.setText(String.format("Total tests run: %d, Passes: %d, Failures: %d, Skips: %d",
                    totalTestCount, passedCount, failedCount, skippedCount));
            blockRun.addCarriageReturn();
            blockRun.setText("===============================================");
            blockRun.addCarriageReturn();

            // Duration
            XWPFParagraph durationPara = document.createParagraph();
            XWPFRun durRun = durationPara.createRun();
            durRun.setText(String.format("⏱️ Total Duration: %02d min %02d sec", minutes, seconds));

            // Save document
            FileOutputStream out = new FileOutputStream(fileName);
            document.write(out);
            out.close();
            // Save Word document

            // Save TXT version in allure-results
            new File("allure-results").mkdirs();
            String txtFileName = "allure-results/Functionality_TestResult_" +
                    new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".txt";
            saveAsTxt(document, txtFileName);

            document.close();

            // Keep only 10 latest Word reports
            maintainLatestFiles(outputFolder, 10);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveAsTxt(XWPFDocument doc, String txtPath) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(txtPath))) {
            for (XWPFParagraph para : doc.getParagraphs()) {
                writer.write(para.getText());
                writer.newLine();
            }
            System.out.println("✅ Text version saved to: " + txtPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void attachScreenshotToWord(ITestResult result) {
        try {
            Object testClass = result.getInstance();
            WebDriver driver = (WebDriver) testClass
                    .getClass()
                    .getDeclaredField("driver")
                    .get(testClass);

            if (driver == null) return;

            File screenshot = WordReportUtil.captureScreenshot(driver);
            if (screenshot == null) return;

            XWPFParagraph para = document.createParagraph();
            XWPFRun run = para.createRun();
            run.addBreak();
            run.setText("📸 Screenshot:");
            run.addBreak();

            try (InputStream is = new FileInputStream(screenshot)) {
                run.addPicture(
                        is,
                        XWPFDocument.PICTURE_TYPE_PNG,
                        screenshot.getName(),
                        Units.toEMU(500),
                        Units.toEMU(300)
                );
            }
            run.addBreak();

        } catch (Exception e) {
            XWPFParagraph errorPara = document.createParagraph();
            XWPFRun r = errorPara.createRun();
            r.setText("❌ Screenshot capture failed: " + e.getMessage());
            r.setColor("FF0000");
        }
    }

    private void maintainLatestFiles(String folderPath, int maxFiles) {
        File folder = new File(folderPath);
        File[] files = folder.listFiles((dir, name) -> name.endsWith(".docx"));
        if (files != null && files.length > maxFiles) {
            Arrays.sort(files, Comparator.comparingLong(File::lastModified));
            for (int i = 0; i < files.length - maxFiles; i++) {
                files[i].delete();
            }
        }
    }

    @Override public void onTestFailedButWithinSuccessPercentage(ITestResult result) {}
    @Override public void onTestFailedWithTimeout(ITestResult result) {}
}
