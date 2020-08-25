package ca.bc.gov.educ.api.penmatch.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import ca.bc.gov.educ.api.penmatch.struct.v1.PenMatchStudent;
import ca.bc.gov.educ.api.penmatch.struct.v1.PenMatchStudentDetail;

@Mapper
public interface PenMatchStudentMapper {
	PenMatchStudentMapper mapper = Mappers.getMapper(PenMatchStudentMapper.class);

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
