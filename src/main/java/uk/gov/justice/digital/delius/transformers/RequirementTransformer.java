package uk.gov.justice.digital.delius.transformers;

import org.springframework.stereotype.Component;
import uk.gov.justice.digital.delius.data.api.KeyValue;
import uk.gov.justice.digital.delius.jpa.standard.entity.AdRequirementTypeMainCategory;
import uk.gov.justice.digital.delius.jpa.standard.entity.Requirement;
import uk.gov.justice.digital.delius.jpa.standard.entity.RequirementTypeMainCategory;
import uk.gov.justice.digital.delius.jpa.standard.entity.StandardReference;

import java.util.Optional;

import static uk.gov.justice.digital.delius.transformers.TypesTransformer.zeroOneToBoolean;

@Component
public class RequirementTransformer {

    public uk.gov.justice.digital.delius.data.api.Requirement requirementOf(Requirement requirement) {
        return Optional.ofNullable(requirement).map(req -> uk.gov.justice.digital.delius.data.api.Requirement.builder()
                .active(zeroOneToBoolean(req.getActiveFlag()))
                .adRequirementTypeMainCategory(adRequirementMainCategoryOf(req.getAdRequirementTypeMainCategory()))
                .adRequirementTypeSubCategory(adRequirementSubCategoryOf(req.getAdRequirementTypeSubCategory()))
                .commencementDate(req.getCommencementDate())
                .expectedEndDate(req.getExpectedEndDate())
                .expectedStartDate(req.getExpectedStartDate())
                .requirementId(req.getRequirementId())
                .requirementNotes(req.getRequirementNotes())
                .requirementTypeMainCategory(requirementTypeMainCategoryOf(req.getRequirementTypeMainCategory()))
                .requirementTypeSubCategory(requirementTypeSubCategoryOf(req.getRequirementTypeSubCategory()))
                .startDate(req.getStartDate())
                .terminationDate(req.getTerminationDate())
                .length(req.getLength())
                .terminationReason(terminationReasonOf(req.getTerminationReason()))
                .build()).orElse(null);
    }

    private KeyValue terminationReasonOf(StandardReference terminationReason) {
        return Optional.ofNullable(terminationReason).map( reason -> KeyValue.builder()
                    .description(reason.getCodeDescription())
                    .code(reason.getCodeValue()).build())
        .orElse(null);
    }

    private KeyValue requirementTypeMainCategoryOf(RequirementTypeMainCategory requirementTypeMainCategory) {
        return Optional.ofNullable(requirementTypeMainCategory).map(mainCat ->
                KeyValue.builder()
                        .code(mainCat.getCode())
                        .description(mainCat.getDescription())
                        .build()).orElse(null);
    }

    private KeyValue adRequirementSubCategoryOf(StandardReference adRequirementTypeSubCategory) {
        return Optional.ofNullable(adRequirementTypeSubCategory).map(adSubCat ->
                KeyValue.builder()
                        .code(adSubCat.getCodeValue())
                        .description(adSubCat.getCodeDescription())
                        .build()).orElse(null);
    }

    private KeyValue requirementTypeSubCategoryOf(StandardReference requirementTypeSubCategory) {
        return Optional.ofNullable(requirementTypeSubCategory).map(subCat ->
                KeyValue.builder()
                        .code(subCat.getCodeValue())
                        .description(subCat.getCodeDescription())
                        .build()).orElse(null);
    }

    private KeyValue adRequirementMainCategoryOf(AdRequirementTypeMainCategory adRequirementTypeMainCategory) {
        return Optional.ofNullable(adRequirementTypeMainCategory).map(mainCat ->
                KeyValue.builder()
                        .code(mainCat.getCode())
                        .description(mainCat.getDescription())
                        .build()).orElse(null);

    }
}
