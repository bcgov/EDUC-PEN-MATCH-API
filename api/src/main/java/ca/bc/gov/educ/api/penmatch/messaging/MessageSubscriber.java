package ca.bc.gov.educ.api.penmatch.messaging;

import ca.bc.gov.educ.api.penmatch.properties.ApplicationProperties;
import ca.bc.gov.educ.api.penmatch.service.EventHandlerService;
import ca.bc.gov.educ.api.penmatch.struct.Event;
import ca.bc.gov.educ.api.penmatch.util.JsonUtil;
import io.nats.streaming.*;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static ca.bc.gov.educ.api.penmatch.constants.Topics.PEN_MATCH_API_TOPIC;
import static lombok.AccessLevel.PRIVATE;

/**
 * This listener uses durable queue groups of nats streaming client.
 * A durable queue group allows you to have all members leave but still maintain state. When a member re-joins, it starts at the last position in that group.
 * <b>DO NOT call unsubscribe on the subscription.</b> please see the below for details.
 * Closing the Group
 * The last member calling Unsubscribe will close (that is destroy) the group. So if you want to maintain durability of the group,
 * <b>you should not be calling Unsubscribe.</b>
 * <p>
 * So unlike for non-durable queue subscribers, it is possible to maintain a queue group with no member in the server.
 * When a new member re-joins the durable queue group, it will resume from where the group left of, actually first receiving
 * all unacknowledged messages that may have been left when the last member previously left.
 */
@Component
@Slf4j
@SuppressWarnings("java:S2142")
public class MessageSubscriber extends MessagePubSub {

  @Getter(PRIVATE)
  private final EventHandlerService eventHandlerService;

  @Autowired
  public MessageSubscriber(final ApplicationProperties applicationProperties, final EventHandlerService eventHandlerService) throws IOException, InterruptedException {
    this.eventHandlerService = eventHandlerService;
    Options options = new Options.Builder()
        .natsUrl(applicationProperties.getNatsUrl())
        .clusterId(applicationProperties.getNatsClusterId())
        .clientId("pen-match-api-subscriber-" + UUID.randomUUID().toString())
        .connectionLostHandler(this::connectionLostHandler).build();
    connectionFactory = new StreamingConnectionFactory(options);
    connection = connectionFactory.createConnection();
  }

  @PostConstruct
  public void subscribe() throws InterruptedException, TimeoutException, IOException {
    SubscriptionOptions options = new SubscriptionOptions.Builder().durableName("pen-match-api-consumer").build();
    connection.subscribe(PEN_MATCH_API_TOPIC.toString(), "pen_match_api", this::onPenMatchApiTopicMessage, options);
  }

  private void onPenMatchApiTopicMessage(Message message) {
    if (message != null && message.getData() != null) {
      String messageData = new String(message.getData());
      if (messageData.contains("eventType")) {
        try {
          Event event = JsonUtil.getJsonObjectFromString(Event.class, messageData);
          log.info("received event for event type :: {} and saga ID :: {}", event.getEventType(), event.getSagaId());
          getEventHandlerService().handleEvent(event);
        } catch (final Exception ex) {
          log.error("Exception ", ex);
        }
      } else {
        log.info("Received Message :: {}", messageData);
      }
    }
  }


  /**
   * This method will keep retrying for a connection.
   */

  protected int connectionLostHandler(StreamingConnection streamingConnection, Exception e) {
    int numOfRetries = 1;
    if (e != null) {
      numOfRetries = super.connectionLostHandler(streamingConnection,e);
      retrySubscription(numOfRetries);
    }
    return numOfRetries;
  }

  private void retrySubscription(int numOfRetries) {
    while (true) {
      try {
        log.trace("retrying subscription as connection was lost :: retrying ::" + numOfRetries++);
        this.subscribe();
        log.info("successfully resubscribed after {} attempts", numOfRetries);
        break;
      } catch (InterruptedException | TimeoutException | IOException exception) {
        log.error("exception occurred while retrying subscription", exception);
        try {
          double sleepTime = (2 * numOfRetries);
          TimeUnit.SECONDS.sleep((long) sleepTime);
        } catch (InterruptedException exc) {
          log.error("InterruptedException occurred while retrying subscription", exc);
        }
      }
    }
  }
}