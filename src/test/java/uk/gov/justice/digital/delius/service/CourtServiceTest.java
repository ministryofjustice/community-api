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
import uk.gov.justice.digital.delius.data.api.NewCourtDto;
import uk.gov.justice.digital.delius.data.api.UpdateCourtDto;
import uk.gov.justice.digital.delius.jpa.standard.entity.Court;
import uk.gov.justice.digital.delius.jpa.standard.entity.ProbationArea;
import uk.gov.justice.digital.delius.jpa.standard.entity.StandardReference;
import uk.gov.justice.digital.delius.jpa.standard.repository.CourtRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.digital.delius.util.EntityHelper.aCourt;

@ExtendWith(MockitoExtension.class)
class CourtServiceTest {
    private final FeatureSwitches featureSwitches = new FeatureSwitches();
    @Mock
    private CourtRepository courtRepository;
    @Mock
    private LookupSupplier lookupSupplier;
    private CourtService courtService;

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
    @MockitoSettings(strictness = Strictness.LENIENT)
    class InsertCourt {
        @BeforeEach
        void setUp() {
            featureSwitches.getRegisters().setCourtCodeAllowedPattern(".*");
            when(lookupSupplier.courtTypeByCode(any()))
                .thenReturn(Optional.of(StandardReference
                    .builder()
                    .codeValue("CRN")
                    .codeDescription("Crown Court")
                    .standardReferenceListId(99L)
                    .build()));

            when(lookupSupplier.probationAreaByCode(any()))
                .thenReturn(Optional.of(ProbationArea
                    .builder()
                    .code("N53")
                    .description("NPS North West")
                    .probationAreaId(99L)
                    .build()));

            when(courtRepository.findByCode("SHEFCC")).thenReturn(List.of(
                aCourt("SHEFCC").toBuilder().selectable("Y").build()));
            when(courtRepository.findByCode("XXXXAA")).thenReturn(List.of());
        }

        @Test
        @DisplayName("will return data error if court already exists")
        void willReturnDataErrorIfCourtAlreadyExists() {
            assertThat(courtService.createNewCourt(newCourt("SHEFCC")).getLeft().courtCode()).isEqualTo("SHEFCC");
        }

        @Test
        @DisplayName("will look up court type")
        void willLookUpCourtType() {
            final var newlyCreatedCourt = courtService
                .createNewCourt(newCourt("XXXXAA", "CRN", "N53", true))
                .getOrElseThrow(() -> new AssertionError("Should have created a court"));

            assertThat(newlyCreatedCourt.getCourtType().getCode()).isEqualTo("CRN");
            assertThat(newlyCreatedCourt.getCourtType().getDescription()).isEqualTo("Crown Court");
        }

        @Test
        @DisplayName("will look up probation area")
        void willLookUpProbationArea() {
            final var newlyCreatedCourt = courtService
                .createNewCourt(newCourt("XXXXAA", "CRN", "N53", true))
                .getOrElseThrow(() -> new AssertionError("Should have created a court"));

            assertThat(newlyCreatedCourt.getProbationArea().getCode()).isEqualTo("N53");
            assertThat(newlyCreatedCourt.getProbationArea().getDescription()).isEqualTo("NPS North West");
        }

        @Test
        @DisplayName("will translate active flag to selectable flag")
        void willTranslateActiveFlagToSelectable() {
            assertThat(courtService
                .createNewCourt(newCourt("XXXXAA", "CRN", "N53", true)).get().getSelectable()).isTrue();

            assertThat(courtService
                .createNewCourt(newCourt("XXXXAA", "CRN", "N53", false)).get().getSelectable()).isFalse();
        }

