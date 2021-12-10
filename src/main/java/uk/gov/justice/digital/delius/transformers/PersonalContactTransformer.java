package uk.gov.justice.digital.delius.transformers;

import uk.gov.justice.digital.delius.data.api.AddressSummary;
import uk.gov.justice.digital.delius.data.api.PersonalContact;
import uk.gov.justice.digital.delius.jpa.standard.entity.StandardReference;

import java.time.LocalDateTime;
import java.util.Optional;

public class PersonalContactTransformer {
    public static PersonalContact personalContactOf(uk.gov.justice.digital.delius.jpa.standard.entity.PersonalContact personalContact) {
        return PersonalContact.builder()
            .personalContactId(personalContact.getPersonalContactId())
            .relationship(personalContact.getRelationship())
            .startDate(personalContact.getStartDate())
            .endDate(personalContact.getEndDate())
            .isActive(isActiveOf(personalContact, LocalDateTime.now()))
            .title(Optional.ofNullable(personalContact.getTitle()).map(StandardReference::getCodeDescription).orElse(null))
            .firstName(personalContact.getFirstName())
            .otherNames(personalContact.getOtherNames())
            .surname(personalContact.getSurname())
            .previousSurname(personalContact.getPreviousSurname())
            .mobileNumber(personalContact.getMobileNumber())
            .emailAddress(personalContact.getEmailAddress())
            .notes(personalContact.getNotes())
            .gender(Optional.ofNullable(personalContact.getGender()).map(StandardReference::getCodeDescription).orElse(null))
            .relationshipType(KeyValueTransformer.keyValueOf(personalContact.getRelationshipType()))
            .createdDatetime(personalContact.getCreatedDatetime())
            .lastUpdatedDatetime(personalContact.getLastUpdatedDatetime())
            .address(Optional.ofNullable(personalContact.getAddress())
                .map(address -> AddressSummary.builder()
                    .addressNumber(address.getAddressNumber())
                    .buildingName(address.getBuildingName())
                    .streetName(address.getStreetName())
                    .district(address.getDistrict())
                    .town(address.getTownCity())
                    .county(address.getCounty())
                    .postcode(address.getPostcode())
                    .telephoneNumber(address.getTelephoneNumber())
                    .build())
                .orElse(null))
            .build();
    }

    private static Boolean isActiveOf(uk.gov.justice.digital.delius.jpa.standard.entity.PersonalContact personalCircumstance, LocalDateTime dateToCompare) {
        if (personalCircumstance.getStartDate().isAfter(dateToCompare)) {
            return false;
        }
        return Optional.ofNullable(personalCircumstance.getEndDate()).map(
            end -> personalCircumstance.getEndDate().isAfter(dateToCompare)
        ).orElse(true);
    }
}
