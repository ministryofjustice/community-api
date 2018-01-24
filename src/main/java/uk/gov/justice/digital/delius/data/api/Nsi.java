package uk.gov.justice.digital.delius.data.api;

import lombok.Builder;
import lombok.Data;

import java.util.Optional;

@Data
@Builder
public class Nsi {

    private Optional<KeyValue> nsiType;
    private Optional<KeyValue> nsiSubType;
    private Optional<Requirement> requirement;

}
