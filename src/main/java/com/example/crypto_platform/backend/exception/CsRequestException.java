package com.example.crypto_platform.backend.exception;

import com.example.crypto_platform.backend.enums.CsRequestErrorCode;
import lombok.Getter;

@Getter
public class CsRequestException extends RuntimeException {
    private final CsRequestErrorCode errorCode;

    public CsRequestException(CsRequestErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public CsRequestException(CsRequestErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public static CsRequestException invalidParameter(String errorMessage) {
        return new CsRequestException(CsRequestErrorCode.INVALID_PARAMETER, "Error: " + errorMessage);
    }

    public static CsRequestException invalidInterval(String errorMessage) {
        return new CsRequestException(CsRequestErrorCode.INVALID_INTERVAL, "Error: " + errorMessage);
    }

    public static CsRequestException marketDataUnavailable(String errorMessage) {
        return new CsRequestException(CsRequestErrorCode.MARKETDATA_UNAVAILABLE, "Market data unavailable for: " + errorMessage);
    }

    public static CsRequestException exchangeUnavailable(String exchange) {
        return new CsRequestException(CsRequestErrorCode.EXCHANGE_UNAVAILABLE, "Exchange unavailable: " + exchange);
    }

    @Deprecated
    public static CsRequestException upstreamError(String exchange, Throwable cause) {
        return new CsRequestException(
                CsRequestErrorCode.UPSTREAM_ERROR,
                "Failed to fetch data from " + exchange,
                cause
        );
    }
}
