package com.example.model;
//補習班定義
public class School {
    private String name;
    private String city;
    private String subject;
    private String url;
    private int score;
    private boolean specialChoice;

    public School(String name, String city, String subject, String url,Boolean specialChoice) {
        this.name = name;
        this.city = city;
        this.subject = subject;
        this.url = url;
        this.score = 0;
        this.specialChoice=false;
    }

    public String getName() {
        return name;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getUrl() {
        return url;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }
    
    public boolean isSpecialChoice() {
        return specialChoice;
    }

    public void setSpecialChoice(boolean specialChoice) {
        this.specialChoice = specialChoice;
    }
    
    
}
