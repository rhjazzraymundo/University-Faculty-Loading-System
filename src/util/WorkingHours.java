package util;

import java.sql.Time;

public final class WorkingHours {

    private WorkingHours() { }

    public static final int START_HOUR = 7;  
    public static final int END_HOUR = 18;      
    public static final int DAYS_PER_WEEK = 6;  

    public static final double WEEKLY_AVAILABLE_HOURS = (END_HOUR - START_HOUR) * DAYS_PER_WEEK;   // 66

    public static final String LABEL = "7:00 AM - 6:00 PM";

    public static boolean isWithin(Time start, Time end) {
        int s = minutes(start);
        int e = minutes(end);
        return s >= START_HOUR * 60 && e <= END_HOUR * 60;
    }

    private static int minutes(Time t) {
        String[] p = t.toString().split(":");
        return Integer.parseInt(p[0]) * 60 + Integer.parseInt(p[1]);
    }
}