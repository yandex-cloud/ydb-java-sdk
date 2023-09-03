package com.yandex.ydb.auth.iam;

import com.yandex.ydb.core.auth.AuthProvider;

import yandex.cloud.sdk.auth.provider.CredentialProvider;

/**
 * This provider is using CredentialProvider of Yandex Cloud SDK for Java.
 * Usage example:
 *
 * <pre>{@code
 * CredentialProvider provider = ApiKeyCredentialProvider.builder()
 *      .fromFile(/path/to/json/file"")
 *      .enableCache()
 *      .build();
 *
 * GrpcTransport transport = GrpcTransport.forEndpoint("some-ydb-endpoint", "my_db")
 *      .withAuthProvider(CloudAuthProvider.newAuthProvider(provider))
 *      .build();
 *
 * } </pre>
 *
 * @author Vasilii Briginets
 */
public class CloudAuthProvider implements AuthProvider {
    CredentialProvider credentialProvider;

    private CloudAuthProvider(CredentialProvider credentialProvider) {
        this.credentialProvider = credentialProvider;
    }

    public static CloudAuthProvider newAuthProvider(CredentialProvider provider) {
        return new CloudAuthProvider(provider);
    }

    @Override
    public String getToken() {
        return credentialProvider.get()
                .getToken();
    }
}
