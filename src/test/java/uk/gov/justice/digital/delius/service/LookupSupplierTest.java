package uk.gov.justice.digital.delius.service;

import io.jsonwebtoken.Claims;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.justice.digital.delius.jpa.national.entity.User;
import uk.gov.justice.digital.delius.jpa.national.repository.UserRepository;
import uk.gov.justice.digital.delius.jpa.oracle.UserProxy;
import uk.gov.justice.digital.delius.jpa.standard.entity.*;
import uk.gov.justice.digital.delius.jpa.standard.repository.*;
import uk.gov.justice.digital.delius.jwt.Jwt;

import java.util.Optional;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
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
    private Claims jwtClaims;

    @Before
    public void before() {
        lookupSupplier = new LookupSupplier(
                offenceRepository,
                userRepository,
                standardReferenceRepository,
                courtRepository,
                probationAreaRepository,
                teamRepository,
                staffRepository,
                transferReasonRepository
        );

        when(offenceRepository.findByCode(any())).thenReturn(Optional.of(Offence.builder().build()));
        when(transferReasonRepository.findByCode(any())).thenReturn(Optional.of(TransferReason.builder().build()));
        when(userRepository.findByDistinguishedNameIgnoreCase(any())).thenReturn(Optional.of(User.builder().build()));
        when(standardReferenceRepository.findByCodeAndCodeSetName(any(), any())).thenReturn(Optional.of(StandardReference.builder().build()));
        when(courtRepository.findOne(any(Long.class))).thenReturn(Court.builder().build());
        when(probationAreaRepository.findOne(any(Long.class))).thenReturn(ProbationArea.builder().build());
        when(teamRepository.findOne(any(Long.class))).thenReturn(Team.builder().build());
        when(teamRepository.findByCode(any())).thenReturn(Optional.of(Team.builder().build()));
        when(staffRepository.findOne(any(Long.class))).thenReturn(Staff.builder().build());
        when(staffRepository.findByOfficerCode(any(String.class))).thenReturn(Optional.of(Staff.builder().build()));
    }

    @Test
    public void offenceSupplierWillLookupByCode() {
        lookupSupplier.offenceSupplier().apply("AA");

        verify(offenceRepository).findByCode("AA");
    }


    @Test
    public void userSupplierLooksUpUsernameFromUserProxyClaim() {
        when(jwtClaims.get(Jwt.UID)).thenReturn("some.user");
        UserProxy.threadLocalClaims.set(jwtClaims);

        lookupSupplier.userSupplier().get();

        verify(userRepository).findByDistinguishedNameIgnoreCase("some.user");
    }

    @Test
    public void courtAppearanceOutcomeSupplierWillLookupByCode() {
        lookupSupplier.courtAppearanceOutcomeSupplier().apply("AA");

        verify(standardReferenceRepository).findByCodeAndCodeSetName("AA", "COURT APPEARANCE OUTCOME");
    }

    @Test
    public void transferReasonSupplierWillLookupByCode() {
        lookupSupplier.transferReasonSupplier().apply("AA");

        verify(transferReasonRepository).findByCode("AA");
    }

    @Test
    public void orderAllocationReasonSupplierWillLookupByCode() {
        lookupSupplier.orderAllocationReasonSupplier().apply("AA");

        verify(standardReferenceRepository).findByCodeAndCodeSetName("AA", "ORDER ALLOCATION REASON");
    }

    @Test
    public void courtSupplierWillLookupById() {
        lookupSupplier.courtSupplier().apply(1L);

        verify(courtRepository).findOne(1L);
    }

    @Test
    public void probationAreaSupplierWillLookupById() {
        lookupSupplier.probationAreaSupplier().apply(uk.gov.justice.digital.delius.data.api.OrderManager
                .builder()
                .probationAreaId(1L)
                .build());

        verify(probationAreaRepository).findOne(1L);
    }

    @Test
    public void teamSupplierWillLookupById() {
        lookupSupplier.teamSupplier().apply(uk.gov.justice.digital.delius.data.api.OrderManager
                .builder()
                .teamId(1L)
                .build());

        verify(teamRepository).findOne(1L);
    }

    @Test
    public void teamSupplierWillLookupUnallocatedTeamForAreaWhenNoTeamIdSupplied() {
        when(probationAreaRepository.findOne(1L)).thenReturn(ProbationArea.builder().code("ABC").build());

        Team team = lookupSupplier.teamSupplier().apply(uk.gov.justice.digital.delius.data.api.OrderManager
                .builder()
                .teamId(null)
                .probationAreaId(1L)
                .build());

        assertThat(team).isNotNull();

        verify(teamRepository).findByCode("ABCUAT");
    }

    @Test
    public void staffSupplierWillLookupById() {
        lookupSupplier.staffSupplier().apply(uk.gov.justice.digital.delius.data.api.OrderManager
                .builder()
                .officerId(1L)
                .build());

        verify(staffRepository).findOne(1L);
    }

    @Test
    public void staffSupplierWillLookupUnallocatedStaffForTeamWhenNoStaffIdSupplied() {
        when(teamRepository.findOne(1L)).thenReturn(Team.builder().code("ABC").build());

        Staff staff = lookupSupplier.staffSupplier().apply(uk.gov.justice.digital.delius.data.api.OrderManager
                .builder()
                .teamId(1L)
                .build());

        assertThat(staff).isNotNull();

        verify(staffRepository).findByOfficerCode("ABCU");
    }

    @Test
    public void staffSupplierWillLookupUnallocatedStaffForUnallocatedTeamWhenNoStaffIdOrTeamIdSupplied() {
        when(probationAreaRepository.findOne(1L)).thenReturn(ProbationArea.builder().code("ABC").build());
        when(teamRepository.findByCode("ABCUAT")).thenReturn(Optional.of(Team.builder().code("XYZ").build()));

        Staff staff = lookupSupplier.staffSupplier().apply(uk.gov.justice.digital.delius.data.api.OrderManager
                .builder()
                .probationAreaId(1L)
                .build());

        assertThat(staff).isNotNull();

        verify(teamRepository).findByCode("ABCUAT");
        verify(staffRepository).findByOfficerCode("XYZU");
    }

}