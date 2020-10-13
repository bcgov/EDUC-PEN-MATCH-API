package ca.bc.gov.educ.api.penmatch.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Class holds all application properties
 *
 * @author Marco Villeneuve
 */
@Component
@Getter
@Setter
public class ApplicationProperties {
  /**
   * The constant API_NAME.
   */
  public static final String API_NAME = "PEN_MATCH_API";

  /**
   * The Nats url.
   */
  @Value("${nats.streaming.server.url}")
  @Getter
  private String natsUrl;

  /**
   * The Nats cluster id.
   */
  @Value("${nats.streaming.server.clusterId}")
  @Getter
  private String natsClusterId;

  /**
   * The Client id.
   */
  @Value("${client.id}")
  private String clientID;

  /**
   * The Client secret.
   */
  @Value("${client.secret}")
  private String clientSecret;

  /**
   * The Token url.
   */
  @Value("${token.url}")
  private String tokenURL;

  /**
   * The Student api url.
   */
  @Value("${student.api.url}")
  private String studentApiURL;
}
