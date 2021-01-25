package ca.bc.gov.educ.api.penmatch.messaging;

import io.nats.client.Connection;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * The type Message publisher.
 */
@Component
@Slf4j
public class MessagePublisher {

  /**
   * The Connection.
   */
  private final Connection connection;

  /**
   * Instantiates a new Message publisher.
   *
   * @param natsConnection the nats connection
   */
  @Autowired
  public MessagePublisher(final NatsConnection natsConnection) {
    this.connection = natsConnection.getNatsCon();
  }

  /**
   * Dispatch message.
   *
   * @param subject the subject
   * @param message the message
   */
  public void dispatchMessage(String subject, byte[] message) {
    connection.publish(subject, message);
  }
}
