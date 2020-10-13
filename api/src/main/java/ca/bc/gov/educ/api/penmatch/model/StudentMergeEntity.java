package ca.bc.gov.educ.api.penmatch.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotNull;

/**
 * The type Student merge entity.
 */
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class StudentMergeEntity {
  /**
   * The constant serialVersionUID.
   */
  private static final long serialVersionUID = 1L;

  /**
   * The Student merge id.
   */
  String studentMergeID;
  /**
   * The Student id.
   */
  @NotNull(message = "Student ID can not be null.")
  String studentID;
  /**
   * The Merge student id.
   */
  @NotNull(message = "Merge Student ID can not be null.")
  String mergeStudentID;
  /**
   * The Student merge direction code.
   */
  @NotNull(message = "Student Merge Direction Code can not be null.")
  String studentMergeDirectionCode;
  /**
   * The Student merge source code.
   */
  @NotNull(message = "Student Merge Source Code can not be null.")
  String studentMergeSourceCode;

  /**
   * The Merge student.
   */
  StudentEntity mergeStudent;
}
