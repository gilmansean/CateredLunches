package com.example.cateredlunches.services.repositories.file;

import com.example.cateredlunches.model.CateredWeek;
import com.example.cateredlunches.model.CateredWeekEntry;
import com.example.cateredlunches.model.MenuDay;
import com.example.cateredlunches.services.repositories.MenuRepository;
import com.example.cateredlunches.services.validation.MenuValidation;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * File-backed JSON repository, serialized with Gson.
 * <p>
 * JSON layout:
 * {
 * "weeks": {
 * "YYYY-MM-DD": [
 * {"dayOfTheWeek":"MONDAY","menuItem":"Tacos"},
 * ...
 * ],
 * ...
 * }
 * }
 * <p>
 * Notes:
 * - Uses CALENDAR_DATA_PATH env/property if available; default "./calendar.data.json".
 * - Atomic writes (temp + move).
 */
@Repository
@ConditionalOnProperty(name = "menu.repository.type", havingValue = "file", matchIfMissing = true)
public class FileMenuRepository implements MenuRepository {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final Path dataPath;

    /**
     * Uses CALENDAR_DATA_PATH env/property if available; default "./calendar.data.json".
     */
    public FileMenuRepository() {
        this(resolvePathFromEnvOrDefault());
    }

    /**
     * Uses the given path.
     *
     * @param dataPath - Path to the JSON file.
     */
    public FileMenuRepository(Path dataPath) {
        this.dataPath = dataPath;
    }

    private static Path resolvePathFromEnvOrDefault() {
        String env = System.getenv("CALENDAR_DATA_PATH");
        if (env != null && !env.isBlank()) {
            return Path.of(env);
        }
        String prop = System.getProperty("CALENDAR_DATA_PATH");
        if (prop != null && !prop.isBlank()) {
            return Path.of(prop);
        }
        return Path.of("./calendar.data.json");
    }

    /**
     * Spring-managed constructor. Resolves the data file path from the
     * {@code CALENDAR_DATA_PATH} property, which Spring's Environment
     * abstraction sources from application.properties, OS environment
     * variables, or JVM system properties (in that order of precedence).
     * Defaults to "./calendar.data.json" if unset.
     */
    @Autowired
    public FileMenuRepository(@Value("${CALENDAR_DATA_PATH:./calendar.data.json}") String dataPath) {
        this(Path.of(dataPath));
    }

    /**
     * Returns all weeks in the repository.
     *
     * @return - List of weeks.
     */
    @Override
    public List<CateredWeek> getAll() {
        Map<LocalDate, List<MenuDay>> weeksByStartDate = readAllSafely(null);
        return toWeekListPreservingOrder(weeksByStartDate);
    }

    /**
     * Returns all weeks in stored order, paired with their key (week start date).
     *
     * @return - List of week entries.
     */
    @Override
    public List<CateredWeekEntry> getAllEntries() {
        Map<LocalDate, List<MenuDay>> weeksByStartDate = readAllSafely(null);
        List<CateredWeekEntry> entries = new ArrayList<>(weeksByStartDate.size());
        for (Map.Entry<LocalDate, List<MenuDay>> entry : weeksByStartDate.entrySet()) {
            CateredWeek week = new CateredWeek();
            week.setDays(copyDays(entry.getValue()));
            entries.add(new CateredWeekEntry(entry.getKey(), week));
        }
        return entries;
    }

    /**
     * Returns all weeks in the repository whose key is on/after the Monday of fromDate.
     *
     * @param fromDate - Weeks whose key is on/after the Monday of fromDate are included.
     * @return - List of weeks.
     */
    @Override
    public List<CateredWeek> getAllFrom(LocalDate fromDate) {
        Map<LocalDate, List<MenuDay>> weeksByStartDate = readAllSafely(fromDate);
        return toWeekListPreservingOrder(weeksByStartDate);
    }

    /**
     * Returns a week by its key (week start date).
     *
     * @param weekStart - Week start date.
     * @return - Week, or empty if not found.
     */
    @Override
    public Optional<CateredWeek> get(LocalDate weekStart) {
        Map<LocalDate, List<MenuDay>> weeksByStartDate = readAllSafely(null);
        List<MenuDay> days = weeksByStartDate.get(weekStart);
        if (days == null) return Optional.empty();
        CateredWeek week = new CateredWeek();
        week.setDays(copyDays(days));
        return Optional.of(week);
    }

    /**
     * Upserts a week by its key (week start date).
     *
     * @param weekStart - Week start date.
     * @param week      - Week to upsert.
     * @return - Stored week.
     */
    @Override
    public CateredWeek upsert(LocalDate weekStart, CateredWeek week) {
        if (weekStart == null) throw new IllegalArgumentException("weekStart key is required");
        MenuValidation.validateWeek(week);
        Map<LocalDate, List<MenuDay>> weeksByStartDate = readAllSafely(null);
        weeksByStartDate.put(weekStart, copyDays(week.getDays()));
        writeAllSafely(weeksByStartDate);
        CateredWeek stored = new CateredWeek();
        stored.setDays(copyDays(weeksByStartDate.get(weekStart)));
        return stored;
    }

