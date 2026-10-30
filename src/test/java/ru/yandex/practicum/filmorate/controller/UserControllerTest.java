package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import ru.yandex.practicum.filmorate.exception.*;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class UserControllerTest {
    private UserController controller;

    @BeforeEach
    void setUp() {
        controller = new UserController();
    }

    private static User validModel() {
        User film = new User();
        film.setEmail("user@example.org");
        film.setLogin("login");
        film.setName("Имя");
        film.setBirthday(LocalDate.of(2000, 1, 1));
        return film;
    }

    static Stream<Consumer<User>> invalidValues() {
        return Stream.of(
                film -> film.setEmail(null),
                film -> film.setEmail(""),
                film -> film.setEmail("   "),
                film -> film.setEmail("invalid.example.org"),
                film -> film.setLogin(null),
                film -> film.setLogin(""),
                film -> film.setLogin("   "),
                film -> film.setLogin("two words"),
                film -> film.setBirthday(null),
                film -> film.setBirthday(LocalDate.now().plusDays(1)));
    }

    static Stream<Consumer<User>> boundaryValues() {
        return Stream.of(
                film -> film.setBirthday(LocalDate.now()),
                film -> film.setBirthday(LocalDate.now().minusDays(1)));
    }

    @ParameterizedTest
    @MethodSource("invalidValues")
    void createRejectsInvalidModel(Consumer<User> change) {
        User film = validModel();
        change.accept(film);
        assertThrows(ValidationException.class, () -> controller.create(film));
        assertTrue(controller.findAll().isEmpty());
        assertNull(film.getId());
    }

    @ParameterizedTest
    @MethodSource("invalidValues")
    void updateRejectsInvalidModelAndPreservesStoredData(Consumer<User> change) {
        User original = validModel();
        controller.create(original);
        User snapshot = validModel();
        snapshot.setId(original.getId());
        User replacement = validModel();
        replacement.setId(original.getId());
        change.accept(replacement);
        assertThrows(ValidationException.class, () -> controller.update(replacement));
        assertEquals(snapshot, controller.findAll().iterator().next());
        assertEquals(1, controller.findAll().size());
    }

    @ParameterizedTest
    @MethodSource("boundaryValues")
    void createAcceptsBoundaryValue(Consumer<User> change) {
        User film = validModel();
        change.accept(film);
        assertEquals(film, controller.create(film));
        assertNotNull(film.getId());
        assertEquals(1, controller.findAll().size());
    }

    @ParameterizedTest
    @MethodSource("boundaryValues")
    void updateAcceptsBoundaryValue(Consumer<User> change) {
        User original = controller.create(validModel());
        User replacement = validModel();
        replacement.setId(original.getId());
        change.accept(replacement);
        assertEquals(replacement, controller.update(replacement));
        assertEquals(replacement, controller.findAll().iterator().next());
    }

    @Test
    void createRejectsEmptyModel() {
        assertThrows(ValidationException.class, () -> controller.create(new User()));
        assertTrue(controller.findAll().isEmpty());
    }

    @Test
    void updateRequiresId() {
        assertThrows(ConditionsNotMetException.class, () -> controller.update(validModel()));
    }

    @Test
    void updateRejectsUnknownId() {
        User film = validModel();
        film.setId(999L);
        assertThrows(NotFoundException.class, () -> controller.update(film));
        assertTrue(controller.findAll().isEmpty());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void createUsesLoginWhenNameIsMissing(String name) {
        User user = validModel();
        user.setName(name);
        assertEquals(user.getLogin(), controller.create(user).getName());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void updateUsesLoginWhenNameIsMissing(String name) {
        User original = controller.create(validModel());
        User user = validModel();
        user.setId(original.getId());
        user.setLogin("new-login");
        user.setName(name);
        assertEquals("new-login", controller.update(user).getName());
    }
}
