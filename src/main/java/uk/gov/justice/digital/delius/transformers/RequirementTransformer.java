package uk.gov.justice.digital.delius.transformers;

import uk.gov.justice.digital.delius.data.api.KeyValue;
import uk.gov.justice.digital.delius.jpa.standard.entity.AdRequirementTypeMainCategory;
import uk.gov.justice.digital.delius.jpa.standard.entity.PssRequirement;
import uk.gov.justice.digital.delius.jpa.standard.entity.PssRequirementTypeMainCategory;
import uk.gov.justice.digital.delius.jpa.standard.entity.PssRequirementTypeSubCategory;
import uk.gov.justice.digital.delius.jpa.standard.entity.Requirement;
import uk.gov.justice.digital.delius.jpa.standard.entity.RequirementTypeMainCategory;
import uk.gov.justice.digital.delius.jpa.standard.entity.StandardReference;

import java.util.Optional;

import static uk.gov.justice.digital.delius.transformers.TypesTransformer.zeroOneToBoolean;

public class RequirementTransformer {

    public static uk.gov.justice.digital.delius.data.api.Requirement requirementOf(Requirement requirement) {
        return Optional.ofNullable(requirement)
                .map(req -> uk.gov.justice.digital.delius.data.api.Requirement.builder()
                        .active(zeroOneToBoolean(req.getActiveFlag()))
                        .adRequirementTypeMainCategory(adRequirementMainCategoryOf(req.getAdRequirementTypeMainCategory()))
                        .adRequirementTypeSubCategory(KeyValueTransformer.keyValueOf(req.getAdRequirementTypeSubCategory()))
                        .commencementDate(req.getCommencementDate())
                        .expectedEndDate(req.getExpectedEndDate())
                        .expectedStartDate(req.getExpectedStartDate())
                        .requirementId(req.getRequirementId())
                        .requirementNotes(req.getRequirementNotes())
                        .requirementTypeMainCategory(requirementTypeMainCategoryOf(req.getRequirementTypeMainCategory()))
                        .requirementTypeSubCategory(KeyValueTransformer.keyValueOf(req.getRequirementTypeSubCategory()))
                        .startDate(req.getStartDate())
                        .terminationDate(req.getTerminationDate())
                        .terminationReason(KeyValueTransformer.keyValueOf(req.getTerminationReason()))
                        .length(req.getLength())
                        .lengthUnit(lengthUnitOf(req))
                        .build())
                .orElse(null);
    }

    public static uk.gov.justice.digital.delius.data.api.PssRequirement pssRequirementOf(PssRequirement pssRequirement) {
        return Optional.ofNullable(pssRequirement)
                .map(pssr -> uk.gov.justice.digital.delius.data.api.PssRequirement.builder()
                        .type(pssRequirmentTypeMainCategoryOf(pssRequirement.getPssRequirementTypeMainCategory()))
                        .subType(pssRequirementTypeSubCategoryOf(pssRequirement.getPssRequirementTypeSubCategory()))
                        .active(pssRequirement.getActiveFlag() == 1L)
                        .build())
                .orElse(null);
    }

    private static KeyValue pssRequirementTypeSubCategoryOf(PssRequirementTypeSubCategory pssRequirementTypeSubCategory) {
        return Optional.ofNullable(pssRequirementTypeSubCategory)
                .map(sub ->
                        KeyValue.builder()
                                .code(sub.getCode())
                                .description(sub.getDescription())
                                .build())
                .orElse(null);
    }

    private static KeyValue pssRequirmentTypeMainCategoryOf(PssRequirementTypeMainCategory pssRequirementTypeMainCategory) {
        return Optional.ofNullable(pssRequirementTypeMainCategory)
                .map(mainCat ->
                KeyValue.builder()
                        .code(mainCat.getCode())
                        .description(mainCat.getDescription())
                        .build())
                .orElse(null);
    }

    private static String lengthUnitOf(Requirement req) {
        return Optional.ofNullable(req.getRequirementTypeMainCategory())
                .map(RequirementTypeMainCategory::getUnits)
                .map(StandardReference::getCodeDescription)
                .orElse(null);
    }

    private static KeyValue requirementTypeMainCategoryOf(RequirementTypeMainCategory requirementTypeMainCategory) {
        return Optional.ofNullable(requirementTypeMainCategory).map(mainCat ->
                KeyValue.builder()
                        .code(mainCat.getCode())
                        .description(mainCat.getDescription())
                        .build())
                .orElse(null);
    }

    private static KeyValue adRequirementMainCategoryOf(AdRequirementTypeMainCategory adRequirementTypeMainCategory) {
        return Optional.ofNullable(adRequirementTypeMainCategory).map(mainCat ->
                KeyValue.builder()
                        .code(mainCat.getCode())
                        .description(mainCat.getDescription())
                        .build())
                .orElse(null);

    }
}
