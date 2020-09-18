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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.concurrent.CompletableFuture;

@RequestMapping("/api/v1/pen-match")
@OpenAPIDefinition(info = @Info(title = "API for PEN Match.", description = "This API is to match students to PENs.", version = "1"), security = {@SecurityRequirement(name = "OAUTH2", scopes = {"READ_PEN_MATCH"})})
public interface PenMatchEndpoint {

  @Async
  @PostMapping
  @PreAuthorize("#oauth2.hasAnyScope('READ_PEN_MATCH')")
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"), @ApiResponse(responseCode = "400", description = "BAD REQUEST")})
  @Transactional(readOnly = true)
  @Tag(name = "Endpoint to run pen match algorithm on the given payload.", description = "Endpoint to run pen match algorithm on the given payload.")
  @Schema(name = "PenMatchStudent", implementation = PenMatchStudent.class)
  CompletableFuture<PenMatchResult> matchStudent(@Validated @RequestBody PenMatchStudent student);

}
