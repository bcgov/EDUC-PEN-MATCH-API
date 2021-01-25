package ca.bc.gov.educ.api.penmatch.health;

import ca.bc.gov.educ.api.penmatch.messaging.NatsConnection;
import io.nats.client.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Status;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

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
    when(natsConnection.getNatsCon()).thenReturn(null);
    assertThat(penMatchAPICustomHealthCheck.getHealth(true)).isNotNull();
    assertThat(penMatchAPICustomHealthCheck.getHealth(true).getStatus()).isEqualTo(Status.DOWN);
  }

  @Test
  public void testGetHealth_givenClosedNatsConnection_shouldReturnStatusDown() {
    when(natsConnection.getNatsCon()).thenReturn(getMockConnection(Connection.Status.CLOSED));
    assertThat(penMatchAPICustomHealthCheck.getHealth(true)).isNotNull();
    assertThat(penMatchAPICustomHealthCheck.getHealth(true).getStatus()).isEqualTo(Status.DOWN);
  }

  @Test
  public void testGetHealth_givenOpenNatsConnection_shouldReturnStatusUp() {
    when(natsConnection.getNatsCon()).thenReturn(getMockConnection(Connection.Status.CONNECTED));
    assertThat(penMatchAPICustomHealthCheck.getHealth(true)).isNotNull();
    assertThat(penMatchAPICustomHealthCheck.getHealth(true).getStatus()).isEqualTo(Status.UP);
  }

  @Test
  public void testHealth_givenNoNatsConnection_shouldReturnStatusDown() {
    when(natsConnection.getNatsCon()).thenReturn(null);
    assertThat(penMatchAPICustomHealthCheck.health()).isNotNull();
    assertThat(penMatchAPICustomHealthCheck.health().getStatus()).isEqualTo(Status.DOWN);
  }

  @Test
  public void testHealth_givenClosedNatsConnection_shouldReturnStatusDown() {
    when(natsConnection.getNatsCon()).thenReturn(getMockConnection(Connection.Status.CLOSED));
    assertThat(penMatchAPICustomHealthCheck.health()).isNotNull();
    assertThat(penMatchAPICustomHealthCheck.health().getStatus()).isEqualTo(Status.DOWN);
  }

  @Test
  public void testHealth_givenOpenNatsConnection_shouldReturnStatusUp() {
    when(natsConnection.getNatsCon()).thenReturn(getMockConnection(Connection.Status.CONNECTED));
    assertThat(penMatchAPICustomHealthCheck.health()).isNotNull();
    assertThat(penMatchAPICustomHealthCheck.health().getStatus()).isEqualTo(Status.UP);
  }

  private Connection getMockConnection(Connection.Status status) {
    return new Connection() {
      @Override
      public void publish(String subject, byte[] body) {

      }

      @Override
      public void publish(String subject, String replyTo, byte[] body) {

      }

      @Override
      public CompletableFuture<Message> request(String subject, byte[] data) {
        return null;
      }

      @Override
      public Message request(String subject, byte[] data, Duration timeout) throws InterruptedException {
        return null;
      }

      @Override
      public Subscription subscribe(String subject) {
        return null;
      }

      @Override
      public Subscription subscribe(String subject, String queueName) {
        return null;
      }

      @Override
      public Dispatcher createDispatcher(MessageHandler handler) {
        return null;
      }

      @Override
      public void closeDispatcher(Dispatcher dispatcher) {

      }

      @Override
      public void flush(Duration timeout) throws TimeoutException, InterruptedException {

      }

      @Override
      public CompletableFuture<Boolean> drain(Duration timeout) throws TimeoutException, InterruptedException {
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
    };
  }
}
