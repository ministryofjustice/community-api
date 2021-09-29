package uk.gov.justice.digital.delius.jpa.standard.entity;

import io.vavr.control.Either;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import uk.gov.justice.digital.delius.util.EntityHelper;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class ContactTest {

    @Nested
    class GettingRarComponent {
        @Test
        void whenRarThroughNsi() {
            final var contact = aRarContact();
            assertThat(contact.getRarComponent()).isEqualTo(Either.left(contact.getNsi()));
        }

        @Test
        void whenRarThroughRequirement() {
            final var contact = aRarContact().toBuilder()
                .requirement(EntityHelper.aRarRequirement())
                .nsi(null)
                .build();
            assertThat(contact.getRarComponent()).isEqualTo(Either.right(contact.getRequirement()));
        }

        @Test
        void whenNoRarComponent() {
            final var contact = aRarContact().toBuilder().nsi(null).build();
            assertThat(contact.getRarComponent()).isNull();
        }

        @Test
        void whenRarActivityFlagUnset() {
            final var contact = aRarContact().toBuilder().rarActivity(null).build();
            assertThat(contact.getRarComponent()).isNull();
        }

        @Test
        void whenNotAttended() {
            final var contact = aRarContact().toBuilder().attended("N").build();
            assertThat(contact.getRarComponent()).isNull();
        }

        @Test
        void whenNsiIsSoftDeleted() {
            final var contact = aRarContact();
            contact.getNsi().setSoftDeleted(true);
            assertThat(contact.getRarComponent()).isNull();
        }

        @Test
        void whenNsiHasSoftDeletedRarRequirement() {
            final var contact = aRarContact();
            contact.getNsi().getRqmnt().setSoftDeleted(true);
            assertThat(contact.getRarComponent()).isNull();
        }

        @Test
        void whenNsiHasNonRarRequirement() {
            final var contact = aRarContact();
            contact.getNsi().getRqmnt().getRequirementTypeMainCategory().setCode("NOT_RAR");
            assertThat(contact.getRarComponent()).isNull();
        }

        private Contact aRarContact() {
            return EntityHelper.aContact().toBuilder()
                .rarActivity("Y")
                .attended(null)
                .requirement(null)
                .nsi(EntityHelper.aNsi().toBuilder()
                    .rqmnt(EntityHelper.aRarRequirement())
                    .build())
                .build();
        }
    }
}
