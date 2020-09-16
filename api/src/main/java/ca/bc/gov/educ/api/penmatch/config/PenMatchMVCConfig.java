package ca.bc.gov.educ.api.penmatch.config;

import lombok.AccessLevel;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class PenMatchMVCConfig implements WebMvcConfigurer {

    @Getter(AccessLevel.PRIVATE)
    private final PenMatchRequestInterceptor penMatchRequestInterceptor;

    @Autowired
    public PenMatchMVCConfig(final PenMatchRequestInterceptor penDemogRequestInterceptor){
        this.penMatchRequestInterceptor = penDemogRequestInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(penMatchRequestInterceptor).addPathPatterns("/**/**/");
    }
}
