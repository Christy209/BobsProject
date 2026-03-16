package Utilities;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

public class ExcelUtils {
	
	private static FileInputStream fi;
	private static FileOutputStream fo;
	private static XSSFWorkbook wb;
	private static XSSFSheet ws;
	private static XSSFRow row;
	private static XSSFCell cell;
	private static XSSFCellStyle style;
	private static XSSFWorkbook workbook;
	  private static XSSFSheet sheet;
	  private DataFormatter formatter = new DataFormatter();
	  public static String latestTranId = null;
	
	public ExcelUtils(InputStream inputStream) throws IOException {
    	try {
             workbook = new XSSFWorkbook(inputStream);
            System.out.println("Workbook loaded successfully");
        } catch (Exception e) {
            System.out.println("Error while loading workbook: " + e.getMessage());
            e.printStackTrace(); // See what’s going wrong here
        }
    }
	public int getRowCount(String sheetName) {
    	System.out.println("Sheet NAme"+sheetName);
        sheet = workbook.getSheet(sheetName);
        return sheet.getLastRowNum();
    }
	public static String formatDate(String inputDate) {
	    if (inputDate == null || inputDate.trim().isEmpty()) {
	        return "";
	    }

	    String[] possibleFormats = {
	        "dd-MMM-yyyy",  // 15-Mar-2025
	        "dd-MM-yyyy",   // 15-03-2025
	        "dd/MM/yyyy",   // 15/03/2025
	        "dd.MM.yyyy"    // 15.03.2025
	    };

	    for (String format : possibleFormats) {
	        try {
	            SimpleDateFormat sourceFormat = new SimpleDateFormat(format);
	            SimpleDateFormat targetFormat = new SimpleDateFormat("dd-MM-yyyy");

	            return targetFormat.format(sourceFormat.parse(inputDate));
	        } catch (Exception ignored) {}
	    }

	    throw new RuntimeException("❌ Invalid date format: " + inputDate);
	}

    public int getCellCount(String sheetName, int rownum) {
        sheet = workbook.getSheet(sheetName);
        row = sheet.getRow(rownum);
        return row.getLastCellNum();
    }

    public String getCellData(String sheetName, int rownum, int colnum) {
        sheet = workbook.getSheet(sheetName);
        row = sheet.getRow(rownum);
        cell = row.getCell(colnum);
        return formatter.formatCellValue(cell);
    }


	public static int getRowCount(String xlfile ,String xlsheet) throws IOException {
		
		fi = new FileInputStream(xlfile);
		wb = new XSSFWorkbook(fi);
		ws = wb.getSheet(xlsheet);
		int rowcount = ws.getLastRowNum();
		wb.close();
		fi.close();
		return rowcount;
	}
	
	public static int getCellCount(String xlfile ,String xlsheet , int rownum) throws IOException {
		
		fi = new FileInputStream(xlfile);
		wb = new XSSFWorkbook(fi);
		ws = wb.getSheet(xlsheet);
		row = ws.getRow(rownum);
		int cellcount = row.getLastCellNum();
		wb.close();
		fi.close();
		return cellcount;
	}
	
	
	public static String getCellData(String xlfile, String xlsheet, int rownum, int column) throws Exception {
	    File file = new File(xlfile);
	    
	    // Ensure the file is properly updated
	    if (file.exists()) {
	        file.setLastModified(System.currentTimeMillis()); // Refresh file timestamp
	    }

	    // Ensure input stream is closed before re-reading
	    FileInputStream fi = null;
	    XSSFWorkbook wb = null;
	    String data = "";

	    try {
	        fi = new FileInputStream(file);
	        wb = new XSSFWorkbook(fi);
	        XSSFSheet ws = wb.getSheet(xlsheet);
	        XSSFRow row = ws.getRow(rownum);
	        XSSFCell cell = row.getCell(column);

	        // Ensure cell formatting is handled properly
	        DataFormatter formatter = new DataFormatter();
	        data = formatter.formatCellValue(cell);

	    } finally {
	        if (wb != null) wb.close();
	        if (fi != null) fi.close();
	    }

	    return data;
	}

	
	//public static void setCellData(String xlfile ,String xlsheet , int rownum , int colnum , boolean data) throws IOException {
		
