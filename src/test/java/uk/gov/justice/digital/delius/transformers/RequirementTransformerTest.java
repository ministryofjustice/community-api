package uk.gov.justice.digital.delius.transformers;

import org.junit.jupiter.api.Test;
import uk.gov.justice.digital.delius.jpa.standard.entity.AdRequirementTypeMainCategory;
import uk.gov.justice.digital.delius.jpa.standard.entity.PssRequirement;
import uk.gov.justice.digital.delius.jpa.standard.entity.PssRequirementTypeMainCategory;
import uk.gov.justice.digital.delius.jpa.standard.entity.PssRequirementTypeSubCategory;
import uk.gov.justice.digital.delius.jpa.standard.entity.Requirement;
import uk.gov.justice.digital.delius.jpa.standard.entity.RequirementTypeMainCategory;
import uk.gov.justice.digital.delius.jpa.standard.entity.StandardReference;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

public class RequirementTransformerTest {
    @Test
    public void whenGetPssRequirementsByConvictionId_thenReturnRequirements() {
        PssRequirement requirement = PssRequirement.builder()
            .pssRequirementId(88L)
            .pssRequirementTypeMainCategory(PssRequirementTypeMainCategory.builder()
                .description("Standard 7 Conditions")
                .code("A")
                .build())
            .pssRequirementTypeSubCategory(PssRequirementTypeSubCategory.builder()
                .description("SubType")
                .code("B")
                .build())
            .activeFlag(1L)
            .build();
        uk.gov.justice.digital.delius.data.api.PssRequirement pssRequirement = RequirementTransformer.pssRequirementOf(requirement);

        assertThat(pssRequirement.getType().getDescription()).isEqualTo("Standard 7 Conditions");
        assertThat(pssRequirement.getType().getCode()).isEqualTo("A");
        assertThat(pssRequirement.getSubType().getDescription()).isEqualTo("SubType");
        assertThat(pssRequirement.getSubType().getCode()).isEqualTo("B");
        assertThat(pssRequirement.getActive()).isEqualTo(true);
    }

    @Test
    public void nonRestrictiveRequirements() {
        Requirement requirement = Requirement.builder().requirementTypeMainCategory(RequirementTypeMainCategory.builder().restrictive("N").build()).build();
        uk.gov.justice.digital.delius.data.api.Requirement actual = RequirementTransformer.requirementOf(requirement);
        assertThat(actual.getRestrictive()).isEqualTo(false);
    }

    @Test
    public void whenGetRequirementsByConvictionId_thenReturnRequirements() {
        LocalDate commencementDate = LocalDate.of(2020, 1, 1);
        LocalDate expectedEndDate = LocalDate.of(2020, 2, 1);
        LocalDate startDate = LocalDate.of(2020, 3, 1);
        LocalDate terminationDate = LocalDate.of(2020, 4, 1);
        LocalDate expectedStartDate = LocalDate.of(2020, 5, 1);
        LocalDateTime createdDatetime = LocalDateTime.of(2020, 5, 1, 2, 3, 4);
        Requirement requirement = Requirement.builder()
            .requirementId(88L)
            .activeFlag(true)
            .commencementDate(commencementDate)
            .expectedStartDate(expectedStartDate)
            .expectedEndDate(expectedEndDate)
            .startDate(startDate)
            .terminationDate(terminationDate)
            .createdDatetime(createdDatetime)
            .length(6L)
            .requirementNotes("Notes")
            .adRequirementTypeMainCategory(AdRequirementTypeMainCategory.builder()
                .description("AdMain")
                .build())
            .adRequirementTypeSubCategory(StandardReference.builder()
                .codeDescription("AdSub")
                .build())
            .requirementTypeMainCategory(RequirementTypeMainCategory.builder()
                .description("Main")
                .units(StandardReference.builder()
                    .codeDescription("Months")
                    .build())
                .restrictive("Y")
                .build())
            .requirementTypeSubCategory(StandardReference.builder()
                .codeDescription("Sub")
                .build())
            .rarCount(10L)
            .softDeleted(true)
            .build();
        uk.gov.justice.digital.delius.data.api.Requirement pssRequirement = RequirementTransformer.requirementOf(requirement);

        assertThat(pssRequirement.getRequirementId()).isEqualTo(88L);
        assertThat(pssRequirement.getActive()).isEqualTo(true);
        assertThat(pssRequirement.getCommencementDate()).isEqualTo(commencementDate);
        assertThat(pssRequirement.getExpectedEndDate()).isEqualTo(expectedEndDate);
        assertThat(pssRequirement.getLength()).isEqualTo(6L);
        assertThat(pssRequirement.getLengthUnit()).isEqualTo("Months");
        assertThat(pssRequirement.getRequirementNotes()).isEqualTo("Notes");
        assertThat(pssRequirement.getStartDate()).isEqualTo(startDate);
        assertThat(pssRequirement.getTerminationDate()).isEqualTo(terminationDate);
        assertThat(pssRequirement.getCreatedDatetime()).isEqualTo(createdDatetime);
        assertThat(pssRequirement.getExpectedStartDate()).isEqualTo(expectedStartDate);
        assertThat(pssRequirement.getAdRequirementTypeMainCategory().getDescription()).isEqualTo("AdMain");
        assertThat(pssRequirement.getAdRequirementTypeSubCategory().getDescription()).isEqualTo("AdSub");
        assertThat(pssRequirement.getRequirementTypeMainCategory().getDescription()).isEqualTo("Main");
        assertThat(pssRequirement.getRequirementTypeSubCategory().getDescription()).isEqualTo("Sub");
        assertThat(pssRequirement.getRestrictive()).isEqualTo(true);
        assertThat(pssRequirement.getRarCount()).isEqualTo(10L);
        assertThat(pssRequirement.getSoftDeleted()).isTrue();
    }

}