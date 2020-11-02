package uk.gov.justice.digital.delius.service;

import org.springframework.stereotype.Service;
import uk.gov.justice.digital.delius.controller.NotFoundException;
import uk.gov.justice.digital.delius.jpa.standard.repository.InstitutionRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.OffenderRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.StandardReferenceRepository;

import javax.transaction.Transactional;

@Service
public class SmokeTestHelperService {
    private final OffenderRepository offenderRepository;
    private final ConvictionService convictionService;
    private final StandardReferenceRepository standardReferenceRepository;
    private final InstitutionRepository institutionRepository;

    private static final String CUSTODY_STATUS_DATASET = "THROUGHCARE STATUS";
    private static final String SENTENCED_IN_CUSTODY = "A";


    public SmokeTestHelperService(OffenderRepository offenderRepository, ConvictionService convictionService, StandardReferenceRepository standardReferenceRepository, InstitutionRepository institutionRepository) {
        this.offenderRepository = offenderRepository;
        this.convictionService = convictionService;
        this.standardReferenceRepository = standardReferenceRepository;
        this.institutionRepository = institutionRepository;
    }

    @Transactional
    public void resetCustodySmokeTestData(String crn) {
        final var offender = offenderRepository
                .findByCrn(crn)
                .orElseThrow(() -> new NotFoundException(String.format("Offender with crn %s not found", crn)));

        final var custody = convictionService.getActiveCustodialEvent(offender.getOffenderId()).getDisposal().getCustody();
        custody.setPrisonerNumber(null);
        custody.getKeyDates().clear();
        custody.setInstitution(institutionRepository.findByCode("UNKNOW").orElseThrow());
        custody.setCustodialStatus(standardReferenceRepository.findByCodeAndCodeSetName(SENTENCED_IN_CUSTODY, CUSTODY_STATUS_DATASET).orElseThrow());
        offender.setNomsNumber(null);
    }
}
