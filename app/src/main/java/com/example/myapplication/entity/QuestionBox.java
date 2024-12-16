package com.example.myapplication.entity;

public class QuestionBox {
    private int Id;
    private String sourcePhone;		// 提问的人
    private String targetPhone;		// 被问的人
    private String targetName;
    private String question;
    private String answer;
    private String state;
    private String questionTime;
    private String answerTime;

    public int getId() {
        return Id;
    }
    public String getSourcePhone() {
        return sourcePhone;
    }
    public void setSourcePhone(String sourcePhone) {
        this.sourcePhone = sourcePhone;
    }
    public String getTargetPhone() {
        return targetPhone;
    }
    public void setTargetPhone(String targetPhone) {
        this.targetPhone = targetPhone;
    }
    public String getTargetName() {
        return targetName;
    }
    public void setTargetName(String targetName) {
        this.targetName = targetName;
    }
    public String getQuestion() {
        return question;
    }
    public void setQuestion(String question) {
        this.question = question;
    }
    public String getAnswer() {
        return answer;
    }
    public void setAnswer(String answer) {
        this.answer = answer;
    }
    public String getState() {
        return state;
    }
    public void setState(String state) {
        this.state = state;
    }
    public String getQuestionTime() {
        return questionTime;
    }
    public void setQuestionTime(String time) {
        this.questionTime = time;
    }
    public String getAnswerTime() {
        return answerTime;
    }
    public void setAnswerTime(String time) {
        this.answerTime = time;
    }
}

