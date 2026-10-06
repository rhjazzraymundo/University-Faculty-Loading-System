package model;

import java.sql.Time;

/** Maps to table SCHEDULES. facultyId stays null until a faculty is assigned (Day 6). */
public class Schedule {
    private int scheduleId;
    private int subjectId;
    private Integer facultyId;
    private int roomId;
    private String section;
    private String term;
    private String dayOfWeek;
    private Time startTime;
    private Time endTime;

    public Schedule() { }
}
