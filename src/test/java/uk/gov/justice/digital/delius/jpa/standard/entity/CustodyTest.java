package uk.gov.justice.digital.delius.jpa.standard.entity;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.digital.delius.util.EntityHelper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.justice.digital.delius.util.EntityHelper.aKeyDate;

@ExtendWith(MockitoExtension.class)
public class CustodyTest {

    private Custody custody;

    @BeforeEach
    void setUp() {
        custody = EntityHelper.aCustodyEvent().getDisposal().getCustody();
    }

    @Test
    public void findLatestRelease_noReleases_returnsEmpty() {
        custody.setReleases(List.of());

        assertThat(custody.findLatestRelease()).isEmpty();
    }

    @Test
    public void findLatestRelease_singleRelease_isReturned() {
        LocalDateTime now = LocalDateTime.now();
        Release release = Release.builder().actualReleaseDate(now).build();
        custody.setReleases(List.of(release));

        Optional<Release> actualRelease = custody.findLatestRelease();

        assertThat(actualRelease).isPresent();
        assertThat(actualRelease.get().getActualReleaseDate()).isEqualTo(now);
    }

    @Test
    public void findLatestRelease_multipleReleases_latestIsReturned() {
        LocalDateTime now = LocalDateTime.now();
        Release release1 = Release.builder().actualReleaseDate(now.minusDays(2)).build();
        Release release2 = Release.builder().actualReleaseDate(now.minusDays(1)).build();
        custody.setReleases(List.of(release1, release2));

        Optional<Release> actualRelease = custody.findLatestRelease();

        assertThat(actualRelease).isPresent();
        assertThat(actualRelease.get().getActualReleaseDate()).isEqualTo(release2.getActualReleaseDate());
    }

    @Test
    public void findLatestRelease_softDeleted_isIgnored() {
        Release release = Release.builder().softDeleted(1L).build();
        custody.setReleases(List.of(release));

        assertThat(custody.findLatestRelease()).isEmpty();
    }

    @Nested
    class HasReleaseLicenceExpired {
        @Test
        @DisplayName("licence not expired when key dates found")
        void licenceNotExpiredWhenNoKeyDatesDateFound() {
             final var custodyWithNoKeyDates = custody.toBuilder().keyDates(List.of()).build();

            assertThat(custodyWithNoKeyDates.hasReleaseLicenceExpired()).isFalse();
        }
        @Test
        @DisplayName("licence not expired when no LED within key dates")
        void licenceNotExpiredWhenNoLEDWithinKeyDates() {
            final var futureSentenceExpiryDate = aKeyDate("SED", "SentenceExpiryDate", LocalDate.now().plusMonths(12));
            final var custodyWithNoLED = custody.toBuilder().keyDates(List.of(futureSentenceExpiryDate)).build();

            assertThat(custodyWithNoLED.hasReleaseLicenceExpired()).isFalse();
        }

        @Test
        @DisplayName("licence not expired when future LED found")
        void licenceNotExpiredWhenFutureLEDFound() {
            final var pastSentenceExpiryDate = aKeyDate("SED", "SentenceExpiryDate", LocalDate.now().minusMonths(12));
            final var futureLicenceExpiryDate = aKeyDate("LED", "LicenceExpiryDate", LocalDate.now().plusDays(1));

            final var custodyWithFutureLED = custody.toBuilder().keyDates(List.of(pastSentenceExpiryDate, futureLicenceExpiryDate)).build();

            assertThat(custodyWithFutureLED.hasReleaseLicenceExpired()).isFalse();
        }

        @Test
        @DisplayName("licence has expired when LED date has past")
        void licenceHasExpiredWhenLEDDateHasPast() {
            final var futureSentenceExpiryDate = aKeyDate("SED", "SentenceExpiryDate", LocalDate.now().plusMonths(12));
            final var pastLicenceExpiryDate = aKeyDate("LED", "LicenceExpiryDate", LocalDate.now().minusDays(1));

            final var custodyWithExpiredLED = custody.toBuilder().keyDates(List.of(futureSentenceExpiryDate, pastLicenceExpiryDate)).build();

            assertThat(custodyWithExpiredLED.hasReleaseLicenceExpired()).isTrue();
        }
    }

}
