package uk.gov.justice.digital.delius.data.api;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class Attendance {

    @ApiModelProperty(required = true)
    private final Long contactId;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @ApiModelProperty(required = true)
    private final LocalDate attendanceDate;

    private final boolean attended;

    private final boolean complied;

    private final String outcome;

    private final ContactTypeDetail contactType;

    @Data
    @Builder
    @AllArgsConstructor
    public static class ContactTypeDetail {

        @ApiModelProperty(required = true)
        private final String description;

        @ApiModelProperty(required = true)
        private final String code;

    }
}
