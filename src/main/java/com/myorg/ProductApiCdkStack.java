package com.myorg;

import software.amazon.awscdk.*;
import software.amazon.awscdk.services.apigateway.IResource;
import software.amazon.awscdk.services.apigateway.Integration;
import software.amazon.awscdk.services.apigateway.LambdaIntegration;
import software.amazon.awscdk.services.apigateway.RestApi;
import software.amazon.awscdk.services.apigateway.RestApiProps;
import software.amazon.awscdk.services.dynamodb.AttributeType;
import software.amazon.awscdk.services.dynamodb.Table;
import software.amazon.awscdk.services.dynamodb.TableProps;
import software.amazon.awscdk.services.lambda.Code;
import software.amazon.awscdk.services.lambda.Function;
import software.amazon.awscdk.services.lambda.FunctionProps;
import software.amazon.awscdk.services.lambda.Runtime;
import software.constructs.Construct;
import software.amazon.awscdk.services.dynamodb.Attribute;

import java.util.HashMap;
import java.util.Map;

// Here we will write our cdk code
public class ProductApiCdkStack extends Stack {
    public ProductApiCdkStack(final Construct scope, final String id) {
        this(scope, id, null);
    }

    public ProductApiCdkStack(final Construct scope, final String id, final StackProps props) {
        super(scope, id, props);

        Attribute partitionKey = Attribute.builder()
                .name("id")
                .type(AttributeType.NUMBER)
                .build();

        TableProps tableProps = TableProps.builder()
                .tableName("products")
                .partitionKey(partitionKey)
                .removalPolicy(RemovalPolicy.DESTROY)
                // this will remove the table when we run cdk destroy
                .build();

        Table dynamoTable = new Table(this, "products", tableProps);

        // Lets create two environment variable that can be accessible from our lambda code

        Map<String, String> lambdaEnvMap = new HashMap<>();
        lambdaEnvMap.put("TABLE_NAME", dynamoTable.getTableName());
        lambdaEnvMap.put("PRIMARY_KEY", "id");

        // Lambda code
        String createFunctionHandlerName = "lambda.api.CreateProduct";
        String getFunctionHandlerName = "lambda.api.GetSingleProduct";
        String updateFunctionHandlerName = "lambda.api.UpdateProduct";
        String deleteFunctionHandlerName = "lambda.api.DeleteProduct";

        Function createFunction = new Function(this, "createProductFunction",
                getLambdaFunctionProps(lambdaEnvMap, createFunctionHandlerName));
        Function getFunction = new Function(this, "getProductFunction",
                getLambdaFunctionProps(lambdaEnvMap, getFunctionHandlerName));
        Function updateFunction = new Function(this, "updateProductFunction",
                getLambdaFunctionProps(lambdaEnvMap, updateFunctionHandlerName));
        Function deleteFunction = new Function(this, "deleteProductFunction",
                getLambdaFunctionProps(lambdaEnvMap, deleteFunctionHandlerName));

        dynamoTable.grantReadWriteData(createFunction);
        dynamoTable.grantReadWriteData(getFunction);
        dynamoTable.grantReadWriteData(updateFunction);
        dynamoTable.grantReadWriteData(deleteFunction);

        // RestAPI Code
        RestApiProps restApiProps = RestApiProps.builder()
                .restApiName("Product Service")
                .build();

        RestApi api = new RestApi(this, "productsApi", restApiProps);

        // Path
        IResource productResource = api.getRoot().addResource("products");

        // Lambda Integration Path
        Integration createIntegration = new LambdaIntegration(createFunction);
        productResource.addMethod("POST", createIntegration);

        // Parameters Resources
        IResource singleProductResource = productResource.addResource("{id}");

        Integration getIntegration = new LambdaIntegration(getFunction);
        singleProductResource.addMethod("GET", getIntegration);

        Integration updateIntegration = new LambdaIntegration(updateFunction);
        singleProductResource.addMethod("PUT", updateIntegration);

        Integration deleteIntegration = new LambdaIntegration(deleteFunction);
        singleProductResource.addMethod("DELETE", deleteIntegration);



    }

    private FunctionProps getLambdaFunctionProps(Map<String, String> lambdaEnvMap, String handler) {
        return FunctionProps.builder()
                .code(Code.fromAsset("C:/Users/Alan/Programming/API/Java/api-aws-requesthandler/target/api-aws-no-streamhandler-1.0-SNAPSHOT.jar"))
                .handler(handler)
                .runtime(Runtime.JAVA_8)
                .environment(lambdaEnvMap)
                .timeout(Duration.seconds(15))
                .memorySize(512)
                .build();
    }
}
