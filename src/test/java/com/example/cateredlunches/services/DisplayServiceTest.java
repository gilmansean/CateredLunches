package com.example.cateredlunches.services;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DisplayServiceTest {

    private final PrintStream originalOut = System.out;
    private final InputStream originalIn = System.in;
    private ByteArrayOutputStream outContent;

    @BeforeEach
    void setUp() {
        outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent, true, StandardCharsets.UTF_8));
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
        System.setIn(originalIn);
    }

    @Test
    void displayOnScreen_printsTextFollowedByNewline() {
        DisplayService displayService = new DisplayService();

        displayService.displayOnScreen("Hello");

        assertEquals("Hello" + System.lineSeparator(), outContent.toString(StandardCharsets.UTF_8));
    }

    @Test
    void showHeader_printsHeaderBanner() {
        DisplayService displayService = new DisplayService();

        displayService.showHeader();

        String output = outContent.toString(StandardCharsets.UTF_8);
        assertTrue(output.contains("Catered Lunch Menu"));
    }

    @Test
    void showFooter_printsFooterBannerAndPrompt() {
        DisplayService displayService = new DisplayService();

        displayService.showFooter();

        String output = outContent.toString(StandardCharsets.UTF_8);
        assertTrue(output.contains("Brought to you by your  employer"));
        assertTrue(output.contains("Choice=>"));
    }

    @Test
    void waitForInput_returnsTrimmedLine() {
        System.setIn(new ByteArrayInputStream("  hello world  \n".getBytes(StandardCharsets.UTF_8)));
        DisplayService displayService = new DisplayService();

        String result = displayService.waitForInput();

        assertEquals("hello world", result);
    }

    @Test
    void waitForInput_multipleLines_returnsEachInSequence() {
        System.setIn(new ByteArrayInputStream("first\nsecond\n".getBytes(StandardCharsets.UTF_8)));
        DisplayService displayService = new DisplayService();

        assertEquals("first", displayService.waitForInput());
        assertEquals("second", displayService.waitForInput());
    }

    @Test
    void displayErrorOnScreen_printsMessageAndConsumesOneLineOfInput() {
        System.setIn(new ByteArrayInputStream("anykey\nnextLine\n".getBytes(StandardCharsets.UTF_8)));
        DisplayService displayService = new DisplayService();

        displayService.displayErrorOnScreen("Something went wrong");

        String output = outContent.toString(StandardCharsets.UTF_8);
        assertTrue(output.contains("Something went wrong"));
        assertTrue(output.contains("Press any key to continue"));

        // The error message consumed the first line as its "press any key" wait;
        // the next waitForInput() call should return the second queued line.
        assertEquals("nextLine", displayService.waitForInput());
    }
}
