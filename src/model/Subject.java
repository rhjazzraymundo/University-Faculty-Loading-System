package model;

public class Subject {

    private int subjectId;
    private String subjectCode;
    private String subjectTitle;
    private int units;
    private int departmentId;

    public Subject() {
    }

    public Subject(int subjectId, String subjectCode, String subjectTitle,
                   int units, int departmentId) {
        this.subjectId = subjectId;
        this.subjectCode = subjectCode;
        this.subjectTitle = subjectTitle;
        this.units = units;
        this.departmentId = departmentId;
    }

    public int getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(int subjectId) {
        this.subjectId = subjectId;
    }

    public String getSubjectCode() {
        return subjectCode;
    }

    public void setSubjectCode(String subjectCode) {
        this.subjectCode = subjectCode;
    }

    public String getSubjectTitle() {
        return subjectTitle;
    }

    public void setSubjectTitle(String subjectTitle) {
        this.subjectTitle = subjectTitle;
    }

    public int getUnits() {
        return units;
    }

    public void setUnits(int units) {
        this.units = units;
    }

    public int getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(int departmentId) {
        this.departmentId = departmentId;
    }
}