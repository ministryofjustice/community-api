package uk.gov.justice.digital.delius.transformers;

import org.springframework.stereotype.Component;
import uk.gov.justice.digital.delius.data.api.Institution;
import uk.gov.justice.digital.delius.data.api.KeyValue;
import uk.gov.justice.digital.delius.jpa.standard.entity.RInstitution;
import uk.gov.justice.digital.delius.jpa.standard.entity.StandardReference;

import java.util.Optional;

import static uk.gov.justice.digital.delius.transformers.TypesTransformer.ynToBoolean;
import static uk.gov.justice.digital.delius.transformers.TypesTransformer.zeroOneToBoolean;

@Component
public class InstitutionTransformer {
    public Institution institutionOf(RInstitution institution) {
        return Optional.ofNullable(institution).map(inst -> Institution.builder()
                .code(inst.getCode())
                .description(inst.getDescription())
                .isEstablishment(ynToBoolean(inst.getEstablishment()))
                .establishmentType(establishmentTypeOf(inst.getEstablishmentType()))
                .institutionId(inst.getInstitutionId())
                .institutionName(inst.getInstitutionName())
                .isPrivate(zeroOneToBoolean(inst.getPrivateFlag()))
                .nomsPrisonInstitutionCode(inst.getNomisCdeCode())
                .build()).orElse(null);
    }

    private KeyValue establishmentTypeOf(StandardReference establishmentType) {
        return Optional.ofNullable(establishmentType).map(et -> KeyValue.builder()
                .code(et.getCodeValue())
                .description(et.getCodeDescription())
                .build())
                .orElse(null);
    }


}
