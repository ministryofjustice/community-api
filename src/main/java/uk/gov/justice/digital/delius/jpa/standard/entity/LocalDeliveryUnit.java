package uk.gov.justice.digital.delius.jpa.standard.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.sql.Time;

@Data
@Entity
@Table(name = "LOCAL_DELIVERY_UNIT")
@AllArgsConstructor
@NoArgsConstructor
public class LocalDeliveryUnit {
    @Id
    @Column(name = "LOCAL_DELIVERY_UNIT_ID")
    private Long localDeliveryUnitId;
    @Column(name = "CODE")
    private String code;
    @Column(name = "DESCRIPTION")
    private String description;
    @Column(name = "SELECTABLE")
    private String selectable;
    @Column(name = "ROW_VERSION")
    private Long rowVersion;
    @Column(name = "CREATED_DATETIME")
    private Time createdDatetime;
    @Column(name = "CREATED_BY_USER_ID")
    private Long createdByUserId;
    @Column(name = "LAST_UPDATED_DATETIME")
    private Time lastUpdatedDatetime;
    @Column(name = "LAST_UPDATED_USER_ID")
    private Long lastUpdatedUserId;

}
