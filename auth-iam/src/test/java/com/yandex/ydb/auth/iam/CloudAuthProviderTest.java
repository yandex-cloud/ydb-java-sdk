package com.yandex.ydb.auth.iam;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockserver.junit.MockServerRule;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;

import yandex.cloud.sdk.auth.provider.ComputeEngineCredentialProvider;
import yandex.cloud.sdk.auth.provider.CredentialProvider;


/**
 * @author Vasilii Briginets
 */
public class CloudAuthProviderTest {

    @Rule
    public MockServerRule mockServerRule = new MockServerRule(this);
    private CredentialProvider credentialProvider;

    @Before
    public void setUp() {
        mockServerRule.getClient()
                .when(HttpRequest.request()
                        .withMethod("GET")
                        .withPath("/computeMetadata/v1/instance/service-accounts/default/token")
                        .withHeader("Metadata-Flavor", "Google"))
                .respond(HttpResponse.response("{\"access_token\":\"my-awesome-token\",\"expires_in\":10}"));


        credentialProvider = ComputeEngineCredentialProvider.builder()
                .metadataServerUrl("http://localhost:" + mockServerRule.getPort())
                .build();
    }

    @Test
    public void getToken() {
        CloudAuthProvider authProvider = CloudAuthProvider.newAuthProvider(credentialProvider);
        Assert.assertEquals("my-awesome-token", authProvider.getToken());
    }
}
