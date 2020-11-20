package ca.bc.gov.educ.api.penmatch.messaging;

import ca.bc.gov.educ.api.penmatch.service.events.EventHandlerDelegatorService;
import ca.bc.gov.educ.api.penmatch.struct.Event;
import ca.bc.gov.educ.api.penmatch.util.JsonUtil;
import io.nats.client.Connection;
import io.nats.client.Message;
import io.nats.client.MessageHandler;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

import static ca.bc.gov.educ.api.penmatch.constants.Topics.PEN_MATCH_API_TOPIC;
import static lombok.AccessLevel.PRIVATE;


@Component
@Slf4j
public class MessageSubscriber extends MessagePubSub {

  @Getter(PRIVATE)
  private final EventHandlerDelegatorService eventHandlerDelegatorService;

  @Autowired
  public MessageSubscriber(final Connection con, EventHandlerDelegatorService eventHandlerDelegatorService) {
    this.eventHandlerDelegatorService = eventHandlerDelegatorService;
    super.connection = con;
  }

  /**
   * This subscription will makes sure the messages are required to acknowledge manually to STAN.
   * Subscribe.
   */
  @PostConstruct
  public void subscribe() {
    String queue = PEN_MATCH_API_TOPIC.toString().replace("_", "-");
    var dispatcher = connection.createDispatcher(onMessage());
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
        log.info("Message received is :: {} ", message);
        try {
          var eventString = new String(message.getData());
          var event = JsonUtil.getJsonObjectFromString(Event.class, eventString);
          eventHandlerDelegatorService.handleEvent(event);
          log.debug("Event is :: {}", event);
        } catch (final Exception e) {
          log.error("Exception ", e);
        }
      }
    };
  }


}
