package com.example.crypto_platform.validation;

import com.example.crypto_platform.dto.CsParam;
import com.example.crypto_platform.dto.CsRequest;

public interface CsRequestValidation {
    CsParam validateCsRequest(CsRequest csRequest);
}