        @Test
        @DisplayName("Will save all data")
        void willSaveAllData() {
            final var newlyCreatedCourt = courtService
                .createNewCourt(new NewCourtDto("XXXXAA",
                    "CRN",
                    true,
                    "Sheffield new court",
                    "0114 555 1234",
                    "0114 555 6666",
                    "Crown Square",
                    "High Street",
                    "Town Centre",
                    "Sheffield",
                    "South Yorkshire",
                    "S1 2BJ",
                    "England",
                    "N53"))
                .getOrElseThrow(() -> new AssertionError("Should have created a court"));


            assertThat(newlyCreatedCourt.getCode()).isEqualTo("XXXXAA");
            assertThat(newlyCreatedCourt.getCourtName()).isEqualTo("Sheffield new court");
            assertThat(newlyCreatedCourt.getTelephoneNumber()).isEqualTo("0114 555 1234");
            assertThat(newlyCreatedCourt.getFax()).isEqualTo("0114 555 6666");
            assertThat(newlyCreatedCourt.getBuildingName()).isEqualTo("Crown Square");
            assertThat(newlyCreatedCourt.getStreet()).isEqualTo("High Street");
            assertThat(newlyCreatedCourt.getLocality()).isEqualTo("Town Centre");
            assertThat(newlyCreatedCourt.getTown()).isEqualTo("Sheffield");
            assertThat(newlyCreatedCourt.getCounty()).isEqualTo("South Yorkshire");
            assertThat(newlyCreatedCourt.getPostcode()).isEqualTo("S1 2BJ");
            assertThat(newlyCreatedCourt.getCountry()).isEqualTo("England");
        }

        @Test
        @DisplayName("will persist new court when court code matches allowed pattern")
        void willPersistNewCourtWhenCourtCodeMatchesAllowedPattern() {
            featureSwitches.getRegisters().setCourtCodeAllowedPattern("XXXX[A-Z]{2}");
            assertThat(courtService.createNewCourt(newCourt("XXXXAA")).isRight()).isTrue();
            verify(courtRepository).save(any());
        }

        @Test
        @DisplayName("will not persist new court when court code does not match allowed pattern")
        void willNotPersistNewCourtWhenCourtCodeDoesNotMatchAllowedPattern() {
            featureSwitches.getRegisters().setCourtCodeAllowedPattern("XXXX[A-Z]{2}");
            assertThat(courtService.createNewCourt(newCourt("AAAAXX")).isRight()).isTrue();
            verify(courtRepository, never()).save(any());
        }

        private NewCourtDto newCourt(String code) {
            return newCourt(code, "CRN", "N53", true);
        }

        @SuppressWarnings("SameParameterValue")
        private NewCourtDto newCourt(String code, String courtTypeCode, String probationArea, boolean active) {
            return new NewCourtDto(code, courtTypeCode, active, "Sheffield new court", null, null, "Crown Square", "High Street", "Town Centre", "Sheffield", "South Yorkshire", "S1 2BJ", "England", probationArea);
        }

    }

    @Nested
    class UpdateCourt {
        @BeforeEach
        void setUp() {
            featureSwitches.getRegisters().setCourtCodeAllowedPattern(".*");
            when(lookupSupplier.courtTypeByCode(any()))
                .thenReturn(Optional.of(StandardReference
                    .builder()
                    .codeValue("CRN")
                    .codeDescription("Crown Court")
                    .standardReferenceListId(99L)
                    .build()));

            when(courtRepository.findByCode("SHEFCC")).thenReturn(List.of(
                aCourt("SHEFCC").toBuilder().selectable("Y").build()));
        }

        @Test
        @DisplayName("will return error if court not found")
        @MockitoSettings(strictness = Strictness.LENIENT)
        void willReturnErrorIfCourtNotFound() {
          assertThat(courtService.updateCourt("ANCDEF", UpdateCourtDto
              .builder()
              .courtTypeCode("CRN")
              .build()).isLeft()).isTrue();  // left is the error value
        }

        @Test
        @DisplayName("will lookup the court type")
        void willLookupTheCourtType() {
            final var updatedCourt = courtService.updateCourt("SHEFCC", UpdateCourtDto
                .builder()
                .courtTypeCode("CRN")
                .build()).get();

            assertThat(updatedCourt.getCourtType().getCode()).isEqualTo("CRN");
            assertThat(updatedCourt.getCourtType().getDescription()).isEqualTo("Crown Court");
        }

        @Test
        @DisplayName("will update court name")
        void willUpdateCourtName() {
            final var newValue = "updated";

            assertThat(courtService
                .updateCourt("SHEFCC", UpdateCourtDto.builder().courtName(newValue).build()).get()
                .getCourtName()).isEqualTo(newValue);
        }

        @Test
        @DisplayName("will update country")
        void willUpdateCountry() {
            final var newValue = "updated";

            assertThat(courtService
                .updateCourt("SHEFCC", UpdateCourtDto.builder().country(newValue).build()).get()
                .getCountry()).isEqualTo(newValue);
        }

        @Test
        @DisplayName("will update county")
        void willUpdateCounty() {
            final var newValue = "updated";

            assertThat(courtService
                .updateCourt("SHEFCC", UpdateCourtDto.builder().county(newValue).build()).get()
                .getCounty()).isEqualTo(newValue);
        }

