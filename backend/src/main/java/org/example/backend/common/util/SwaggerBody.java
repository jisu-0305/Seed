package org.example.backend.common.util;

import io.swagger.v3.oas.annotations.extensions.Extension;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import org.springframework.core.annotation.AliasFor;
import java.lang.annotation.*;

/*
 * 생성 이유: Swagger에서 Mutipart formdata 형식 업로드 가능하게 하기 위한 Custom Swagger Body
 * 순서: Swagger의 RequestBody를 사용해야지만 가능 -> Controller에서 Spring의 RequestBody이름과 겹침 따라서 별도의 Custom 생성 후 이름 변경 필요
 * */

@Target({ElementType.METHOD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@RequestBody
public @interface SwaggerBody {

    @AliasFor(annotation = RequestBody.class)
    String description() default "";

    @AliasFor(annotation = RequestBody.class)
    Content[] content() default {};

    @AliasFor(annotation = RequestBody.class)
    boolean required() default false;

    @AliasFor(annotation = RequestBody.class)
    Extension[] extensions() default {};

    @AliasFor(annotation = RequestBody.class)
    String ref() default "";
}