package com.example.crypto_platform.backend.utils;

@FunctionalInterface
public interface LockCallbackUtil<T> {
    T doWithLock();
}
