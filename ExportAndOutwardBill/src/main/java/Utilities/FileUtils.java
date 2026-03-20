package Utilities;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FileUtils {

    /**
     * Saves any given text into a .txt file under the specified folder.
     * Automatically creates the folder if it doesn't exist.
     *
     * @param folderPath Folder where the file should be saved (e.g., "C:\\AutomationReports\\HLARA")
     * @param baseFileName Base name for the file (without extension)
     * @param content Text content to write inside the file
     * @return The full absolute file path where content was saved
     */
    public static String saveTextResult(String folderPath, String baseFileName, String content) {
        try {
            // Create the folder if it doesn't exist
            File folder = new File(folderPath);
            if (!folder.exists()) {
                folder.mkdirs();
            }

            // Add timestamp to avoid overwriting
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String fileName = baseFileName + "_" + timestamp + ".txt";
            File file = new File(folder, fileName);

            // Write content into file
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(content);
            }

            System.out.println("✅ Result saved to: " + file.getAbsolutePath());
            return file.getAbsolutePath();

        } catch (IOException e) {
            System.out.println("❌ Error saving result: " + e.getMessage());
            return null;
        }
    }
}
