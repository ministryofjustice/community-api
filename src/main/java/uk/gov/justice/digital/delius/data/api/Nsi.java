package uk.gov.justice.digital.delius.data.api;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Nsi {
    private KeyValue nsiType;
    private KeyValue nsiSubType;
    private Requirement requirement;
    private KeyValue nsiStatus;

}
