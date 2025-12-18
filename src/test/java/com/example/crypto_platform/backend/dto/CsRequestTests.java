package com.example.crypto_platform.backend.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.validation.Validation;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class CsRequestTests {
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }


    @Test
    void validCsRequest_passesValidation() {
        CsRequest csRequest = new CsRequest(
                "BINANCE",
                "BTC-USDT",
                "1m",
                1000L,
                2000L
        );
        Set<ConstraintViolation<CsRequest>> violations = validator.validate(csRequest);
        assertThat(violations).isEmpty();
    }

    @Test
    void blankFields_failValidation() {
        CsRequest csRequest = new CsRequest(
                "",
                "",
                "",
                1000L,
                2000L
        );
        Set<ConstraintViolation<CsRequest>> violations = validator.validate(csRequest);
        assertThat(violations).hasSize(3);
        Set<String> violationFields = violations.stream()
                .map(v -> v.getPropertyPath().toString())
                .collect(Collectors.toSet());

        assertThat(violationFields).containsExactlyInAnyOrder(
                "exchange",
                "symbol",
                "interval"
        );
    }

    @Test
    void nullFields_failValidation() {
        CsRequest csRequest = new CsRequest(
                "BINANCE",
                "BTC-USDT",
                "1m",
                null,
                null
        );
        Set<ConstraintViolation<CsRequest>> violations = validator.validate(csRequest);
        assertThat(violations).hasSize(2);
        Set<String> violationFields = violations.stream()
                .map(v -> v.getPropertyPath().toString())
                .collect(Collectors.toSet());

        assertThat(violationFields).containsExactlyInAnyOrder(
                "openTime",
                "closeTime"
        );
    }

    @Test
    void negativeFields_failValidation() {
        CsRequest csRequest = new CsRequest(
                "BINANCE",
                "BTC-USDT",
                "1m",
                -1L,
                -2L
        );
        Set<ConstraintViolation<CsRequest>> violations = validator.validate(csRequest);
        assertThat(violations).hasSize(2);
        Set<String> violationFields = violations.stream()
                .map(v -> v.getPropertyPath().toString())
                .collect(Collectors.toSet());

        assertThat(violationFields).containsExactlyInAnyOrder(
                "openTime",
                "closeTime"
        );
    }
}
