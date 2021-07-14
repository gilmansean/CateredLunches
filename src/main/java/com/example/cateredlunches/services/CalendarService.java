package com.example.cateredlunches.services;

import com.example.cateredlunches.model.CateredWeek;
import com.example.cateredlunches.model.MenuDay;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for displaying and managing the calendar of menus
 */
@Component
@Configurable
@ConfigurationProperties(prefix = "calendar")
public class CalendarService {
    private FileService fileService;
    private DisplayService displayService;
    private String[] daysOfTheWeek = {"Monday","Tuesday","Wednesday","Thursday","Friday"};

    private String dataFile;
    private List<CateredWeek> cateredMenu;

    public CalendarService(FileService fileService, DisplayService displayService) {
        this.fileService = fileService;
        this.displayService = displayService;
    }

    @PostConstruct
    public void init() {
        loadCateredMenu();
    }

    private void loadCateredMenu() {
        try {
            cateredMenu = fileService.readFile(dataFile, CateredWeek.class);
        } catch(IOException e) {
            e.printStackTrace();
            cateredMenu = new ArrayList<CateredWeek>();
        }
    }

    /**
     * Build and display the current calendar.
     * @return - A formatted String of the weeks and each catered menu item for that day.
     */
    public String showCalendar() {
        StringBuilder cateredMenu = new StringBuilder();
        int weekCount = 1;
        for(CateredWeek week: this.cateredMenu){
            StringBuilder dayOfTheWeek = new StringBuilder();
            StringBuilder menuItem = new StringBuilder();
            for(MenuDay day: week.getDays()){
                int padLength = day.getDayOfTheWeek().length() + 1;
                if(day.getMenuItem().length() > day.getDayOfTheWeek().length()) {
                    padLength = day.getMenuItem().length() + 1;
                }
                dayOfTheWeek.append(String.format("%1$-" + padLength + "s |", day.getDayOfTheWeek()));
                menuItem.append(String.format("%1$-" + padLength + "s |", day.getMenuItem()));
            }
            cateredMenu.append("Week ").append(weekCount++).append(":\n");
            cateredMenu.append("    | ").append(dayOfTheWeek).append("\n");
            cateredMenu.append("    | ").append(menuItem).append("\n");
        }
        return cateredMenu.toString();
    }

    /**
     * Interaction call to delete a week from the catered menu.
     */
    public void deleteWeek() {
        displayService.displayOnScreen("Enter week number to remove.");
        int weekNumber = Integer.parseInt(displayService.waitForInput());
        CateredWeek removeWeek = cateredMenu.get(weekNumber - 1);
        cateredMenu.remove(removeWeek);
        try {
            fileService.safeToFile(dataFile, cateredMenu);
        } catch(IOException e) {
            displayService.displayErrorOnScreen("Error removing week. "+e.getMessage());
        }
        loadCateredMenu();
    }

    /**
     * Interaction for adding a week to the catered menu.
     */
    public void addWeek() {
        CateredWeek newWeek = new CateredWeek();
        String menuItem = "";
        displayService.displayOnScreen("Enter the menu item for the day of the week\n");
        for(String dayOfTheWeek:daysOfTheWeek) {
            displayService.displayOnScreen(dayOfTheWeek+":");
            menuItem = displayService.waitForInput();
            newWeek.addDay(new MenuDay(dayOfTheWeek, menuItem));
        }
        cateredMenu.add(newWeek);
        try {
            fileService.safeToFile(dataFile, cateredMenu);
        } catch(IOException e) {
            displayService.displayErrorOnScreen("Problem saving week to menu. " + e.getMessage());
        }
        loadCateredMenu();
    }

    public String getDataFile() {
        return dataFile;
    }

    public void setDataFile(String dataFile) {
        this.dataFile = dataFile;
    }

}
