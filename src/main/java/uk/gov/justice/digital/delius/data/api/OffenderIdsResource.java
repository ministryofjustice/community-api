package uk.gov.justice.digital.delius.data.api;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.hateoas.ResourceSupport;

import java.math.BigDecimal;
import java.util.List;

@Setter
@Getter
@Builder
public class OffenderIdsResource extends ResourceSupport {

    private final List<BigDecimal> offenderIds;

    public OffenderIdsResource(List<BigDecimal> offenderIds) {
        this.offenderIds = offenderIds;
    }
}
