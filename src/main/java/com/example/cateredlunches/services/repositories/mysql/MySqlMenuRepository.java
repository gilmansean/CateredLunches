package com.example.cateredlunches.services.repositories.mysql;

import com.example.cateredlunches.model.CateredWeek;
import com.example.cateredlunches.model.CateredWeekEntry;
import com.example.cateredlunches.services.repositories.MenuRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Placeholder MySQL-backed repository.
 * <p>
 * Active when {@code menu.repository.type=mysql}.
 * <p>
 * This class demonstrates the repository-swap pattern (see {@link MenuRepository})
 * without committing to a schema, JDBC/JPA setup, or connection config yet.
 * Every method throws {@link UnsupportedOperationException} until a real
 * implementation is built.
 */
@Repository
@ConditionalOnProperty(name = "menu.repository.type", havingValue = "mysql")
public class MySqlMenuRepository implements MenuRepository {

    private static final String NOT_IMPLEMENTED =
            "MySqlMenuRepository is not implemented yet. Set menu.repository.type=file to use the file-backed repository.";

    @Override
    public List<CateredWeek> getAll() {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED);
    }

    @Override
    public List<CateredWeekEntry> getAllEntries() {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED);
    }

    @Override
    public List<CateredWeek> getAllFrom(LocalDate fromDate) {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED);
    }

    @Override
    public Optional<CateredWeek> get(LocalDate weekStart) {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED);
    }

    @Override
    public CateredWeek upsert(LocalDate weekStart, CateredWeek week) {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED);
    }

    @Override
    public boolean delete(LocalDate weekStart) {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED);
    }
}
