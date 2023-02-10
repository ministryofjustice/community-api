package uk.gov.justice.digital.delius.data.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.ALWAYS;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamCreationResult {
    @Schema(description = "List of teams created")
    @JsonInclude(ALWAYS)
    private List<Team> teams;
    @Schema(description = "List of unallocated staff created")
    @JsonInclude(ALWAYS)
    private List<StaffHuman> unallocatedStaff;
}
