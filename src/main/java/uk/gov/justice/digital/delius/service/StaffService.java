package uk.gov.justice.digital.delius.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.digital.delius.data.api.Human;
import uk.gov.justice.digital.delius.data.api.ManagedOffender;
import uk.gov.justice.digital.delius.data.api.StaffDetails;
import uk.gov.justice.digital.delius.jpa.standard.entity.ProbationArea;
import uk.gov.justice.digital.delius.jpa.standard.entity.Staff;
import uk.gov.justice.digital.delius.jpa.standard.repository.StaffHelperRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.StaffRepository;
import uk.gov.justice.digital.delius.ldap.repository.LdapRepository;
import uk.gov.justice.digital.delius.transformers.OffenderTransformer;
import uk.gov.justice.digital.delius.transformers.StaffTransformer;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Service
@AllArgsConstructor
public class StaffService {

    private final StaffRepository staffRepository;
    private final LdapRepository ldapRepository;
    private final OffenderTransformer offenderTransformer;
    private final StaffTransformer staffTransformer;
    private final StaffHelperRepository staffHelperRepository;


    @Transactional(readOnly = true)
    public Optional<List<ManagedOffender>> getManagedOffendersByStaffCode(String staffCode, boolean current) {

        return staffRepository.findByOfficerCode(staffCode).map(
                staff -> offenderTransformer.managedOffenderOf(staff, current)
        );
    }

    @Transactional(readOnly = true)
    public Optional<StaffDetails> getStaffDetails(String staffCode) {
        return staffRepository
                .findByOfficerCode(staffCode)
                .map(staffTransformer::staffDetailsOf)
                .map(staffDetails ->
                        Optional.ofNullable(staffDetails.getUsername())
                                .map(username -> staffDetails
                                        .toBuilder()
                                        .email(ldapRepository.getEmail(username))
                                        .build())
                                .orElse(staffDetails));
    }

    @Transactional(readOnly = true)
    public Optional<StaffDetails> getStaffDetailsByUsername(String username) {
        return staffRepository.findByUsername(username)
                .map(staffTransformer::staffDetailsOf)
                .map(staffDetails ->
                        staffDetails
                                .toBuilder()
                                .email(ldapRepository.getEmail(username))
                                .build());
    }

    @Transactional
    public  Staff findOrCreateStaffInArea(Human staff, ProbationArea probationArea) {
        return staffRepository.findBySurnameAndForenameAndProbationArea(staff.getSurname(), firstNameIn(staff.getForenames()), probationArea)
                .orElseGet(() -> createStaffInArea(staff.getSurname(), firstNameIn(staff.getForenames()), probationArea));
    }

    @Transactional
    public Optional<Staff> findByOfficerCode(String officerCode) {
        return staffRepository.findByOfficerCode(officerCode);
    }

    private Staff createStaffInArea(String surname, String forename, ProbationArea probationArea) {
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

    private String generateStaffCodeFor(ProbationArea probationArea) {
        return staffHelperRepository.getNextStaffCode(probationArea.getCode());
    }

    private String firstNameIn(String forenames) {
        return Stream.of(forenames.split("[, ]")).findFirst().orElseThrow();
    }

}
