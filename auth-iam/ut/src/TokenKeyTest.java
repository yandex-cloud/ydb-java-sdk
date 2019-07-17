package ru.yandex.ydb.auth.iam;


import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.Jwts;
import org.junit.Assert;
import org.junit.Test;


/**
 * @author Sergey Polovko
 */
public class TokenKeyTest {

    @Test
    public void toJwt() {
        PublicKey publicKey = Keys.publicKey(TokenKeyTest.class.getResourceAsStream("public.txt"));
        PrivateKey privateKey = Keys.privateKey(TokenKeyTest.class.getResourceAsStream("private.txt"));

        TokenKey key = new TokenKey("my-account", "my-key");
        long nowMillis = System.currentTimeMillis();
        long ttlMillis = TimeUnit.SECONDS.toMillis(90);

        Jwt jwt = Jwts.parser()
            .setSigningKey(publicKey)
            .parse(key.toJwt(privateKey, nowMillis, ttlMillis));

        Header header = jwt.getHeader();
        Assert.assertEquals(key.getKeyId(), header.get("kid"));
        Assert.assertEquals("PS256", header.get("alg"));

        Claims body = (Claims) jwt.getBody();
        Assert.assertEquals(key.getAccountId(), body.getIssuer());
        Assert.assertEquals("https://iam.api.cloud.yandex.net/iam/v1/tokens", body.getAudience());
        Assert.assertEquals(new Date((nowMillis / 1000) * 1000), body.getIssuedAt());
        Assert.assertEquals(new Date(((nowMillis + ttlMillis) / 1000) * 1000), body.getExpiration());
    }
}
