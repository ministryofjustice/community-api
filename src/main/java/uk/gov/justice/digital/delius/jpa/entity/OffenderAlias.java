package uk.gov.justice.digital.delius.jpa.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "ALIAS")
public class OffenderAlias {

    @Id
    @Column(name = "ALIAS_ID")
    private Long aliasID;

    @Column(name = "OFFENDER_ID")
    private Long offenderID;

    @Column(name = "DATE_OF_BIRTH_DATE")
    private LocalDate dateOfBirth;

    @Column(name = "FIRST_NAME")
    private String firstName;

    @Column(name = "SECOND_NAME")
    private String secondName;

    @Column(name = "SOFT_DELETED")
    private boolean softDeleted;

    private String surname;

    @Column(name = "THIRD_NAME")
    private String thirdName;

    @ManyToOne()
    @JoinColumn(name = "GENDER_ID", insertable = false, updatable = false)
    private StandardReference gender;

    @Column(name = "GENDER_ID")
    private Long genderID;

    @Column(name = "FIRST_NAME_SOUNDEX", nullable = false)
    private String firstnameSoundex;

    @Column(name = "MIDDLE_NAME_SOUNDEX", nullable = false)
    private String middlenameSoundex;

    @Column(name = "SURNAME_SOUNDEX", nullable = false)
    private String surnameSoundex;

}
