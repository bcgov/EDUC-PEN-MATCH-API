package ca.bc.gov.educ.api.pendemog.mappers;

import ca.bc.gov.educ.api.pendemog.model.PenDemographicsEntity;
import ca.bc.gov.educ.api.pendemog.model.PenDemographicsEntity.PenDemographicsEntityBuilder;
import ca.bc.gov.educ.api.pendemog.struct.PenDemographics;
import ca.bc.gov.educ.api.pendemog.struct.PenDemographics.PenDemographicsBuilder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import javax.annotation.processing.Generated;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2020-07-28T11:28:03-0700",
    comments = "version: 1.3.1.Final, compiler: javac, environment: Java 11.0.7 (Oracle Corporation)"
)
public class PenDemographicsMapperImpl implements PenDemographicsMapper {

    private final StringMapper stringMapper = new StringMapper();

    @Override
    public PenDemographics toStructure(PenDemographicsEntity penDemographicsEntity) {
        if ( penDemographicsEntity == null ) {
            return null;
        }

        PenDemographicsBuilder penDemographics = PenDemographics.builder();

        penDemographics.pen( stringMapper.map( penDemographicsEntity.getStudNo() ) );
        penDemographics.studSurname( stringMapper.map( penDemographicsEntity.getStudSurname() ) );
        penDemographics.studGiven( stringMapper.map( penDemographicsEntity.getStudGiven() ) );
        penDemographics.studMiddle( stringMapper.map( penDemographicsEntity.getStudMiddle() ) );
        penDemographics.usualSurname( stringMapper.map( penDemographicsEntity.getUsualSurname() ) );
        penDemographics.usualGiven( stringMapper.map( penDemographicsEntity.getUsualGiven() ) );
        penDemographics.usualMiddle( stringMapper.map( penDemographicsEntity.getUsualMiddle() ) );
        penDemographics.studBirth( stringMapper.map( penDemographicsEntity.getStudBirth() ) );
        penDemographics.studSex( stringMapper.map( penDemographicsEntity.getStudSex() ) );
        penDemographics.studStatus( stringMapper.map( penDemographicsEntity.getStudStatus() ) );
        penDemographics.localID( stringMapper.map( penDemographicsEntity.getLocalID() ) );
        penDemographics.postalCode( stringMapper.map( penDemographicsEntity.getPostalCode() ) );
        penDemographics.grade( stringMapper.map( penDemographicsEntity.getGrade() ) );
        penDemographics.gradeYear( stringMapper.map( penDemographicsEntity.getGradeYear() ) );
        penDemographics.demogCode( stringMapper.map( penDemographicsEntity.getDemogCode() ) );
        penDemographics.mincode( stringMapper.map( penDemographicsEntity.getMincode() ) );
        if ( penDemographicsEntity.getCreateDate() != null ) {
            penDemographics.createDate( new SimpleDateFormat( "yyyy-MM-dd" ).format( penDemographicsEntity.getCreateDate() ) );
        }
        penDemographics.createUserName( stringMapper.map( penDemographicsEntity.getCreateUserName() ) );

        return penDemographics.build();
    }

    @Override
    public PenDemographicsEntity toModel(PenDemographics penDemographics) {
        if ( penDemographics == null ) {
            return null;
        }

        PenDemographicsEntityBuilder penDemographicsEntity = PenDemographicsEntity.builder();

        penDemographicsEntity.studNo( stringMapper.map( penDemographics.getPen() ) );
        penDemographicsEntity.studSurname( stringMapper.map( penDemographics.getStudSurname() ) );
        penDemographicsEntity.studGiven( stringMapper.map( penDemographics.getStudGiven() ) );
        penDemographicsEntity.studMiddle( stringMapper.map( penDemographics.getStudMiddle() ) );
        penDemographicsEntity.usualSurname( stringMapper.map( penDemographics.getUsualSurname() ) );
        penDemographicsEntity.usualGiven( stringMapper.map( penDemographics.getUsualGiven() ) );
        penDemographicsEntity.usualMiddle( stringMapper.map( penDemographics.getUsualMiddle() ) );
        penDemographicsEntity.studBirth( stringMapper.map( penDemographics.getStudBirth() ) );
        penDemographicsEntity.studSex( stringMapper.map( penDemographics.getStudSex() ) );
        penDemographicsEntity.studStatus( stringMapper.map( penDemographics.getStudStatus() ) );
        penDemographicsEntity.localID( stringMapper.map( penDemographics.getLocalID() ) );
        penDemographicsEntity.postalCode( stringMapper.map( penDemographics.getPostalCode() ) );
        penDemographicsEntity.grade( stringMapper.map( penDemographics.getGrade() ) );
        penDemographicsEntity.gradeYear( stringMapper.map( penDemographics.getGradeYear() ) );
        penDemographicsEntity.demogCode( stringMapper.map( penDemographics.getDemogCode() ) );
        penDemographicsEntity.mincode( stringMapper.map( penDemographics.getMincode() ) );
        try {
            if ( penDemographics.getCreateDate() != null ) {
                penDemographicsEntity.createDate( new SimpleDateFormat( "yyyy-MM-dd" ).parse( penDemographics.getCreateDate() ) );
            }
        }
        catch ( ParseException e ) {
            throw new RuntimeException( e );
        }
        penDemographicsEntity.createUserName( stringMapper.map( penDemographics.getCreateUserName() ) );

        return penDemographicsEntity.build();
    }
}
