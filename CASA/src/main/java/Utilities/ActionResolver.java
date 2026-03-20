package Utilities;
import java.util.HashMap;
import java.util.Map;

public class ActionResolver {

    // Static map so it is created only once
    private static final Map<String, String> ACTION_MAP = new HashMap<>();

    static {
        ACTION_MAP.put("D - DELETE", "deleted");
        ACTION_MAP.put("M - MODIFY", "modified");
        ACTION_MAP.put("V - VERIFY", "verified");
        ACTION_MAP.put("A - ADD", "added");
        ACTION_MAP.put("P - POST", "posted");
    }

    /**
     * Resolves action based on funCode and post flag
     *
     * @param funCode  Function code from Excel / UI
     * @param isPostTC True if Post transaction case
     * @return action string
     */
    public static String getAction(String funCode, boolean isPostTC) {

        // Post TC has highest priority
        if (isPostTC) {
            return "posted";
        }

        if (funCode == null || funCode.trim().isEmpty()) {
            return "unknown";
        }

        String normalizedFunCode = funCode.trim().toUpperCase();

        return ACTION_MAP.getOrDefault(normalizedFunCode, "unknown");
    }
}
