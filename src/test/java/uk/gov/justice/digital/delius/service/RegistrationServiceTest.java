package uk.gov.justice.digital.delius.service;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.Test;
import uk.gov.justice.digital.delius.jpa.standard.entity.ProbationArea;
import uk.gov.justice.digital.delius.jpa.standard.entity.RegisterType;
import uk.gov.justice.digital.delius.jpa.standard.entity.Registration;
import uk.gov.justice.digital.delius.jpa.standard.entity.StandardReference;
import uk.gov.justice.digital.delius.jpa.standard.entity.Team;
import uk.gov.justice.digital.delius.jpa.standard.repository.RegistrationRepository;

import java.time.LocalDate;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class RegistrationServiceTest {

    private RegistrationService registrationService;

    @Mock
    private RegistrationRepository registrationRepository;

    @BeforeEach
    void before() {
        registrationService = new RegistrationService(registrationRepository);
    }

    @Test
    public void registrationsOrderedByRegistrationDateReversed() {
        Mockito.when(registrationRepository.findByOffenderId(1L))
                .thenReturn(ImmutableList.of(
                        aRegistration().toBuilder().registrationDate(LocalDate.of(2018, 10, 1)).registrationId(1L).build(),
                        aRegistration().toBuilder().registrationDate(LocalDate.of(2018, 11, 1)).registrationId(2L).build(),
                        aRegistration().toBuilder().registrationDate(LocalDate.of(2018, 9, 1)).registrationId(3L).build()
                ));

        assertThat(registrationService.registrationsFor(1L)
                .stream().map(uk.gov.justice.digital.delius.data.api.Registration::getRegistrationId)
                .collect(Collectors.toList()))
                .containsSequence(2L, 1L, 3L);

    }

    @Test
    public void deletedRecordsIgnored() {
        Mockito.when(registrationRepository.findByOffenderId(1L))
                .thenReturn(ImmutableList.of(
                        aRegistration().toBuilder().registrationId(1L).build(),
                        aRegistration().toBuilder().registrationId(2L).softDeleted(1L).build(),
                        aRegistration().toBuilder().registrationId(3L).build()
                ));

        assertThat(registrationService.registrationsFor(1L)
                .stream().map(uk.gov.justice.digital.delius.data.api.Registration::getRegistrationId)
                .collect(Collectors.toList()))
                .contains(1L, 3L);


    }

    private Registration aRegistration() {
        return Registration.builder()
                .registerType(RegisterType
                        .builder()
                        .registerTypeFlag(StandardReference.builder().
                                codeDescription("ROSH")
                                .build())
                        .build())
                .registeringTeam(Team
                        .builder()
                        .probationArea(
                                ProbationArea
                                .builder()
                                .build())
                        .build()
                )
                .registrationDate(LocalDate.now())
                .build();
    }

}