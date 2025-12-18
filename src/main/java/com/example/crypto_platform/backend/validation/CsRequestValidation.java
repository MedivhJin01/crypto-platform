package com.example.crypto_platform.backend.validation;

import com.example.crypto_platform.backend.dto.CsParam;
import com.example.crypto_platform.backend.dto.CsRequest;

public interface CsRequestValidation {
    CsParam validateCsRequest(CsRequest csRequest);
}
