package uk.gov.justice.digital.delius.transformers;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import uk.gov.justice.digital.delius.data.api.ContactSummary;
import uk.gov.justice.digital.delius.jpa.standard.entity.LicenceCondition;
import uk.gov.justice.digital.delius.jpa.standard.entity.LicenceConditionTypeMainCat;
import uk.gov.justice.digital.delius.jpa.standard.entity.StandardReference;
import uk.gov.justice.digital.delius.util.EntityHelper;
import uk.gov.justice.digital.delius.utils.DateConverter;

import static org.assertj.core.api.Assertions.assertThat;

class ContactTransformerTest {

    @DisplayName("Transform entity LicenceCondition to API equivalent")
    @Test
    void whenLicenceConditionsSet_thenTransformToApi() {
        var commencementDate = LocalDate.of(2020, Month.AUGUST, 1);
        var startDate = LocalDate.of(2020, Month.JULY, 1);
        var terminationDate = LocalDate.of(2020, Month.AUGUST, 1);
        var createdTime = startDate.atTime(9, 0);

        var licenceCondition = ContactTransformer.licenceConditionOf(LicenceCondition.builder()
            .activeFlag(1L)
            .commencementDate(commencementDate)
            .commencementNotes("comm notes")
            .createdDateTime(createdTime)
            .licenceConditionNotes("lc notes")
            .licenceConditionTypeMainCat(LicenceConditionTypeMainCat.builder().code("code").description("desc").build())
            .licenceConditionTypeSubCat(StandardReference.builder().codeValue("sub code").codeDescription("sub desc").build())
            .startDate(startDate)
            .terminationDate(terminationDate)
            .terminationNotes("tm notes")
            .build());

        assertThat(licenceCondition.getActive()).isTrue();
        assertThat(licenceCondition.getCommencementDate()).isEqualTo(commencementDate);
        assertThat(licenceCondition.getCommencementNotes()).isEqualTo("comm notes");
        assertThat(licenceCondition.getCreatedDateTime()).isEqualTo(createdTime);
        assertThat(licenceCondition.getLicenceConditionNotes()).isEqualTo("lc notes");
        assertThat(licenceCondition.getLicenceConditionTypeMainCat().getDescription()).isEqualTo("desc");
        assertThat(licenceCondition.getLicenceConditionTypeMainCat().getCode()).isEqualTo("code");
        assertThat(licenceCondition.getLicenceConditionTypeSubCat().getDescription()).isEqualTo("sub desc");
        assertThat(licenceCondition.getLicenceConditionTypeSubCat().getCode()).isEqualTo("sub code");
        assertThat(licenceCondition.getStartDate()).isEqualTo(startDate);
        assertThat(licenceCondition.getTerminationDate()).isEqualTo(terminationDate);
        assertThat(licenceCondition.getTerminationNotes()).isEqualTo("tm notes");
    }

    @DisplayName("Transform entity LicenceCondition with null content to API equivalent")
    @Test
    void givenNulls_whenTransform_thenTransformToApi() {
        var startDate = LocalDate.of(2020, Month.JULY, 1);

        var licenceCondition = ContactTransformer.licenceConditionOf(LicenceCondition.builder()
            .activeFlag(0L)
            .startDate(startDate)
            .build());

        assertThat(licenceCondition.getActive()).isFalse();
        assertThat(licenceCondition.getLicenceConditionTypeSubCat()).isNull();
        assertThat(licenceCondition.getLicenceConditionTypeMainCat()).isNull();
        assertThat(licenceCondition.getStartDate()).isEqualTo(startDate);
    }

    @Test
    public void contactSummaryFromContactHandlesNulls() {
        final var contact = EntityHelper.aContact().toBuilder()
            .contactStartTime(null)
            .contactEndTime(null)
            .officeLocation(null)
            .notes(null)
            .sensitive(null)
            .contactOutcomeType(null)
            .build();
        final var observed = ContactTransformer.contactSummaryOf(contact);
        final var expectedDate = DateConverter.toOffsetDateTime(LocalDateTime.of(contact.getContactDate(), LocalTime.MIDNIGHT));
        assertThat(observed)
            .isNotNull()
            .isInstanceOf(ContactSummary.class)
            .hasFieldOrPropertyWithValue("contactStart", expectedDate)
            .hasFieldOrPropertyWithValue("contactEnd", expectedDate)
            .hasFieldOrPropertyWithValue("officeLocation", null)
            .hasFieldOrPropertyWithValue("notes", null)
            .hasFieldOrPropertyWithValue("sensitive", null)
            .hasFieldOrPropertyWithValue("outcome", null)
            .hasFieldOrPropertyWithValue("rarActivity", false);
    }

