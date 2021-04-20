package ca.bc.gov.educ.api.penmatch.health;

import ca.bc.gov.educ.api.penmatch.messaging.NatsConnection;
import io.nats.client.*;
import io.nats.client.api.ServerInfo;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Status;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.time.Duration;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
@RunWith(SpringRunner.class)
public class PenMatchAPICustomHealthCheckTest {

  @Autowired
  NatsConnection natsConnection;

  @Autowired
  private PenMatchAPICustomHealthCheck penMatchAPICustomHealthCheck;

  @Test
  public void testGetHealth_givenNoNatsConnection_shouldReturnStatusDown() {
    when(this.natsConnection.getNatsCon()).thenReturn(null);
    assertThat(this.penMatchAPICustomHealthCheck.getHealth(true)).isNotNull();
    assertThat(this.penMatchAPICustomHealthCheck.getHealth(true).getStatus()).isEqualTo(Status.DOWN);
  }

  @Test
  public void testGetHealth_givenClosedNatsConnection_shouldReturnStatusDown() {
    when(this.natsConnection.getNatsCon()).thenReturn(this.getMockConnection(Connection.Status.CLOSED));
    assertThat(this.penMatchAPICustomHealthCheck.getHealth(true)).isNotNull();
    assertThat(this.penMatchAPICustomHealthCheck.getHealth(true).getStatus()).isEqualTo(Status.DOWN);
  }

  @Test
  public void testGetHealth_givenOpenNatsConnection_shouldReturnStatusUp() {
    when(this.natsConnection.getNatsCon()).thenReturn(this.getMockConnection(Connection.Status.CONNECTED));
    assertThat(this.penMatchAPICustomHealthCheck.getHealth(true)).isNotNull();
    assertThat(this.penMatchAPICustomHealthCheck.getHealth(true).getStatus()).isEqualTo(Status.UP);
  }

  @Test
  public void testHealth_givenNoNatsConnection_shouldReturnStatusDown() {
    when(this.natsConnection.getNatsCon()).thenReturn(null);
    assertThat(this.penMatchAPICustomHealthCheck.health()).isNotNull();
    assertThat(this.penMatchAPICustomHealthCheck.health().getStatus()).isEqualTo(Status.DOWN);
  }

  @Test
  public void testHealth_givenClosedNatsConnection_shouldReturnStatusDown() {
    when(this.natsConnection.getNatsCon()).thenReturn(this.getMockConnection(Connection.Status.CLOSED));
    assertThat(this.penMatchAPICustomHealthCheck.health()).isNotNull();
    assertThat(this.penMatchAPICustomHealthCheck.health().getStatus()).isEqualTo(Status.DOWN);
  }

  @Test
  public void testHealth_givenOpenNatsConnection_shouldReturnStatusUp() {
    when(this.natsConnection.getNatsCon()).thenReturn(this.getMockConnection(Connection.Status.CONNECTED));
    assertThat(this.penMatchAPICustomHealthCheck.health()).isNotNull();
    assertThat(this.penMatchAPICustomHealthCheck.health().getStatus()).isEqualTo(Status.UP);
  }