		public static void setCellData(String xlfile ,String xlsheet , int rownum , int colnum , String data) throws IOException {
		fi = new FileInputStream(xlfile);
		wb = new XSSFWorkbook(fi);
		ws = wb.getSheet(xlsheet);
		row = ws.getRow(rownum);
		cell = row.createCell(colnum);
		cell.setCellValue(data);
		fo = new FileOutputStream(xlfile);
		wb.write(fo);
		wb.close();
		fi.close();
		fo.close();
		
		
}
public static void fillGreenColor(String xlfile ,String xlsheet , int rownum , int colnum) throws IOException {
		
		fi = new FileInputStream(xlfile);
		wb = new XSSFWorkbook(fi);
		ws = wb.getSheet(xlsheet);
		row = ws.getRow(rownum);
		cell = row.getCell(colnum);
		
		style = wb.createCellStyle();
		style.setFillForegroundColor(IndexedColors.GREEN.getIndex());
		style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		
		cell.setCellStyle(style);
		fo = new FileOutputStream(xlfile);
		wb.write(fo);
		wb.close();
		fi.close();
		fo.close();
		
		
}

public static void fillRedColor(String xlfile ,String xlsheet , int rownum , int colnum) throws IOException {
	
	fi = new FileInputStream(xlfile);
	wb = new XSSFWorkbook(fi);
	ws = wb.getSheet(xlsheet);
	row = ws.getRow(rownum);
	cell = row.getCell(colnum);
	
	style = wb.createCellStyle();
	style.setFillForegroundColor(IndexedColors.RED.getIndex());
	style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
	
	cell.setCellStyle(style);
	fo = new FileOutputStream(xlfile);
	wb.write(fo);
	wb.close();
	fi.close();
	fo.close();
	
	
}
public static void updateExcel(String filePath, String sheetName, int rowNum, String columnName, String newValue) throws IOException {
    FileInputStream file = new FileInputStream(new File(filePath));
    Workbook workbook = new XSSFWorkbook(file);
    Sheet sheet = workbook.getSheet(sheetName);

    // Find column index for "Created_AccountID"
    Row headerRow = sheet.getRow(0);
    int columnIndex = -1;
    for (Cell cell : headerRow) {
        if (cell.getStringCellValue().trim().equalsIgnoreCase(columnName)) {
            columnIndex = cell.getColumnIndex();
            break;
        }
    }

    if (columnIndex == -1) {
        System.out.println("❌ Column '" + columnName + "' not found!");
        workbook.close();
        return;
    }

    // Get row and update value
    Row row = sheet.getRow(rowNum); // Row index 1 (second row)
    if (row == null) {
        row = sheet.createRow(rowNum);
    }

    Cell cell = row.getCell(columnIndex, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
    cell.setCellValue(newValue);

    // Save the changes
    file.close();
    FileOutputStream outFile = new FileOutputStream(new File(filePath));
    workbook.write(outFile);
    outFile.close();
    workbook.close();

    System.out.println("✅ Excel updated: " + columnName + " = " + newValue);
}

	public static String getTextOrValue(WebDriver driver, WebDriverWait wait, String locator, String type) {
        try {
            By byLocator;

            switch (type.toLowerCase()) {
                case "id":
                    byLocator = By.id(locator);
                    break;
                case "name":
                    byLocator = By.name(locator);
                    break;
                case "xpath":
                    byLocator = By.xpath(locator);
                    break;
                default:
                    return "Invalid locator type: " + type;
            }

            WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(byLocator));
            String text = element.getText().trim();

            if (text.isEmpty()) {
                text = element.getAttribute("value");
            }
            if (text.isEmpty()) {
                JavascriptExecutor js = (JavascriptExecutor) driver;
                text = (String) js.executeScript("return arguments[0].value;", element);
            }

            if (element.getTagName().equalsIgnoreCase("select")) {
                Select select = new Select(element);
                return select.getFirstSelectedOption().getText();
            }

            return text != null ? text : "N/A";

        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
        
    }
	
	  public static void saveToFile(String fileName, String data) {
	        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
	            writer.write(data);
	            System.out.println("Data saved to: " + fileName);
	        } catch (IOException e) {
	            System.err.println("Error writing to file: " + e.getMessage());
	        }
	    }
	  public static Row getRow(String sheetName, int rowIndex) {
		    Sheet sheet = workbook.getSheet(sheetName);
		    if (sheet == null) {
		        throw new IllegalArgumentException("Sheet not found: " + sheetName);
		    }
		    Row row = sheet.getRow(rowIndex);
		    if (row == null) {
		        throw new IllegalArgumentException("Row " + rowIndex + " not found in sheet: " + sheetName);
		    }
		    return row;
		}
	  
