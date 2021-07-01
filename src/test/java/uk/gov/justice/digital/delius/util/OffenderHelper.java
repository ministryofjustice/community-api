package uk.gov.justice.digital.delius.util;

import org.assertj.core.util.Lists;
import uk.gov.justice.digital.delius.jpa.standard.entity.AddressAssessment;
import uk.gov.justice.digital.delius.jpa.standard.entity.CircumstanceSubType;
import uk.gov.justice.digital.delius.jpa.standard.entity.Disability;
import uk.gov.justice.digital.delius.jpa.standard.entity.Offender;
import uk.gov.justice.digital.delius.jpa.standard.entity.OffenderAddress;
import uk.gov.justice.digital.delius.jpa.standard.entity.OffenderAlias;
import uk.gov.justice.digital.delius.jpa.standard.entity.OffenderManager;
import uk.gov.justice.digital.delius.jpa.standard.entity.Officer;
import uk.gov.justice.digital.delius.jpa.standard.entity.PartitionArea;
import uk.gov.justice.digital.delius.jpa.standard.entity.PersonalCircumstance;
import uk.gov.justice.digital.delius.jpa.standard.entity.ProbationArea;
import uk.gov.justice.digital.delius.jpa.standard.entity.StandardReference;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface OffenderHelper {
    static OffenderAddress anOffenderAddress() {
        return OffenderAddress.builder()
            .offenderAddressID(123L)
            .offenderID(100L)
            .startDate(LocalDate.of(2010, 6, 10))
            .endDate(LocalDate.of(2020, 7, 11))
            .softDeleted(0L)
            .addressStatus(StandardReference.builder().codeValue("M").codeDescription("Main").build())
            .noFixedAbode("Y")
            .notes("Some address notes")
            .addressNumber("32")
            .buildingName("HMPPS Digital Studio")
            .streetName("Scotland Street")
            .district("Sheffield City Centre")
            .townCity("Sheffield")
            .county("South Yorkshire")
            .postcode("S3 7BS")
            .telephoneNumber("0123456789")
            .createdDatetime(LocalDateTime.of(2021, 6, 10, 13, 0))
            .lastUpdatedDatetime(LocalDateTime.of(2021, 6, 10, 14, 0))
            .personalCircumstances(List.of(PersonalCircumstance.builder()
                .startDate(LocalDate.of(2010, 6, 11))
                .circumstanceSubType(CircumstanceSubType.builder()
                    .codeValue("A02")
                    .codeDescription("Approved Premises")
                    .build())
                .evidenced("Y")
                .build()))
            .addressAssessments(List.of(
                AddressAssessment.builder().assessmentDate(LocalDateTime.of(2010, 6, 10, 12, 0)).build(),
                AddressAssessment.builder().assessmentDate(LocalDateTime.of(2010, 6, 11, 12, 0)).build()
            ))
            .build();
    }

     static Offender anOffender() {
        return Offender.builder()
                .allowSMS("Y")
                .crn("crn123")
                .croNumber("cro123")
                .currentDisposal(1L)
                .currentHighestRiskColour("AMBER")
                .currentRemandStatus("ON_REMAND")
                .dateOfBirthDate(LocalDate.of(1970, 1, 1))
                .emailAddress("bill@sykes.com")
                .establishment('A')
                .ethnicity(StandardReference.builder().codeDescription("IC1").build())
                .exclusionMessage("exclusion message")
                .firstName("Bill")
                .gender(StandardReference.builder().codeDescription("M").build())
                .immigrationNumber("IM123")
                .immigrationStatus(StandardReference.builder().codeDescription("N/A").build())
                .institutionId(4L)
                .interpreterRequired("N")
                .language(StandardReference.builder().codeDescription("ENGLISH").build())
                .languageConcerns("None")
                .mobileNumber("0718118055")
                .nationality(StandardReference.builder().codeDescription("BRITISH").build())
                .mostRecentPrisonerNumber("PN123")
                .niNumber("NI1234567")
                .nomsNumber("NOMS1234")
                .offenderId(5L)
                .pendingTransfer(6L)
                .pncNumber("PNC1234")
                .previousSurname("Jones")
                .religion(StandardReference.builder().codeDescription("COFE").build())
                .restrictionMessage("Restriction message")
                .secondName("Arthur")
                .surname("Sykes")
                .telephoneNumber("018118055")
                .title(StandardReference.builder().codeDescription("Mr").build())
                .secondNationality(StandardReference.builder().codeDescription("EIRE").build())
                .sexualOrientation(StandardReference.builder().codeDescription("STR").build())
                .previousConvictionDate(LocalDate.of(2016, 1, 1))
                .prevConvictionDocumentName("CONV1234")
                .offenderAliases(Lists.newArrayList(OffenderAlias.builder().build()))
                .offenderAddresses(Lists.newArrayList(anOffenderAddress()))
                .partitionArea(PartitionArea.builder().area("Fulchester").build())
                .softDeleted(false)
                .currentHighestRiskColour("FUSCHIA")
                .currentDisposal(0L)
                .currentRestriction(0L)
                .currentExclusion(0L)
                .offenderManagers(Lists.newArrayList(OffenderManager.builder()
                        .activeFlag(1L)
                        .allocationDate(LocalDate.now())
                        .officer(Officer.builder().surname("Jones").build())
                        .probationArea(ProbationArea.builder().code("A").description("B").privateSector(1L).build())
                        .build()))
                .disabilities(Lists.newArrayList(Disability
                        .builder()
                        .softDeleted(0L)
                        .disabilityId(1L)
                        .startDate(LocalDate.now())
                        .disabilityType(StandardReference.builder().codeValue("SI").codeDescription("Speech Impairment").build())
                        .build()))
                .build();
    }
}
