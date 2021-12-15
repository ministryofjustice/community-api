package uk.gov.justice.digital.delius.data.api;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class PersonalContact {
    @ApiModelProperty(name = "Personal contact id", example = "1")
    private Long personalContactId;

    @ApiModelProperty(name = "Relationship to offender", example = "Father")
    private String relationship;

    @ApiModelProperty(name = "Date the relationship started", example = "2021-06-10T00:00:00Z")
    private LocalDateTime startDate;

    @ApiModelProperty(name = "Optional date the relationship ended", example = "2021-07-10T00:00:00Z")
    private LocalDateTime endDate;

    @ApiModelProperty(name = "Title of the personal contact", example = "Mr")
    private String title;

    @ApiModelProperty(name = "First name of the personal contact", example = "Brian")
    private String firstName;

    @ApiModelProperty(name = "Middle names of the personal contact", example = "Danger")
    private String otherNames;

    @ApiModelProperty(name = "Last name of the personal contact", example = "Cheese")
    private String surname;

    @ApiModelProperty(name = "Previous name of the personal contact", example = "Haggis")
    private String previousSurname;

    @ApiModelProperty(name = "Mobile number of the personal contact", example = "0123456789")
    private String mobileNumber;

    @ApiModelProperty(name = "Email address of the personal contact", example = "example@example.com")
    private String emailAddress;

    @ApiModelProperty(name = "Notes about the personal contact", example = "Some notes about this personal contact")
    private String notes;

    @ApiModelProperty(name = "Gender of the personal contact", example = "Male")
    private String gender;

    @ApiModelProperty(name = "Type of relationship to the offender")
    private KeyValue relationshipType;

    @ApiModelProperty(name = "Date and time this record was created", example = "2021-06-10T12:00:00Z")
    private LocalDateTime createdDatetime;

    @ApiModelProperty(name = "Date and time this record was last updated", example = "2021-06-10T14:00:00Z")
    private LocalDateTime lastUpdatedDatetime;

    @ApiModelProperty(name = "The personal contact address")
    private AddressSummary address;

    @ApiModelProperty(value = "The active status of this record, if the start date is before or on today and the end date is after today or null", example = "true")
    private Boolean isActive;
}
