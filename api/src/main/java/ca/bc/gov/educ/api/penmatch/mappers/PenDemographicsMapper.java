package ca.bc.gov.educ.api.pendemog.mappers;

import ca.bc.gov.educ.api.pendemog.model.PenDemographicsEntity;
import ca.bc.gov.educ.api.pendemog.struct.PenDemographics;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(uses = StringMapper.class)
@SuppressWarnings("squid:S1214")
public interface PenDemographicsMapper {
  PenDemographicsMapper mapper = Mappers.getMapper(PenDemographicsMapper.class);


  @Mapping(dateFormat = "yyyy-MM-dd", target = "createDate")
  @Mapping(target = "pen", source = "studNo")
  PenDemographics toStructure(PenDemographicsEntity penDemographicsEntity);

  /**
   * the toModel is only used in unit testing.
   *
   * @param penDemographics the struct which will be converted to entity.
   * @return the converted entity.
   */
  @Mapping(dateFormat = "yyyy-MM-dd", target = "createDate")
  @Mapping(target = "studNo", source = "pen")
  PenDemographicsEntity toModel(PenDemographics penDemographics);
}
