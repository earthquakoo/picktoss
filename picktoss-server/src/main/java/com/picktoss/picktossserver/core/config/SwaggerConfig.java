package com.picktoss.picktossserver.core.config;

import com.picktoss.picktossserver.core.exception.ErrorInfo;
import com.picktoss.picktossserver.core.exception.ErrorResponse;
import com.picktoss.picktossserver.core.exception.ErrorResponseDto;
import com.picktoss.picktossserver.core.swagger.ApiErrorCodeExample;
import com.picktoss.picktossserver.core.swagger.ApiErrorCodeExamples;
import com.picktoss.picktossserver.core.swagger.ExampleHolder;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.annotations.servers.ServerVariable;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.HandlerMethod;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
@OpenAPIDefinition(
        servers = {
                @Server(url = "https://api.picktoss.com:{port}",
                        description = "The production API server",
                        variables = {
                                @ServerVariable(name = "port", defaultValue = "444")
                        }
                ),
                @Server(url = "http://localhost:8181",
                        description = "Local API Server"
                )
        }
)
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        String jwtSchemeName = "jwtAuth";
        SecurityRequirement securityRequirement = new SecurityRequirement().addList(jwtSchemeName);
        Components components = new Components()
                .addSecuritySchemes(jwtSchemeName, new SecurityScheme()
                        .name(jwtSchemeName)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT"));
        return new OpenAPI()
                .addSecurityItem(securityRequirement)
                .components(components)
                .info(apiInfo());
    }

    private Info apiInfo() {
        return new Info()
                .title("Picktoss Server")
                .description("Picktoss Server API Documentation")
                .version("1.0.0");
    }

    @Bean
    public OperationCustomizer customize() {
        return (Operation operation, HandlerMethod handlerMethod) -> {
            ApiErrorCodeExamples apiErrorCodeExamples = handlerMethod.getMethodAnnotation(
                    ApiErrorCodeExamples.class);

            if (apiErrorCodeExamples != null) {
                generateErrorCodeResponseExample(operation, apiErrorCodeExamples.value());
            } else {
                ApiErrorCodeExample apiErrorCodeExample = handlerMethod.getMethodAnnotation(
                        ApiErrorCodeExample.class);

                if (apiErrorCodeExample != null) {
                    generateErrorCodeResponseExample(operation, apiErrorCodeExample.value());
                }
            }

            Parameter timezoneHeader = new Parameter()
                    .in("header")
                    .name("X-Timezone")
                    .description("사용자의 IANA 타임존 ID (e.g., Asia/Seoul)")
                    .required(false)
                    .schema(new StringSchema()._default("Asia/Seoul").example("Asia/Seoul"));

            operation.addParametersItem(timezoneHeader);

            return operation;
        };
    }

    private void generateErrorCodeResponseExample(Operation operation, ErrorInfo[] errorInfos) {
        ApiResponses responses = operation.getResponses();

        Map<Integer, List<ExampleHolder>> statusWithExampleHolders = Arrays.stream(errorInfos)
                .map(
                        errorCode -> ExampleHolder.builder()
                                .holder(getSwaggerExample(errorCode))
                                .code(errorCode.getStatusCode())
                                .name(errorCode.name())
                                .build()
                )
                .collect(Collectors.groupingBy(ExampleHolder::getCode));

        addExamplesToResponses(responses, statusWithExampleHolders);
    }

    private void generateErrorCodeResponseExample(Operation operation, ErrorInfo errorInfo) {
        ApiResponses responses = operation.getResponses();

        ExampleHolder exampleHolder = ExampleHolder.builder()
                .holder(getSwaggerExample(errorInfo))
                .name(errorInfo.name())
                .code(errorInfo.getStatusCode())
                .build();
        addExamplesToResponses(responses, exampleHolder);
    }

    private Example getSwaggerExample(ErrorInfo errorInfo) {
        ErrorResponseDto errorResponseDto = ErrorResponse.from(errorInfo);
        Example example = new Example();
        example.setValue(errorResponseDto);

        return example;
    }

    private void addExamplesToResponses(ApiResponses responses,
                                        Map<Integer, List<ExampleHolder>> statusWithExampleHolders) {
        statusWithExampleHolders.forEach(
                (status, v) -> {
                    Content content = new Content();
                    MediaType mediaType = new MediaType();
                    ApiResponse apiResponse = new ApiResponse();

                    v.forEach(
                            exampleHolder -> mediaType.addExamples(
                                    exampleHolder.getName(),
                                    exampleHolder.getHolder()
                            )
                    );
                    content.addMediaType("application/json", mediaType);
                    apiResponse.setContent(content);
                    responses.addApiResponse(String.valueOf(status), apiResponse);
                }
        );
    }

    private void addExamplesToResponses(ApiResponses responses, ExampleHolder exampleHolder) {
        Content content = new Content();
        MediaType mediaType = new MediaType();
        ApiResponse apiResponse = new ApiResponse();

        mediaType.addExamples(exampleHolder.getName(), exampleHolder.getHolder());
        content.addMediaType("application/json", mediaType);
        apiResponse.content(content);
        responses.addApiResponse(String.valueOf(exampleHolder.getCode()), apiResponse);
    }

}
