package com.example.cateredlunches.services.repositories;

import com.example.cateredlunches.model.CateredWeek;
import com.example.cateredlunches.model.CateredWeekEntry;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Persistence boundary for catered weeks.
 * Keys are external (ISO date indicating the week's start) and not part of CateredWeek.
 */
public interface MenuRepository {

    /**
     * Returns all weeks in stored order (implementation may preserve file order).
     */
    List<CateredWeek> getAll();

    /**
     * Returns all weeks in stored order, paired with their key (week start date).
     * Useful when a caller needs to display the key alongside each week's contents.
     */
    List<CateredWeekEntry> getAllEntries();

    /**
     * Returns all weeks from the current date's week (inclusive), preserving stored order.
     */
    default List<CateredWeek> getAllCurrent() {
        return getAllFrom(LocalDate.now());
    }

    /**
     * Returns all weeks from the week containing the given date (inclusive).
     * If the given date is null, returns all weeks.
     */
    List<CateredWeek> getAllFrom(LocalDate fromDate);

    /**
     * Returns a week by its key (week start date).
     */
    Optional<CateredWeek> get(LocalDate weekStart);

    /**
     * Creates or replaces a week identified by its key (weekStart).
     * Returns the stored entity (after persistence).
     */
    CateredWeek upsert(LocalDate weekStart, CateredWeek week);

    /**
     * Legacy signature without a key is not supported for file-backed storage.
     */
    default CateredWeek upsert(CateredWeek week) {
        throw new UnsupportedOperationException("Use upsert(weekStart, week)");
    }

    /**
     * Deletes a week by its key if present.
     * Returns true if a deletion happened, false if nothing was deleted.
     */
    boolean delete(LocalDate weekStart);
}
