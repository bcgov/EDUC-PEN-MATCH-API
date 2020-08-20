package ca.bc.gov.educ.api.penmatch.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import ca.bc.gov.educ.api.penmatch.struct.PenMatchStudent;
import ca.bc.gov.educ.api.penmatch.struct.PenMatchStudentDetail;

@Mapper
public interface PenMatchStudentMapper {
	PenMatchStudentMapper mapper = Mappers.getMapper(PenMatchStudentMapper.class);

	PenMatchStudentDetail toPenMatchStudentDetails(PenMatchStudent penMatchStudent);

}
