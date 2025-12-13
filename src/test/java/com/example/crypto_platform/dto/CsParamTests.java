package com.example.crypto_platform.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.validation.Validation;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class CsParamTests {
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void validCsParam_passesValidation() {
        CsParam csParam = new CsParam(
                1L,
                60_000L,
                1,
                "BINANCE",
                "BTC-USDT",
                "1m",
                1000L,
                2000L
        );
        Set<ConstraintViolation<CsParam>> violations = validator.validate(csParam);
        assertThat(violations).isEmpty();
    }

    @Test
    void allNullFields_failValidation() {
        CsParam csParam = new CsParam(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
        Set<ConstraintViolation<CsParam>> violations = validator.validate(csParam);
        assertThat(violations).hasSize(8);
        Set<String> violationFields = violations.stream()
                .map(v -> v.getPropertyPath().toString())
                .collect(Collectors.toSet());

        assertThat(violationFields).containsExactlyInAnyOrder(
                "marketId",
                "intervalMs",
                "limit",
                "exchange",
                "symbol",
                "interval",
                "openTime",
                "closeTime"
        );
    }

    @Test
    void negativeFields_failValidation() {
        CsParam csParam = new CsParam(
                1L,
                60_000L,
                -1,
                "BINANCE",
                "BTC-USDT",
                "1m",
                -1L,
                -2L
        );

        Set<ConstraintViolation<CsParam>> violations = validator.validate(csParam);
        assertThat(violations).hasSize(3);
        Set<String> violationFields = violations.stream()
                .map(v -> v.getPropertyPath().toString())
                .collect(Collectors.toSet());
        assertThat(violationFields).containsExactlyInAnyOrder(
                "openTime",
                "closeTime",
                "limit"
        );
    }
}
