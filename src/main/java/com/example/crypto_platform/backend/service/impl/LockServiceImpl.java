package com.example.crypto_platform.backend.service.impl;

import com.example.crypto_platform.backend.service.LockService;
import com.example.crypto_platform.backend.utils.LockCallbackUtil;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class LockServiceImpl implements LockService {

    @Autowired
    private RedissonClient redissonClient;

    @Override
    public <T> T executeWithLock(
            String key,
            long waitTimeMs,
            long leaseTimeMs,
            LockCallbackUtil<T> callback) {
        RLock lock = redissonClient.getLock(key);
        boolean locked = false;

        try {
            locked = lock.tryLock(waitTimeMs, leaseTimeMs, TimeUnit.MILLISECONDS);

            if (!locked) {
                return null;
            }

            return callback.doWithLock();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        } finally {
            if (locked && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
