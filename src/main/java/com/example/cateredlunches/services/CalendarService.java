package com.example.cateredlunches.services;

import com.example.cateredlunches.model.CateredWeek;
import com.example.cateredlunches.model.CateredWeekEntry;
import com.example.cateredlunches.model.DayOfWeekIndicator;
import com.example.cateredlunches.model.MenuDay;
import com.example.cateredlunches.services.repositories.MenuRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Optional;

/**
 * Service for displaying and managing the calendar of menus.
 * Delegates all persistence to a {@link MenuRepository}; this class stays
 * focused on CLI interaction and presentation formatting.
 */
@Component
public class CalendarService {
    private final MenuRepository menuRepository;
    private final DisplayService displayService;

    public CalendarService(MenuRepository menuRepository, DisplayService displayService) {
        this.menuRepository = menuRepository;
        this.displayService = displayService;
    }

    /**
     * Build and display the current calendar.
     *
     * @return - A formatted String of the weeks and each catered menu item for that day.
     */
    public String showCalendar() {
        StringBuilder calendarText = new StringBuilder();
        int weekCount = 1;
        for (CateredWeekEntry entry : menuRepository.getAllEntries()) {
            calendarText.append("Week ").append(weekCount++).append(": ").append(entry.getWeekStartKey()).append("\n");
            calendarText.append(formatWeekBody(entry.getWeekValue()));
        }
        return calendarText.toString();
    }

    private String formatWeekBody(CateredWeek week) {
        StringBuilder dayOfTheWeekLine = new StringBuilder();
        StringBuilder menuItemLine = new StringBuilder();
        for (MenuDay day : week.getDays()) {
            String dayLabel = day.getDayOfTheWeek() == null ? "" : day.getDayOfTheWeek().shortName();
            String itemLabel = day.getMenuItem() == null ? "" : day.getMenuItem();
            int padLength = Math.max(dayLabel.length(), itemLabel.length()) + 1;
            dayOfTheWeekLine.append(String.format("%1$-" + padLength + "s |", dayLabel));
            menuItemLine.append(String.format("%1$-" + padLength + "s |", itemLabel));
        }
        return "    | " + dayOfTheWeekLine + "\n" + "    | " + menuItemLine + "\n";
    }

    /**
     * Interaction call to view a single week's menu, identified by its start date.
     */
    public void viewWeek() {
        displayService.displayOnScreen("Enter the week's start date to view (yyyy-MM-dd):");
        LocalDate weekStart = readWeekStartDate();
        if (weekStart == null) {
            return;
        }

        Optional<CateredWeek> week = menuRepository.get(weekStart);
        if (week.isEmpty()) {
            displayService.displayErrorOnScreen("No week found starting " + weekStart);
            return;
        }

        StringBuilder weekText = new StringBuilder();
        weekText.append("Week of ").append(weekStart).append(":\n");
        weekText.append(formatWeekBody(week.get()));
        displayService.displayOnScreen(weekText.toString());
    }

    private LocalDate readWeekStartDate() {
        String rawInput = displayService.waitForInput();
        try {
            return LocalDate.parse(rawInput.trim());
        } catch (DateTimeParseException e) {
            displayService.displayErrorOnScreen("Invalid date: " + rawInput);
            return null;
        }
    }

    /**
     * Interaction call to delete a week from the catered menu, identified by its start date.
     * Requires an explicit y/yes confirmation before deleting.
     */
    public void deleteWeek() {
        displayService.displayOnScreen("Enter the week's start date to remove (yyyy-MM-dd):");
        LocalDate weekStart = readWeekStartDate();
        if (weekStart == null) {
            return;
        }

        displayService.displayOnScreen("Are you sure you want to delete the week starting " + weekStart + "? (y/n)");
        if (!isConfirmed(displayService.waitForInput())) {
            displayService.displayOnScreen("Deletion cancelled.");
            return;
        }

        boolean deleted = menuRepository.delete(weekStart);
        if (!deleted) {
            displayService.displayErrorOnScreen("No week found starting " + weekStart);
        }
    }

    private boolean isConfirmed(String input) {
        String normalized = input == null ? "" : input.trim().toLowerCase();
        return normalized.equals("y") || normalized.equals("yes");
    }

    /**
     * Interaction for adding a week to the catered menu.
     */
    public void addWeek() {
        displayService.displayOnScreen("Enter the week's start date (yyyy-MM-dd):");
        LocalDate weekStart = readWeekStartDate();
        if (weekStart == null) {
            return;
        }

        CateredWeek newWeek = new CateredWeek();
        displayService.displayOnScreen("Enter the menu item for each day of the week\n");
        for (DayOfWeekIndicator dayOfTheWeek : DayOfWeekIndicator.values()) {
            if (dayOfTheWeek.isWeekend()) {
                continue;
            }
            displayService.displayOnScreen(dayOfTheWeek.toString() + ":");
            String menuItem = displayService.waitForInput();
            newWeek.addDay(new MenuDay(dayOfTheWeek, menuItem));
        }

        try {
            menuRepository.upsert(weekStart, newWeek);
        } catch (IllegalArgumentException e) {
            displayService.displayErrorOnScreen("Problem saving week to menu. " + e.getMessage());
        }
    }
}