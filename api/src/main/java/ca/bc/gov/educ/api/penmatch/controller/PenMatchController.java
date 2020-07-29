package ca.bc.gov.educ.api.pendemog.controller;

import ca.bc.gov.educ.api.pendemog.endpoint.PenDemographicsEndpoint;
import ca.bc.gov.educ.api.pendemog.exception.InvalidParameterException;
import ca.bc.gov.educ.api.pendemog.mappers.PenDemographicsMapper;
import ca.bc.gov.educ.api.pendemog.service.PenDemographicsService;
import ca.bc.gov.educ.api.pendemog.struct.PenDemographics;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@EnableResourceServer
@Slf4j
public class PenDemographicsController implements PenDemographicsEndpoint {
  @Getter(AccessLevel.PRIVATE)
  private final PenDemographicsService penDemographicsService;
  private static final PenDemographicsMapper mapper = PenDemographicsMapper.mapper;

  @Autowired
  public PenDemographicsController(final PenDemographicsService penDemographicsService) {
    this.penDemographicsService = penDemographicsService;
  }

  @Override
  public PenDemographics getPenDemographicsByPen(String pen) {
    log.debug("Retrieving Pen Data");
    pen = StringUtils.rightPad(pen, 10);
    return mapper.toStructure(getPenDemographicsService().getPenDemographicsByPen(pen));
  }

  @Override
  public List<PenDemographics> searchPenDemographics(String studSurName, String studGiven, String studMiddle, String studBirth, String studSex) {
    validateDateOfBirth(studBirth);
    return getPenDemographicsService().searchPenDemographics(studSurName, studGiven, studMiddle, studBirth, studSex).stream().map(mapper::toStructure).collect(Collectors.toList());
  }

  private void validateDateOfBirth(String studBirth) {
    if (StringUtils.isNotBlank(studBirth)) {
      try {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        dateFormat.setLenient(false);
        dateFormat.parse(studBirth);
      } catch (final ParseException ex) {
        throw new InvalidParameterException(studBirth, "Student  Date of Birth should be in yyyyMMdd format.");
      }
    }
  }

}
