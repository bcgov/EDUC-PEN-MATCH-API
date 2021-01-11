package ca.bc.gov.educ.api.penmatch.endpoint.v1;

import ca.bc.gov.educ.api.penmatch.struct.v1.MatchReasonCode;
import ca.bc.gov.educ.api.penmatch.struct.v1.PenMatchResult;
import ca.bc.gov.educ.api.penmatch.struct.v1.PenMatchStudent;
import ca.bc.gov.educ.api.penmatch.struct.v1.PossibleMatch;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

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
  @PreAuthorize("hasAuthority('SCOPE_READ_PEN_MATCH')")
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"), @ApiResponse(responseCode = "400", description = "BAD REQUEST")})
  @Transactional(readOnly = true)
  @Tag(name = "Endpoint to run pen match algorithm on the given payload.", description = "Endpoint to run pen match algorithm on the given payload.")
  @Schema(name = "PenMatchStudent", implementation = PenMatchStudent.class)
  PenMatchResult matchStudent(@Validated @RequestBody PenMatchStudent student);

  /**
   * Save possible matches list.
   *
   * @param possibleMatches the possible matches
   * @return the list
   */
  @PostMapping("/possible-match")
  @PreAuthorize("hasAuthority('SCOPE_WRITE_POSSIBLE_MATCH')")
  @ApiResponses(value = {@ApiResponse(responseCode = "201", description = "CREATED."), @ApiResponse(responseCode = "400", description = "BAD REQUEST")})
  @Transactional
  @Tag(name = "Endpoint to store possible matches.", description = "Endpoint to store possible matches.")
  @Schema(name = "possible match", implementation = PossibleMatch.class)
  @ResponseStatus(HttpStatus.CREATED)
  List<PossibleMatch> savePossibleMatches(@Validated @RequestBody List<PossibleMatch> possibleMatches);

  /**
   * Gets possible matches by student id.
   *
   * @param studentID the student id
   * @return the possible matches by student id
   */
  @GetMapping("/possible-match/{studentID}")
  @PreAuthorize("hasAuthority('SCOPE_READ_POSSIBLE_MATCH')")
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK."), @ApiResponse(responseCode = "404", description = "NOT FOUND.")})
  @Transactional(readOnly = true)
  @Tag(name = "Endpoint to get possible matches by student id.", description = "Endpoint to get possible matches by student id.")
  @Schema(name = "possible match", implementation = PossibleMatch.class)
  List<PossibleMatch> getPossibleMatchesByStudentID(@PathVariable UUID studentID);

  /**
   * Delete possible matches by student id and matched student id response entity.
   *
   * @param studentID        the student id
   * @param matchedStudentID the matched student id
   * @return the response entity
   */
  @DeleteMapping("/possible-match/{studentID}/{matchedStudentID}")
  @PreAuthorize("hasAuthority('SCOPE_DELETE_POSSIBLE_MATCH')")
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "CREATED."), @ApiResponse(responseCode = "404", description = "NOT FOUND.")})
  @Transactional
  @Tag(name = "Endpoint to delete possible matches by student id.", description = "Endpoint to delete possible matches by student id.")
  @Schema(name = "possible match", implementation = PossibleMatch.class)
  ResponseEntity<Void> deletePossibleMatchesByStudentIDAndMatchedStudentID(@PathVariable UUID studentID, @PathVariable UUID matchedStudentID);

  /**
   * Retrieve nicknames for a given name
   *
   * @param givenName the given name
   * @return List of Nicknames
   */
  @GetMapping("/nicknames/{givenName}")
  @PreAuthorize("hasAuthority('SCOPE_READ_NICKNAMES')")
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"), @ApiResponse(responseCode = "400", description = "BAD REQUEST")})
  @Transactional(readOnly = true)
  @Tag(name = "Endpoint to retrieve nicknames for a given name", description = "Endpoint to to retrieve nicknames for a given name.")
  List<String> getNicknames(@PathVariable("givenName") String givenName);

  /**
   * Gets all match reason codes.
   *
   * @return the all match reason codes
   */
  @GetMapping("/match-reason-codes")
  @PreAuthorize("hasAuthority('SCOPE_READ_POSSIBLE_MATCH')")
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
  @Transactional(readOnly = true)
  @Tag(name = "Endpoint to retrieve all possible match reason codes.", description = "Endpoint to retrieve all possible match reason codes.")
  List<MatchReasonCode> getAllMatchReasonCodes();
}
