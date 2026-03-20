package Utilities;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

public class Headers {

	
	public static Map<Integer, String> getHeaders(String filePath, String sheetName) throws IOException {
        Map<Integer, String> headers = new HashMap<>();

        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheet(sheetName);
            if (sheet == null) {
                throw new IllegalArgumentException("Sheet " + sheetName + " does not exist");
            }

            Row headerRow = sheet.getRow(0); // assuming first row is header
            if (headerRow == null) {
                throw new IllegalArgumentException("No header row found in sheet");
            }

            for (Cell cell : headerRow) {
                int colIndex = cell.getColumnIndex();
                String headerName = cell.getStringCellValue().trim();
                headers.put(colIndex, headerName);
            }
        }
        return headers;
    }

    /**
     * Example method to read a data row by index as String array
     */
    public static String[] getRowData(String filePath, String sheetName, int rowIndex) throws IOException {
        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheet(sheetName);
            if (sheet == null) {
                throw new IllegalArgumentException("Sheet " + sheetName + " does not exist");
            }

            Row row = sheet.getRow(rowIndex);
            if (row == null) {
                throw new IllegalArgumentException("Row " + rowIndex + " does not exist");
            }

            int lastCellNum = row.getLastCellNum();
            String[] rowData = new String[lastCellNum];
            for (int i = 0; i < lastCellNum; i++) {
                Cell cell = row.getCell(i);
                rowData[i] = (cell == null) ? "" : cell.toString().trim();
            }
            return rowData;
        }        }
}
