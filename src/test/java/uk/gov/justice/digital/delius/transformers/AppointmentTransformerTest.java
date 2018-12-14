package uk.gov.justice.digital.delius.transformers;

import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Test;
import uk.gov.justice.digital.delius.data.api.Appointment;
import uk.gov.justice.digital.delius.jpa.standard.entity.*;

import static org.assertj.core.api.Assertions.assertThat;

public class AppointmentTransformerTest {
    private AppointmentTransformer transformer;

    @Before
    public void before() {
        transformer = new AppointmentTransformer(new ContactTransformer());
    }

    @Test
    public void appointmentOutcomeMappedFromContactOutcomeType() {
        assertThat(transformer.appointmentsOf(ImmutableList.of(
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
        assertThat(transformer.appointmentsOf(ImmutableList.of(
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
        assertThat(transformer.appointmentsOf(ImmutableList.of(
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
        assertThat(transformer.appointmentsOf(ImmutableList.of(
                aContact()
                        .toBuilder()
                        .attended(null)
                        .build())).get(0).getAttended())
                .isEqualTo(Appointment.Attended.NOT_RECORDED);

    }

    @Test
    public void attendedMappedToAttendedWhenY() {
        assertThat(transformer.appointmentsOf(ImmutableList.of(
                aContact()
                        .toBuilder()
                        .attended("Y")
                        .build())).get(0).getAttended())
                .isEqualTo(Appointment.Attended.ATTENDED);

    }

    @Test
    public void attendedMappedToNotAttendedWhenN() {
        assertThat(transformer.appointmentsOf(ImmutableList.of(
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