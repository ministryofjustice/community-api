package uk.gov.justice.digital.delius.data.api;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CustodialType {
    private String description;
    private String code;
}
