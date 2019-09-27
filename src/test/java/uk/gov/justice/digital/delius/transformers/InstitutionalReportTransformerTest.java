package uk.gov.justice.digital.delius.transformers;

import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.justice.digital.delius.jpa.standard.entity.Custody;
import uk.gov.justice.digital.delius.jpa.standard.entity.Disposal;
import uk.gov.justice.digital.delius.jpa.standard.entity.Event;
import uk.gov.justice.digital.delius.jpa.standard.entity.InstitutionalReport;
import uk.gov.justice.digital.delius.service.LookupSupplier;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class InstitutionalReportTransformerTest {
    @Mock
    private LookupSupplier lookupSupplier;
    private InstitutionalReportTransformer institutionalReportTransformer;

    @Before
    public void setup() {
        institutionalReportTransformer = new InstitutionalReportTransformer(
                new ConvictionTransformer(
                        new MainOffenceTransformer(lookupSupplier),
                        new AdditionalOffenceTransformer(lookupSupplier),
                        new CourtAppearanceTransformer(
                                new CourtReportTransformer(
                                        new CourtTransformer()),
                                new CourtTransformer(),
                                lookupSupplier),
                        lookupSupplier,
                        new InstitutionTransformer()));
    }

    @Test
    public void itTransformsOKWhenNothingIsSoftDeleted() {
        InstitutionalReport institutionalReport =
            InstitutionalReport.builder()
                .custody(aCustody())
                .institutionalReportId(1L)
                .offenderId(2L)
                .softDeleted(1L)
                .build();

        assertThat(institutionalReportTransformer.institutionalReportOf(institutionalReport)).isNotNull();
    }

    @Test
    public void itFiltersOutSoftDeletedCustody() {
        InstitutionalReport institutionalReport =
            InstitutionalReport.builder()
                .custody(aSoftDeletedCustody())
                .institutionalReportId(1L)
                .offenderId(2L)
                .softDeleted(1L)
                .build();

        assertThat(institutionalReportTransformer.institutionalReportOf(institutionalReport).getConviction()).isNull();
    }

    @Test
    public void itFiltersOutSoftDeletedDisposals() {
        InstitutionalReport institutionalReport =
            InstitutionalReport.builder()
                .custody(aCustodyWithASoftDeletedDisposal())
                .institutionalReportId(1L)
                .offenderId(2L)
                .softDeleted(1L)
                .build();

        assertThat(institutionalReportTransformer.institutionalReportOf(institutionalReport).getConviction()).isNull();
    }

    @Test
    public void itFiltersOutSoftDeletedEvents() {
        InstitutionalReport institutionalReport =
            InstitutionalReport.builder()
                .custody(aCustodyWithADisposalWithASoftDeletedEvent())
                .institutionalReportId(1L)
                .offenderId(2L)
                .softDeleted(1L)
                .build();

        assertThat(institutionalReportTransformer.institutionalReportOf(institutionalReport).getConviction()).isNull();
    }


    private Custody aCustody() {
        return Custody.builder()
            .custodyId(1L)
            .disposal(aDisposal())
            .offenderId(1L)
            .softDeleted(0L)
            .build();
    }

    private Custody aSoftDeletedCustody() {
        return Custody.builder()
            .custodyId(111L)
            .disposal(aDisposal())
            .offenderId(111L)
            .softDeleted(1L)
            .build();
    }

    private Custody aCustodyWithASoftDeletedDisposal() {
        return Custody.builder()
            .custodyId(1L)
            .disposal(aSoftDeletedDisposal())
            .offenderId(1L)
            .softDeleted(0L)
            .build();
    }

    private Custody aCustodyWithADisposalWithASoftDeletedEvent() {
        return Custody.builder()
            .custodyId(1L)
            .disposal(aDisposalWithASoftDeletedEvent())
            .offenderId(1L)
            .softDeleted(0L)
            .build();
    }

    private Disposal aDisposal() {
        return Disposal.builder()
            .disposalId(1L)
            .event(anEvent())
            .offenderId(1L)
            .softDeleted(0L)
            .build();
    }

    private Disposal aSoftDeletedDisposal() {
        return Disposal.builder()
            .disposalId(999L)
            .event(anEvent())
            .offenderId(999L)
            .softDeleted(1L)
            .build();
    }

    private Disposal aDisposalWithASoftDeletedEvent() {
        return Disposal.builder()
            .disposalId(999L)
            .event(aSoftDeletedEvent())
            .offenderId(999L)
            .softDeleted(0L)
            .build();
    }

    private Event anEvent() {
        return Event.builder()
            .activeFlag(1L)
            .convictionDate(LocalDate.of(2018, 11, 2))
            .additionalOffences(ImmutableList.of())
            .eventId(1L)
            .softDeleted(0L)
            .build();
    }

    private Event aSoftDeletedEvent() {
        return Event.builder()
            .activeFlag(1L)
            .convictionDate(LocalDate.of(2018, 11, 2))
            .eventId(1L)
            .softDeleted(1L)
            .build();
    }
}
