package ca.bc.gov.educ.api.penmatch.mappers.v1;

import ca.bc.gov.educ.api.penmatch.struct.v1.PenMatchStudent;
import ca.bc.gov.educ.api.penmatch.struct.v1.PenMatchStudentDetail;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * The interface Pen match student mapper.
 */
@Mapper
public interface PenMatchStudentMapper {
  /**
   * The constant mapper.
   */
  PenMatchStudentMapper mapper = Mappers.getMapper(PenMatchStudentMapper.class);

  /**
   * To pen match student details pen match student detail.
   *
   * @param penMatchStudent the pen match student
   * @return the pen match student detail
   */
  @Mapping(target = "penMatchTransactionNames", ignore = true)
  @Mapping(target = "partialSurnameFrequency", ignore = true)
  @Mapping(target = "partialStudentSurname", ignore = true)
  @Mapping(target = "partialStudentGiven", ignore = true)
  @Mapping(target = "minSurnameSearchSize", ignore = true)
  @Mapping(target = "maxSurnameSearchSize", ignore = true)
  @Mapping(target = "fullSurnameFrequency", ignore = true)
  @Mapping(target = "alternateLocalID", ignore = true)
  PenMatchStudentDetail toPenMatchStudentDetails(PenMatchStudent penMatchStudent);

}
