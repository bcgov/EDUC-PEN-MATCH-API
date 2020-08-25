package ca.bc.gov.educ.api.penmatch.messaging;

import ca.bc.gov.educ.api.penmatch.properties.ApplicationProperties;
import ca.bc.gov.educ.api.penmatch.service.EventHandlerService;
import io.nats.streaming.AckHandler;
import io.nats.streaming.Options;
import io.nats.streaming.StreamingConnectionFactory;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

import static lombok.AccessLevel.PRIVATE;

@Component
@Slf4j
@SuppressWarnings("java:S2142")
public class MessagePublisher extends MessagePubSub {

  @Getter(PRIVATE)
  private final EventHandlerService eventHandlerService;

  @Autowired
  public MessagePublisher(final ApplicationProperties applicationProperties, final EventHandlerService eventHandlerService) throws IOException, InterruptedException {
    this.eventHandlerService = eventHandlerService;
    Options options = new Options.Builder()
            .natsUrl(applicationProperties.getNatsUrl())
            .clusterId(applicationProperties.getNatsClusterId())
            .connectionLostHandler(this::connectionLostHandler)
            .clientId("pen-match-api-publisher-" + UUID.randomUUID().toString()).build();
    connectionFactory = new StreamingConnectionFactory(options);
    connection = connectionFactory.createConnection();
  }

  public void dispatchMessage(String subject, byte[] message) throws InterruptedException, TimeoutException, IOException {
    try {
      AckHandler ackHandler = getAckHandler();
      connection.publish(subject, message, ackHandler);
    } catch (IllegalStateException e) {
      log.error(e.getMessage());
      if (e.getMessage() != null && e.getMessage().contains("stan: connection closed")) {
        executorService.execute(() -> {
          try {
            this.connectionLostHandler(connection, e);
            dispatchMessage(subject, message);
          } catch (InterruptedException | TimeoutException | IOException exception) {
            log.error("exception occurred :: ", exception);
          }
        });
      }
    }
  }


  private AckHandler getAckHandler() {
    return new AckHandler() {
      @Override
      public void onAck(String guid, Exception err) {
        log.trace("already handled.");
      }
      @Override
      public void onAck(String guid, String subject, byte[] data, Exception ex) {
        if (ex != null) {
          executorService.execute(() -> {
            try {
              retryPublish(subject, data);
            } catch (InterruptedException | TimeoutException | IOException e) {
              log.error("Exception", e);
            }
          });
        } else {
          log.trace("acknowledgement received {}", guid);
        }
      }
    };
  }

  public void retryPublish(String subject, byte[] message) throws InterruptedException, TimeoutException, IOException {
    log.trace("retrying...");
    this.dispatchMessage(subject, message);
  }
}
