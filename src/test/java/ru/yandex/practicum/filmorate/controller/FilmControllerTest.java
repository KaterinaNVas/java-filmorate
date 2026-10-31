package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class FilmControllerTest {
    private FilmController controller;

    @BeforeEach
    void setUp() {
        controller = new FilmController();
    }

    private static Film validModel() {
        Film film = new Film();
        film.setName("Фильм");
        film.setDescription("Описание");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(90);
        return film;
    }

    static Stream<Consumer<Film>> invalidValues() {
        return Stream.of(
                film -> film.setName(null),
                film -> film.setName(""),
                film -> film.setName("   "),
                film -> film.setReleaseDate(null),
                film -> film.setReleaseDate(LocalDate.of(1895, 12, 27)),
                film -> film.setDuration(null),
                film -> film.setDuration(0),
                film -> film.setDuration(-1),
                film -> film.setDescription("a".repeat(201))
        );
    }

    static Stream<Consumer<Film>> boundaryValues() {
        return Stream.of(
                film -> film.setReleaseDate(LocalDate.of(1895, 12, 28)),
                film -> film.setReleaseDate(LocalDate.of(1895, 12, 29)),
                film -> film.setDuration(1),
                film -> film.setDescription("a".repeat(199)),
                film -> film.setDescription("a".repeat(200)),
                film -> film.setDescription(""),
                film -> film.setDescription(null)
        );
    }

    @ParameterizedTest
    @MethodSource("invalidValues")
    void createRejectsInvalidModel(Consumer<Film> change) {
        Film film = validModel();
        change.accept(film);

        assertThrows(ValidationException.class, () -> controller.create(film));
        assertTrue(controller.findAll().isEmpty());
        assertNull(film.getId());
    }

    @ParameterizedTest
    @MethodSource("invalidValues")
    void updateRejectsInvalidModelAndPreservesStoredData(Consumer<Film> change) {
        Film original = validModel();
        controller.create(original);

        Film snapshot = validModel();
        snapshot.setId(original.getId());

        Film replacement = validModel();
        replacement.setId(original.getId());
        change.accept(replacement);

        assertThrows(ValidationException.class, () -> controller.update(replacement));
        assertEquals(snapshot, controller.findAll().iterator().next());
        assertEquals(1, controller.findAll().size());
    }

    @ParameterizedTest
    @MethodSource("boundaryValues")
    void createAcceptsBoundaryValue(Consumer<Film> change) {
        Film film = validModel();
        change.accept(film);

        assertEquals(film, controller.create(film));
        assertNotNull(film.getId());
        assertEquals(1, controller.findAll().size());
    }

    @ParameterizedTest
    @MethodSource("boundaryValues")
    void updateAcceptsBoundaryValue(Consumer<Film> change) {
        Film original = controller.create(validModel());

        Film replacement = validModel();
        replacement.setId(original.getId());
        change.accept(replacement);

        assertEquals(replacement, controller.update(replacement));
        assertEquals(replacement, controller.findAll().iterator().next());
    }

    @Test
    void createRejectsEmptyModel() {
        assertThrows(ValidationException.class, () -> controller.create(new Film()));
        assertTrue(controller.findAll().isEmpty());
    }

    @Test
    void updateRequiresId() {
        assertThrows(ConditionsNotMetException.class, () -> controller.update(validModel()));
    }

    @Test
    void updateRejectsUnknownId() {
        Film film = validModel();
        film.setId(999L);

        assertThrows(NotFoundException.class, () -> controller.update(film));
        assertTrue(controller.findAll().isEmpty());
    }
}