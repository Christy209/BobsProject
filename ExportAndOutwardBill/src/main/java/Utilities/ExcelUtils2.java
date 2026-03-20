package Utilities;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

public class ExcelUtils2 {
	
	private static FileInputStream fi;
	private static FileOutputStream fo;
	private static XSSFWorkbook wb;
	private static XSSFSheet ws;
	private static XSSFRow row;
	private static XSSFCell cell;
	private static XSSFCellStyle style;
	private static XSSFWorkbook workbook;
	  private XSSFSheet sheet;
	  private DataFormatter formatter = new DataFormatter();
	
	public ExcelUtils2(InputStream inputStream) throws IOException {
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
	
	public static String formatDate(String dateStr) {
	    try {
	        SimpleDateFormat format1 = new SimpleDateFormat("dd-MM-yyyy");
	        Date date = format1.parse(dateStr);
	        return format1.format(date);
	    } catch (Exception e1) {
	        try {
	            SimpleDateFormat format2 = new SimpleDateFormat("MM/dd/yy");
	            Date date = format2.parse(dateStr);
	            SimpleDateFormat outputFormat = new SimpleDateFormat("dd-MM-yyyy");
	            return outputFormat.format(date);
	        } catch (Exception e2) {
	            System.out.println("❌ Invalid date format for: " + dateStr);
	            return "";
	        }
	    }
	}

	public static String cleanString(String input) {
	    if (input == null) return "";
	    return input
	        .replaceAll("[^\\x20-\\x7E]", "") // remove all non-printable ASCII characters
	        .replace("\u00A0", "")           // remove non-breaking space
	        .replaceAll("\\s+", "")          // remove all whitespace (space, tab, etc.)
	        .trim();                         // final trim
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
	public static String getCellData(String xlfile, String xlsheet, int rownum, String columnName) throws IOException {
	    FileInputStream fi = new FileInputStream(xlfile);
	    XSSFWorkbook wb = new XSSFWorkbook(fi);
	    XSSFSheet sheet = wb.getSheet(xlsheet);

	    // Read header row to find the correct column index
	    XSSFRow headerRow = sheet.getRow(0);
	    int columnIndex = -1;

	    for (int i = 0; i < headerRow.getLastCellNum(); i++) {
	        XSSFCell headerCell = headerRow.getCell(i);
	        if (headerCell != null && columnName.trim().equalsIgnoreCase(headerCell.getStringCellValue().trim())) {
	            columnIndex = i;
	            break;
	        }
	    }

	    if (columnIndex == -1) {
	        wb.close();
	        fi.close();
	        throw new IllegalArgumentException("❌ Column '" + columnName + "' not found in Excel sheet '" + xlsheet + "'");
	    }

	    XSSFRow dataRow = sheet.getRow(rownum);
	    XSSFCell dataCell = dataRow.getCell(columnIndex);

	    DataFormatter formatter = new DataFormatter();
	    String data = formatter.formatCellValue(dataCell);

	    wb.close();
	    fi.close();

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
}
	
	
