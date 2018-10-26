package uk.gov.justice.digital.delius.service;

import com.google.common.collect.ImmutableList;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.justice.digital.delius.jpa.standard.entity.CircumstanceSubType;
import uk.gov.justice.digital.delius.jpa.standard.entity.CircumstanceType;
import uk.gov.justice.digital.delius.jpa.standard.entity.PersonalCircumstance;
import uk.gov.justice.digital.delius.jpa.standard.repository.PersonalCircumstanceRepository;
import uk.gov.justice.digital.delius.transformers.PersonalCircumstanceTransformer;

import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@Import({PersonalCircumstanceService.class, PersonalCircumstanceTransformer.class})
public class PersonalCircumstanceServiceTest {

    @Autowired
    private PersonalCircumstanceService personalCircumstanceService;

    @MockBean
    private PersonalCircumstanceRepository personalCircumstanceRepository;


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