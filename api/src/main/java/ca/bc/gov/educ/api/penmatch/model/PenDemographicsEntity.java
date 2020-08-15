package ca.bc.gov.educ.api.penmatch.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedNativeQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Immutable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Immutable
@NamedNativeQuery(name = "PenDemographicsEntity.penDemogNoInit", query = "SELECT * FROM PEN_DEMOG WHERE (STUD_BIRTH = ?) OR (STUD_SURNAME LIKE ?) OR (PEN_MINCODE = ? AND PEN_LOCAL_ID = ?)", resultClass = PenDemographicsEntity.class)
@NamedNativeQuery(name = "PenDemographicsEntity.penDemogWithAllParts", query = "SELECT * FROM PEN_DEMOG WHERE (STUD_BIRTH = ?) OR (STUD_SURNAME LIKE ? AND STUD_GIVEN LIKE ?) OR (PEN_MINCODE = ? AND PEN_LOCAL_ID = ?)", resultClass = PenDemographicsEntity.class)
@NamedNativeQuery(name = "PenDemographicsEntity.penDemogNoLocalID", query = "SELECT * FROM PEN_DEMOG WHERE (STUD_BIRTH = ?) OR (STUD_SURNAME LIKE ? AND STUD_GIVEN LIKE ?)", resultClass = PenDemographicsEntity.class)
@NamedNativeQuery(name = "PenDemographicsEntity.penDemogNoInitNoLocalID", query = "SELECT * FROM PEN_DEMOG WHERE (STUD_BIRTH = ?) OR (STUD_SURNAME LIKE ?)", resultClass = PenDemographicsEntity.class)
@Table(name = "PEN_DEMOG")
public class PenDemographicsEntity {

	@Id
	@Column(name = "STUD_NO")
	private String studNo;

	@Column(name = "STUD_SURNAME")
	private String studSurname;

	@Column(name = "STUD_GIVEN")
	private String studGiven;

	@Column(name = "STUD_MIDDLE")
	private String studMiddle;

	@Column(name = "USUAL_SURNAME")
	private String usualSurname;

	@Column(name = "USUAL_GIVEN")
	private String usualGiven;

	@Column(name = "USUAL_MIDDLE")
	private String usualMiddle;

	@Column(name = "STUD_BIRTH")
	private String studBirth;

	@Column(name = "STUD_SEX")
	private String studSex;

	@Column(name = "STUD_STATUS")
	private String studStatus;

	@Column(name = "PEN_LOCAL_ID")
	private String localID;

	@Column(name = "POSTAL")
	private String postalCode;

	@Column(name = "STUD_GRADE")
	private String grade;

	@Column(name = "STUD_GRADE_YEAR")
	private String gradeYear;

	@Column(name = "STUD_DEMOG_CODE")
	private String demogCode;

	@Column(name = "PEN_MINCODE")
	private String mincode;

	@Column(name = "STUD_TRUE_NO")
	private String trueNumber;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "CREATE_DATE")
	private Date createDate;

	@Column(name = "CREATE_USER_NAME")
	private String createUserName;

}
