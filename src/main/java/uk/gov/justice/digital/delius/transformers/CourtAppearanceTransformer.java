package uk.gov.justice.digital.delius.transformers;

import org.springframework.stereotype.Component;
import uk.gov.justice.digital.delius.data.api.Court;
import uk.gov.justice.digital.delius.data.api.CourtAppearance;
import uk.gov.justice.digital.delius.data.api.CourtReport;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class CourtAppearanceTransformer {

    public List<CourtAppearance> courtAppearancesOf(List<uk.gov.justice.digital.delius.jpa.standard.entity.CourtAppearance> courtAppearances) {
        return courtAppearances.stream()
            .sorted(Comparator.comparing(uk.gov.justice.digital.delius.jpa.standard.entity.CourtAppearance::getAppearanceDate).reversed())
            .map(this::courtAppearanceOf)
            .collect(Collectors.toList());
    }

    private CourtAppearance courtAppearanceOf(uk.gov.justice.digital.delius.jpa.standard.entity.CourtAppearance courtAppearance) {
        return CourtAppearance.builder()
            .courtAppearanceId(courtAppearance.getCourtAppearanceId())
            .appearanceDate(courtAppearance.getAppearanceDate())
            .court(courtOf(courtAppearance.getCourt()))
            .courtReports(courtReportsOf(courtAppearance.getCourtReports()))
            .build();
    }

    private List<CourtReport> courtReportsOf(List<uk.gov.justice.digital.delius.jpa.standard.entity.CourtReport> courtReports) {
        return courtReports.stream()
            .map(report -> CourtReport.builder()
                .courtReportId(report.getCourtReportId()).build())
            .collect(Collectors.toList());
    }

    private Court courtOf(uk.gov.justice.digital.delius.jpa.standard.entity.Court court) {
        return Court.builder()
            .courtId(court.getCourtId())
            .localJusticeArea(court.getLocality())
            .name(court.getCourtName())
            .build();
    }

}
