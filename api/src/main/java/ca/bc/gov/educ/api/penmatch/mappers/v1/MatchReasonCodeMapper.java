package ca.bc.gov.educ.api.penmatch.mappers.v1;

import ca.bc.gov.educ.api.penmatch.mappers.LocalDateTimeMapper;
import ca.bc.gov.educ.api.penmatch.model.v1.MatchReasonCodeEntity;
import ca.bc.gov.educ.api.penmatch.struct.v1.MatchReasonCode;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * The interface Match reason code mapper.
 */
@Mapper(uses = {LocalDateTimeMapper.class})
@SuppressWarnings("squid:S1214")
public interface MatchReasonCodeMapper {

  /**
   * The constant mapper.
   */
  MatchReasonCodeMapper mapper = Mappers.getMapper(MatchReasonCodeMapper.class);

  /**
   * To struct match reason code.
   *
   * @param entity the entity
   * @return the match reason code
   */
  MatchReasonCode toStruct(MatchReasonCodeEntity entity);
}
