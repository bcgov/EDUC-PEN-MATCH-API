package ca.bc.gov.educ.api.penmatch.health;

import ca.bc.gov.educ.api.penmatch.messaging.NatsConnection;
import io.nats.client.Connection;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * The type Pen match api custom health check.
 */
@Component
public class PenMatchAPICustomHealthCheck implements HealthIndicator {
  /**
   * The Nats connection.
   */
  private final NatsConnection natsConnection;

  /**
   * Instantiates a new Pen match api custom health check.
   *
   * @param natsConnection the nats connection
   */
  public PenMatchAPICustomHealthCheck(NatsConnection natsConnection) {
    this.natsConnection = natsConnection;
  }

  @Override
  public Health getHealth(boolean includeDetails) {
    return healthCheck();
  }


  @Override
  public Health health() {
    return healthCheck();
  }

  /**
   * Health check health.
   *
   * @return the health
   */
  private Health healthCheck() {
    if (this.natsConnection.getNatsCon() == null) {
      return Health.down().withDetail("NATS", " Connection is null.").build();
    } else if (this.natsConnection.getNatsCon().getStatus() == Connection.Status.CLOSED) {
      return Health.down().withDetail("NATS", " Connection is Closed.").build();
    }
    return Health.up().build();
  }
}