        @Test
        @DisplayName("will update locality")
        void willUpdateLocality() {
            final var newValue = "updated";

            assertThat(courtService
                .updateCourt("SHEFCC", UpdateCourtDto.builder().locality(newValue).build()).get()
                .getLocality()).isEqualTo(newValue);
        }

        @Test
        @DisplayName("will update street")
        void willUpdateStreet() {
            final var newValue = "updated";

            assertThat(courtService
                .updateCourt("SHEFCC", UpdateCourtDto.builder().street(newValue).build()).get()
                .getStreet()).isEqualTo(newValue);
        }

        @Test
        @DisplayName("will update postcode")
        void willUpdatePostcode() {
            final var newValue = "updated";

            assertThat(courtService
                .updateCourt("SHEFCC", UpdateCourtDto.builder().postcode(newValue).build()).get()
                .getPostcode()).isEqualTo(newValue);
        }

        @Test
        @DisplayName("will update town")
        void willUpdateTown() {
            final var newValue = "updated";

            assertThat(courtService
                .updateCourt("SHEFCC", UpdateCourtDto.builder().town(newValue).build()).get()
                .getTown()).isEqualTo(newValue);
        }

        @Test
        @DisplayName("will update fax")
        void willUpdateFax() {
            final var newValue = "updated";

            assertThat(courtService
                .updateCourt("SHEFCC", UpdateCourtDto.builder().fax(newValue).build()).get()
                .getFax()).isEqualTo(newValue);
        }

        @Test
        @DisplayName("will update telephoneNumber")
        void willUpdateTelephoneNumber() {
            final var newValue = "updated";

            assertThat(courtService
                .updateCourt("SHEFCC", UpdateCourtDto.builder().telephoneNumber(newValue).build()).get()
                .getTelephoneNumber()).isEqualTo(newValue);
        }

        @Test
        @DisplayName("will update buildingName")
        void willUpdateBuildingName() {
            final var newValue = "updated";

            assertThat(courtService
                .updateCourt("SHEFCC", UpdateCourtDto.builder().buildingName(newValue).build()).get()
                .getBuildingName()).isEqualTo(newValue);
        }

        @Test
        @DisplayName("will update selectable state")
        void willUpdateSelectableState() {
            assertThat(courtService
                .updateCourt("SHEFCC", UpdateCourtDto.builder().active(false).build()).get()
                .getSelectable()).isFalse();
            assertThat(courtService
                .updateCourt("SHEFCC", UpdateCourtDto.builder().active(true).build()).get()
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
                    .updateCourt("SHEFCC", UpdateCourtDto.builder().buildingName("New building").build()).get()
                    .getBuildingName()).isNotEqualTo("New building");

            }

            @Test
            @DisplayName("Will accept updates when code does match regular expression")
            @MockitoSettings(strictness = Strictness.LENIENT)
            void willAcceptUpdatesWhenCodeDoesMatchRegularExpression() {
                assertThat(courtService
                    .updateCourt("SHXXXX", UpdateCourtDto.builder().buildingName("New building").build()).get()
                    .getBuildingName()).isEqualTo("New building");

            }
        }
    }

    @Nested
    class GetAll {
        @BeforeEach
        void setUp() {
            final var activeCrownCourt = aCourt("SHEFCC").toBuilder().courtId(1L).selectable("Y").build();
            final var inactiveCrownCourt = aCourt("SHEFCC").toBuilder().courtId(2L).selectable("N").build();
            final Court magistratesCourt = aCourt("SHEFMC").toBuilder().courtId(3L).selectable("N").build();

            when(courtRepository.findByCode("SHEFCC")).thenReturn(List.of(
                activeCrownCourt,
                inactiveCrownCourt));

            when(courtRepository.findByCode("SHEFMC")).thenReturn(List.of(
                magistratesCourt));

            when(courtRepository.findAll()).thenReturn(List.of(
                activeCrownCourt,
                inactiveCrownCourt,
                magistratesCourt));
        }

        @Test
        @DisplayName("will filter out duplicates")
        void willFilterOutDuplicates() {
            final var allUniqueCourts = courtService.getCourts();

            assertThat(allUniqueCourts)
                .extracting(uk.gov.justice.digital.delius.data.api.Court::getCourtId)
                .containsExactly(1L, 3L);
        }
    }
}
