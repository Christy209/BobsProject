package Utilities;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelReader {

    private final String filePath;

    public ExcelReader(String filePath) {
        this.filePath = filePath;
    }

    public Map<String, String> getRowAsMap(String sheetName, int rowIndex) {
        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheet(sheetName);
            Row headerRow = sheet.getRow(0);
            Row dataRow = sheet.getRow(rowIndex);

            if (headerRow == null || dataRow == null) {
                throw new RuntimeException("❌ Header or data row is missing.");
            }

            DataFormatter formatter = new DataFormatter();
            Map<String, String> dataMap = new HashMap<>();

            for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                Cell headerCell = headerRow.getCell(i);
                Cell dataCell = dataRow.getCell(i);

                String key = (headerCell != null) ? formatter.formatCellValue(headerCell).trim() : "Column" + i;
                String value = (dataCell != null) ? formatter.formatCellValue(dataCell).trim() : "";

                dataMap.put(key, value);
            }

            return dataMap;

        } catch (IOException e) {
            throw new RuntimeException("❌ Error reading Excel file: " + e.getMessage(), e);
        }
    }
}
