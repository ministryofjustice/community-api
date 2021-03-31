package uk.gov.justice.digital.delius.data.api;

import javax.validation.constraints.NotNull;

public class Origin {

    @NotNull
    private String service;

    @NotNull
    private String url;
}
