package com.example.cateredlunches.services;

import org.springframework.stereotype.Component;

import java.util.Scanner;

/**
 * Consolidated service to display things to the console.  Also has the input interaction.
 */
@Component
public class DisplayService {
    private final Scanner input = new Scanner(System.in);

    /**
     * Prints text to the screen.
     * @param text - What to print on the screen
     */
    public void displayOnScreen(String text){
        System.out.println(text);
    }

    /**
     * Header for displaying the catered menu.
     */
    public void showHeader() {
        displayOnScreen( "############################################################\n" +
                                 "#################### Catered Lunch Menu ####################\n" +
                                 "############################################################\n");
    }

    /**
     * Footer, has the input entry text so user knows they need to do something.
     */
    public void showFooter(){
        displayOnScreen( "============================================================\n" +
                                 "========== Brought to you by your loving employer ==========\n" +
                                 "============================================================\n" +
                                 "Choice=>");
    }

    /**
     * Prints a error message and waits for the user to acknowledge.
     * @param errorMessage - Message of the error.
     */
    public void displayErrorOnScreen(String errorMessage) {
        displayOnScreen(errorMessage+"\nPress any key to continue:\n");
        waitForInput();
    }

    /**
     * Waits for user input and returns the string, minus the ending newline.
     * @return - String that the user input.
     */
    public String waitForInput() {
        return input.nextLine().trim();
    }
}
