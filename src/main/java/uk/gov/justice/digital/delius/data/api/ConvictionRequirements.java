package uk.gov.justice.digital.delius.data.api;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class ConvictionRequirements {
    private List<Requirement> requirements;
}
