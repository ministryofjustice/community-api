package uk.gov.justice.digital.delius.service;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.digital.delius.jpa.standard.entity.CircumstanceSubType;
import uk.gov.justice.digital.delius.jpa.standard.entity.CircumstanceType;
import uk.gov.justice.digital.delius.jpa.standard.entity.PersonalCircumstance;
import uk.gov.justice.digital.delius.jpa.standard.repository.PersonalCircumstanceRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class PersonalCircumstanceServiceTest {

    private PersonalCircumstanceService personalCircumstanceService;

    @Mock
    private PersonalCircumstanceRepository personalCircumstanceRepository;

    @BeforeEach
    void setUp() {
        personalCircumstanceService = new PersonalCircumstanceService(personalCircumstanceRepository);
    }

    @Test
    public void personalCircumstancesOrderedNaturallyByPrimaryKeyReversed() {
        Mockito.when(personalCircumstanceRepository.findByOffenderId(1L))
                .thenReturn(ImmutableList.of(
                        aPersonalCircumstance().toBuilder().personalCircumstanceId(99L).startDate(LocalDate.now()).build(),
                        aPersonalCircumstance().toBuilder().personalCircumstanceId(9L).startDate(LocalDate.now()).build(),
                        aPersonalCircumstance().toBuilder().personalCircumstanceId(999L).startDate(LocalDate.now()).build()
                ));

        assertThat(personalCircumstanceService.personalCircumstancesFor(1L)
                .stream().map(uk.gov.justice.digital.delius.data.api.PersonalCircumstance::getPersonalCircumstanceId)
                .collect(Collectors.toList()))
                .containsSequence(999L, 99L, 9L);

    }

    @Test
    public void deletedRecordsIgnored() {
        Mockito.when(personalCircumstanceRepository.findByOffenderId(1L))
                .thenReturn(ImmutableList.of(
                        aPersonalCircumstance().toBuilder().personalCircumstanceId(1L).startDate(LocalDate.now()).build(),
                        aPersonalCircumstance().toBuilder().personalCircumstanceId(2L).startDate(LocalDate.now()).softDeleted(1L).build(),
                        aPersonalCircumstance().toBuilder().personalCircumstanceId(3L).startDate(LocalDate.now()).build()
                ));

        assertThat(personalCircumstanceService.personalCircumstancesFor(1L)
                .stream().map(uk.gov.justice.digital.delius.data.api.PersonalCircumstance::getPersonalCircumstanceId)
                .collect(Collectors.toList()))
                .contains(1L, 3L);

    }

    @Test
    public void personalCircumstancesHaveIsActiveFlag() {
        final var today = LocalDate.now();
        final var futureDate = LocalDate.now().plusDays(1);
        final var pastDate = LocalDate.now().minusDays(1);

        Mockito.when(personalCircumstanceRepository.findByOffenderId(1L))
                .thenReturn(ImmutableList.of(
                        aPersonalCircumstance().toBuilder().personalCircumstanceId(1L).startDate(pastDate).endDate(today).build(),
                        aPersonalCircumstance().toBuilder().personalCircumstanceId(2L).startDate(pastDate).endDate(futureDate).build(),
                        aPersonalCircumstance().toBuilder().personalCircumstanceId(3L).startDate(pastDate).build()
                ));

        List<uk.gov.justice.digital.delius.data.api.PersonalCircumstance> personalCircumstances =
            personalCircumstanceService.personalCircumstancesFor(1L);

        assertThat(personalCircumstances.stream().filter(c -> c.getPersonalCircumstanceId().equals(1L))
            .findFirst().get().getIsActive()).isEqualTo(false);
        assertThat(personalCircumstances.stream().filter(c -> c.getPersonalCircumstanceId().equals(2L))
            .findFirst().get().getIsActive()).isEqualTo(true);
        assertThat(personalCircumstances.stream().filter(c -> c.getPersonalCircumstanceId().equals(3L))
            .findFirst().get().getIsActive()).isEqualTo(true);

    }

    private PersonalCircumstance aPersonalCircumstance() {
        return PersonalCircumstance.builder()
                .circumstanceSubType(CircumstanceSubType.builder().codeValue("X").codeDescription("Description").build())
                .circumstanceType(CircumstanceType.builder().codeValue("X").codeDescription("Description").build())
                .build();
    }

}