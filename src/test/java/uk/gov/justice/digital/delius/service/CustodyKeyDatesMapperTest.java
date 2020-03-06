package uk.gov.justice.digital.delius.service;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.justice.digital.delius.data.api.ReplaceCustodyKeyDates;

import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.justice.digital.delius.service.CustodyKeyDatesMapper.*;

class CustodyKeyDatesMapperTest {

    @Test
    void custodyManagedKeyDatesWillCollectAllCodes() {
        assertThat(custodyManagedKeyDates())
                .containsExactlyInAnyOrder("LED", "ACR", "PED", "SED", "EXP", "HDE", "PSSED");
    }


    @Nested
    class MissingKeyDateTypesCodes {
        @Test
        void willReportAllIfAllMissing() {
            assertThat(missingKeyDateTypesCodes(ReplaceCustodyKeyDates.builder().build()))
                    .containsExactlyInAnyOrder("LED", "ACR", "PED", "SED", "EXP", "HDE", "PSSED");
        }

        @Test
        void willReportNoneIfNoneMissing() {
            assertThat(missingKeyDateTypesCodes(ReplaceCustodyKeyDates.builder()
                    .postSentenceSupervisionEndDate(LocalDate.now())
                    .expectedReleaseDate(LocalDate.now())
                    .sentenceExpiryDate(LocalDate.now())
                    .paroleEligibilityDate(LocalDate.now())
                    .hdcEligibilityDate(LocalDate.now())
                    .licenceExpiryDate(LocalDate.now())
                    .conditionalReleaseDate(LocalDate.now())
                    .build()))
                    .isEmpty();
        }

        @Test
        void willReturnLEDIfLicenceExpiryDateMissing() {
            assertThat(missingKeyDateTypesCodes(ReplaceCustodyKeyDates.builder()
                    .postSentenceSupervisionEndDate(LocalDate.now())
                    .expectedReleaseDate(LocalDate.now())
                    .sentenceExpiryDate(LocalDate.now())
                    .paroleEligibilityDate(LocalDate.now())
                    .hdcEligibilityDate(LocalDate.now())
                    .conditionalReleaseDate(LocalDate.now())
                    .build()))
                    .containsExactly("LED");
        }

        @Test
        void willReturnACRIfConditionalReleaseDateMissing() {
            assertThat(missingKeyDateTypesCodes(ReplaceCustodyKeyDates.builder()
                    .postSentenceSupervisionEndDate(LocalDate.now())
                    .expectedReleaseDate(LocalDate.now())
                    .sentenceExpiryDate(LocalDate.now())
                    .paroleEligibilityDate(LocalDate.now())
                    .hdcEligibilityDate(LocalDate.now())
                    .licenceExpiryDate(LocalDate.now())
                    .build()))
                    .containsExactly("ACR");
        }

        @Test
        void willReturnPEDIfParoleEligibilityDateMissing() {
            assertThat(missingKeyDateTypesCodes(ReplaceCustodyKeyDates.builder()
                    .postSentenceSupervisionEndDate(LocalDate.now())
                    .expectedReleaseDate(LocalDate.now())
                    .sentenceExpiryDate(LocalDate.now())
                    .hdcEligibilityDate(LocalDate.now())
                    .licenceExpiryDate(LocalDate.now())
                    .conditionalReleaseDate(LocalDate.now())
                    .build()))
                    .containsExactly("PED");
        }

        @Test
        void willReturnSEDIfExpectedReleaseDateMissing() {
            assertThat(missingKeyDateTypesCodes(ReplaceCustodyKeyDates.builder()
                    .postSentenceSupervisionEndDate(LocalDate.now())
                    .sentenceExpiryDate(LocalDate.now())
                    .hdcEligibilityDate(LocalDate.now())
                    .paroleEligibilityDate(LocalDate.now())
                    .licenceExpiryDate(LocalDate.now())
                    .conditionalReleaseDate(LocalDate.now())
                    .build()))
                    .containsExactly("EXP");
        }

        @Test
        void willReturnHDEIfHdcEligibilityDateMissing() {
            assertThat(missingKeyDateTypesCodes(ReplaceCustodyKeyDates.builder()
                    .postSentenceSupervisionEndDate(LocalDate.now())
                    .expectedReleaseDate(LocalDate.now())
                    .sentenceExpiryDate(LocalDate.now())
                    .paroleEligibilityDate(LocalDate.now())
                    .licenceExpiryDate(LocalDate.now())
                    .conditionalReleaseDate(LocalDate.now())
                    .build()))
                    .containsExactly("HDE");
        }

        @Test
        void willReturnSEDIfSentenceExpiryDateMissing() {
            assertThat(missingKeyDateTypesCodes(ReplaceCustodyKeyDates.builder()
                    .postSentenceSupervisionEndDate(LocalDate.now())
                    .expectedReleaseDate(LocalDate.now())
                    .hdcEligibilityDate(LocalDate.now())
                    .paroleEligibilityDate(LocalDate.now())
                    .licenceExpiryDate(LocalDate.now())
                    .conditionalReleaseDate(LocalDate.now())
                    .build()))
                    .containsExactly("SED");
        }

        @Test
        void willReturnPSSEDIfPostSentenceSupervisionEndDateMissing() {
            assertThat(missingKeyDateTypesCodes(ReplaceCustodyKeyDates.builder()
                    .expectedReleaseDate(LocalDate.now())
                    .sentenceExpiryDate(LocalDate.now())
                    .hdcEligibilityDate(LocalDate.now())
                    .paroleEligibilityDate(LocalDate.now())
                    .licenceExpiryDate(LocalDate.now())
                    .conditionalReleaseDate(LocalDate.now())
                    .build()))
                    .containsExactly("PSSED");
        }


    }

    @Nested
    class KeyDatesOf {
        @Test
        void willReturnNothingIfNoneSet() {
            assertThat(keyDatesOf(ReplaceCustodyKeyDates.builder().build())).isEmpty();
        }

        @Test
        void willReturnAllIfAllSet() {
            assertThat(keyDatesOf(ReplaceCustodyKeyDates.builder()
                    .postSentenceSupervisionEndDate(LocalDate.now())
                    .expectedReleaseDate(LocalDate.now())
                    .sentenceExpiryDate(LocalDate.now())
                    .paroleEligibilityDate(LocalDate.now())
                    .hdcEligibilityDate(LocalDate.now())
                    .licenceExpiryDate(LocalDate.now())
                    .conditionalReleaseDate(LocalDate.now())
                    .build())).containsOnlyKeys("LED", "ACR", "PED", "SED", "EXP", "HDE", "PSSED");
        }

        @Test
        void willReturnKeysAndDates() {
            assertThat(keyDatesOf(ReplaceCustodyKeyDates.builder()
                    .postSentenceSupervisionEndDate(LocalDate.of(2030, 1, 1))
                    .expectedReleaseDate(LocalDate.of(2030, 1, 2))
                    .build())).containsOnly(
                        Map.entry("PSSED", LocalDate.of(2030, 1, 1)),
                        Map.entry("EXP", LocalDate.of(2030, 1, 2)));
        }

    }
}