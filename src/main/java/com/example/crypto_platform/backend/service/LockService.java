package com.example.crypto_platform.backend.service;

import com.example.crypto_platform.backend.utils.LockCallbackUtil;

public interface LockService {
    <T> T executeWithLock(String key, long waitTimeMs, long leaseTimeMs, LockCallbackUtil<T> callback);
}
