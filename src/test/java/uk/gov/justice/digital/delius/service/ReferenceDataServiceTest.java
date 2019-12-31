package uk.gov.justice.digital.delius.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.justice.digital.delius.data.api.Human;
import uk.gov.justice.digital.delius.data.api.KeyValue;
import uk.gov.justice.digital.delius.data.api.StaffDetails;
import uk.gov.justice.digital.delius.jpa.filters.ProbationAreaFilter;
import uk.gov.justice.digital.delius.jpa.standard.entity.ProbationArea;
import uk.gov.justice.digital.delius.jpa.standard.entity.Staff;
import uk.gov.justice.digital.delius.jpa.standard.repository.ProbationAreaRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.StaffHelperRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.StaffRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.StandardReferenceRepository;
import uk.gov.justice.digital.delius.ldap.repository.LdapRepository;
import uk.gov.justice.digital.delius.transformers.*;
import uk.gov.justice.digital.delius.util.EntityHelper;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static uk.gov.justice.digital.delius.util.EntityHelper.*;

@RunWith(MockitoJUnitRunner.class)
public class ReferenceDataServiceTest {

    private ReferenceDataService referenceDataService;

    @Mock
    private StaffRepository staffRepository;

    @Mock
    private StandardReferenceRepository standardReferenceRepository;

    @Mock
    private ProbationAreaRepository probationAreaRepository;

    @Before
    public void setup() {
        referenceDataService = new ReferenceDataService(
                new ProbationAreaTransformer(new InstitutionTransformer()),
                probationAreaRepository,
                standardReferenceRepository);
    }

    @Test
    public void getProbationAreaCodes_includeInactive() {
        final var filter = ProbationAreaFilter.builder().restrictActive(false).build();

        when(probationAreaRepository.findAll(filter)).thenReturn(List.of(aProbationArea()));

        assertThat(referenceDataService.getProbationAreasCodes(false).getContent())
                .hasSize(1)
                .first()
                .isEqualTo(new KeyValue(aProbationArea().getCode(), aProbationArea().getDescription()));
    }

    @Test
    public void getProbationAreaCodes_excludeInactive() {
        final var filter = ProbationAreaFilter.builder().restrictActive(true).build();

        when(probationAreaRepository.findAll(filter)).thenReturn(List.of(aProbationArea()));

        assertThat(referenceDataService.getProbationAreasCodes(true).getContent())
                .hasSize(1)
                .first()
                .isEqualTo(new KeyValue(aProbationArea().getCode(), aProbationArea().getDescription()));
    }

    @Test
    public void getLocalDeliveryUnits() {
        when(probationAreaRepository.findByCode(aProbationArea().getCode())).thenReturn(
                Optional.of(
                        aProbationArea().toBuilder()
                                .localDeliveryUnits(List.of(
                                        aLocalDeliveryUnit("LDU-1"),
                                        aLocalDeliveryUnit("LDU-2"),
                                        aLocalDeliveryUnit("LDU-3")))
                                .build()));

        assertThat(referenceDataService.getLocalDeliveryUnitsForProbationArea(aProbationArea().getCode()).getContent())
                .hasSize(3)
                .extracting(KeyValue::getCode)
                .containsExactly("LDU-1", "LDU-2", "LDU-3");
    }

    @Test
    public void getLocalDeliveryUnits_missingProbationArea() {
        when(probationAreaRepository.findByCode(aProbationArea().getCode())).thenReturn(Optional.empty());
        assertThat(referenceDataService.getLocalDeliveryUnitsForProbationArea(aProbationArea().getCode()).getContent()).isEmpty();
    }
}