	    public static Map<String, String> getRowAsMap(String sheetName, int rowIndex) {
	        Sheet sheet = workbook.getSheet(sheetName);
	        if (sheet == null) {
	            throw new IllegalArgumentException("Sheet not found: " + sheetName);
	        }
	        Row headerRow = sheet.getRow(0);
	        if (headerRow == null) {
	            throw new IllegalArgumentException("Header row (row 0) missing in sheet: " + sheetName);
	        }
	        Row dataRow = sheet.getRow(rowIndex);
	        if (dataRow == null) {
	            throw new IllegalArgumentException("Row " + rowIndex + " not found in sheet: " + sheetName);
	        }

	        Map<String, String> data = new HashMap<>();
	        int lastCellNum = headerRow.getLastCellNum();

	        for (int i = 0; i < lastCellNum; i++) {
	            Cell headerCell = headerRow.getCell(i);
	            Cell dataCell = dataRow.getCell(i);

	            String key = headerCell != null ? headerCell.getStringCellValue().trim() : "Column" + i;
	            String value = getCellValueAsString(dataCell);

	            data.put(key, value);
	        }
	        return data;
	    }
	    
	    private static String getCellValueAsString(Cell cell) {
	        if (cell == null) {
	            return "";
	        }
	        switch (cell.getCellType()) {
	            case STRING:
	                return cell.getStringCellValue();
	            case NUMERIC:
	                if (DateUtil.isCellDateFormatted(cell)) {
	                    return cell.getDateCellValue().toString();
	                } else {
	                    double d = cell.getNumericCellValue();
	                    if (d == Math.floor(d)) {
	                        return String.valueOf((long)d);
	                    }
	                    return String.valueOf(d);
	                }
	            case BOOLEAN:
	                return String.valueOf(cell.getBooleanCellValue());
	            case FORMULA:
	                return cell.getCellFormula();
	            case BLANK:
	                return "";
	            default:
	                return "";
	        }
	    }
	   
		
		 
		    
		    
		    public static List<String> getRowAsList(String sheetName, int rowIndex) {
		        Sheet sheet = workbook.getSheet(sheetName);
		        if (sheet == null) {
		            throw new IllegalArgumentException("Sheet not found: " + sheetName);
		        }
		        Row row = sheet.getRow(rowIndex);
		        if (row == null) {
		            throw new IllegalArgumentException("Row " + rowIndex + " not found in sheet: " + sheetName);
		        }
		        List<String> rowData = new ArrayList<>();
		        int lastCellNum = row.getLastCellNum();
		        for (int i = 0; i < lastCellNum; i++) {
		            Cell cell = row.getCell(i);
		            rowData.add(getCellValueAsString(cell));
		        }
		        return rowData;
		    }
		    
