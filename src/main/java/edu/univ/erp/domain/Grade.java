package edu.univ.erp.domain;

public class Grade {
    private int gradeId;
    private int studentId;
    private int sectionId;
    private Integer quiz;
    private Integer midsem;
    private Integer endsem;
    private String finalGrade;

    // getters & setters
    public int getGradeId() { return gradeId; }
    public void setGradeId(int gradeId) { this.gradeId = gradeId; }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public int getSectionId() { return sectionId; }
    public void setSectionId(int sectionId) { this.sectionId = sectionId; }

    public Integer getQuiz() { return quiz; }
    public void setQuiz(Integer quiz) { this.quiz = quiz; }

    public Integer getMidsem() { return midsem; }
    public void setMidsem(Integer midsem) { this.midsem = midsem; }

    public Integer getEndsem() { return endsem; }
    public void setEndsem(Integer endsem) { this.endsem = endsem; }

    public String getFinalGrade() { return finalGrade; }
    public void setFinalGrade(String finalGrade) { this.finalGrade = finalGrade; }
}
