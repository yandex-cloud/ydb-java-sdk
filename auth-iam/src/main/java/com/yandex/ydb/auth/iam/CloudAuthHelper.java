package com.yandex.ydb.auth.iam;

import java.nio.file.Paths;

import com.yandex.ydb.core.auth.AuthProvider;
import com.yandex.ydb.core.auth.NopAuthProvider;
import yandex.cloud.sdk.auth.provider.ApiKeyCredentialProvider;
import yandex.cloud.sdk.auth.provider.ComputeEngineCredentialProvider;
import yandex.cloud.sdk.auth.provider.IamTokenCredentialProvider;

public class CloudAuthHelper {

    public static AuthProvider getAuthProviderFromEnviron() {
        String saKeyFile = System.getenv("YDB_SERVICE_ACCOUNT_KEY_FILE_CREDENTIALS");
        if (saKeyFile != null) {
            return CloudAuthProvider.newAuthProvider(
                    ApiKeyCredentialProvider.builder()
                            .fromFile(Paths.get(saKeyFile))
                            .enableCache()
                            .build()
            );
        }

        String anonCredentials = System.getenv("YDB_ANONYMOUS_CREDENTIALS");
        if (anonCredentials != null && anonCredentials.equals("1")) {
            return NopAuthProvider.INSTANCE;
        }

        String metadataCredentials = System.getenv("YDB_METADATA_CREDENTIALS");
        if (metadataCredentials != null && metadataCredentials.equals("1")) {
            return CloudAuthProvider.newAuthProvider(
                    ComputeEngineCredentialProvider.builder()
                            .enableCache()
                            .build()
            );
        }

        String accessToken = System.getenv("YDB_ACCESS_TOKEN_CREDENTIALS");
        if (accessToken != null) {
            return CloudAuthProvider.newAuthProvider(
                    IamTokenCredentialProvider.builder()
                            .token(accessToken)
                            .build()
            );
        }

        return CloudAuthProvider.newAuthProvider(
                ComputeEngineCredentialProvider.builder()
                        .build()
        );
    }
}