		    public static void loadExcel(String filePath) throws IOException {
		        FileInputStream fis = new FileInputStream(filePath);
		        workbook = new XSSFWorkbook(fis);
		    }
		    public static RowData getRowAsRowData(String sheetName, int rowIndex) throws IOException {
		        Sheet sheet = workbook.getSheet(sheetName);
		        Row headerRow = sheet.getRow(0); // assuming headers are in row 0
		        Row dataRow = sheet.getRow(rowIndex);

		        if (headerRow == null || dataRow == null) {
		            throw new IllegalArgumentException("Header row or data row is null");
		        }

		        int cellCount = headerRow.getLastCellNum();
		        String[] headers = new String[cellCount];
		        String[] rowData = new String[cellCount];

		        for (int i = 0; i < cellCount; i++) {
		            Cell headerCell = headerRow.getCell(i);
		            Cell dataCell = dataRow.getCell(i);

		            headers[i] = (headerCell != null) ? headerCell.toString().trim() : "";
		            rowData[i] = (dataCell != null) ? dataCell.toString().trim() : "";
		        }

		        return new RowData(headers, rowData);
		    }
		    public static String formatDate(String inputDate, String inputPattern, String outputPattern) {
		        try {
		            SimpleDateFormat inputFormat = new SimpleDateFormat(inputPattern);
		            SimpleDateFormat outputFormat = new SimpleDateFormat(outputPattern);

		            Date date = inputFormat.parse(inputDate);
		            return outputFormat.format(date);
		        } catch (ParseException e) {
		            System.err.println("❌ Date parsing error: " + e.getMessage());
		            return inputDate; // fallback: return original if parse fails
		        }
		    }
		    public static void clickPopupLinkByText(WebDriver driver, String visibleText) {
		        try {
		            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

		            // Use contains(text()) to support partial or space-trimmed matching
		            String xpath = "//a[contains(normalize-space(text()),'" + visibleText.trim() + "')]";

		            WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(xpath)));

