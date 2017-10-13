package uk.gov.justice.digital.delius.service;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.justice.digital.delius.data.api.ContactDetails;
import uk.gov.justice.digital.delius.data.api.Conviction;
import uk.gov.justice.digital.delius.data.api.IDs;
import uk.gov.justice.digital.delius.data.api.OffenderDetail;
import uk.gov.justice.digital.delius.data.api.OffenderLanguages;
import uk.gov.justice.digital.delius.data.api.OffenderProfile;
import uk.gov.justice.digital.delius.data.api.PhoneNumber;
import uk.gov.justice.digital.delius.jpa.entity.Offender;
import uk.gov.justice.digital.delius.jpa.repository.OffenderRepository;

import java.util.Optional;

@Service
public class OffenderService {

    private final OffenderRepository offenderRepository;

    @Autowired
    public OffenderService(OffenderRepository offenderRepository) {
        this.offenderRepository = offenderRepository;
    }

    public Optional<OffenderDetail> getOffender(Long offenderId) {
        Optional<Offender> maybeOffender = offenderRepository.findByOffenderId(offenderId);

        return maybeOffender.map(offender -> OffenderDetail.builder()
                .dateOfBirth(offender.getDateOfBirthDate())
                .firstName(offender.getFirstName())
                .gender(offender.getGender().getCodeDescription())
                .middleNames(offender.getSecondName())
                .surname(offender.getSurname())
                .title(offender.getTitle().getCodeDescription())
                .contactDetails(ContactDetails.builder()
                        .allowSMS(offender.getAllowSMS().equals("Y"))
                        .emailAddresses(ImmutableList.of(offender.getEmailAddress()))
                        .phoneNumbers(ImmutableList.of(
                                PhoneNumber.builder().number(offender.getTelephoneNumber()).type(PhoneNumber.PhoneTypes.TELEPHONE).build(),
                                PhoneNumber.builder().number(offender.getMobileNumber()).type(PhoneNumber.PhoneTypes.MOBILE).build()
                        ))
                        .build())
                .ids(IDs.builder()
                        .CRN(offender.getCrn())
                        .CRONumber(offender.getCroNumber())
                        .immigrationNumber(offender.getImmigrationNumber())
                        .NINumber(offender.getNiNumber())
                        .NOMSNumber(offender.getNomsNumber())
                        .PNCNumber(offender.getPncNumber())
                        .mostRecentPrisonerNumber(offender.getMostRecentPrisonerNumber())
                        .build())
                .offenderProfile(OffenderProfile.builder()
                        .ethnicity(offender.getEthnicity().getCodeDescription())
                        .exclusionMessage(offender.getExclusionMessage())
                        .immigrationStatus(offender.getImmigrationStatus().getCodeDescription())
                        .nationality(offender.getNationality().getCodeDescription())
                        .offenderLanguages(OffenderLanguages.builder()
                                .primaryLanguage(offender.getLanguage().getCodeDescription())
                                .languageConcerns(offender.getLanguageConcerns())
                                .requiresInterpreter(offender.getInterpreterRequired().equals('Y'))
                                .build())
                        .previousConviction(Conviction.builder()
                                .convictionDate(offender.getPreviousConvictionDate())
                                .detail(ImmutableMap.of("documentName", offender.getPrevConvictionDocumentName()))
                                .build())
                        .religion(offender.getReligion().getCodeDescription())
                        .remandStatus(offender.getCurrentRemandStatus())
                        .secondaryNationality(offender.getSecondNationality().getCodeDescription())
                        .restrictionMessage(offender.getRestrictionMessage())
                        .sexualOrientation(offender.getSexualOrientation().getCodeDescription())
                        .build())
                .build());
    }
}
