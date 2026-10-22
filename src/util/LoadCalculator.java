package util;

/**
 * Teaching-load rules
 *
 *   Credited (assigned) hours = lecture hours + lab hours x LAB_WEIGHT
 *   FTE (regular load)        = credited hours, up to the regular load (18 hrs)
 *   Overtime (OT)             = credited hours above the regular load, up to MAX_OVERTIME_HOURS
 *   Total load                = FTE + OT
 */
public final class LoadCalculator {

    private LoadCalculator() { }

    public static final double REGULAR_HOURS = 18.0;       
    public static final double MAX_OVERTIME_HOURS = 12.0;
    public static final double LAB_WEIGHT = 0.75;         

    private static final double EPS = 1e-6;

    public static double regularHours(String employmentType) {
        return REGULAR_HOURS;
    }

    public static double creditedHours(double lectureHours, double labHours) {
        return lectureHours + labHours * LAB_WEIGHT;
    }

    public static double fte(double credited, double regular) {
        return Math.min(credited, regular);
    }

    public static double overtime(double credited, double regular) {
        return Math.min(Math.max(credited - regular, 0.0), MAX_OVERTIME_HOURS);
    }

    public static double maxHours(double regular) {
        return regular + MAX_OVERTIME_HOURS;
    }

    public static boolean exceedsMax(double credited, double regular) {
        return credited > maxHours(regular) + EPS;
    }

    public static boolean hasOvertime(double credited, double regular) {
        return credited > regular + EPS;
    }

    public static String status(double credited, double regular) {
        if (credited < EPS) return "No Load";
        if (credited < regular - EPS) return "Regular";
        if (credited <= regular + EPS) return "Full Load";
        if (credited < maxHours(regular) - EPS) return "With OT";
        return "Max OT";
    }
}