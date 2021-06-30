package uk.gov.justice.digital.delius.data.api;

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
    private Long personalContactId;
    private String relationship;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String title;
    private String firstName;
    private String otherNames;
    private String surname;
    private String previousSurname;
    private String mobileNumber;
    private String emailAddress;
    private String notes;
    private String gender;
    private KeyValue relationshipType;
    private LocalDateTime createdDatetime;
    private LocalDateTime lastUpdatedDatetime;
}
