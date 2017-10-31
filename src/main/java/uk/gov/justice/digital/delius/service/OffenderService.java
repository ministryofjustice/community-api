package uk.gov.justice.digital.delius.service;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.digital.delius.data.api.ContactDetails;
import uk.gov.justice.digital.delius.data.api.Conviction;
import uk.gov.justice.digital.delius.data.api.IDs;
import uk.gov.justice.digital.delius.data.api.OffenderDetail;
import uk.gov.justice.digital.delius.data.api.OffenderLanguages;
import uk.gov.justice.digital.delius.data.api.OffenderProfile;
import uk.gov.justice.digital.delius.data.api.PhoneNumber;
import uk.gov.justice.digital.delius.jpa.entity.Offender;
import uk.gov.justice.digital.delius.jpa.entity.StandardReference;
import uk.gov.justice.digital.delius.jpa.repository.OffenderRepository;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class OffenderService {

    private final OffenderRepository offenderRepository;

    @Autowired
    public OffenderService(OffenderRepository offenderRepository) {
        this.offenderRepository = offenderRepository;
    }

    @Transactional(readOnly = true)
    public Optional<OffenderDetail> getOffender(Long offenderId) {

        Optional<Offender> maybeOffender = offenderRepository.findByOffenderId(offenderId);

        return maybeOffender.map(offender -> OffenderDetail.builder()
                .dateOfBirth(offender.getDateOfBirthDate())
                .firstName(offender.getFirstName())
                .gender(offender.getGender().getCodeDescription())
                .middleNames(Optional.ofNullable(offender.getSecondName()))
                .surname(offender.getSurname())
                .title(Optional.ofNullable(offender.getTitle()).map(StandardReference::getCodeDescription))
                .contactDetails(ContactDetails.builder()
                        .allowSMS("Y".equals(offender.getAllowSMS()))
                        .emailAddresses(
                                Optional.ofNullable(offender.getEmailAddress()).map(Arrays::asList).orElse(Collections.emptyList()))
                        .phoneNumbers(buildPhoneNumbers(offender))
                        .build())
                .ids(IDs.builder()
                        .CRN(offender.getCrn())
                        .CRONumber(Optional.ofNullable(offender.getCroNumber()))
                        .immigrationNumber(Optional.ofNullable(offender.getImmigrationNumber()))
                        .NINumber(Optional.ofNullable(offender.getNiNumber()))
                        .NOMSNumber(Optional.ofNullable(offender.getNomsNumber()))
                        .PNCNumber(Optional.ofNullable(offender.getPncNumber()))
                        .mostRecentPrisonerNumber(Optional.ofNullable(offender.getMostRecentPrisonerNumber()))
                        .build())
                .offenderProfile(OffenderProfile.builder()
                        .ethnicity(Optional.ofNullable(offender.getEthnicity()).map(StandardReference::getCodeDescription))
                        .immigrationStatus(Optional.ofNullable(offender.getImmigrationStatus()).map(StandardReference::getCodeDescription))
                        .nationality(Optional.ofNullable(offender.getNationality()).map(StandardReference::getCodeDescription))
                        .offenderLanguages(OffenderLanguages.builder()
                                .primaryLanguage(Optional.ofNullable(offender.getLanguage()).map(StandardReference::getCodeDescription))
                                .languageConcerns(Optional.ofNullable(offender.getLanguageConcerns()))
                                .requiresInterpreter("Y".equals(offender.getInterpreterRequired()))
                                .build())
                        .previousConviction(Conviction.builder()
                                .convictionDate(Optional.ofNullable(offender.getPreviousConvictionDate()))
                                .detail(Optional.ofNullable(offender.getPrevConvictionDocumentName()).map(doc -> ImmutableMap.of("documentName", doc)).orElse(null))
                                .build())
                        .religion(Optional.ofNullable(offender.getReligion()).map(StandardReference::getCodeDescription))
                        .remandStatus(Optional.ofNullable(offender.getCurrentRemandStatus()))
                        .secondaryNationality(Optional.ofNullable(offender.getSecondNationality()).map(StandardReference::getCodeDescription))
                        .sexualOrientation(Optional.ofNullable(offender.getSexualOrientation()).map(StandardReference::getCodeDescription))
                        .build())
                .build());
    }

    private List<PhoneNumber> buildPhoneNumbers(Offender offender) {
        return ImmutableList.of(
                PhoneNumber.builder().number(Optional.ofNullable(offender.getTelephoneNumber())).type(PhoneNumber.PhoneTypes.TELEPHONE).build(),
                PhoneNumber.builder().number(Optional.ofNullable(offender.getMobileNumber())).type(PhoneNumber.PhoneTypes.MOBILE).build()
        ).stream().filter(phoneNumber -> phoneNumber.getNumber().isPresent()).collect(Collectors.toList());
    }
}
