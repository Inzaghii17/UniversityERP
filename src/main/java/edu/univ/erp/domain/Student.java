package edu.univ.erp.domain;

public class Student extends User {
    private String rollNo;
    private String program;
    private int year;

    public Student() { super(); }

    public String getRollNo() { return rollNo; }
    public void setRollNo(String rollNo) { this.rollNo = rollNo; }
    public String getProgram() { return program; }
    public void setProgram(String program) { this.program = program; }
    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }
}
