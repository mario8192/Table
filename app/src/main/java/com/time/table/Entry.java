package com.time.table;

public class Entry {
    String day;
    String time;
    String subject;
    String teacher;

    Entry(String day,  String time, String subject, String teacher){
        this.day = day;
        this.time = time;
        this.subject = subject;
        this.teacher = teacher;
    }

    @Override
    public String toString() {
        return "[ "+this.day+" , "+this.time+" , "+this.subject+" , "+this.teacher+" ]";
    }


}
