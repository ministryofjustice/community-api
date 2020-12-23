package uk.gov.justice.digital.delius.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.digital.delius.data.api.ReferralSent;
import uk.gov.justice.digital.delius.jpa.standard.repository.ContactRepository;

import static uk.gov.justice.digital.delius.util.EntityHelper.anOffender;

@ExtendWith(MockitoExtension.class)
public class ReferralServiceTest {

    private ReferralService referralService;
    private ReferralSent referralSent;
    private Long offenderId;

    @Mock
    private ContactRepository contactRepository;

    @BeforeEach
    public void setup() {
        offenderId = anOffender().getOffenderId();
        referralService = new ReferralService(contactRepository);
        referralSent = new ReferralSent(3000L,         // ID
                                        "1500001001",  // Probation Area
                                        "418",         // Contact Type
                                        "2500031218",  // Provider Team
                                        "2500000002",  // Probation Officer
                                        "306",         // Employee ID
                                        "2500000002"); // Context
    }

    @Test
    public void addReferralSentContactTest() {
        referralService.addReferralSentContactEntry(offenderId, referralSent);
    }
}
