package util;

import java.sql.Date;

public final class RequestUtil {

    private RequestUtil() {
    }

    public static int parseInt(String raw, int def) {
        try {
            return (raw == null || raw.isBlank()) ? def : Integer.parseInt(raw);
        } catch (Exception e) {
            return def;
        }
    }

    public static long parseLong(String raw, long def) {
        try {
            return (raw == null || raw.isBlank()) ? def : Long.parseLong(raw);
        } catch (Exception e) {
            return def;
        }
    }

    public static Date parseSqlDate(String s) {
        try {
            if (s == null || s.isBlank()) {
                return null;
            }
            return Date.valueOf(s);
        } catch (Exception e) {
            return null;
        }
    }

}

