package ru.yandex.practicum;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MainTest {

    // main метод пока подменяет графический интерфейс, надо ли его тестировать?
    //в main только один метод, который не использует input и/или output
    @Test
    void isPositiveInteger() {

        assertFalse(Main.isPositiveInteger(""), "Пустая строка не должна быть положительным числом");
        assertTrue(Main.isPositiveInteger("4"), "Строка '4' должна быть положительным числом");
        assertFalse(Main.isPositiveInteger("12a"), "Строка '12a' не должна быть положительным числом");
        assertFalse(Main.isPositiveInteger("0"), "Строка '0' не должна быть положительным числом");
        assertFalse(Main.isPositiveInteger("-73"), "Строка '-73' не должна быть положительным числом");
        assertFalse(Main.isPositiveInteger(" 35 "), "Строка с пробелами не должна быть положительным числом");
    }
}
