package com.myorg;

import software.amazon.awscdk.App;
import software.amazon.awscdk.Environment;
import software.amazon.awscdk.StackProps;

// Here we will set our account info
public class ProductApiCdkApp {

    public static Environment makeEnvironment(String account, String region) {
        account = (account == null) ? System.getenv("CDK_DEFAULT_ACCOUNT") : account;
        region = (region == null) ? System.getenv("CDK_DEFAULT_REGION") : region;

        return Environment.builder().account(account).region(region).build();
    }

    public static void main(final String[] args) {
        App app = new App();
        Environment env = makeEnvironment(null, null);

        new ProductApiCdkStack(app, "ProductApiCdkStack",
                StackProps.builder()
                        .env(env)
                        .build());

        app.synth();
    }
}

