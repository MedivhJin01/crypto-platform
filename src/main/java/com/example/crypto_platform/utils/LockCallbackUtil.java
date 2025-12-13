package com.example.crypto_platform.utils;

@FunctionalInterface
public interface LockCallbackUtil<T> {
    T doWithLock();
}