  private Connection getMockConnection(final Connection.Status status) {
    return new Connection() {
      @Override
      public void publish(final String subject, final byte[] body) {

      }

      @Override
      public void publish(final String subject, final String replyTo, final byte[] body) {

      }

      /**
       * Send a message to the specified subject. The message body <strong>will
       * not</strong> be copied. The expected usage with string content is something
       * like:
       *
       * <pre>
       * nc = Nats.connect()
       * nc.publish(NatsMessage.builder()...build())
       * </pre>
       * <p>
       * where the sender creates a byte array immediately before calling publish.
       * <p>
       * See {@link #publish(String, String, byte[]) publish()} for more details on
       * publish during reconnect.
       *
       * @param message the message
       * @throws IllegalStateException if the reconnect buffer is exceeded
       */
      @Override
      public void publish(final Message message) {

      }

      @Override
      public CompletableFuture<Message> request(final String subject, final byte[] data) {
        return null;
      }

      /**
       * Send a request. The returned future will be completed when the
       * response comes back.
       *
       * @param message the message
       * @return a Future for the response, which may be cancelled on error or timed out
       */
      @Override
      public CompletableFuture<Message> request(final Message message) {
        return null;
      }

      @Override
      public Message request(final String subject, final byte[] data, final Duration timeout) throws InterruptedException {
        return null;
      }

      /**
       * Send a request and returns the reply or null. This version of request is equivalent
       * to calling get on the future returned from {@link #request(String, byte[]) request()} with
       * the timeout and handling the ExecutionException and TimeoutException.
       *
       * @param message the message
       * @param timeout the time to wait for a response
       * @return the reply message or null if the timeout is reached
       * @throws InterruptedException if one is thrown while waiting, in order to propagate it up
       */
      @Override
      public Message request(final Message message, final Duration timeout) throws InterruptedException {
        return null;
      }

      @Override
      public Subscription subscribe(final String subject) {
        return null;
      }

      @Override
      public Subscription subscribe(final String subject, final String queueName) {
        return null;
      }

      @Override
      public Dispatcher createDispatcher(final MessageHandler handler) {
        return null;
      }

      /**
       * Convenience method to create a dispatcher with no default handler. Only used
       * with JetStream push subscriptions that require specific handlers per subscription.
       *
       * @return a new Dispatcher
       */
      @Override
      public Dispatcher createDispatcher() {
        return null;
      }

      @Override
      public void closeDispatcher(final Dispatcher dispatcher) {

      }

      @Override
      public void flush(final Duration timeout) throws TimeoutException, InterruptedException {

      }

      @Override
      public CompletableFuture<Boolean> drain(final Duration timeout) throws TimeoutException, InterruptedException {
        return null;
      }

      @Override
      public void close() throws InterruptedException {

      }

      @Override
      public Status getStatus() {
        return status;
      }

      @Override
      public long getMaxPayload() {
        return 0;
      }

      @Override
      public Collection<String> getServers() {
        return null;
      }

      @Override
      public Statistics getStatistics() {
        return null;
      }

      @Override
      public Options getOptions() {
        return null;
      }

      /**
       * @return the server information such as id, client info, etc.
       */
      @Override
      public ServerInfo getServerInfo() {
        return null;
      }

      @Override
      public String getConnectedUrl() {
        return null;
      }

      @Override
      public String getLastError() {
        return null;
      }

      @Override
      public String createInbox() {
        return null;
      }

      /**
       * Immediately flushes the underlying connection buffer if the connection is valid.
       *
       * @throws IOException           the connection flush fails
       * @throws IllegalStateException the connection is not connected
       */
      @Override
      public void flushBuffer() throws IOException {

      }

      /**
       * Gets a context for publishing and subscribing to subjects backed by Jetstream streams
       * and consumers.
       *
       * @return a JetStream instance.
       * @throws IOException various IO exception such as timeout or interruption
       */
      @Override
      public JetStream jetStream() throws IOException {
        return null;
      }

      /**
       * Gets a context for publishing and subscribing to subjects backed by Jetstream streams
       * and consumers.
       *
       * @param options JetStream options.
       * @return a JetStream instance.
       * @throws IOException covers various communication issues with the NATS
       *                     server such as timeout or interruption
       */
      @Override
      public JetStream jetStream(final JetStreamOptions options) throws IOException {
        return null;
      }

      /**
       * Gets a context for managing Jetstream streams
       * and consumers.
       *
       * @return a JetStream instance.
       * @throws IOException various IO exception such as timeout or interruption
       */
      @Override
      public JetStreamManagement jetStreamManagement() throws IOException {
        return null;
      }

      /**
       * Gets a context for managing Jetstream streams
       * and consumers.
       *
       * @param options JetStream options.
       * @return a JetStream instance.
       * @throws IOException covers various communication issues with the NATS
       *                     server such as timeout or interruption
       */
      @Override
      public JetStreamManagement jetStreamManagement(final JetStreamOptions options) throws IOException {
        return null;
      }
    };
  }
}
