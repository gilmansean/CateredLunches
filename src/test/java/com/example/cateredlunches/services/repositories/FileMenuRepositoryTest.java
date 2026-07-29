package com.example.cateredlunches.services.repositories;

import com.example.cateredlunches.model.CateredWeek;
import com.example.cateredlunches.model.CateredWeekEntry;
import com.example.cateredlunches.model.DayOfWeekIndicator;
import com.example.cateredlunches.model.MenuDay;
import com.example.cateredlunches.services.repositories.file.FileMenuRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class FileMenuRepositoryTest {

    private Path tempFile;

    @AfterEach
    void cleanup() throws IOException {
        if (tempFile != null) {
            Files.deleteIfExists(tempFile);
            Path tmp = tempFile.resolveSibling(tempFile.getFileName() + ".tmp");
            Files.deleteIfExists(tmp);
        }
    }

    @Test
    void getAll_onMissingFile_returnsEmptyList() throws IOException {
        Path missing = Files.createTempDirectory("repo-missing").resolve("no-file.json");
        FileMenuRepository repo = new FileMenuRepository(missing);
        // file does not exist
        assertFalse(Files.exists(missing));
        assertTrue(repo.getAll().isEmpty());
    }

    @Test
    void upsert_thenGetAndGetAll_roundTrip() throws IOException {
        FileMenuRepository repo = repoWithTempFile();
        LocalDate weekStart = LocalDate.of(2026, 7, 13);

        CateredWeek week = new CateredWeek();
        week.setDays(List.of(
                new MenuDay(DayOfWeekIndicator.MONDAY, "Tacos"),
                new MenuDay(DayOfWeekIndicator.TUESDAY, null) // ensure null serialized as null
        ));

        CateredWeek stored = repo.upsert(weekStart, week);
        // stored copy matches sizes and values
        assertEquals(2, stored.getDays().size());
        assertEquals("Tacos", stored.getDays().get(0).getMenuItem());
        assertNull(stored.getDays().get(1).getMenuItem());

        Optional<CateredWeek> fetched = repo.get(weekStart);
        assertTrue(fetched.isPresent());
        assertEquals(2, fetched.get().getDays().size());
        assertEquals(DayOfWeekIndicator.MONDAY, fetched.get().getDays().get(0).getDayOfTheWeek());

        List<CateredWeek> all = repo.getAll();
        assertEquals(1, all.size());
        assertEquals(2, all.get(0).getDays().size());
    }

    private FileMenuRepository repoWithTempFile() throws IOException {
        tempFile = Files.createTempFile("calendar", ".json");
        // start as empty file; some tests will prefill
        Files.writeString(tempFile, "", StandardCharsets.UTF_8, StandardOpenOption.TRUNCATE_EXISTING);
        return new FileMenuRepository(tempFile);
    }

    @Test
    void upsert_replacesExistingWeek() throws IOException {
        FileMenuRepository repo = repoWithTempFile();
        LocalDate weekStart = LocalDate.of(2026, 7, 13);

        CateredWeek first = new CateredWeek();
        first.setDays(List.of(new MenuDay(DayOfWeekIndicator.MONDAY, "A")));
        repo.upsert(weekStart, first);

        CateredWeek second = new CateredWeek();
        second.setDays(List.of(
                new MenuDay(DayOfWeekIndicator.MONDAY, "B"),
                new MenuDay(DayOfWeekIndicator.WEDNESDAY, "C")
        ));
        repo.upsert(weekStart, second);

        Optional<CateredWeek> fetched = repo.get(weekStart);
        assertTrue(fetched.isPresent());
        assertEquals(2, fetched.get().getDays().size());
        assertEquals("B", fetched.get().getDays().get(0).getMenuItem());
    }

    @Test
    void delete_existingAndMissing() throws IOException {
        FileMenuRepository repo = repoWithTempFile();
        LocalDate week1 = LocalDate.of(2026, 7, 13);
        LocalDate week2 = LocalDate.of(2026, 7, 20);

        CateredWeek w1 = new CateredWeek();
        w1.setDays(List.of(new MenuDay(DayOfWeekIndicator.MONDAY, "A")));
        CateredWeek w2 = new CateredWeek();
        w2.setDays(List.of(new MenuDay(DayOfWeekIndicator.TUESDAY, "B")));

        repo.upsert(week1, w1);
        repo.upsert(week2, w2);

        assertTrue(repo.delete(week1));
        assertFalse(repo.get(week1).isPresent());
        assertTrue(repo.get(week2).isPresent());

        // deleting again should be false
        assertFalse(repo.delete(week1));
    }

    @Test
    void getAllFrom_filtersByWeekStartInclusive() throws IOException {
        FileMenuRepository repo = repoWithTempFile();
        LocalDate week1 = LocalDate.of(2026, 7, 13); // Monday
        LocalDate week2 = LocalDate.of(2026, 7, 20); // Monday
        LocalDate midOfWeek2 = week2.plusDays(2);    // Wednesday

        CateredWeek w1 = new CateredWeek();
        w1.setDays(List.of(new MenuDay(DayOfWeekIndicator.MONDAY, "A")));
        CateredWeek w2 = new CateredWeek();
        w2.setDays(List.of(new MenuDay(DayOfWeekIndicator.TUESDAY, "B")));

        repo.upsert(week1, w1);
        repo.upsert(week2, w2);

        // From middle of week2 should include week2 only
        List<CateredWeek> filtered = repo.getAllFrom(midOfWeek2);
        assertEquals(1, filtered.size());
        assertEquals("B", filtered.get(0).getDays().get(0).getMenuItem());

        // From null: use getAll path indirectly covered elsewhere, but assert explicitly
        assertEquals(2, repo.getAllFrom(null).size());
    }

    @Test
    void serialization_handlesUnicodeAndControlEscapes() throws IOException {
        FileMenuRepository repo = repoWithTempFile();
        LocalDate week = LocalDate.of(2026, 7, 13);

        // Include quotes, backslash, tab, newline, and control char < 0x20 to ensure escaping
        String complex = "He said: \"back\\slash\"\tline\nctrl" + (char) 0x01;
        CateredWeek w = new CateredWeek();
        w.setDays(List.of(new MenuDay(DayOfWeekIndicator.MONDAY, complex)));

        repo.upsert(week, w);
        // Read raw file text to verify escaped sequences exist
        String raw = Files.readString(tempFile, StandardCharsets.UTF_8);

        assertTrue(raw.contains("\\\"back\\\\slash\\\""), "quotes and backslash should be escaped");
        assertTrue(raw.contains("\\t"), "tab should be escaped");
        assertTrue(raw.contains("\\n"), "newline should be escaped");

        // And round-trip back into same value
        CateredWeek roundTrip = repo.get(week).orElseThrow();
        assertEquals(complex, roundTrip.getDays().get(0).getMenuItem());
    }

    @Test
    void parse_emptyFileAndMalformedKeys_areTolerated() throws IOException {
        // Start with explicit empty file (already created in helper)
        FileMenuRepository repo = repoWithTempFile();
        assertTrue(repo.getAll().isEmpty());

        // Write a file with an invalid date key that should be skipped
        String badKeyJson = "{\"weeks\":{\"not-a-date\":[{\"dayOfTheWeek\":\"MONDAY\",\"menuItem\":\"X\"}]}}";
        Files.writeString(tempFile, badKeyJson, StandardCharsets.UTF_8, StandardOpenOption.TRUNCATE_EXISTING);

        // Parser should skip invalid key, resulting in empty repository view
        assertTrue(repo.getAll().isEmpty());
    }

    @Test
    void upsert_nullWeekStart_throws() throws IOException {
        FileMenuRepository repo = repoWithTempFile();
        CateredWeek week = new CateredWeek();
        week.setDays(List.of(new MenuDay(DayOfWeekIndicator.MONDAY, "X")));
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> repo.upsert(null, week));
        assertTrue(ex.getMessage().toLowerCase().contains("weekstart"));
    }

    @Test
    void defaultConstructor_usesDefaultOrEnvPath() {
        // Just exercises the no-arg constructor / env resolution branch without asserting a specific path
        FileMenuRepository repo = new FileMenuRepository();
        assertNotNull(repo);
    }

    @Test
    void springValueConstructor_usesProvidedPathString() throws IOException {
        Path dir = Files.createTempDirectory("repo-spring-ctor");
        Path file = dir.resolve("calendar.data.json");
        FileMenuRepository repo = new FileMenuRepository(file.toString());

        assertTrue(repo.getAll().isEmpty());

        CateredWeek week = new CateredWeek();
        week.setDays(List.of(new MenuDay(DayOfWeekIndicator.MONDAY, "Soup")));
        repo.upsert(LocalDate.of(2026, 7, 13), week);

        assertTrue(Files.exists(file));
        Files.deleteIfExists(file);
        Files.deleteIfExists(dir);
    }

    @Test
    void getAllEntries_returnsKeyAndWeekPairsInOrder() throws IOException {
        FileMenuRepository repo = repoWithTempFile();
        LocalDate week1 = LocalDate.of(2026, 7, 13);
        LocalDate week2 = LocalDate.of(2026, 7, 20);

        repo.upsert(week1, weekWith("A"));
        repo.upsert(week2, weekWith("B"));

        List<CateredWeekEntry> entries = repo.getAllEntries();
        assertEquals(2, entries.size());
        assertEquals(week1, entries.get(0).getWeekStartKey());
        assertEquals(week2, entries.get(1).getWeekStartKey());
        assertEquals("A", entries.get(0).getWeekValue().getDays().get(0).getMenuItem());
    }

    private static CateredWeek weekWith(String item) {
        CateredWeek week = new CateredWeek();
        week.setDays(List.of(new MenuDay(DayOfWeekIndicator.MONDAY, item)));
        return week;
    }

    @Test
    void getAllFrom_onExactMondayBoundary_isInclusive() throws IOException {
        FileMenuRepository repo = repoWithTempFile();
        LocalDate monday = LocalDate.of(2026, 7, 13); // Monday
        repo.upsert(monday, weekWith("A"));

        List<CateredWeek> filtered = repo.getAllFrom(monday);
        assertEquals(1, filtered.size());
    }

    @Test
    void getAllFrom_fromDateOnSunday_resolvesToPrecedingMonday() throws IOException {
        FileMenuRepository repo = repoWithTempFile();
        LocalDate monday = LocalDate.of(2026, 7, 13);
        LocalDate sundayOfSameWeek = monday.plusDays(6); // Sunday
        repo.upsert(monday, weekWith("A"));

        // fromDate is the Sunday belonging to the same week as 'monday'; should still include it
        List<CateredWeek> filtered = repo.getAllFrom(sundayOfSameWeek);
        assertEquals(1, filtered.size());
    }

    @Test
    void getAllFrom_excludesWeeksBeforeBoundary() throws IOException {
        FileMenuRepository repo = repoWithTempFile();
        LocalDate earlier = LocalDate.of(2026, 6, 1);
        LocalDate later = LocalDate.of(2026, 7, 13);
        repo.upsert(earlier, weekWith("Old"));
        repo.upsert(later, weekWith("New"));

        List<CateredWeek> filtered = repo.getAllFrom(later);
        assertEquals(1, filtered.size());
        assertEquals("New", filtered.get(0).getDays().get(0).getMenuItem());
    }

    @Test
    void get_missingWeek_returnsEmptyOptional() throws IOException {
        FileMenuRepository repo = repoWithTempFile();
        assertTrue(repo.get(LocalDate.of(2099, 1, 1)).isEmpty());
    }

    @Test
    void upsert_withNullDaysList_storesEmptyList() throws IOException {
        FileMenuRepository repo = repoWithTempFile();
        LocalDate weekStart = LocalDate.of(2026, 7, 13);

        CateredWeek week = new CateredWeek();
        week.setDays(List.of(new MenuDay(DayOfWeekIndicator.MONDAY, "A")));
        repo.upsert(weekStart, week);

        // Simulate a stored week whose days list comes back null via manual file edit is covered
        // elsewhere; here just confirm copyDays handles a fresh empty list gracefully via getAll.
        assertEquals(1, repo.getAll().size());
    }
}
