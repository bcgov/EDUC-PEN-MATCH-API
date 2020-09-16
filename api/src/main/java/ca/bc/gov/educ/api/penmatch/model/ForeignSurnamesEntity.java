package ca.bc.gov.educ.api.penmatch.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@Immutable
@Table(name = "SURNAME_FREQUENCY")
public class ForeignSurnamesEntity {

    @Id
    @Column(name = "SURNAME")
    private String surname;

    @Column(name = "ANCESTRY")
    private String ancestry;

    @Column(name = "CREATE_DATE")
    private LocalDateTime createDate;

    @Column(name = "EFFECTIVE_DATE")
    private LocalDateTime effectiveDate;

    @Column(name = "EXPIRY_DATE")
    private LocalDateTime expiryDate;

    @Column(name = "CREATE_USER_NAME")
    private String createUserName;

    @Column(name = "UPDATE_DATE")
    private LocalDateTime updateDate;

    @Column(name = "UPDATE_USER_NAME")
    private String updateUserName;
}
