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
  public static final String STREAM_NAME = "PEN_MATCH_EVENTS";

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

  /**
   * The PEN Services api url.
   */
  @Value("${penservices.api.url}")
  private String penServicesApiURL;

  /**
   * The Nats url.
   */
  @Value("${nats.url}")
  String natsUrl;


  /**
   * The Nats max reconnect.
   */
  @Value("${nats.maxReconnect}")
  Integer natsMaxReconnect;

}
