package uk.gov.justice.digital.delius.data.api;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CaseloadRole {
    OFFENDER_MANAGER("OM"),
    ORDER_SUPERVISOR("OS"),
    // Component managers (not in use yet):
    LICENCE_CONDITION_MANAGER("LC"),
    REQUIREMENT_MANAGER("RQ"),
    PSSR_MANAGER("PQ"), // PSSR = Post-sentence supervision requirement
    NSI_MANAGER("NSI"), // NSI = Non-statutory intervention
    PRISON_OFFENDER_MANAGER("POM"),
    REPORT_MANAGER("RM");

    private final String roleCode;
}
