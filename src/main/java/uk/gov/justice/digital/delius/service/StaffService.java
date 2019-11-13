package uk.gov.justice.digital.delius.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.digital.delius.data.api.ManagedOffender;
import uk.gov.justice.digital.delius.data.api.StaffDetails;
import uk.gov.justice.digital.delius.jpa.standard.repository.StaffRepository;
import uk.gov.justice.digital.delius.ldap.repository.LdapRepository;
import uk.gov.justice.digital.delius.transformers.OffenderTransformer;
import uk.gov.justice.digital.delius.transformers.StaffTransformer;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class StaffService {

    private final StaffRepository staffRepository;
    private final LdapRepository ldapRepository;
    private final OffenderTransformer offenderTransformer;
    private final StaffTransformer staffTransformer;


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
}
