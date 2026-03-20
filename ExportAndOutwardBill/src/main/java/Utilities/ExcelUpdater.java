package Utilities;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class ExcelUpdater {

    public static void updateCell(String filePath, String sheetName, int rowNumber, int cellNumber, String newValue) {
        try {
            File file = new File(filePath);
            FileInputStream fis = new FileInputStream(file);
            Workbook workbook = new XSSFWorkbook(fis);

            Sheet sheet = workbook.getSheet(sheetName);
            if(sheet == null){
                sheet = workbook.createSheet(sheetName);
            }

            Row row = sheet.getRow(rowNumber);
            if(row == null){
                row = sheet.createRow(rowNumber);
            }

            Cell cell = row.getCell(cellNumber);
            if(cell == null){
                cell = row.createCell(cellNumber);
            }

            cell.setCellValue(newValue);

            fis.close();

            FileOutputStream fos = new FileOutputStream(file);
            workbook.write(fos);
            fos.close();
            workbook.close();

            System.out.println("Excel updated successfully: " + filePath);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
