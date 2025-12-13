package com.example.crypto_platform.service;

import com.example.crypto_platform.utils.LockCallbackUtil;

public interface LockService {
    <T> T executeWithLock(String key, long waitTimeMs, long leaseTimeMs, LockCallbackUtil<T> callback);
}