		            // Scroll and JS Click
		            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", element);
		            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);

		            System.out.println("✅ Clicked popup value: " + visibleText);

		        } catch (Exception e) {
		            System.out.println("❌ Failed to click popup value: " + visibleText + " → " + e.getMessage());
		        }
		    }
		    
		    public static List<Map<String, String>> getMultipleRowsAsMaps(String sheetName) {
		        List<Map<String, String>> dataList = new ArrayList<>();

		        Sheet sheet = workbook.getSheet(sheetName);
		        if (sheet == null) {
		            throw new RuntimeException("Sheet " + sheetName + " not found.");
		        }

		        Row headerRow = sheet.getRow(0);
		        int columnCount = headerRow.getPhysicalNumberOfCells();

		        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
		            Row row = sheet.getRow(i);
		            if (row == null) continue;

		            Map<String, String> rowData = new HashMap<>();
		            for (int j = 0; j < columnCount; j++) {
		                Cell keyCell = headerRow.getCell(j);
		                Cell valueCell = row.getCell(j);

		                String key = keyCell.getStringCellValue();
		                String value = valueCell != null ? valueCell.toString().trim() : "";

		                rowData.put(key, value);
		            }

		            // You can add a filter here if needed
		            dataList.add(rowData);
		        }

		        return dataList;
		    }
		    public static List<Map<String, String>> readRelatedPartyRows(String sheetName) {
		        List<Map<String, String>> relatedPartyRecords = new ArrayList<>();

		        try {
		            Sheet sheet = workbook.getSheet(sheetName);
		            if (sheet == null) {
		                throw new IllegalArgumentException("Sheet not found: " + sheetName);
		            }

		            Row headerRow = sheet.getRow(0);
		            int rowCount = sheet.getPhysicalNumberOfRows();
		            int colCount = headerRow.getPhysicalNumberOfCells();

		            for (int i = 1; i < rowCount; i++) {
		                Row row = sheet.getRow(i);
		                if (row == null) continue;

		                Map<String, String> record = new HashMap<>();
		                boolean hasData = false;

		                for (int j = 0; j < colCount; j++) {
		                    Cell headerCell = headerRow.getCell(j);
		                    Cell valueCell = row.getCell(j);

		                    if (headerCell == null) continue;

		                    String key = headerCell.getStringCellValue().trim();
		                    String value = "";

		                    if (valueCell != null) {
		                        switch (valueCell.getCellType()) {
		                            case STRING:
		                                value = valueCell.getStringCellValue().trim();
		                                break;
		                            case NUMERIC:
		                                value = String.valueOf((int) valueCell.getNumericCellValue());
		                                break;
		                            case BOOLEAN:
		                                value = String.valueOf(valueCell.getBooleanCellValue());
		                                break;
		                            default:
		                                value = "";
		                        }
		                    }

		                    if (!value.isEmpty()) {
		                        hasData = true;
		                    }

		                    record.put(key, value);
		                }

		                if (hasData) {
		                    relatedPartyRecords.add(record);
		                }
		            }

		        } catch (Exception e) {
		            System.err.println("Error reading related party rows: " + e.getMessage());
		            e.printStackTrace();
		        }

		        return relatedPartyRecords;
		    }
		    public static String getCellDataByColumnName(String filePath, String sheetName, int rowNum, String columnName) {
		        try (FileInputStream fis = new FileInputStream(filePath);
		             XSSFWorkbook workbook = new XSSFWorkbook(fis)) {

		            Sheet sheet = workbook.getSheet(sheetName);
		            if (sheet == null) {
		                System.out.println("❌ Sheet not found: " + sheetName);
		                return "";
		            }

		            Row headerRow = sheet.getRow(0);
		            if (headerRow == null) {
		                System.out.println("❌ Header row missing in sheet: " + sheetName);
		                return "";
		            }

		            int colIndex = -1;
		            for (int i = 0; i < headerRow.getLastCellNum(); i++) {
		                Cell cell = headerRow.getCell(i);
		                if (cell != null && cell.getStringCellValue().trim().equalsIgnoreCase(columnName)) {
		                    colIndex = i;
		                    break;
		                }
		            }

		            if (colIndex == -1) {
		                System.out.println("❌ Column not found: " + columnName);
		                return "";
		            }

		            Row dataRow = sheet.getRow(rowNum);
		            if (dataRow == null || dataRow.getCell(colIndex) == null) {
		                System.out.println("⚠️ Data not found at row: " + rowNum);
		                return "";
		            }

		            Cell cell = dataRow.getCell(colIndex);
		            String value = "";

		            switch (cell.getCellType()) {
		                case STRING:
		                    value = cell.getStringCellValue().trim();
		                    break;

		                case NUMERIC:
		                    // Preserve leading zeros
		                    BigDecimal bd = new BigDecimal(cell.getNumericCellValue());
		                    value = bd.toPlainString().trim();
		                    break;

		                case FORMULA:
		                    try {
		                        value = cell.getStringCellValue().trim();
		                    } catch (Exception e) {
		                        BigDecimal bdFormula = new BigDecimal(cell.getNumericCellValue());
		                        value = bdFormula.toPlainString().trim();
		                    }
		                    break;

		                case BOOLEAN:
		                    value = Boolean.toString(cell.getBooleanCellValue());
		                    break;

		                case BLANK:
		                    value = "";
		                    break;

		                default:
		                    value = cell.toString().trim();
		            }

		            return value;

		        } catch (Exception e) {
		            System.out.println("❌ Error reading Excel cell: " + e.getMessage());
		            return "";
		        }
		    }
		
		    public static Map<String, String> getRelatedPartyRecordWhereFlagIsY(String sheetName) {
		        List<Map<String, String>> allRecords = readRelatedPartyRows(sheetName); // however you're reading rows
		        for (Map<String, String> record : allRecords) {
		            String flag = record.getOrDefault("RelatedPartyflg", "").trim();
		            if ("Y".equalsIgnoreCase(flag)) {
		                return record;
		            }
		        }
		        return null;
		    }
	    
	   

}
	
	
