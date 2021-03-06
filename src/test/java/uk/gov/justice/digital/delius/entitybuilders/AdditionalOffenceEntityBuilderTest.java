package uk.gov.justice.digital.delius.entitybuilders;

import com.google.common.collect.ImmutableList;
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
        ImmutableList<uk.gov.justice.digital.delius.data.api.Offence> offences = ImmutableList.of(
                anOffence(),
                anOffence()
        );

        assertThat(additionalOffenceEntityBuilder.additionalOffencesOf(offences, anEvent())).hasSize(2);
    }

    @Test
    public void setsAuditFields() {
        when(lookupSupplier.userSupplier()).thenReturn(() -> User.builder().userId(99L).build());

        ImmutableList<uk.gov.justice.digital.delius.data.api.Offence> offences = ImmutableList.of(
                anOffence()
        );

        assertThat(additionalOffenceEntityBuilder.additionalOffencesOf(offences, anEvent()).get(0).getCreatedByUserId()).isEqualTo(99L);
        assertThat(additionalOffenceEntityBuilder.additionalOffencesOf(offences, anEvent()).get(0).getLastUpdatedUserId()).isEqualTo(99L);
        assertThat(additionalOffenceEntityBuilder.additionalOffencesOf(offences, anEvent()).get(0).getCreatedDatetime()).isNotNull();
        assertThat(additionalOffenceEntityBuilder.additionalOffencesOf(offences, anEvent()).get(0).getLastUpdatedDatetime()).isNotNull();
    }

    @Test
    public void setsSensibleDefaults() {
        ImmutableList<uk.gov.justice.digital.delius.data.api.Offence> offences = ImmutableList.of(
                anOffence()
        );

        assertThat(additionalOffenceEntityBuilder.additionalOffencesOf(offences, anEvent()).get(0).getSoftDeleted()).isEqualTo(0L);
        assertThat(additionalOffenceEntityBuilder.additionalOffencesOf(offences, anEvent()).get(0).getPartitionAreaId()).isEqualTo(0L);
        assertThat(additionalOffenceEntityBuilder.additionalOffencesOf(offences, anEvent()).get(0).getRowVersion()).isEqualTo(1L);

    }

    @Test
    public void offenceDetailLookedUpFromCode() {
        when(lookupSupplier.offenceSupplier()).thenReturn(code -> Offence.builder().offenceId(88L).build());

        ImmutableList<uk.gov.justice.digital.delius.data.api.Offence> offences = ImmutableList.of(
                anOffence()
                        .toBuilder()
                        .detail(OffenceDetail
                                .builder()
                                .code("ABC")
                                .build())
                        .build()
        );

        assertThat(additionalOffenceEntityBuilder.additionalOffencesOf(offences, anEvent()).get(0).getOffence().getOffenceId()).isEqualTo(88L);

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
