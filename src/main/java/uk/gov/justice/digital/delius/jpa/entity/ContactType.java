package uk.gov.justice.digital.delius.jpa.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "R_CONTACT_TYPE")
public class ContactType {
    @Id
    @Column(name = "CONTACT_TYPE_ID")
    private long contactTypeId;

    @Column(name = "CODE")
    private String code;

    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "SHORT_DESCRIPTION")
    private String shortDescription;

}
