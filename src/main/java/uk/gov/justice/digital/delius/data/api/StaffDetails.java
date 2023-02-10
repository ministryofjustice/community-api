package uk.gov.justice.digital.delius.data.api;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class StaffDetails {
    @Schema(description = "the optional username of this staff member, will be absent if the staff member is not a user of Delius", example = "SheilaHancockNPS")
    private String username;
    @Schema(description = "the optional email address of this staff member, will be absent if the staff member is not a user of Delius", example = "sheila.hancock@test.justice.gov.uk")
    private String email;
    @Schema(description = "the optional telephone number of this staff member, will be absent if the staff member is not a user of Delius", example = "020 1111 2222")
    private String telephoneNumber;
    @Schema(description = "staff code AKA officer code", example = "SH00001")
    private String staffCode;
    @Schema(description = "staff identifier", example = "123456")
    private Long staffIdentifier;
    @Schema(description = "staff name details")
    private Human staff;
    @Schema(description = "all teams related to this staff member")
    private List<Team> teams;
    @Schema(description = "provider this staff member is associated with")
    private ProbationArea probationArea;
    @Schema(description = "Staff Grade", example = "PO,CRC - PO")
    private KeyValue staffGrade;
}