    @Test
    public void contactSummaryFromContact() {
        final var contact = EntityHelper.aContact().toBuilder()
            .contactId(1L)
            .contactDate(LocalDate.of(2021, 6, 1))
            .contactStartTime(LocalTime.of(12, 0))
            .contactEndTime(LocalTime.of(13, 0))
            .contactType(EntityHelper.aContactType().toBuilder()
                .attendanceContact("Y")
                .code("CT1")
                .description("Some contact type")
                .shortDescription("Some contact type short description")
                .build())
            .officeLocation(EntityHelper.anOfficeLocation().toBuilder()
                .code("OL1")
                .description("Some office location")
                .build())
            .notes("Some notes")
            .probationArea(EntityHelper.aProbationArea().toBuilder()
                .code("PA1")
                .description("Some probation area")
                .build())
            .team(EntityHelper.aTeam().toBuilder()
                .code("T1")
                .description("Some team")
                .build())
            .staff(EntityHelper.aStaff().toBuilder()
                .officerCode("S1")
                .forename("FN1")
                .forname2("FN2")
                .surname("SN")
                .build())
            .sensitive("Y")
            .contactOutcomeType(EntityHelper.aContactOutcomeType().toBuilder()
                .code("O1")
                .description("Some outcome")
                .build())
            .attended("Y")
            .complied("Y")
            .hoursCredited(123.456)
            .rarActivity("Y")
            .build();

        final var observed = ContactTransformer.contactSummaryOf(contact);
        final var date = LocalDate.of(2021, 6, 1);
        assertThat(observed)
            .isNotNull()
            .isInstanceOf(ContactSummary.class)
            .hasFieldOrPropertyWithValue("contactId", 1L)
            .hasFieldOrPropertyWithValue("contactStart", OffsetDateTime.of(date, LocalTime.of(12, 0), ZoneOffset.ofHours(1)))
            .hasFieldOrPropertyWithValue("contactEnd", OffsetDateTime.of(date, LocalTime.of(13, 0), ZoneOffset.ofHours(1)))
            .hasFieldOrPropertyWithValue("type.code", "CT1")
            .hasFieldOrPropertyWithValue("type.description", "Some contact type")
            .hasFieldOrPropertyWithValue("type.shortDescription", "Some contact type short description")
            .hasFieldOrPropertyWithValue("type.appointment", true)
            .hasFieldOrPropertyWithValue("officeLocation.code", "OL1")
            .hasFieldOrPropertyWithValue("officeLocation.description", "Some office location")
            .hasFieldOrPropertyWithValue("notes", "Some notes")
            .hasFieldOrPropertyWithValue("provider.code", "PA1")
            .hasFieldOrPropertyWithValue("provider.description", "Some probation area")
            .hasFieldOrPropertyWithValue("team.code", "T1")
            .hasFieldOrPropertyWithValue("team.description", "Some team")
            .hasFieldOrPropertyWithValue("staff.code", "S1")
            .hasFieldOrPropertyWithValue("staff.forenames", "FN1 FN2")
            .hasFieldOrPropertyWithValue("staff.surname", "SN")
            .hasFieldOrPropertyWithValue("sensitive", true)
            .hasFieldOrPropertyWithValue("outcome.code", "O1")
            .hasFieldOrPropertyWithValue("outcome.description", "Some outcome")
            .hasFieldOrPropertyWithValue("outcome.attended", true)
            .hasFieldOrPropertyWithValue("outcome.complied", true)
            .hasFieldOrPropertyWithValue("outcome.hoursCredited", 123.456)
            .hasFieldOrPropertyWithValue("rarActivity", true);

    }
}
