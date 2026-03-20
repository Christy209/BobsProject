package Utilities;

import java.io.File;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class Validation {

    // ✅ Normalize string
    public static String normalize(String value) {
        if (value == null) return "";
        return value
                .replaceAll("[\\u00A0\\u2007\\u202F\\u200B\\u200C\\u200D\\uFEFF]", "")
                .replaceAll("\\.0+$", "")
                .trim()
                .toLowerCase();
    }

    // ✅ Check if numeric
    public static boolean isNumeric(String str) {
        if (str == null || str.trim().isEmpty()) return false;
        try {
            new BigDecimal(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    // ✅ Format date
    public static String formatDateToStandard(String dateStr) {
        SimpleDateFormat inputFormat1 = new SimpleDateFormat("dd/MM/yyyy");
        SimpleDateFormat inputFormat2 = new SimpleDateFormat("dd-MM-yyyy");
        SimpleDateFormat outputFormat = new SimpleDateFormat("dd-MM-yyyy");
        try {
            Date date;
            if (dateStr.contains("/")) {
                date = inputFormat1.parse(dateStr);
            } else {
                date = inputFormat2.parse(dateStr);
            }

            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            int year = cal.get(Calendar.YEAR);
            if (year < 1000) {
                cal.set(Calendar.YEAR, year + 2000);
            }

            return outputFormat.format(cal.getTime());
        } catch (ParseException e) {
            return dateStr;
        }
    }

    // ✅ Main validation logic
    public static int validateData(Map<String, String> expected, Map<String, String> actual) {
        System.out.println("\n🔍 Validating Excel vs. Application Data...\n");

        int matchedCount = 0;
        int mismatchCount = 0;
        int missingCount = 0;
        int extraCount = 0;

        for (Map.Entry<String, String> entry : expected.entrySet()) {
            String key = entry.getKey();
            String expectedValue = entry.getValue();
            String actualValue = actual.get(key);

            if (expectedValue == null || expectedValue.trim().isEmpty()) continue;

            if (actualValue == null || actualValue.trim().isEmpty() || actualValue.equalsIgnoreCase("Not Found")) {
                missingCount++;
                continue;
            }

            if (key.toLowerCase().contains("date")) {
                try {
                    expectedValue = formatDateToStandard(expectedValue);
                    actualValue = formatDateToStandard(actualValue);
                } catch (Exception e) {
                    System.out.printf("⚠️ Failed to parse date at key '%s': Expected='%s', Actual='%s'%n", key, expectedValue, actualValue);
                }
            }

            String normExpected = normalize(expectedValue);
            String normActual = normalize(actualValue);

            if (isNumeric(normExpected) && isNumeric(normActual)) {
                BigDecimal excelNum = new BigDecimal(normExpected).stripTrailingZeros();
                BigDecimal appNum = new BigDecimal(normActual).stripTrailingZeros();

                if (excelNum.compareTo(appNum) == 0) {
                    matchedCount++;
                } else {
                    mismatchCount++;
                    System.out.printf("❌ Mismatch at key '%s': Expected='%s', Actual='%s'%n", key, excelNum, appNum);
                }
            } else {
                if (normExpected.equals(normActual)) {
                    matchedCount++;
                } else {
                    mismatchCount++;
                    System.out.printf("❌ Mismatch at key '%s': Expected='%s', Actual='%s'%n", key, expectedValue, actualValue);
                }
            }
        }

        for (String key : actual.keySet()) {
            if (!expected.containsKey(key)) {
                extraCount++;
            }
        }

        System.out.println("--------------------------");
        System.out.printf("✅ Matched Count      : %d%n", matchedCount);
        System.out.printf("❌ Mismatched Count   : %d%n", mismatchCount);
        System.out.printf("⚠️ Missing Count      : %d%n", missingCount);
        System.out.printf("⚠️ Extra Key Count    : %d%n", extraCount);
        System.out.printf("🔢 Total Expected Keys: %d%n", expected.size());

        return mismatchCount;
    }

    // ✅ Optional: fetch latest .txt file
    public static File getLatestFile(String directoryPath, String filePrefix) {
        File directory = new File(directoryPath);
        File[] files = directory.listFiles((dir, name) -> name.startsWith(filePrefix) && name.endsWith(".txt"));

        if (files == null || files.length == 0) return null;

        Arrays.sort(files, Comparator.comparingLong(File::lastModified).reversed());
        return files[0];
    }
}
