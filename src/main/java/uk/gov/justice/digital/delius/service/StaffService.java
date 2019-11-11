package uk.gov.justice.digital.delius.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.digital.delius.data.api.Human;
import uk.gov.justice.digital.delius.data.api.ManagedOffender;
import uk.gov.justice.digital.delius.data.api.StaffDetails;
import uk.gov.justice.digital.delius.data.api.Team;
import uk.gov.justice.digital.delius.jpa.standard.repository.StaffRepository;
import uk.gov.justice.digital.delius.transformers.OffenderTransformer;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class StaffService {

    private final StaffRepository staffRepository;
    private final OffenderTransformer offenderTransformer;

    @Transactional(readOnly = true)
    public Optional<List<ManagedOffender>> getManagedOffendersByStaffCode(String staffCode, boolean current) {

        return staffRepository.findByOfficerCode(staffCode).map(
                staff -> offenderTransformer.managedOffenderOf(staff, current)
        );
    }

	public Optional<StaffDetails> getStaffDetails(String staffCode) {
        return staffRepository.findByOfficerCode(staffCode).map(
            staff -> StaffDetails.builder()
                    .staff(Human.builder()
                    .forenames(
                        offenderTransformer.combinedMiddleNamesOf(staff.getForename(), staff.getForname2())
                        .stream()
                        .collect(Collectors.joining(" ")))
                    .surname(staff.getSurname())
                    .build())
                    .staffCode(staff.getOfficerCode())
                    .teams(staff.getTeams().stream().map(team -> Team.builder().code(team.getCode()).build()).collect(Collectors.toList()))
            .build());
	}
}
