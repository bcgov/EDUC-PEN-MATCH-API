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
  private final PenMatchRequestInterceptor penMatchRequestInterceptor;

  /**
   * Instantiates a new Pen match mvc config.
   *
   * @param penDemogRequestInterceptor the pen demog request interceptor
   */
  @Autowired
  public PenMatchMVCConfig(final PenMatchRequestInterceptor penDemogRequestInterceptor) {
    this.penMatchRequestInterceptor = penDemogRequestInterceptor;
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(penMatchRequestInterceptor).addPathPatterns("/**");
  }
}
