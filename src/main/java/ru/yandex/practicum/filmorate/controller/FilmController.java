package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {

    // Дата первого публичного платного киносеанса братьев Люмьер.
    private static final LocalDate FIRST_FILM_RELEASE_DATE = LocalDate.of(1895, 12, 28);

    private final Map<Long, Film> films = new HashMap<>();
    private long currentId = 0;

    @GetMapping
    public Collection<Film> findAll() {
        log.info("Получен запрос на получение всех фильмов. Количество: {}", films.size());
        return films.values();
    }

    @PostMapping
    public Film create(@Valid @RequestBody Film film) {
        log.info("Получен запрос на создание фильма");

        validateFilm(film);

        film.setId(getNextId());
        films.put(film.getId(), film);

        log.info("Создан фильм с id={}", film.getId());
        return film;
    }

    @PutMapping
    public Film update(@Valid @RequestBody Film newFilm) {
        log.info("Получен запрос на обновление фильма с id={}", newFilm.getId());

        if (newFilm.getId() == null) {
            log.warn("Ошибка обновления фильма: id не указан");
            throw new ConditionsNotMetException("Id должен быть указан");
        }

        Film oldFilm = films.get(newFilm.getId());

        if (oldFilm == null) {
            log.warn("Ошибка обновления: фильм с id={} не найден", newFilm.getId());
            throw new NotFoundException("Фильм с id = " + newFilm.getId() + " не найден");
        }

        validateFilm(newFilm);

        oldFilm.setName(newFilm.getName());
        oldFilm.setDescription(newFilm.getDescription());
        oldFilm.setReleaseDate(newFilm.getReleaseDate());
        oldFilm.setDuration(newFilm.getDuration());

        log.info("Обновлён фильм с id={}", oldFilm.getId());
        return copyFilm(oldFilm);
    }

    private Film copyFilm(Film film) {
        Film copy = new Film();
        copy.setId(film.getId());
        copy.setName(film.getName());
        copy.setDescription(film.getDescription());
        copy.setReleaseDate(film.getReleaseDate());
        copy.setDuration(film.getDuration());
        return copy;
    }

    private void validateFilm(Film film) {
        if (film.getName() == null || film.getName().isBlank()) {
            log.warn("Ошибка валидации фильма: название не указано");
            throw new ValidationException("Название фильма не может быть пустым");
        }

        if (film.getDescription() != null && film.getDescription().length() > 200) {
            log.warn("Ошибка валидации фильма: описание длиннее 200 символов");
            throw new ValidationException("Длина описания должна быть не больше 200 символов");
        }

        if (film.getReleaseDate() == null) {
            log.warn("Ошибка валидации фильма: дата выхода не указана");
            throw new ValidationException("Дата выхода фильма должна быть указана");
        }

        if (film.getReleaseDate().isBefore(FIRST_FILM_RELEASE_DATE)) {
            log.warn("Ошибка валидации фильма: дата выхода раньше {}", FIRST_FILM_RELEASE_DATE);
            throw new ValidationException("Дата выхода фильма должна быть не раньше 28 декабря 1895 года");
        }

        if (film.getDuration() == null || film.getDuration() <= 0) {
            log.warn("Ошибка валидации фильма: продолжительность не указана " + "или неположительна");
            throw new ValidationException("Продолжительность фильма должна быть положительным числом");
        }
    }

    private long getNextId() {
        return ++currentId;
    }
}