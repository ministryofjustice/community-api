package uk.gov.justice.digital.delius.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.justice.digital.delius.config.FeatureSwitches;
import uk.gov.justice.digital.delius.controller.NotFoundException;
import uk.gov.justice.digital.delius.data.api.UpdateCourtDto;
import uk.gov.justice.digital.delius.jpa.standard.entity.StandardReference;
import uk.gov.justice.digital.delius.jpa.standard.repository.CourtRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;
import static uk.gov.justice.digital.delius.util.EntityHelper.aCourt;

@ExtendWith(MockitoExtension.class)
class CourtServiceTest {
    @Mock
    private CourtRepository courtRepository;
    @Mock
    private LookupSupplier lookupSupplier;

    private CourtService courtService;
    private final FeatureSwitches featureSwitches = new FeatureSwitches();

    @BeforeEach
    void setUp() {
        courtService = new CourtService(courtRepository, lookupSupplier, featureSwitches);
    }

    @Nested
    class GetCourt {
        @Test
        @DisplayName("will throw NotFound return when not found")
        void willThrowNotFound() {
            when(courtRepository.findByCode("SHEFCC")).thenReturn(List.of());

            assertThatThrownBy(() -> courtService.getCourt("SHEFCC")).isInstanceOf(NotFoundException.class);
        }

        @Test
        @DisplayName("Will return court even if it is inactive")
        void willReturnCourtEvenIfItIsInactive() {
            when(courtRepository.findByCode("SHEFCC")).thenReturn(List.of(
                aCourt("SHEFCC").toBuilder().selectable("N").build()));

            assertThat(courtService.getCourt("SHEFCC").getSelectable()).isFalse();
        }

        @Test
        @DisplayName("Will return court")
        void willReturnCourt() {
            when(courtRepository.findByCode("SHEFCC")).thenReturn(List.of(
                aCourt("SHEFCC").toBuilder().selectable("Y").build()));

            assertThat(courtService.getCourt("SHEFCC").getSelectable()).isTrue();
        }

        @Test
        @DisplayName("Will return active court if there are duplicates")
        void willReturnActiveCourtWhenThereAreDuplicates() {
            when(courtRepository.findByCode("SHEFCC")).thenReturn(List.of(
                aCourt("SHEFCC").toBuilder().selectable("Y").build(),
                aCourt("SHEFCC").toBuilder().selectable("N").build()));

            assertThat(courtService.getCourt("SHEFCC").getSelectable()).isTrue();
        }

        @Test
        @DisplayName("Will return any of the duplicates if they are all active")
        void willReturnAnyCourtWhenThereAreDuplicatesAllTheSame() {
            when(courtRepository.findByCode("SHEFCC")).thenReturn(List.of(
                aCourt("SHEFCC").toBuilder().selectable("Y").build(),
                aCourt("SHEFCC").toBuilder().selectable("Y").build()));

            assertThat(courtService.getCourt("SHEFCC").getSelectable()).isTrue();
        }
    }

    @Nested
    class UpdateCourt {
        @BeforeEach
        void setUp() {
            featureSwitches.getRegisters().setCourtCodeAllowedPattern(".*");
            when(lookupSupplier.courtTypeSupplier())
                .thenReturn((code) -> Optional.of(StandardReference
                    .builder()
                    .codeValue(code)
                    .codeDescription("Crown Court")
                    .standardReferenceListId(99L)
                    .build()));

            when(courtRepository.findByCode("SHEFCC")).thenReturn(List.of(
                aCourt("SHEFCC").toBuilder().selectable("Y").build()));
        }

        @Test
        @DisplayName("will lookup the court type")
        void willLookupTheCourtType() {
            final var updatedCourt = courtService.updateCourt("SHEFCC", UpdateCourtDto
                .builder()
                .courtTypeCode("CRN")
                .build());

            assertThat(updatedCourt.getCourtType().getCode()).isEqualTo("CRN");
            assertThat(updatedCourt.getCourtType().getDescription()).isEqualTo("Crown Court");
        }

        @Test
        @DisplayName("will update court name")
        void willUpdateCourtName() {
            final var newValue = "updated";

            assertThat(courtService
                .updateCourt("SHEFCC", UpdateCourtDto.builder().courtName(newValue).build())
                .getCourtName()).isEqualTo(newValue);
        }

