package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class EmptyRequestTest {
    private MockMvc mvc;
    private FilmController films;
    private UserController users;

    @BeforeEach
    void setUp() {
        films = new FilmController();
        users = new UserController();
        mvc = MockMvcBuilders.standaloneSetup(films, users).build();
    }

    @ParameterizedTest
    @CsvSource({"POST,/films", "PUT,/films", "POST,/users", "PUT,/users"})
    void missingRequestBodyReturnsBadRequest(String method, String path) throws Exception {
        mvc.perform(request(HttpMethod.valueOf(method), path)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertInstanceOf(HttpMessageNotReadableException.class,
                        result.getResolvedException()));
        assertTrue(films.findAll().isEmpty());
        assertTrue(users.findAll().isEmpty());
    }

    @ParameterizedTest
    @CsvSource({"POST,/films", "PUT,/films", "POST,/users", "PUT,/users"})
    void jsonNullReturnsBadRequest(String method, String path) throws Exception {
        mvc.perform(request(HttpMethod.valueOf(method), path)
                        .contentType(MediaType.APPLICATION_JSON).content("null"))
                .andExpect(status().isBadRequest());
        assertTrue(films.findAll().isEmpty());
        assertTrue(users.findAll().isEmpty());
    }
}
