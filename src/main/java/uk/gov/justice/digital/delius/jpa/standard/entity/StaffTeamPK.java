package uk.gov.justice.digital.delius.jpa.standard.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import java.io.Serializable;

@Data
public class StaffTeamPK implements Serializable {
    @Column(name = "STAFF_ID")@Id
    private Long staffId;
    @Column(name = "TEAM_ID")@Id
    private Long teamId;

}
