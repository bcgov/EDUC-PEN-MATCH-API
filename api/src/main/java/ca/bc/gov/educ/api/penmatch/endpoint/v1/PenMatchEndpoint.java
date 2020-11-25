package ca.bc.gov.educ.api.penmatch.endpoint.v1;

import ca.bc.gov.educ.api.penmatch.struct.v1.PenMatchResult;
import ca.bc.gov.educ.api.penmatch.struct.v1.PenMatchStudent;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * The interface Pen match endpoint.
 */
@RequestMapping("/api/v1/pen-match")
@OpenAPIDefinition(info = @Info(title = "API for PEN Match.", description = "This API is to match students to PENs.", version = "1"), security = {@SecurityRequirement(name = "OAUTH2", scopes = {"READ_PEN_MATCH"})})
public interface PenMatchEndpoint {

  /**
   * Match student completable future.
   *
   * @param student the student
   * @return the completable future
   */
  @PostMapping
  @PreAuthorize("#oauth2.hasAnyScope('READ_PEN_MATCH')")
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"), @ApiResponse(responseCode = "400", description = "BAD REQUEST")})
  @Transactional(readOnly = true)
  @Tag(name = "Endpoint to run pen match algorithm on the given payload.", description = "Endpoint to run pen match algorithm on the given payload.")
  @Schema(name = "PenMatchStudent", implementation = PenMatchStudent.class)
  @Async("controllerExecutor")
  CompletableFuture<PenMatchResult> matchStudent(@Validated @RequestBody PenMatchStudent student);

  /**
   * Retrieve nicknames for a given name
   *
   * @param givenName the given name
   * @return List of Nicknames
   */
  @GetMapping("/nicknames/{givenName}")
  @PreAuthorize("#oauth2.hasAnyScope('READ_PEN_MATCH')")
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"), @ApiResponse(responseCode = "400", description = "BAD REQUEST")})
  @Transactional(readOnly = true)
  @Tag(name = "Endpoint to retrieve nicknames for a given name", description = "Endpoint to to retrieve nicknames for a given name.")
  List<String> getNicknames(@PathVariable("givenName") String givenName);
  
}
