package uk.gov.justice.digital.delius.service;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.digital.delius.helpers.CurrentUserSupplier;
import uk.gov.justice.digital.delius.jpa.national.entity.User;
import uk.gov.justice.digital.delius.jpa.national.repository.UserRepository;
import uk.gov.justice.digital.delius.jpa.standard.entity.*;
import uk.gov.justice.digital.delius.jpa.standard.repository.*;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class LookupSupplierTest {
    private LookupSupplier lookupSupplier;

    @Mock
    private OffenceRepository offenceRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private StandardReferenceRepository standardReferenceRepository;
    @Mock
    private CourtRepository courtRepository;
    @Mock
    private ProbationAreaRepository probationAreaRepository;
    @Mock
    private TeamRepository teamRepository;
    @Mock
    private StaffRepository staffRepository;
    @Mock
    private TransferReasonRepository transferReasonRepository;
    @Mock
    private CurrentUserSupplier currentUserSupplier;

    @BeforeEach
    public void before() {
        lookupSupplier = new LookupSupplier(
                offenceRepository,
                userRepository,
                standardReferenceRepository,
                courtRepository,
                probationAreaRepository,
                teamRepository,
                staffRepository,
                transferReasonRepository,
                currentUserSupplier);
    }

    @Test
    public void offenceSupplierWillLookupByCode() {
        when(offenceRepository.findByCode(any())).thenReturn(Optional.of(Offence.builder().build()));
        lookupSupplier.offenceSupplier().apply("AA");

        verify(offenceRepository).findByCode("AA");
    }


    @Test
    public void userSupplierLooksUpUsernameFromUserProxyClaim() {
        when(userRepository.findByDistinguishedNameIgnoreCase(any())).thenReturn(Optional.of(User.builder().build()));
        when(currentUserSupplier.username()).thenReturn(Optional.of("some.user"));

        lookupSupplier.userSupplier().get();

        verify(userRepository).findByDistinguishedNameIgnoreCase("some.user");
    }

    @Test
    public void courtAppearanceOutcomeSupplierWillLookupByCode() {
        when(standardReferenceRepository.findByCodeAndCodeSetName(any(), any())).thenReturn(Optional.of(StandardReference.builder().build()));
        lookupSupplier.courtAppearanceOutcomeSupplier().apply("AA");

        verify(standardReferenceRepository).findByCodeAndCodeSetName("AA", "COURT APPEARANCE OUTCOME");
    }

    @Test
    public void transferReasonSupplierWillLookupByCode() {
        when(transferReasonRepository.findByCode(any())).thenReturn(Optional.of(TransferReason.builder().build()));
        lookupSupplier.transferReasonSupplier().apply("AA");

        verify(transferReasonRepository).findByCode("AA");
    }

    @Test
    public void orderAllocationReasonSupplierWillLookupByCode() {
        when(standardReferenceRepository.findByCodeAndCodeSetName(any(), any())).thenReturn(Optional.of(StandardReference.builder().build()));
        lookupSupplier.orderAllocationReasonSupplier().apply("AA");

        verify(standardReferenceRepository).findByCodeAndCodeSetName("AA", "ORDER ALLOCATION REASON");
    }

    @Test
    public void custodyKeyDateTypeSupplierWillLookupByCode() {
        lookupSupplier.custodyKeyDateTypeSupplier().apply("AA");

        verify(standardReferenceRepository).findByCodeAndCodeSetName("AA", "THROUGHCARE DATE TYPE");
    }

    @Test
    public void custodyKeyDateTypeSupplierWillReturnEmptyWhenNotFound() {
        when(standardReferenceRepository.findByCodeAndCodeSetName("AA", "THROUGHCARE DATE TYPE")).thenReturn(Optional.empty());

        val maybeCustodyKeyDateType = lookupSupplier.custodyKeyDateTypeSupplier().apply("AA");
        assertThat(maybeCustodyKeyDateType).isNotPresent();
    }

    @Test
    public void custodyKeyDateTypeSupplierWillReturnRefDataWhenFound() {
        when(standardReferenceRepository.findByCodeAndCodeSetName("AA", "THROUGHCARE DATE TYPE")).thenReturn(Optional.of(StandardReference.builder().build()));

        val maybeCustodyKeyDateType = lookupSupplier.custodyKeyDateTypeSupplier().apply("AA");
        assertThat(maybeCustodyKeyDateType).isPresent();
    }


    @Test
    public void courtSupplierWillLookupById() {
        when(courtRepository.findById(any(Long.class))).thenReturn(Optional.ofNullable(Court.builder().build()));
        lookupSupplier.courtSupplier().apply(1L);

        verify(courtRepository).findById(1L);
    }

    @Test
    public void probationAreaSupplierWillLookupById() {
        when(probationAreaRepository.findById(any(Long.class))).thenReturn(Optional.ofNullable(ProbationArea.builder().build()));
        lookupSupplier.probationAreaSupplier().apply(uk.gov.justice.digital.delius.data.api.OrderManager
                .builder()
                .probationAreaId(1L)
                .build());

        verify(probationAreaRepository).findById(1L);
    }

    @Test
    public void teamSupplierWillLookupById() {
        when(teamRepository.findById(any(Long.class))).thenReturn(Optional.ofNullable(Team.builder().build()));
        lookupSupplier.teamSupplier().apply(uk.gov.justice.digital.delius.data.api.OrderManager
                .builder()
                .teamId(1L)
                .build());

        verify(teamRepository).findById(1L);
    }

    @Test
    public void teamSupplierWillLookupUnallocatedTeamForAreaWhenNoTeamIdSupplied() {
        when(teamRepository.findByCode(any())).thenReturn(Optional.of(Team.builder().build()));
        when(probationAreaRepository.findById(1L)).thenReturn(Optional.ofNullable(ProbationArea.builder().code("ABC").build()));

        val team = lookupSupplier.teamSupplier().apply(uk.gov.justice.digital.delius.data.api.OrderManager
                .builder()
                .teamId(null)
                .probationAreaId(1L)
                .build());

        assertThat(team).isNotNull();

        verify(teamRepository).findByCode("ABCUAT");
    }

    @Test
    public void staffSupplierWillLookupById() {
        when(staffRepository.findById(any(Long.class))).thenReturn(Optional.ofNullable(Staff.builder().build()));
        lookupSupplier.staffSupplier().apply(uk.gov.justice.digital.delius.data.api.OrderManager
                .builder()
                .officerId(1L)
                .build());

        verify(staffRepository).findById(1L);
    }

    @Test
    public void staffSupplierWillLookupUnallocatedStaffForTeamWhenNoStaffIdSupplied() {
        when(teamRepository.findById(1L)).thenReturn(Optional.ofNullable(Team.builder().code("ABC").build()));
        when(staffRepository.findByOfficerCode(any(String.class))).thenReturn(Optional.of(Staff.builder().build()));

        val staff = lookupSupplier.staffSupplier().apply(uk.gov.justice.digital.delius.data.api.OrderManager
                .builder()
                .teamId(1L)
                .build());

        assertThat(staff).isNotNull();

        verify(staffRepository).findByOfficerCode("ABCU");
    }

    @Test
    public void staffSupplierWillLookupUnallocatedStaffForUnallocatedTeamWhenNoStaffIdOrTeamIdSupplied() {
        when(probationAreaRepository.findById(1L)).thenReturn(Optional.ofNullable(ProbationArea.builder().code("ABC").build()));
        when(teamRepository.findByCode("ABCUAT")).thenReturn(Optional.of(Team.builder().code("XYZ").build()));
        when(staffRepository.findByOfficerCode(any(String.class))).thenReturn(Optional.of(Staff.builder().build()));

        val staff = lookupSupplier.staffSupplier().apply(uk.gov.justice.digital.delius.data.api.OrderManager
                .builder()
                .probationAreaId(1L)
                .build());

        assertThat(staff).isNotNull();

        verify(teamRepository).findByCode("ABCUAT");
        verify(staffRepository).findByOfficerCode("XYZU");
    }

}
