package model;

import java.sql.Time;

/** Describes an existing schedule that overlaps a new one (used by the conflict warnings). */
public class ScheduleConflict {
    private final String subjectCode;
    private final String section;
    private final String roomName;
    private final String instructor;
    private final Time startTime;
    private final Time endTime;

    public ScheduleConflict(String subjectCode, String section, String roomName,
                            String instructor, Time startTime, Time endTime) {
        this.subjectCode = subjectCode;
        this.section = section;
        this.roomName = roomName;
        this.instructor = instructor;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public String getSubjectCode() { return subjectCode; }
    public String getSection() { return section; }
    public String getRoomName() { return roomName; }
    public String getInstructor() { return instructor; }
    public Time getStartTime() { return startTime; }
    public Time getEndTime() { return endTime; }
}