package uk.gov.justice.digital.delius.transformers;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.springframework.stereotype.Component;
import uk.gov.justice.digital.delius.data.api.Address;
import uk.gov.justice.digital.delius.data.api.ContactDetails;
import uk.gov.justice.digital.delius.data.api.Conviction;
import uk.gov.justice.digital.delius.data.api.IDs;
import uk.gov.justice.digital.delius.data.api.OffenderDetail;
import uk.gov.justice.digital.delius.data.api.OffenderLanguages;
import uk.gov.justice.digital.delius.data.api.OffenderProfile;
import uk.gov.justice.digital.delius.data.api.PhoneNumber;
import uk.gov.justice.digital.delius.jpa.entity.Offender;
import uk.gov.justice.digital.delius.jpa.entity.OffenderAddress;
import uk.gov.justice.digital.delius.jpa.entity.OffenderAlias;
import uk.gov.justice.digital.delius.jpa.entity.StandardReference;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class OffenderTransformer {

    private List<PhoneNumber> phoneNumbersOf(Offender offender) {
        return ImmutableList.of(
                PhoneNumber.builder().number(Optional.ofNullable(offender.getTelephoneNumber())).type(PhoneNumber.PhoneTypes.TELEPHONE).build(),
                PhoneNumber.builder().number(Optional.ofNullable(offender.getMobileNumber())).type(PhoneNumber.PhoneTypes.MOBILE).build()
        ).stream().filter(phoneNumber -> phoneNumber.getNumber().isPresent()).collect(Collectors.toList());
    }

    private OffenderLanguages languagesOf(Offender offender) {
        return OffenderLanguages.builder()
                .primaryLanguage(Optional.ofNullable(offender.getLanguage()).map(StandardReference::getCodeDescription))
                .languageConcerns(Optional.ofNullable(offender.getLanguageConcerns()))
                .requiresInterpreter(Optional.ofNullable(offender.getInterpreterRequired()).map(intReq -> "Y".equals(intReq)))
                .build();
    }

    private Conviction previousConvictionOf(Offender offender) {
        return Conviction.builder()
                .convictionDate(Optional.ofNullable(offender.getPreviousConvictionDate()))
                .detail(Optional.ofNullable(offender.getPrevConvictionDocumentName()).map(doc -> ImmutableMap.of("documentName", doc)).orElse(null))
                .build();
    }

    private OffenderProfile offenderProfileOf(Offender offender) {
        return OffenderProfile.builder()
                .ethnicity(Optional.ofNullable(offender.getEthnicity()).map(StandardReference::getCodeDescription))
                .immigrationStatus(Optional.ofNullable(offender.getImmigrationStatus()).map(StandardReference::getCodeDescription))
                .nationality(Optional.ofNullable(offender.getNationality()).map(StandardReference::getCodeDescription))
                .offenderLanguages(languagesOf(offender))
                .previousConviction(previousConvictionOf(offender))
                .religion(Optional.ofNullable(offender.getReligion()).map(StandardReference::getCodeDescription))
                .remandStatus(Optional.ofNullable(offender.getCurrentRemandStatus()))
                .secondaryNationality(Optional.ofNullable(offender.getSecondNationality()).map(StandardReference::getCodeDescription))
                .sexualOrientation(Optional.ofNullable(offender.getSexualOrientation()).map(StandardReference::getCodeDescription))
                .build();
    }

    private IDs idsOf(Offender offender) {
        return IDs.builder()
                .CRN(offender.getCrn())
                .CRONumber(Optional.ofNullable(offender.getCroNumber()))
                .immigrationNumber(Optional.ofNullable(offender.getImmigrationNumber()))
                .NINumber(Optional.ofNullable(offender.getNiNumber()))
                .NOMSNumber(Optional.ofNullable(offender.getNomsNumber()))
                .PNCNumber(Optional.ofNullable(offender.getPncNumber()))
                .mostRecentPrisonerNumber(Optional.ofNullable(offender.getMostRecentPrisonerNumber()))
                .build();
    }

    private Address addressOf(OffenderAddress address) {
        return Address.builder()
                .addressNumber(Optional.ofNullable(address.getAddressNumber()))
                .buildingName(Optional.ofNullable(address.getBuildingName()))
                .streetName(Optional.ofNullable(address.getStreetName()))
                .district(Optional.ofNullable(address.getDistrict()))
                .town(Optional.ofNullable(address.getTownCity()))
                .county(Optional.ofNullable(address.getCounty()))
                .postcode(Optional.ofNullable(address.getPostcode()))
                .telephoneNumber(Optional.ofNullable(address.getTelephoneNumber()))
                .notes(Optional.ofNullable(address.getNotes()))
                .noFixedAbode(Optional.ofNullable(address.getNoFixedAbode()).map(nfa -> nfa.equalsIgnoreCase("Y")))
                .from(address.getStartDate())
                .to(Optional.ofNullable(address.getEndDate()))
                .build();
    }

    private ContactDetails contactDetailsOf(Offender offender) {
        return ContactDetails.builder()
                .allowSMS(Optional.ofNullable(offender.getAllowSMS()).map(allowSms -> "Y".equals(allowSms)))
                .emailAddresses(emailAddressesOf(offender))
                .phoneNumbers(phoneNumbersOf(offender))
                .addresses(Optional.of(addressesOf(offender)))
                .build();
    }

    private List<String> emailAddressesOf(Offender offender) {
        return Optional.ofNullable(offender.getEmailAddress()).map(Arrays::asList).orElse(Collections.emptyList());
    }

    private List<Address> addressesOf(Offender offender) {
        return offender.getOffenderAddresses().stream().map(
                address -> addressOf(address)).collect(Collectors.toList());
    }

    private uk.gov.justice.digital.delius.data.api.OffenderAlias aliasOf(OffenderAlias alias) {
        return uk.gov.justice.digital.delius.data.api.OffenderAlias.builder()
                .dateOfBirth(Optional.ofNullable(alias.getDateOfBirth()))
                .firstName(Optional.ofNullable(alias.getFirstName()))
                .secondName(Optional.ofNullable(alias.getSecondName()))
                .thirdName(Optional.ofNullable(alias.getThirdName()))
                .surname(Optional.ofNullable(alias.getSurname()))
                .gender(Optional.ofNullable(Optional.ofNullable(alias.getGender()).map(StandardReference::getCodeDescription).orElse(null)))
                .build();
    }

    private List<uk.gov.justice.digital.delius.data.api.OffenderAlias> offenderAliasesOf(List<OffenderAlias> offenderAliases) {
        return offenderAliases.stream().map(this::aliasOf).collect(Collectors.toList());

    }

    public OffenderDetail offenderOf(Offender offender) {
        return OffenderDetail.builder()
                .offenderId(offender.getOffenderId())
                .dateOfBirth(offender.getDateOfBirthDate())
                .firstName(offender.getFirstName())
                .gender(offender.getGender().getCodeDescription())
                .middleNames(Optional.ofNullable(offender.getSecondName()))
                .surname(offender.getSurname())
                .title(Optional.ofNullable(offender.getTitle()).map(StandardReference::getCodeDescription))
                .contactDetails(contactDetailsOf(offender))
                .ids(idsOf(offender))
                .offenderProfile(offenderProfileOf(offender))
                .offenderAliases(Optional.of(offenderAliasesOf(offender.getOffenderAliases())))
                .build();
    }
}
