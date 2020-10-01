package uk.gov.justice.digital.delius.data.api;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Attendance {

    @ApiModelProperty(required = true)
    private Long contactId;

    @ApiModelProperty(required = true)
    private LocalDate attendanceDate;

    @ApiModelProperty(required = true)
    private boolean attended;

    @ApiModelProperty(required = true )
    private boolean complied;

    private String outcome;

    private ContactTypeDetail contactType;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ContactTypeDetail {

        @ApiModelProperty(required = true)
        private String description;

        @ApiModelProperty(required = true)
        private String code;

    }
}
