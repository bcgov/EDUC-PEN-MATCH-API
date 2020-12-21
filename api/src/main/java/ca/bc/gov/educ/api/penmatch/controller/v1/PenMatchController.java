package ca.bc.gov.educ.api.penmatch.controller.v1;

import ca.bc.gov.educ.api.penmatch.endpoint.v1.PenMatchEndpoint;
import ca.bc.gov.educ.api.penmatch.mappers.PenMatchStudentMapper;
import ca.bc.gov.educ.api.penmatch.service.match.PenMatchService;
import ca.bc.gov.educ.api.penmatch.struct.v1.PenMatchResult;
import ca.bc.gov.educ.api.penmatch.struct.v1.PenMatchStudent;
import lombok.AccessLevel;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * The type Pen match controller.
 */
@RestController
@EnableResourceServer
public class PenMatchController implements PenMatchEndpoint {
  /**
   * The constant mapper.
   */
  private static final PenMatchStudentMapper mapper = PenMatchStudentMapper.mapper;
  /**
   * The Pen match service.
   */
  @Getter(AccessLevel.PRIVATE)
  private final PenMatchService penMatchService;

  /**
   * Instantiates a new Pen match controller.
   *
   * @param penMatchService the pen match service
   */
  @Autowired
  public PenMatchController(final PenMatchService penMatchService) {
    this.penMatchService = penMatchService;
  }

  @Override
  public CompletableFuture<PenMatchResult> matchStudent(PenMatchStudent student) {
    return CompletableFuture.completedFuture(penMatchService.matchStudent(mapper.toPenMatchStudentDetails(student), UUID.randomUUID()));
  }

  @Override
  public List<String> getNicknames(String givenName) {
    return penMatchService.getNicknames(givenName).stream().map(entity -> entity.getNickname2().trim()).collect(Collectors.toList());
  }

}
