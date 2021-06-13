package ca.bc.gov.educ.api.penmatch.config;

import lombok.AccessLevel;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * The type Pen match mvc config.
 */
@Configuration
public class PenMatchMVCConfig implements WebMvcConfigurer {

  /**
   * The Pen match request interceptor.
   */
  @Getter(AccessLevel.PRIVATE)
  private final RequestResponseInterceptor requestResponseInterceptor;

  @Autowired
  public PenMatchMVCConfig(final RequestResponseInterceptor requestResponseInterceptor) {
    this.requestResponseInterceptor = requestResponseInterceptor;
  }

  @Override
  public void addInterceptors(final InterceptorRegistry registry) {
    registry.addInterceptor(this.requestResponseInterceptor).addPathPatterns("/**");
  }
}
