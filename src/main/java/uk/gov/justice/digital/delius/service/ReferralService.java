package uk.gov.justice.digital.delius.service;

import lombok.AllArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import uk.gov.justice.digital.delius.data.api.ReferralSent;
import uk.gov.justice.digital.delius.jpa.standard.entity.ContactType;
import uk.gov.justice.digital.delius.jpa.standard.entity.ProbationArea;
import uk.gov.justice.digital.delius.jpa.standard.entity.ProviderEmployee;
import uk.gov.justice.digital.delius.jpa.standard.entity.ProviderTeam;
import uk.gov.justice.digital.delius.jpa.standard.repository.ContactRepository;

import java.time.LocalDate;
import java.time.LocalTime;

import static uk.gov.justice.digital.delius.jpa.standard.entity.Contact.*;

@Service
@AllArgsConstructor
public class ReferralService {

    private final ContactRepository contactRepository;

    @Transactional
    public void addReferralSentContactEntry(final Long offenderId, final ReferralSent referralSent) {
        contactRepository
            .save(builder()
                  .contactDate(LocalDate.now()).contactStartTime(LocalTime.now())
                  .contactType(ContactType
                               .builder()
                               .contactTypeId(Long.valueOf(referralSent.getContactType()))
                               .build())
                  .staffEmployeeId(Long.valueOf(referralSent.getEmployeeId()))
                  .probationArea(ProbationArea
                                 .builder()
                                 .probationAreaId(Long.valueOf(referralSent.getProbationArea()))
                                 .build())
                  .teamProviderId(Long.valueOf(referralSent.getProviderTeam()))
                  .providerTeam(ProviderTeam
                                .builder()
                                .providerTeamId(Long
                                                .valueOf(referralSent.getProviderTeam())).build())
                  .providerEmployee(ProviderEmployee
                                    .builder()
                                    .providerEmployeeId(Long.valueOf(referralSent.getEmployeeId()))
                                    .build())
                  .offenderId(offenderId).build());

    }
}
