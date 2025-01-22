package ca.bc.gov.educ.api.penmatch.messaging.jetstream;

import ca.bc.gov.educ.api.penmatch.helpers.LogHelper;
import ca.bc.gov.educ.api.penmatch.properties.ApplicationProperties;
import ca.bc.gov.educ.api.penmatch.service.v1.events.JetStreamEventHandlerService;
import ca.bc.gov.educ.api.penmatch.struct.ChoreographedEvent;
import ca.bc.gov.educ.api.penmatch.util.JsonUtil;
import io.nats.client.Connection;
import io.nats.client.JetStreamApiException;
import io.nats.client.Message;
import io.nats.client.PushSubscribeOptions;
import io.nats.client.api.ConsumerConfiguration;
import io.nats.client.api.DeliverPolicy;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static ca.bc.gov.educ.api.penmatch.constants.Topics.PEN_MATCH_EVENTS_TOPIC;

/**
 * The type Subscriber.
 */
@Component
@DependsOn("publisher")
@Slf4j
public class Subscriber {
  /**
   * The Stan event handler service.
   */
  private final JetStreamEventHandlerService jetStreamEventHandlerService;
  private final Connection natsConnection;

  /**
   * Instantiates a new Subscriber.
   *
   * @param natsConnection               the nats connection
   * @param jetStreamEventHandlerService the stan event handler service
   */
  @Autowired
  public Subscriber(final Connection natsConnection, final JetStreamEventHandlerService jetStreamEventHandlerService) {
    this.jetStreamEventHandlerService = jetStreamEventHandlerService;
    this.natsConnection = natsConnection;
  }


  /**
   * This subscription will makes sure the messages are required to acknowledge manually to Jet Stream.
   * Subscribe.
   *
   * @throws IOException           the io exception
   * @throws JetStreamApiException the jet stream api exception
   */
  @PostConstruct
  public void subscribe() throws IOException, JetStreamApiException {
    val qName = "PEN-MATCH-EVENTS-TOPIC-PEN-MATCH-API";
    val autoAck = false;
    final PushSubscribeOptions options = PushSubscribeOptions.builder().stream(ApplicationProperties.STREAM_NAME)
        .durable("PEN-MATCH-API-PEN-MATCH-EVENTS-TOPIC-DURABLE")
        .configuration(ConsumerConfiguration.builder().deliverPolicy(DeliverPolicy.New).build()).build();
    this.natsConnection.jetStream().subscribe(PEN_MATCH_EVENTS_TOPIC.toString(), qName, this.natsConnection.createDispatcher(), this::onPenMatchEventsTopicMessage,
        autoAck, options);
  }

  /**
   * This method will process the event message pushed into the PEN_MATCH_EVENTS_TOPIC.
   * this will get the message and update the event status to mark that the event reached the message broker.
   * On message message handler.
   *
   * @param message the string representation of {@link ChoreographedEvent} if it not type of event then it will throw exception and will be ignored.
   */
  public void onPenMatchEventsTopicMessage(final Message message) {
    log.debug("Received message Subject:: {} , SID :: {} , sequence :: {}, pending :: {} ", message.getSubject(), message.getSID(), message.metaData().consumerSequence(), message.metaData().pendingCount());
    try {
      val eventString = new String(message.getData());
      LogHelper.logMessagingEventDetails(eventString);
      final ChoreographedEvent event = JsonUtil.getJsonObjectFromString(ChoreographedEvent.class, eventString);
      this.jetStreamEventHandlerService.updateEventStatus(event);
      log.debug("received event :: {} ", event);
      message.ack();
    } catch (final Exception ex) {
      log.error("Exception ", ex);
    }
  }
}
