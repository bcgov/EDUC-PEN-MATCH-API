package ca.bc.gov.educ.api.pendemog.endpoint;

import ca.bc.gov.educ.api.pendemog.struct.PenDemographics;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/")
@OpenAPIDefinition(info = @Info(title = "API for Pen Demographics.", description = "This Read API is for Reading demographics data of a student in BC from open vms system.", version = "1"), security = {@SecurityRequirement(name = "OAUTH2", scopes = {"READ_PEN_DEMOGRAPHICS"})})
public interface PenDemographicsEndpoint {

  @GetMapping("/{pen}")
  @PreAuthorize("#oauth2.hasScope('READ_PEN_MATCH')")
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"), @ApiResponse(responseCode = "404", description = "NOT FOUND.")})
  PenDemographics matchStudent(@Validated @RequestBody PenMatchStudent student);

}
