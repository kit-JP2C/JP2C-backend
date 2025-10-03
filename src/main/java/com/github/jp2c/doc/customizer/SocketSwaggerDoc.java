package com.github.jp2c.doc.customizer;

import com.github.jp2c.annotation.SocketEvent;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Component
public class SocketSwaggerDoc implements OpenApiCustomizer {

    private final ApplicationContext context;

    public SocketSwaggerDoc(ApplicationContext context) {
        this.context = context;
    }

    @Override
    public void customise(OpenAPI openApi) {
        for (String beanName : context.getBeanDefinitionNames()) {
            Object bean = context.getBean(beanName);

            for (Method method : bean.getClass().getDeclaredMethods()) {
                if (!method.isAnnotationPresent(SocketEvent.class)) continue;

                SocketEvent ann = method.getAnnotation(SocketEvent.class);

                // === 요청 스키마 생성 ===
                Schema<?> payloadSchema = generateSchemaFromClass(ann.payload());

                RequestBody jsonBody = null;
                if (ann.payload() != Void.class) {
                    jsonBody = new RequestBody()
                        .required(true)
                        .content(new Content().addMediaType(
                            "application/json",
                            new MediaType().schema(payloadSchema)
                        ));
                }

                // === 응답 스키마 생성 ===
                Schema<?> responseSchema = generateSchemaFromClass(ann.response());

                ApiResponse successResponse = new ApiResponse();
                if (ann.response() != Void.class) {
                    successResponse.setContent(new Content().addMediaType(
                        "application/json",
                        new MediaType().schema(responseSchema)
                    ));
                }

                ApiResponses responses = new ApiResponses()
                    .addApiResponse("200", successResponse);

                // === Operation 생성 ===
                Operation op = new Operation()
                    .summary(ann.summary())
                    .description(Optional.ofNullable(ann.description()).orElse(""))
                    .addTagsItem("소켓")
                    .responses(responses);

                if (jsonBody != null) {
                    op.requestBody(jsonBody);
                }

                op.addExtension("x-socket-event", ann.value());

                // Swagger Path 주입
                openApi.path(ann.value(), new PathItem().post(op));
            }
        }
    }

    /**
     * DTO → Swagger Schema 변환
     */
    private Schema<?> generateSchemaFromClass(Class<?> clazz) {
        if (clazz == Void.class || clazz == void.class) {
            return null; // payload/response 없음
        }

        ObjectSchema schema = new ObjectSchema();
        schema.setName(clazz.getSimpleName());

        for (Field field : clazz.getDeclaredFields()) {
            Class<?> fieldType = field.getType();
            Schema<?> fieldSchema = mapToSchema(fieldType);

            schema.addProperties(field.getName(), fieldSchema);
        }

        return schema;
    }

    private Schema<?> mapToSchema(Class<?> type) {
        if (type == String.class) return new StringSchema();

        if (Number.class.isAssignableFrom(type) || type.isPrimitive()) {
            return new Schema<>().type("number");
        }

        if (type == Boolean.class || type == boolean.class) {
            return new Schema<>().type("boolean");
        }

        // ✅ 컬렉션(Set, List 등)은 배열로 매핑
        if (Collection.class.isAssignableFrom(type) || Set.class.isAssignableFrom(type)) {
            return new ArraySchema().items(new StringSchema());
            // 여기선 일단 String으로 가정, 제네릭 타입 추출하면 더 정확히 가능
        }

        // ✅ Map은 Object로 매핑
        if (Map.class.isAssignableFrom(type)) {
            return new ObjectSchema().additionalProperties(true);
        }

        // ✅ DTO 같은 복합 객체는 재귀 처리
        ObjectSchema obj = new ObjectSchema();
        obj.description("Complex type: " + type.getSimpleName());
        for (Field field : type.getDeclaredFields()) {
            obj.addProperties(field.getName(), mapToSchema(field.getType()));
        }
        return obj;
    }
}