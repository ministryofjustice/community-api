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
                        aPersonalCircumstance().toBuilder().personalCircumstanceId(99L).build(),
                        aPersonalCircumstance().toBuilder().personalCircumstanceId(9L).build(),
                        aPersonalCircumstance().toBuilder().personalCircumstanceId(999L).build()
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
                        aPersonalCircumstance().toBuilder().personalCircumstanceId(1L).build(),
                        aPersonalCircumstance().toBuilder().personalCircumstanceId(2L).softDeleted(1L).build(),
                        aPersonalCircumstance().toBuilder().personalCircumstanceId(3L).build()
                ));

        assertThat(personalCircumstanceService.personalCircumstancesFor(1L)
                .stream().map(uk.gov.justice.digital.delius.data.api.PersonalCircumstance::getPersonalCircumstanceId)
                .collect(Collectors.toList()))
                .contains(1L, 3L);


    }

    private PersonalCircumstance aPersonalCircumstance() {
        return PersonalCircumstance.builder()
                .circumstanceSubType(CircumstanceSubType.builder().codeValue("X").codeDescription("Description").build())
                .circumstanceType(CircumstanceType.builder().codeValue("X").codeDescription("Description").build())
                .build();
    }

}