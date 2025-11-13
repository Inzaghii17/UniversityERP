package edu.univ.erp.domain;

public class Instructor extends User {
    private String department;
    public Instructor() { super(); }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
}
