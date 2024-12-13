package com.example.myapplication;

public class listviewItem {
    private String question;
    private String questiontime;
    public listviewItem(String question, String time) {
        this.question = question;
        this.questiontime= time;
    }
    public void setQuestion(String question) {
        this.question = question;
    }
    public void setQuestionTime(String time) {
        this.questiontime = time;
    }
    public String getQuestion() {
        return question;
    }

    public String getQuestionTime() {
        return questiontime;
    }
}
