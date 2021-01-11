package ca.bc.gov.educ.api.penmatch.mappers.v1;

import ca.bc.gov.educ.api.penmatch.mappers.LocalDateTimeMapper;
import ca.bc.gov.educ.api.penmatch.mappers.UUIDMapper;
import ca.bc.gov.educ.api.penmatch.model.v1.PossibleMatchEntity;
import ca.bc.gov.educ.api.penmatch.struct.v1.PossibleMatch;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * The interface Possible match mapper.
 */
@Mapper(uses = {UUIDMapper.class, LocalDateTimeMapper.class})
@SuppressWarnings("squid:S1214")
public interface PossibleMatchMapper {
  /**
   * The constant mapper.
   */
  PossibleMatchMapper mapper = Mappers.getMapper(PossibleMatchMapper.class);

  /**
   * To model possible match entity.
   *
   * @param possibleMatch the possible match
   * @return the possible match entity
   */
  @Mapping(target = "updateDate", expression = "java(java.time.LocalDateTime.now())")
  @Mapping(target = "createDate", expression = "java(java.time.LocalDateTime.now())")
  PossibleMatchEntity toModel(PossibleMatch possibleMatch);

  /**
   * To struct possible match.
   *
   * @param possibleMatch the possible match
   * @return the possible match
   */
  PossibleMatch toStruct(PossibleMatchEntity possibleMatch);
}
