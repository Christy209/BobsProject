package Utilities;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class OutPutFiles {

    public static String createFileWithTimestamp(String content) {
        try {
            // Generate timestamp
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
            String timestamp = dateFormat.format(new Date());

            // Define the folder where the file will be stored
            String folderPath = "OutPut"; // Change to absolute path if needed
            File folder = new File(folderPath);

            // Create folder if it doesn't exist
            if (!folder.exists()) {
                folder.mkdir();
            }

            // Define file name with full path
            String fileName = folderPath + "/SB_Account_Details_" + timestamp + ".txt";
            File file = new File(fileName);

            // Write content to file
            FileWriter writer = new FileWriter(file);
            writer.write(content);
            writer.close();

            System.out.println("File created: " + fileName);
            return fileName; // Return file path for reference

        } catch (IOException e) {
            System.out.println("Error writing file: " + e.getMessage());
            return null;
        }
    }
}
