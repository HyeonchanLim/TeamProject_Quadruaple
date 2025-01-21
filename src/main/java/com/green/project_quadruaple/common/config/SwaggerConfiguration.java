package com.green.project_quadruaple.common.config;

import com.green.project_quadruaple.common.config.constant.InfoConst;
import com.green.project_quadruaple.common.config.constant.JwtConst;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class SwaggerConfiguration {

    private final InfoConst infoConst;
    private final JwtConst jwtConst;

    @Bean
    public OpenAPI openAPI() {
        SecurityRequirement securityRequirement = new SecurityRequirement().addList(jwtConst.getHeaderKey());

        SecurityScheme securityScheme = new SecurityScheme().type(SecurityScheme.Type.HTTP)
                .in(SecurityScheme.In.HEADER)
                .name(jwtConst.getHeaderKey())
                .scheme(jwtConst.getScheme())
                .bearerFormat(jwtConst.getBearerFormat());

        return new OpenAPI().components(
                        new Components().addSecuritySchemes(jwtConst.getHeaderKey(), securityScheme))
                .addSecurityItem(securityRequirement)
                .info(new Info().title(infoConst.getTitle())
                        .description(infoConst.getDescription())
                        .version(infoConst.getVersion())
                );
    }


}