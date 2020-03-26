package uk.gov.justice.digital.delius.controller.secure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.digital.delius.data.api.ConvictionRequirements;
import uk.gov.justice.digital.delius.service.RequirementService;

@ExtendWith(MockitoExtension.class)
class RequirementsResourceTest {

    @Mock
    private RequirementService service;

    @InjectMocks
    private RequirementsResource requirementsResource;

    @DisplayName("Requirements retrieved by the service are returned as-is")
    @Test
    void getRequirementsByConvictionId() {

        final ConvictionRequirements convictionRequirements = ConvictionRequirements.builder().build();
        when(service.getRequirementsByConvictionId("CRN", 1234L)).thenReturn(convictionRequirements);

        assertThat(requirementsResource.getRequirementsByConvictionId("CRN", 1234L)).isSameAs(convictionRequirements);

        verify(service).getRequirementsByConvictionId("CRN", 1234L);
        verifyNoMoreInteractions(service);
    }
}