    // ---------- IO via Gson ----------

    /**
     * Deletes a week by its key (week start date).
     *
     * @param weekStart - Week start date.
     * @return - True if the week was found and deleted, false otherwise.
     */
    @Override
    public boolean delete(LocalDate weekStart) {
        Map<LocalDate, List<MenuDay>> weeksByStartDate = readAllSafely(null);
        if (weeksByStartDate.remove(weekStart) != null) {
            writeAllSafely(weeksByStartDate);
            return true;
        }
        return false;
    }

    private void writeAllSafely(Map<LocalDate, List<MenuDay>> weeksByStartDate) {
        WeeksFile file = new WeeksFile();
        for (Map.Entry<LocalDate, List<MenuDay>> entry : weeksByStartDate.entrySet()) {
            file.weeks.put(entry.getKey().toString(), entry.getValue());
        }
        Path tempPath = dataPath.resolveSibling(dataPath.getFileName() + ".tmp");
        try (BufferedWriter writer = ensureParentDirAndOpenWriter(tempPath)) {
            GSON.toJson(file, writer);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write temp data file: " + tempPath, e);
        }
        try {
            Files.move(tempPath, dataPath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            throw new RuntimeException("Failed to finalize data file move to: " + dataPath, e);
        }
    }

    private BufferedWriter ensureParentDirAndOpenWriter(Path path) throws IOException {
        Path parent = path.toAbsolutePath().getParent();
        if (parent != null && !Files.exists(parent)) {
            Files.createDirectories(parent);
        }
        return Files.newBufferedWriter(path, StandardCharsets.UTF_8);
    }

    /*
     * Reads all data. If fromDate is non-null, includes only weeks whose key is on/after
     * the Monday of fromDate. If fromDate is in the middle of a week, that week is included.
     * If fromDate is null, returns all weeks.
     */
    private Map<LocalDate, List<MenuDay>> readAllSafely(LocalDate fromDate) {
        if (!Files.exists(dataPath)) {
            return new LinkedHashMap<>();
        }
        WeeksFile file;
        try (BufferedReader reader = Files.newBufferedReader(dataPath, StandardCharsets.UTF_8)) {
            file = GSON.fromJson(reader, WeeksFile.class);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read data file: " + dataPath, e);
        }

        Map<LocalDate, List<MenuDay>> parsed = new LinkedHashMap<>();
        if (file != null && file.weeks != null) {
            for (Map.Entry<String, List<MenuDay>> entry : file.weeks.entrySet()) {
                try {
                    LocalDate keyDate = LocalDate.parse(entry.getKey());
                    parsed.put(keyDate, entry.getValue() == null ? List.of() : entry.getValue());
                } catch (DateTimeParseException e) {
                    // Skip invalid date keys silently
                }
            }
        }

        if (fromDate == null) {
            return parsed;
        }
        LocalDate inclusiveWeekStart = toWeekStart(fromDate);
        Map<LocalDate, List<MenuDay>> filtered = new LinkedHashMap<>();
        for (Map.Entry<LocalDate, List<MenuDay>> entry : parsed.entrySet()) {
            if (!entry.getKey().isBefore(inclusiveWeekStart)) {
                filtered.put(entry.getKey(), entry.getValue());
            }
        }
        return filtered;
    }

    private List<CateredWeek> toWeekListPreservingOrder(Map<LocalDate, List<MenuDay>> weeksByStartDate) {
        List<CateredWeek> weeks = new ArrayList<>(weeksByStartDate.size());
        for (List<MenuDay> days : weeksByStartDate.values()) {
            CateredWeek week = new CateredWeek();
            week.setDays(copyDays(days));
            weeks.add(week);
        }
        return weeks;
    }

    private static LocalDate toWeekStart(LocalDate anyDate) {
        DayOfWeek dayOfWeek = anyDate.getDayOfWeek();
        int daysBackToMonday = (dayOfWeek.getValue() + 6) % 7; // Monday=1 -> 0 back; Sunday=7 -> 6 back
        return anyDate.minusDays(daysBackToMonday);
    }

    private static List<MenuDay> copyDays(List<MenuDay> sourceDays) {
        if (sourceDays == null) return List.of();
        List<MenuDay> copy = new ArrayList<>(sourceDays.size());
        for (MenuDay sourceDay : sourceDays) {
            copy.add(new MenuDay(sourceDay.getDayOfTheWeek(), sourceDay.getMenuItem()));
        }
        return copy;
    }

    /**
     * Gson wire-format wrapper. LocalDate keys are represented as ISO-8601 strings
     * (Gson map keys must be Strings), converted to/from LocalDate at the boundary.
     */
    private static final class WeeksFile {
        Map<String, List<MenuDay>> weeks = new LinkedHashMap<>();
    }
}