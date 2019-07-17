package ru.yandex.ydb.auth.iam;

import java.security.PrivateKey;
import java.util.Date;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;


/**
 * @author Sergey Polovko
 */
final class TokenKey {
    private final String accountId;
    private final String keyId;

    TokenKey(String accountId, String keyId) {
        this.accountId = accountId;
        this.keyId = keyId;
    }

    String getAccountId() {
        return accountId;
    }

    String getKeyId() {
        return keyId;
    }

    String toJwt(PrivateKey privateKey, long nowMillis, long ttlMillis) {
        return Jwts.builder()
            .setHeaderParam("kid", keyId)
            .setIssuer(accountId)
            .setAudience("https://iam.api.cloud.yandex.net/iam/v1/tokens")
            .setIssuedAt(new Date(nowMillis))
            .setExpiration(new Date(nowMillis + ttlMillis))
            .signWith(privateKey, SignatureAlgorithm.PS256)
            .compact();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TokenKey tokenKey = (TokenKey) o;
        if (!accountId.equals(tokenKey.accountId)) return false;
        return keyId.equals(tokenKey.keyId);
    }

    @Override
    public int hashCode() {
        int result = accountId.hashCode();
        result = 31 * result + keyId.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "TokenKey{accountId='" + accountId + "', keyId='" + keyId + "'}";
    }
}
