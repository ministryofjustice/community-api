package uk.gov.justice.digital.delius.service;

import lombok.AllArgsConstructor;
import lombok.val;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.digital.delius.data.api.Caseload;
import uk.gov.justice.digital.delius.data.api.CaseloadRole;
import uk.gov.justice.digital.delius.jpa.standard.repository.CaseloadRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.StaffRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.TeamRepository;
import uk.gov.justice.digital.delius.transformers.CaseloadTransformer;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

@Service
@AllArgsConstructor
public class CaseloadService {
    private final CaseloadRepository caseloadRepository;
    private final StaffRepository staffRepository;
    private final TeamRepository teamRepository;

    @Transactional(readOnly = true)
    public Optional<Caseload> getCaseloadByStaffIdentifier(final long staffIdentifier, final CaseloadRole... roles) {
        if (!staffRepository.existsById(staffIdentifier)) return Optional.empty();

        val roleCodes = Arrays.stream(roles).map(CaseloadRole::getRoleCode).collect(toList());
        val caseload = caseloadRepository.findByStaffStaffIdAndRoleCodeIn(staffIdentifier, roleCodes);

        return Optional.of(CaseloadTransformer.caseloadOf(caseload));
    }

    @Transactional(readOnly = true)
    public Optional<Caseload> getCaseloadByTeamCode(
        final String teamCode, final Pageable pageable, final CaseloadRole... roles
    ) {
        return teamRepository.findActiveByCode(teamCode)
            .map(t -> caseloadRepository.findByTeamTeamIdAndRoleCodeIn(
                t.getTeamId(),
                Stream.of(roles).map(CaseloadRole::getRoleCode).toList(),
                pageable
            )).map(CaseloadTransformer::caseloadOf);
    }
}
