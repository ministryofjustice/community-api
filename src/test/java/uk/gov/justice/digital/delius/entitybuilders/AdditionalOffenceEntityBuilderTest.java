package uk.gov.justice.digital.delius.entitybuilders;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.digital.delius.data.api.OffenceDetail;
import uk.gov.justice.digital.delius.jpa.national.entity.User;
import uk.gov.justice.digital.delius.jpa.standard.entity.Event;
import uk.gov.justice.digital.delius.jpa.standard.entity.Offence;
import uk.gov.justice.digital.delius.service.LookupSupplier;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AdditionalOffenceEntityBuilderTest {
    @Mock
    private LookupSupplier lookupSupplier;

    private AdditionalOffenceEntityBuilder additionalOffenceEntityBuilder;

    @BeforeEach
    public void setup() {
        additionalOffenceEntityBuilder = new AdditionalOffenceEntityBuilder(lookupSupplier);
        when(lookupSupplier.userSupplier()).thenReturn(() -> User.builder().userId(99L).build());
        when(lookupSupplier.offenceSupplier()).thenReturn((code) -> Offence.builder().offenceId(88L).build());
    }

    @Test
    public void eachOffenceIsCopied() {
        List<uk.gov.justice.digital.delius.data.api.Offence> offences = List.of(
                anOffence(),
                anOffence()
        );

        assertThat(additionalOffenceEntityBuilder.additionalOffencesOf(offences, anEvent())).hasSize(2);
    }

    @Test
    public void setsAuditFields() {
        when(lookupSupplier.userSupplier()).thenReturn(() -> User.builder().userId(99L).build());

        List<uk.gov.justice.digital.delius.data.api.Offence> offences = List.of(
                anOffence()
        );

        assertThat(additionalOffenceEntityBuilder.additionalOffencesOf(offences, anEvent()).getFirst().getCreatedByUserId()).isEqualTo(99L);
        assertThat(additionalOffenceEntityBuilder.additionalOffencesOf(offences, anEvent()).getFirst().getLastUpdatedUserId()).isEqualTo(99L);
        assertThat(additionalOffenceEntityBuilder.additionalOffencesOf(offences, anEvent()).getFirst().getCreatedDatetime()).isNotNull();
        assertThat(additionalOffenceEntityBuilder.additionalOffencesOf(offences, anEvent()).getFirst().getLastUpdatedDatetime()).isNotNull();
    }

    @Test
    public void setsSensibleDefaults() {
        List<uk.gov.justice.digital.delius.data.api.Offence> offences = List.of(
                anOffence()
        );

        assertThat(additionalOffenceEntityBuilder.additionalOffencesOf(offences, anEvent()).getFirst().getSoftDeleted()).isEqualTo(0L);
        assertThat(additionalOffenceEntityBuilder.additionalOffencesOf(offences, anEvent()).getFirst().getPartitionAreaId()).isEqualTo(0L);
        assertThat(additionalOffenceEntityBuilder.additionalOffencesOf(offences, anEvent()).getFirst().getRowVersion()).isEqualTo(1L);

    }

    @Test
    public void offenceDetailLookedUpFromCode() {
        when(lookupSupplier.offenceSupplier()).thenReturn(code -> Offence.builder().offenceId(88L).build());

        List<uk.gov.justice.digital.delius.data.api.Offence> offences = List.of(
                anOffence()
                        .toBuilder()
                        .detail(OffenceDetail
                                .builder()
                                .code("ABC")
                                .build())
                        .build()
        );

        assertThat(additionalOffenceEntityBuilder.additionalOffencesOf(offences, anEvent()).getFirst().getOffence().getOffenceId()).isEqualTo(88L);

    }
    private Event anEvent() {
        return Event.builder().build();
    }

    private uk.gov.justice.digital.delius.data.api.Offence anOffence() {
        return uk.gov.justice.digital.delius.data.api.Offence
                .builder()
                .detail(OffenceDetail
                        .builder()
                        .build())
                .build();
    }

}
