package com.example.cateredlunches.model;

public class MenuDay {
    private DayOfWeekIndicator dayOfTheWeek;
    private String menuItem;

    public MenuDay() {
    }

    public MenuDay(DayOfWeekIndicator dayOfTheWeek, String menuItem) {
        this.dayOfTheWeek = dayOfTheWeek;
        this.menuItem = menuItem;
    }

    public DayOfWeekIndicator getDayOfTheWeek() {
        return dayOfTheWeek;
    }

    public void setDayOfTheWeek(DayOfWeekIndicator dayOfTheWeek) {
        this.dayOfTheWeek = dayOfTheWeek;
    }

    public String getMenuItem() {
        return menuItem;
    }

    public void setMenuItem(String menuItem) {
        this.menuItem = menuItem;
    }
}
