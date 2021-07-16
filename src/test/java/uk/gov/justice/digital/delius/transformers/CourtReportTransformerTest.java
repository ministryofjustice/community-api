package uk.gov.justice.digital.delius.transformers;

import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import uk.gov.justice.digital.delius.jpa.standard.entity.RCourtReportType;
import uk.gov.justice.digital.delius.util.EntityHelper;

import static org.assertj.core.api.Assertions.assertThat;

class CourtReportTransformerTest {

    @DisplayName("Simple mapping of completely populated court report entity")
    @Test
    void whenTransform_thenPopulateAllFields() {

        final var requestedDate = LocalDateTime.now();
        final var requiredDate = requestedDate.plusDays(7);
        final var completedDate = requestedDate.plusDays(4);
        final var courtReportType = RCourtReportType
            .builder()
            .description("Pre-Sentence Report - Fast")
            .code("CJF")
            .build();
        final var reportManager1 = EntityHelper.aReportManager(false);
        final var reportManager2 = EntityHelper.aReportManager(true);

        final var courtReportEntity = EntityHelper.aCourtReport(requestedDate, requiredDate, completedDate, courtReportType, List.of(reportManager1, reportManager2));

        var courtReport = CourtReportTransformer.courtReportMinimalOf(courtReportEntity);

        assertThat(courtReport.getRequestedDate()).isEqualTo(requestedDate);
        assertThat(courtReport.getRequiredDate()).isEqualTo(requiredDate);
        assertThat(courtReport.getCompletedDate()).isEqualTo(completedDate);
        assertThat(courtReport.getCourtReportType().getCode()).isEqualTo("CJF");
        assertThat(courtReport.getCourtReportType().getDescription()).isEqualTo("Pre-Sentence Report - Fast");
        assertThat(courtReport.getOffenderId()).isNotNull();
        assertThat(courtReport.getCourtReportId()).isNotNull();
        assertThat(courtReport.getReceivedByCourtDate()).isNotNull();
        assertThat(courtReport.getAllocationDate()).isNotNull();
        assertThat(courtReport.getSentToCourtDate()).isNotNull();
        assertThat(courtReport.getReportManagers()).hasSize(2);
        assertThat(courtReport.getReportManagers().get(0).getStaff().getForenames()).isEqualTo("John");
        assertThat(courtReport.getReportManagers().get(0).getStaff().getForenames()).isEqualTo("John");
        assertThat(courtReport.getReportManagers()).extracting("active").contains(true, false);
    }

    @DisplayName("Mapping of court report entity with no type ensuring null proof")
    @Test
    void givenNullCourtReportType_whenTransform_thenProtectNullPointerException() {

        final var requestedDate = LocalDateTime.now();
        final var requiredDate = requestedDate.plusDays(7);
        final var completedDate = requestedDate.plusDays(4);
        // There is no not-null constraint present in the DB for court report type
        final var courtReportEntity = EntityHelper.aCourtReport(requestedDate, requiredDate, completedDate, null);

        var courtReport = CourtReportTransformer.courtReportMinimalOf(courtReportEntity);

        assertThat(courtReport.getRequestedDate()).isEqualTo(requestedDate);
        assertThat(courtReport.getRequiredDate()).isEqualTo(requiredDate);
        assertThat(courtReport.getCompletedDate()).isEqualTo(completedDate);
        assertThat(courtReport.getCourtReportType()).isNull();
        assertThat(courtReport.getOffenderId()).isNotNull();
        assertThat(courtReport.getCourtReportId()).isNotNull();
        assertThat(courtReport.getReceivedByCourtDate()).isNotNull();
        assertThat(courtReport.getAllocationDate()).isNotNull();
        assertThat(courtReport.getSentToCourtDate()).isNotNull();
    }
}
