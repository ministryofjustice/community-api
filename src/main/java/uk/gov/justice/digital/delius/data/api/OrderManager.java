package uk.gov.justice.digital.delius.data.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderManager {
    private Long probationAreaId;
    private Long teamId;
    private Long officerId;
    private String name;
    private String staffCode;
    private LocalDateTime dateStartOfAllocation;
    private LocalDateTime dateEndOfAllocation;
    private String gradeCode;
    private String teamCode;
    private String probationAreaCode;

}
