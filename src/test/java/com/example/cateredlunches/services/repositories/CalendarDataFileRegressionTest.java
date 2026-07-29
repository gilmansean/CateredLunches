package com.example.cateredlunches.services.repositories;

import com.example.cateredlunches.model.CateredWeek;
import com.example.cateredlunches.model.MenuDay;
import com.example.cateredlunches.services.repositories.file.FileMenuRepository;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Regression test guarding the committed calendar.data.json against format drift.
 * <p>
 * This file is what a fresh clone sees on first {@code spring-boot:run} - if its shape
 * ever falls out of sync with what the active {@link com.example.cateredlunches.services.repositories.MenuRepository}
 * implementation expects (e.g. a leftover legacy array format vs. the current
 * {"weeks": {...}} object format), the app fails at startup with a JsonSyntaxException.
 * <p>
 * This test loads the real file directly (not a temp copy) through {@link FileMenuRepository}
 * to catch that failure mode in CI/local test runs, before anyone tries to demo the app.
 */
class CalendarDataFileRegressionTest {

    private static final Path REPO_ROOT_DATA_FILE = Path.of("calendar.data.json");

    @Test
    void committedDataFile_exists() {
        assertTrue(Files.exists(REPO_ROOT_DATA_FILE),
                "calendar.data.json should exist at the project root for the default demo experience");
    }

    @Test
    void committedDataFile_parsesWithoutError() {
        FileMenuRepository repository = new FileMenuRepository(REPO_ROOT_DATA_FILE);

        List<CateredWeek> weeks = assertDoesNotThrow(repository::getAll,
                "calendar.data.json failed to parse - check it matches the current "
                        + "{\"weeks\": {\"yyyy-MM-dd\": [...]}} format expected by FileMenuRepository");

        assertNotNull(weeks);
    }

    @Test
    void committedDataFile_hasAtLeastOneWeekWithValidDays() {
        FileMenuRepository repository = new FileMenuRepository(REPO_ROOT_DATA_FILE);

        List<CateredWeek> weeks = repository.getAll();

        assertFalse(weeks.isEmpty(), "calendar.data.json should ship with at least one sample week for the demo");

        for (CateredWeek week : weeks) {
            List<MenuDay> days = week.getDays();
            assertNotNull(days, "each week should have a days list");
            assertFalse(days.isEmpty(), "each week should have at least one day entry");
            for (MenuDay day : days) {
                assertNotNull(day.getDayOfTheWeek(), "each day entry should have a valid dayOfTheWeek");
            }
        }
    }
}