        @Test
        @DisplayName("will update country")
        void willUpdateCountry() {
            final var newValue = "updated";

            assertThat(courtService
                .updateCourt("SHEFCC", UpdateCourtDto.builder().country(newValue).build())
                .getCountry()).isEqualTo(newValue);
        }

        @Test
        @DisplayName("will update county")
        void willUpdateCounty() {
            final var newValue = "updated";

            assertThat(courtService
                .updateCourt("SHEFCC", UpdateCourtDto.builder().county(newValue).build())
                .getCounty()).isEqualTo(newValue);
        }

        @Test
        @DisplayName("will update locality")
        void willUpdateLocality() {
            final var newValue = "updated";

            assertThat(courtService
                .updateCourt("SHEFCC", UpdateCourtDto.builder().locality(newValue).build())
                .getLocality()).isEqualTo(newValue);
        }

        @Test
        @DisplayName("will update street")
        void willUpdateStreet() {
            final var newValue = "updated";

            assertThat(courtService
                .updateCourt("SHEFCC", UpdateCourtDto.builder().street(newValue).build())
                .getStreet()).isEqualTo(newValue);
        }

        @Test
        @DisplayName("will update postcode")
        void willUpdatePostcode() {
            final var newValue = "updated";

            assertThat(courtService
                .updateCourt("SHEFCC", UpdateCourtDto.builder().postcode(newValue).build())
                .getPostcode()).isEqualTo(newValue);
        }

        @Test
        @DisplayName("will update town")
        void willUpdateTown() {
            final var newValue = "updated";

            assertThat(courtService
                .updateCourt("SHEFCC", UpdateCourtDto.builder().town(newValue).build())
                .getTown()).isEqualTo(newValue);
        }

        @Test
        @DisplayName("will update fax")
        void willUpdateFax() {
            final var newValue = "updated";

            assertThat(courtService
                .updateCourt("SHEFCC", UpdateCourtDto.builder().fax(newValue).build())
                .getFax()).isEqualTo(newValue);
        }

        @Test
        @DisplayName("will update telephoneNumber")
        void willUpdateTelephoneNumber() {
            final var newValue = "updated";

            assertThat(courtService
                .updateCourt("SHEFCC", UpdateCourtDto.builder().telephoneNumber(newValue).build())
                .getTelephoneNumber()).isEqualTo(newValue);
        }

        @Test
        @DisplayName("will update buildingName")
        void willUpdateBuildingName() {
            final var newValue = "updated";

            assertThat(courtService
                .updateCourt("SHEFCC", UpdateCourtDto.builder().buildingName(newValue).build())
                .getBuildingName()).isEqualTo(newValue);
        }

        @Test
        @DisplayName("will update selectable state")
        void willUpdateSelectableState() {
            assertThat(courtService
                .updateCourt("SHEFCC", UpdateCourtDto.builder().active(false).build())
                .getSelectable()).isFalse();
            assertThat(courtService
                .updateCourt("SHEFCC", UpdateCourtDto.builder().active(true).build())
                .getSelectable()).isTrue();
        }

        @Nested
        class FeatureSwitch {
            @BeforeEach
            void setUp() {
                featureSwitches.getRegisters().setCourtCodeAllowedPattern("[a-zA-Z]{2}XXXX");
                when(courtRepository.findByCode("SHXXXX")).thenReturn(List.of(
                    aCourt("SHXXXX")));
            }

            @Test
            @DisplayName("Will silently reject updates when code does not match regular expression")
            @MockitoSettings(strictness = Strictness.LENIENT)
            void willSilentlyRejectUpdatesWhenCodeDoesNotMatchRegularExpression() {
                assertThat(courtService
                    .updateCourt("SHEFCC", UpdateCourtDto.builder().buildingName("New building").build())
                    .getBuildingName()).isNotEqualTo("New building");

            }

            @Test
            @DisplayName("Will accept updates when code does match regular expression")
            @MockitoSettings(strictness = Strictness.LENIENT)
            void willAcceptUpdatesWhenCodeDoesMatchRegularExpression() {
                assertThat(courtService
                    .updateCourt("SHXXXX", UpdateCourtDto.builder().buildingName("New building").build())
                    .getBuildingName()).isEqualTo("New building");

            }
        }
    }
}
