package com.example.cateredlunches.model;

import java.util.ArrayList;
import java.util.List;

public class CateredWeek {
    private List<MenuDay> days;

    public List<MenuDay> getDays() {
        return days;
    }

    public void setDays(List<MenuDay> days) {
        this.days = days;
    }

    public void addDay(MenuDay day){
        if(days == null){
            days = new ArrayList<>();
        }
        days.add(day);
    }
}
