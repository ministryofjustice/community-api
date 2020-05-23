package uk.gov.justice.digital.delius.transformers;

import com.google.common.collect.ImmutableList;
import org.junit.Test;
import uk.gov.justice.digital.delius.data.api.Appointment;
import uk.gov.justice.digital.delius.jpa.standard.entity.Contact;
import uk.gov.justice.digital.delius.jpa.standard.entity.ContactOutcomeType;
import uk.gov.justice.digital.delius.jpa.standard.entity.ContactType;
import uk.gov.justice.digital.delius.jpa.standard.entity.Explanation;
import uk.gov.justice.digital.delius.jpa.standard.entity.LicenceCondition;
import uk.gov.justice.digital.delius.jpa.standard.entity.OfficeLocation;
import uk.gov.justice.digital.delius.jpa.standard.entity.ProbationArea;
import uk.gov.justice.digital.delius.jpa.standard.entity.Requirement;
import uk.gov.justice.digital.delius.jpa.standard.entity.Staff;
import uk.gov.justice.digital.delius.jpa.standard.entity.Team;

import static org.assertj.core.api.Assertions.assertThat;

public class AppointmentTransformerTest {
    @Test
    public void appointmentOutcomeMappedFromContactOutcomeType() {
        assertThat(AppointmentTransformer.appointmentsOf(ImmutableList.of(
                aContact()
                    .toBuilder()
                    .contactOutcomeType(ContactOutcomeType
                            .builder()
                            .code("XX")
                            .description("Did not turn up")
                            .build())
                .build())).get(0).getAppointmentOutcomeType())
                .isNotNull()
                .hasFieldOrPropertyWithValue("code", "XX")
                .hasFieldOrPropertyWithValue("description", "Did not turn up");
    }

    @Test
    public void appointmentTypeMappedFromContactType() {
        assertThat(AppointmentTransformer.appointmentsOf(ImmutableList.of(
                aContact()
                    .toBuilder()
                    .contactType(ContactType
                            .builder()
                            .code("XX")
                            .description("Accommodation")
                            .build())
                .build())).get(0).getAppointmentType())
                .isNotNull()
                .hasFieldOrPropertyWithValue("code", "XX")
                .hasFieldOrPropertyWithValue("description", "Accommodation");
    }
    @Test
    public void officeLocationMappedFromOfficeLocation() {
        assertThat(AppointmentTransformer.appointmentsOf(ImmutableList.of(
                aContact()
                    .toBuilder()
                    .officeLocation(OfficeLocation
                            .builder()
                            .code("XX")
                            .description("Barnet")
                            .build())
                .build())).get(0).getOfficeLocation())
                .isNotNull()
                .hasFieldOrPropertyWithValue("code", "XX")
                .hasFieldOrPropertyWithValue("description", "Barnet");
    }

    @Test
    public void attendedMappedToNotRecordedWhenNull() {
        assertThat(AppointmentTransformer.appointmentsOf(ImmutableList.of(
                aContact()
                        .toBuilder()
                        .attended(null)
                        .build())).get(0).getAttended())
                .isEqualTo(Appointment.Attended.NOT_RECORDED);

    }

    @Test
    public void attendedMappedToAttendedWhenY() {
        assertThat(AppointmentTransformer.appointmentsOf(ImmutableList.of(
                aContact()
                        .toBuilder()
                        .attended("Y")
                        .build())).get(0).getAttended())
                .isEqualTo(Appointment.Attended.ATTENDED);

    }

    @Test
    public void attendedMappedToNotAttendedWhenN() {
        assertThat(AppointmentTransformer.appointmentsOf(ImmutableList.of(
                aContact()
                        .toBuilder()
                        .attended("N")
                        .build())).get(0).getAttended())
                .isEqualTo(Appointment.Attended.UNATTENDED);

    }

    private Contact aContact() {
        return Contact
                .builder()
                .contactOutcomeType(ContactOutcomeType
                        .builder()
                        .build())
                .contactType(ContactType
                        .builder()
                        .build())
                .explanation(Explanation
                        .builder()
                        .build())
                .licenceCondition(LicenceCondition
                        .builder()
                        .build())
                .probationArea(ProbationArea
                        .builder()
                        .build())
                .team(Team
                        .builder()
                        .build())
                .requirement(Requirement
                        .builder()
                        .build())
                .staff(Staff
                        .builder()
                        .build())
                .build();
    }
}