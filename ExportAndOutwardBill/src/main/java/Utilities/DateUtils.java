package Utilities;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateUtils {

    private static final String OUTPUT_PATTERN = "dd-MM-yyyy";

    // Other patterns that may come from Excel / DB etc.
    private static final String[] OTHER_PATTERNS = {
        "EEE MMM dd HH:mm:ss z yyyy",  // Sun Mar 15 00:00:00 IST 2029
        "yyyy-MM-dd",
        "dd/MM/yyyy",
        "dd-MM-yyyy",
        "dd-MMM-yyyy"
    };

    /**
     * Converts many possible date strings to dd-MM-yyyy,
     * explicitly handling 2-digit years (e.g. 3/15/25 → 15-03-2025).
     */
    public static String toDDMMYYYY(String raw) throws ParseException {
        if (raw == null || raw.trim().isEmpty()) {
            throw new ParseException("Empty date", 0);
        }
        raw = raw.trim();

        // ✅ Explicitly handle M/d/yy or MM/dd/yy (2-digit year)
        // e.g. 3/15/25  or 03/15/25
        if (raw.matches("\\d{1,2}/\\d{1,2}/\\d{2}$")) {
            String[] p = raw.split("/");
            int month = Integer.parseInt(p[0]);
            int day   = Integer.parseInt(p[1]);
            int year  = Integer.parseInt(p[2]);
            year += 2000; // force 2000-2099 range
            return String.format("%02d-%02d-%04d", day, month, year);
        }

        // 🔄 Try the other known full-year patterns
        for (String pattern : OTHER_PATTERNS) {
            try {
                SimpleDateFormat parser = new SimpleDateFormat(pattern, Locale.ENGLISH);
                parser.setLenient(false);
                Date d = parser.parse(raw);
                return new SimpleDateFormat(OUTPUT_PATTERN).format(d);
            } catch (ParseException ignore) { }
        }

        throw new ParseException("Unrecognized date format: " + raw, 0);
    }

    // Quick test
    public static void main(String[] args) {
        try {
            System.out.println(toDDMMYYYY("3/15/25"));          // 15-03-2025
            System.out.println(toDDMMYYYY("03/03/2026"));       // 03-03-2026
            System.out.println(toDDMMYYYY("2026-03-03"));       // 03-03-2026
            System.out.println(toDDMMYYYY("Tue Mar 03 00:00:00 IST 2026")); // 03-03-2026
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}
