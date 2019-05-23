package uk.gov.justice.digital.delius.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.digital.delius.data.api.ManagedOffender;
import uk.gov.justice.digital.delius.jpa.standard.repository.StaffRepository;
import uk.gov.justice.digital.delius.transformers.OffenderTransformer;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class StaffService {

    private final StaffRepository staffRepository;
    private final OffenderTransformer offenderTransformer;

    @Autowired
    public StaffService(StaffRepository staffRepository,
                        OffenderTransformer offenderTransformer) {
        this.offenderTransformer = offenderTransformer;
        this.staffRepository = staffRepository;
    }

    @Transactional(readOnly = true)
    public Optional<List<ManagedOffender>> getManagedOffendersByStaffCode(String staffCode, String current) {

        return staffRepository.findByOfficerCode(staffCode).map(
                staff -> offenderTransformer.managedOffenderOf(staff, Boolean.getBoolean(current))
        );
    }
}
