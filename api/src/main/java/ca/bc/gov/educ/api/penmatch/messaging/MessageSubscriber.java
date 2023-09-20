package ca.bc.gov.educ.api.penmatch.messaging;

import ca.bc.gov.educ.api.penmatch.helpers.LogHelper;
import ca.bc.gov.educ.api.penmatch.service.v1.events.EventHandlerDelegatorService;
import ca.bc.gov.educ.api.penmatch.struct.Event;
import ca.bc.gov.educ.api.penmatch.util.JsonUtil;
import io.nats.client.Connection;
import io.nats.client.Message;
import io.nats.client.MessageHandler;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

import static ca.bc.gov.educ.api.penmatch.constants.Topics.PEN_MATCH_API_TOPIC;
import static lombok.AccessLevel.PRIVATE;


/**
 * The type Message subscriber.
 */
@Component
@Slf4j
public class MessageSubscriber {
  /**
   * The Connection.
   */
  private final Connection connection;
  /**
   * The Event handler delegator service.
   */
  @Getter(PRIVATE)
  private final EventHandlerDelegatorService eventHandlerDelegatorService;

  /**
   * Instantiates a new Message subscriber.
   *
   * @param natsConnection               the nats connection
   * @param eventHandlerDelegatorService the event handler delegator service
   */
  @Autowired
  public MessageSubscriber(final NatsConnection natsConnection, final EventHandlerDelegatorService eventHandlerDelegatorService) {
    this.eventHandlerDelegatorService = eventHandlerDelegatorService;
    this.connection = natsConnection.getNatsCon();
  }

  /**
   * Subscribe.
   */
  @PostConstruct
  public void subscribe() {
    final String queue = PEN_MATCH_API_TOPIC.toString().replace("_", "-");
    final var dispatcher = this.connection.createDispatcher(this.onMessage());
    dispatcher.subscribe(PEN_MATCH_API_TOPIC.toString(), queue);
  }

  /**
   * On message message handler.
   *
   * @return the message handler
   */
  private MessageHandler onMessage() {
    return (Message message) -> {
      if (message != null) {
        log.info("Message received subject :: {},  replyTo :: {}, subscriptionID :: {}", message.getSubject(), message.getReplyTo(), message.getSID());
        try {
          final var eventString = new String(message.getData());
          LogHelper.logMessagingEventDetails(eventString);
          final var event = JsonUtil.getJsonObjectFromString(Event.class, eventString);
          this.eventHandlerDelegatorService.handleEvent(event, message);
        } catch (final Exception e) {
          log.error("Exception ", e);
        }
      }
    };
  }


}
