package uk.gov.justice.digital.delius.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.digital.delius.data.api.Human;
import uk.gov.justice.digital.delius.data.api.ManagedOffender;
import uk.gov.justice.digital.delius.data.api.StaffDetails;
import uk.gov.justice.digital.delius.jpa.standard.entity.ProbationArea;
import uk.gov.justice.digital.delius.jpa.standard.entity.Staff;
import uk.gov.justice.digital.delius.jpa.standard.entity.Team;
import uk.gov.justice.digital.delius.jpa.standard.repository.StaffHelperRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.StaffRepository;
import uk.gov.justice.digital.delius.ldap.repository.LdapRepository;
import uk.gov.justice.digital.delius.transformers.OffenderTransformer;
import uk.gov.justice.digital.delius.transformers.StaffTransformer;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@AllArgsConstructor
public class StaffService {

    private final StaffRepository staffRepository;
    private final LdapRepository ldapRepository;
    private final StaffHelperRepository staffHelperRepository;


    @Transactional(readOnly = true)
    public Optional<List<ManagedOffender>> getManagedOffendersByStaffCode(final String staffCode, final boolean current) {

        return staffRepository.findByOfficerCode(staffCode).map(
                staff -> OffenderTransformer.managedOffenderOf(staff, current)
        );
    }

    @Transactional(readOnly = true)
    public Optional<List<ManagedOffender>> getManagedOffendersByStaffIdentifier(final long staffIdentifier, final boolean current) {

        return staffRepository.findByStaffId(staffIdentifier).map(
                staff -> OffenderTransformer.managedOffenderOf(staff, current)
        );
    }

    @Transactional(readOnly = true)
    public Optional<StaffDetails> getStaffDetailsByStaffIdentifier(final long staffIdentifier) {
        return staffRepository
                .findByStaffId(staffIdentifier)
                .map(StaffTransformer::staffDetailsOf)
                .map(staffDetails ->
                        Optional.ofNullable(staffDetails.getUsername())
                                .map(username -> staffDetails
                                        .toBuilder()
                                        .email(ldapRepository.getEmail(username))
                                        .build())
                                .orElse(staffDetails));
    }

    @Transactional(readOnly = true)
    public Optional<StaffDetails> getStaffDetailsByUsername(final String username) {
        return staffRepository.findByUsername(username)
                .map(StaffTransformer::staffDetailsOf)
                .map(addEmailFromLdap());
    }

    @Transactional(readOnly = true)
    public List<StaffDetails> getStaffDetailsByUsernames(final Set<String> usernames) {
        final var capitalisedUsernames = usernames.stream().map(String::toUpperCase).collect(Collectors.toSet());

        return staffRepository.findByUsernames(capitalisedUsernames)
                .stream()
                .map(StaffTransformer::staffDetailsOf)
                .map(addEmailFromLdap())
                .collect(Collectors.toList());
    }

    @Transactional
    public Staff findOrCreateStaffInArea(final Human staff, final ProbationArea probationArea) {
        return staffRepository.findFirstBySurnameIgnoreCaseAndForenameIgnoreCaseAndProbationArea(staff.getSurname(), firstNameIn(staff.getForenames()), probationArea)
                .orElseGet(() -> createStaffInArea(staff.getSurname(), firstNameIn(staff.getForenames()), probationArea));
    }

    @Transactional
    public Optional<Staff> findByStaffId(final Long staffId) {
        return staffRepository.findByStaffId(staffId);
    }

    @Transactional
    public Staff createUnallocatedStaffInArea(final String teamPrefix, final ProbationArea probationArea) {
        return staffRepository.save(
            Staff
                .builder()
                .officerCode(String.format("%s%sU", probationArea.getCode(), teamPrefix))
                .forename("Unallocated")
                .surname("Staff")
                .privateSector(probationArea.getPrivateSector())
                .probationArea(probationArea)
                .teams(List.of())
                .build()
        );
    }


    Optional<Staff> findUnallocatedForTeam(final Team team) {
        return staffRepository.findByUnallocatedByTeam(team.getTeamId());
    }

    private Function<StaffDetails, StaffDetails> addEmailFromLdap() {
        return staffDetails ->
                staffDetails
                        .toBuilder()
                        .email(ldapRepository.getEmail(staffDetails.getUsername()))
                        .build();
    }

    private Staff createStaffInArea(final String surname, final String forename, final ProbationArea probationArea) {
        return staffRepository.save(
                Staff
                        .builder()
                        .officerCode(generateStaffCodeFor(probationArea))
                        .forename(forename)
                        .surname(surname)
                        .privateSector(probationArea.getPrivateSector())
                        .probationArea(probationArea)
                        .teams(List.of())
                        .build()
        );
    }

    private String generateStaffCodeFor(final ProbationArea probationArea) {
        return staffHelperRepository.getNextStaffCode(probationArea.getCode());
    }

    private String firstNameIn(final String forenames) {
        return Stream.of(forenames.split("[, ]")).findFirst().orElseThrow();
    }

}
