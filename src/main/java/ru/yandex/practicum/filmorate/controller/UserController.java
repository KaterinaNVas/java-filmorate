package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {

    private final Map<Long, User> users = new HashMap<>();

    @GetMapping
    public Collection<User> findAll() {
        log.info("Получен запрос на получение всех пользователей. Количество: {}", users.size());
        return users.values();
    }

    @PostMapping
    public User create(@Valid @RequestBody User user) {
        log.info("Получен запрос на создание пользователя");

        validateUser(user);
        setDefaultName(user);

        user.setId(getNextId());
        users.put(user.getId(), user);

        log.info("Создан пользователь с id={}", user.getId());
        return user;
    }

    @PutMapping
    public User update(@Valid @RequestBody User newUser) {
        log.info("Получен запрос на обновление пользователя с id={}", newUser.getId());

        if (newUser.getId() == null) {
            log.warn("Ошибка обновления пользователя: id не указан");
            throw new ConditionsNotMetException("Id должен быть указан");
        }

        User oldUser = users.get(newUser.getId());

        if (oldUser == null) {
            log.warn("Ошибка обновления: пользователь с id={} не найден", newUser.getId());
            throw new NotFoundException("Пользователь с id = " + newUser.getId() + " не найден");
        }

        validateUser(newUser);
        setDefaultName(newUser);

        oldUser.setLogin(newUser.getLogin());
        oldUser.setEmail(newUser.getEmail());
        oldUser.setName(newUser.getName());
        oldUser.setBirthday(newUser.getBirthday());

        log.info("Обновлён пользователь с id={}", oldUser.getId());
        return oldUser;
    }

    private void validateUser(User user) {
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            log.warn("Ошибка валидации пользователя: email не указан");
            throw new ValidationException("Email должен быть указан");
        }

        if (!user.getEmail().contains("@")) {
            log.warn("Ошибка валидации пользователя: email не содержит @");
            throw new ValidationException("Email должен содержать @");
        }

        if (user.getLogin() == null || user.getLogin().isBlank() || user.getLogin().matches("(?s).*\\s.*")) {
            log.warn("Ошибка валидации пользователя: логин пустой " + "или содержит пробельные символы");
            throw new ValidationException("Логин не может быть пустым или содержать пробельные символы");
        }

        if (user.getBirthday() == null) {
            log.warn("Ошибка валидации пользователя: дата рождения не указана");
            throw new ValidationException("Дата рождения должна быть указана");
        }

        if (user.getBirthday().isAfter(LocalDate.now())) {
            log.warn("Ошибка валидации пользователя: дата рождения в будущем");
            throw new ValidationException("Дата рождения не может быть в будущем");
        }
    }

    private void setDefaultName(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }

    private long getNextId() {
        long currentMaxId = users.keySet().stream().mapToLong(id -> id).max().orElse(0);

        return ++currentMaxId;
    }
}