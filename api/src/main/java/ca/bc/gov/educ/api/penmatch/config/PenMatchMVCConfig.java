package ca.bc.gov.educ.api.pendemog.config;

import lombok.AccessLevel;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class PenDemogMVCConfig implements WebMvcConfigurer {

    @Getter(AccessLevel.PRIVATE)
    private final PenDemogRequestInterceptor penDemogRequestInterceptor;

    @Autowired
    public PenDemogMVCConfig(final PenDemogRequestInterceptor penDemogRequestInterceptor){
        this.penDemogRequestInterceptor = penDemogRequestInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(penDemogRequestInterceptor).addPathPatterns("/**/**/");
    }
}
