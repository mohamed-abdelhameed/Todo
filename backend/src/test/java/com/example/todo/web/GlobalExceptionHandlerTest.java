package com.example.todo.web;

import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    @Test
    void noDeclaredHandlerMapsExceptionOrThrowable() {
        assertThat(handledExceptionTypes())
                .isNotEmpty()
                .doesNotContain(Exception.class, Throwable.class);
    }

    @Test
    void mapsOnlyTaskNotFound() {
        assertThat(handledExceptionTypes())
                .containsExactly(com.example.todo.service.TaskNotFoundException.class);
    }

    private List<Class<?>> handledExceptionTypes() {
        return Arrays.stream(GlobalExceptionHandler.class.getDeclaredMethods())
                .filter(method -> method.isAnnotationPresent(ExceptionHandler.class))
                .flatMap(method -> Arrays.stream(exceptionTypesOf(method)))
                .toList();
    }

    private Class<?>[] exceptionTypesOf(Method method) {
        return method.getAnnotation(ExceptionHandler.class).value();
    }
}
