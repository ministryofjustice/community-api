package uk.gov.justice.digital.delius.data.api.deliusapi;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NsiManagerDto {
    private Long id;
    private String staff;
    private String team;
    private String provider;
}
