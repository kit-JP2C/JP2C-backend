package com.github.jp2c.doc.customizer;

import com.github.jp2c.annotation.SocketEvent;
import com.github.jp2c.common.dto.CommonResponse;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.BooleanSchema;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.NumberSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
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

                // === 요청 스키마 생성 (payload) ===
                Schema<?> payloadSchema = (ann.payload() == Void.class) ? null : generateSchemaFromClass(ann.payload());

                RequestBody jsonBody = null;
                if (payloadSchema != null) {
                    jsonBody = new RequestBody()
                        .required(true)
                        .content(new Content().addMediaType(
                            "application/json",
                            new MediaType().schema(payloadSchema)
                        ));
                }

                // === 응답 스키마 생성: 항상 CommonResponse<T> ===
                Schema<?> commonResponseSchema = generateCommonResponseSchemaByReflection(ann.response());

                ApiResponse successResponse = new ApiResponse()
                    .description("성공 응답")
                    .content(new Content().addMediaType(
                        "application/json",
                        new MediaType().schema(commonResponseSchema)
                    ));

                ApiResponses responses = new ApiResponses().addApiResponse("200", successResponse);

                // === Operation 생성 ===
                Operation op = new Operation()
                    .summary(ann.summary())
                    .description(Optional.ofNullable(ann.description()).orElse(""))
                    .addTagsItem("소켓")
                    .responses(responses);

                if (jsonBody != null) {
                    op.requestBody(jsonBody);
                }

                // 참고용 Vendor extension
                op.addExtension("x-socket-event", ann.value());

                // Swagger Path 주입 (가상 REST endpoint)
                openApi.path(ann.value(), new PathItem().post(op));
            }
        }
    }

    /* =========================
       CommonResponse<T> 리플렉션 기반 스키마
       ========================= */
    private Schema<?> generateCommonResponseSchemaByReflection(Class<?> dataClass) {
        ObjectSchema wrapper = new ObjectSchema();
        wrapper.name(CommonResponse.class.getSimpleName());

        for (Field field : getAllFields(CommonResponse.class)) {
            if (shouldSkip(field)) continue;

            String name = field.getName();

            if ("data".equals(name)) {
                // data 필드를 @SocketEvent.response() 타입으로 대체
                if (dataClass == Void.class || dataClass == void.class) {
                    // 빈/옵셔널 데이터
                    ObjectSchema empty = new ObjectSchema();
                    empty.nullable(true);
                    empty.description("빈 응답");
                    wrapper.addProperties("data", empty);
                } else {
                    Schema<?> dataSchema = generateSchemaFromClass(dataClass);
                    wrapper.addProperties("data", dataSchema);
                }
            } else {
                // CommonResponse의 나머지 필드는 실제 타입을 리플렉션으로 해석
                Schema<?> fieldSchema = mapToSchema(field.getType(), field.getGenericType(), new HashSet<>());
                wrapper.addProperties(name, fieldSchema);
            }
        }

        return wrapper;
    }

    /* =========================
       DTO → Swagger Schema (재귀)
       ========================= */
    private Schema<?> generateSchemaFromClass(Class<?> clazz) {
        return mapToSchema(clazz, clazz, new HashSet<>());
    }

    /* =========================
       타입 매핑 (재귀)
       - genericType을 함께 받아서 List<String> 같은 케이스 정밀 매핑
       - visited로 순환 참조 방지
       ========================= */
    private Schema<?> mapToSchema(Class<?> rawType, Type genericType, Set<Type> visited) {
        if (rawType == null) return new ObjectSchema();

        // 순환참조 방지
        if (genericType != null) {
            if (visited.contains(genericType)) {
                return new ObjectSchema().description("Cyclic reference: " + rawType.getSimpleName());
            }
            visited.add(genericType);
        }

        // 프리미티브 & Wrapper & 기본형
        if (rawType == String.class) return new StringSchema();
        if (rawType == Integer.class || rawType == int.class) return new IntegerSchema();
        if (rawType == Long.class || rawType == long.class) return new IntegerSchema().format("int64");
        if (rawType == Short.class || rawType == short.class) return new IntegerSchema().format("int32");
        if (rawType == Byte.class || rawType == byte.class) return new IntegerSchema().format("int32");

        if (rawType == Float.class || rawType == float.class) return new NumberSchema().format("float");
        if (rawType == Double.class || rawType == double.class) return new NumberSchema().format("double");
        if (rawType == BigDecimal.class) return new NumberSchema();
        if (rawType == BigInteger.class) return new IntegerSchema();

        if (rawType == Boolean.class || rawType == boolean.class) return new BooleanSchema();

        // 시간/날짜
        if (rawType == Date.class || rawType == Instant.class || rawType == OffsetDateTime.class) {
            return new StringSchema().format("date-time");
        }
        if (rawType == LocalDateTime.class) return new StringSchema().format("date-time");
        if (rawType == LocalDate.class) return new StringSchema().format("date");
        if (rawType == LocalTime.class) return new StringSchema().format("time");

        // Enum
        if (rawType.isEnum()) {
            StringSchema e = new StringSchema();
            Object[] constants = rawType.getEnumConstants();
            if (constants != null) {
                List<String> values = new ArrayList<>();
                for (Object c : constants) values.add(String.valueOf(c));
                e.setEnum(values);
            }
            return e;
        }

        // Optional<T>
        if (Optional.class.isAssignableFrom(rawType)) {
            Type t = (genericType instanceof ParameterizedType)
                ? ((ParameterizedType) genericType).getActualTypeArguments()[0]
                : Object.class;
            Class<?> inner = toClass(t);
            Schema<?> innerSchema = mapToSchema(inner, t, visited);
            innerSchema.setNullable(true);
            return innerSchema;
        }

        // 배열 T[]
        if (rawType.isArray()) {
            Class<?> component = rawType.getComponentType();
            ArraySchema arr = new ArraySchema();
            arr.setItems(mapToSchema(component, component, visited));
            return arr;
        }

        // Collection/List/Set
        if (Collection.class.isAssignableFrom(rawType)) {
            ArraySchema arr = new ArraySchema();
            Type itemType = (genericType instanceof ParameterizedType)
                ? ((ParameterizedType) genericType).getActualTypeArguments()[0]
                : String.class;
            Class<?> itemClass = toClass(itemType);
            arr.setItems(mapToSchema(itemClass, itemType, visited));
            return arr;
        }

        // Map<K,V>
        if (Map.class.isAssignableFrom(rawType)) {
            ObjectSchema m = new ObjectSchema();
            Type valueType = (genericType instanceof ParameterizedType)
                ? ((ParameterizedType) genericType).getActualTypeArguments()[1]
                : Object.class;
            Class<?> valueClass = toClass(valueType);
            m.setAdditionalProperties(mapToSchema(valueClass, valueType, visited));
            return m;
        }

        // POJO (재귀)
        ObjectSchema obj = new ObjectSchema();
        obj.description("Object: " + rawType.getSimpleName());
        for (Field f : getAllFields(rawType)) {
            if (shouldSkip(f)) continue;
            obj.addProperties(f.getName(), mapToSchema(f.getType(), f.getGenericType(), visited));
        }
        return obj;
    }

    /* =========================
       유틸
       ========================= */
    private static boolean shouldSkip(Field f) {
        int mod = f.getModifiers();
        return f.isSynthetic() || Modifier.isStatic(mod) || Modifier.isTransient(mod);
    }

    private static List<Field> getAllFields(Class<?> type) {
        List<Field> fields = new ArrayList<>();
        for (Class<?> t = type; t != null && t != Object.class; t = t.getSuperclass()) {
            for (Field f : t.getDeclaredFields()) {
                f.setAccessible(true);
                fields.add(f);
            }
        }
        return fields;
    }

    private static Class<?> toClass(Type type) {
        if (type instanceof Class) return (Class<?>) type;
        if (type instanceof ParameterizedType) {
            Type raw = ((ParameterizedType) type).getRawType();
            return (raw instanceof Class) ? (Class<?>) raw : Object.class;
        }
        if (type instanceof GenericArrayType) {
            Type comp = ((GenericArrayType) type).getGenericComponentType();
            Class<?> compClass = toClass(comp);
            return Array.newInstance(compClass, 0).getClass();
        }
        if (type instanceof TypeVariable) return Object.class;
        if (type instanceof WildcardType) return Object.class;
        return Object.class;
    }
}