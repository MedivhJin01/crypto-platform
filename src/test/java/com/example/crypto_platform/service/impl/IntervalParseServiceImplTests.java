package com.example.crypto_platform.service.impl;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;


public class IntervalParseServiceImplTests {

    private static Validator validator;
    private static IntervalParseServiceImpl intervalParseService;

    @BeforeAll
    static void setUp() {
        intervalParseService = new IntervalParseServiceImpl();
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    }

    @Test
    void toMillis_oneMinute() {
        long ms = intervalParseService.toMillis("1m");
        assertThat(ms).isEqualTo(60_000L);
    }

    @Test
    void toMillis_fiveHours() {
        long ms = intervalParseService.toMillis("5h");
        assertThat(ms).isEqualTo(5 * 3_600_000L);
    }

    @Test
    void toMillis_twoDays() {
        long ms = intervalParseService.toMillis("2d");
        assertThat(ms).isEqualTo(2 * 86_400_000L);
    }

    @Test
    void toMillis_threeWeeks() {
        long ms = intervalParseService.toMillis("3w");
        assertThat(ms).isEqualTo(3 * 604_800_000L);
    }

    @Test
    void toMillis_twoMonths() {
        long ms = intervalParseService.toMillis("2M");
        assertThat(ms).isEqualTo(2 * 2_629_746_000L);
    }

    @Test
    void toMillis_oneYear() {
        long ms = intervalParseService.toMillis("1y");
        assertThat(ms).isEqualTo(31_556_952_000L);
    }

    @Test
    void blankParam_failValidation() {
        assertThrows(RuntimeException.class, () -> intervalParseService.toMillis(""));
    }

}
