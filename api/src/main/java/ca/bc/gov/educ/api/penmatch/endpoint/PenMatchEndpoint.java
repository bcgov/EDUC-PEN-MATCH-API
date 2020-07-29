package ca.bc.gov.educ.api.penmatch.endpoint;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import ca.bc.gov.educ.api.penmatch.struct.PenMatchStudent;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RequestMapping("/")
@OpenAPIDefinition(info = @Info(title = "API for PEN Match.", description = "This API is to match students to PENs.", version = "1"), security = {@SecurityRequirement(name = "OAUTH2", scopes = {"READ_PEN_MATCH"})})
public interface PenMatchEndpoint {

  @GetMapping("/{pen}")
  @PreAuthorize("#oauth2.hasScope('READ_PEN_MATCH')")
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"), @ApiResponse(responseCode = "404", description = "NOT FOUND.")})
  PenMatchStudent matchStudent(@Validated @RequestBody PenMatchStudent student);

}
