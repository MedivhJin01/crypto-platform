package com.example.crypto_platform.backend.service.impl;

import com.example.crypto_platform.backend.service.IntervalParseService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
public class IntervalParseServiceImpl implements IntervalParseService {

    @Validated
    @Override
    public long toMillis(@NotBlank String interval){
        String amount = interval.substring(0, interval.length() - 1);
        char unit = interval.charAt(interval.length() - 1);
        long millisPerUnit = switch (unit) {
            case 'm' -> 60_000L;
            case 'h' -> 3_600_000L;
            case 'd' -> 86_400_000L;
            case 'w' -> 604_800_000L;
            case 'M' -> 2_629_746_000L;
            case 'y' -> 31_556_952_000L;
            default -> throw new RuntimeException();
        };
        return Long.parseLong(amount) * millisPerUnit;
    }
}
