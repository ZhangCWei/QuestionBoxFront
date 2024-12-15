package com.example.myapplication;

public class listviewItem {
    private String question;
    private String questionTime;

    public listviewItem(String question, String time) {
        this.question = question;
        this.questionTime= time;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public void setQuestionTime(String time) {
        this.questionTime = time;
    }

    public String getQuestion() {
        return question;
    }

    public String getQuestionTime() {
        return questionTime;
    }

}
