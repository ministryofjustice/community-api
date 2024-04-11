package uk.gov.justice.digital.delius.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.digital.delius.data.api.ContactableHuman;
import uk.gov.justice.digital.delius.data.api.StaffDetails;
import uk.gov.justice.digital.delius.jpa.standard.entity.Borough;
import uk.gov.justice.digital.delius.jpa.standard.entity.ProbationArea;
import uk.gov.justice.digital.delius.jpa.standard.entity.Staff;
import uk.gov.justice.digital.delius.jpa.standard.repository.BoroughRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.StaffHelperRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.StaffRepository;
import uk.gov.justice.digital.delius.ldap.repository.LdapRepository;
import uk.gov.justice.digital.delius.ldap.repository.entity.NDeliusUser;
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
    private final BoroughRepository boroughRepository;

    @Transactional(readOnly = true)
    public Optional<Long> getStaffIdByStaffCode(final String staffCode) {
        return staffRepository.findStaffIdByOfficerCode(staffCode);
    }

    @Transactional(readOnly = true)
    public Optional<StaffDetails> getStaffDetailsByStaffIdentifier(final long staffIdentifier) {
        return staffRepository
            .findByStaffId(staffIdentifier)
            .map(StaffTransformer::staffDetailsOf)
            .map(addFieldsFromLdap());
    }

    @Transactional(readOnly = true)
    public Optional<StaffDetails> getStaffDetailsByUsername(final String username) {
        return staffRepository.findByUsername(username)
            .map(StaffTransformer::staffDetailsOf)
            .map(addFieldsFromLdap());
    }

    @Transactional(readOnly = true)
    public Optional<StaffDetails> getStaffDetailsByStaffCode(final String staffCode) {
        return staffRepository.findByOfficerCode(staffCode)
            .map(StaffTransformer::staffDetailsOf)
            .map(addFieldsFromLdap());
    }

    @Transactional(readOnly = true)
    public List<StaffDetails> getStaffDetailsByUsernames(final Set<String> usernames) {
        final var capitalisedUsernames = usernames.stream().map(String::toUpperCase).collect(Collectors.toSet());

        return staffRepository.findByUsernames(capitalisedUsernames)
            .stream()
            .map(StaffTransformer::staffDetailsOf)
            .map(addFieldsFromLdap())
            .toList();
    }

    @Transactional(readOnly = true)
    public List<StaffDetails> getStaffDetailsByStaffCodes(final Set<String> staffCodes) {
        return staffRepository.findByOfficerCodeIn(staffCodes)
            .stream()
            .map(StaffTransformer::staffDetailsOf)
            .map(addFieldsFromLdap())
            .toList();
    }

    @Transactional
    public Staff findOrCreateStaffInArea(final ContactableHuman staff, final ProbationArea probationArea) {
        return staffRepository.findFirstBySurnameIgnoreCaseAndForenameIgnoreCaseAndProbationArea(staff.getSurname(), firstNameIn(staff.getForenames()), probationArea)
            .orElseGet(() -> createStaffInArea(staff.getSurname(), firstNameIn(staff.getForenames()), probationArea));
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

    public Optional<List<StaffDetails>> getProbationDeliveryUnitHeads(String boroughCode) {
        return boroughRepository.findActiveByCode(boroughCode)
            .map(Borough::getHeadsOfProbationDeliveryUnit)
            .map(list -> list.stream()
                .map(StaffTransformer::staffDetailsOf)
                .map(addFieldsFromLdap())
                .toList());
    }

    private Function<StaffDetails, StaffDetails> addFieldsFromLdap() {
        return staffDetails -> {
            var nDeliusUser = ldapRepository.getDeliusUserNoRoles(staffDetails.getUsername());
            return staffDetails
                .toBuilder()
                .email(nDeliusUser.map(NDeliusUser::getMail).orElse(null))
                .telephoneNumber(nDeliusUser.map(NDeliusUser::getTelephoneNumber).orElse(null))
                .build();
        };
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

    public List<StaffDetails> findStaffByTeam(Long teamId) {
        return staffRepository.findStaffByTeamId(teamId)
            .stream()
            .map(StaffTransformer::staffDetailsOnlyOf)
            .map(addFieldsFromLdap())
            .toList();
    }
}
