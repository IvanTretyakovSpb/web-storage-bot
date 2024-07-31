package com.tretyakov.utils;

import org.hashids.Hashids;

public class CryptoTool {
    int MIN_HASH_LENGTH = 10;
    private final Hashids hashids;

    public CryptoTool(String salt) {
        this.hashids = new Hashids(salt, MIN_HASH_LENGTH);
    }

    public String hashOf(Long value) {
        return hashids.encode(value);
    }

    public Long idOf(String value) {
        long[] res = hashids.decode(value);
        if (res != null && res.length > 0) {
            return res[0];
        }
        return null;
    }
}
