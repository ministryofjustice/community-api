package uk.gov.justice.digital.delius.transformers;

import org.springframework.stereotype.Component;
import uk.gov.justice.digital.delius.data.api.KeyValue;
import uk.gov.justice.digital.delius.data.api.PersonalCircumstance;
import uk.gov.justice.digital.delius.jpa.standard.entity.CircumstanceSubType;
import uk.gov.justice.digital.delius.jpa.standard.entity.CircumstanceType;
import uk.gov.justice.digital.delius.jpa.standard.entity.ProbationArea;

import java.util.Optional;

import static uk.gov.justice.digital.delius.transformers.TypesTransformer.ynToBoolean;

@Component
public class PersonalCircumstanceTransformer {
    public PersonalCircumstance personalCircumstanceOf(uk.gov.justice.digital.delius.jpa.standard.entity.PersonalCircumstance personalCircumstance) {
        return PersonalCircumstance.builder()
                .type(circumstanceTypeOf(personalCircumstance.getCircumstanceType()))
                .subType(circumstanceSubTypeOf(personalCircumstance.getCircumstanceSubType()))
                .personalCircumstanceId(personalCircumstance.getPersonalCircumstanceId())
                .probationArea(Optional.ofNullable(personalCircumstance.getProbationArea()).map(this::probationAreaOf).orElse(null))
                .endDate(personalCircumstance.getEndDate())
                .startDate(personalCircumstance.getStartDate())
                .offenderId(personalCircumstance.getOffenderId())
                .notes(personalCircumstance.getNotes())
                .evidenced(ynToBoolean(personalCircumstance.getEvidenced()))
            .build();
    }

    private KeyValue probationAreaOf(ProbationArea probationArea) {
        return KeyValue.builder()
                .code(probationArea.getCode())
                .description(probationArea.getDescription())
                .build();
    }

    private KeyValue circumstanceTypeOf(CircumstanceType circumstanceType) {
        return KeyValue.builder()
                .code(circumstanceType.getCodeValue())
                .description(circumstanceType.getCodeDescription())
                .build();
    }

    private KeyValue circumstanceSubTypeOf(CircumstanceSubType circumstanceSubType) {
        return KeyValue.builder()
                .code(circumstanceSubType.getCodeValue())
                .description(circumstanceSubType.getCodeDescription())
                .build();
    }


}
