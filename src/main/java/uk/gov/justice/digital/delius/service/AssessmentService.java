package uk.gov.justice.digital.delius.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.digital.delius.data.api.OffenderAssessments;
import uk.gov.justice.digital.delius.jpa.standard.entity.OGRSAssessment;
import uk.gov.justice.digital.delius.jpa.standard.entity.Offender;
import uk.gov.justice.digital.delius.jpa.standard.repository.OGRSAssessmentRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.OffenderRepository;
import uk.gov.justice.digital.delius.transformers.OffenderTransformer;

import java.util.Optional;

@Service
@Slf4j
@AllArgsConstructor
public class AssessmentService {

    private final OffenderRepository offenderRepository;
    private final OGRSAssessmentRepository OGRSAssessmentRepository;

    @Transactional(readOnly = true)
    public Optional<OffenderAssessments> getAssessments(String crn) {
        Optional<Offender> offender= offenderRepository.findByCrn(crn);
        return offender.map(off -> {
            OGRSAssessment OGRSAssessment = OGRSAssessmentRepository.findByOffenderId(off.getOffenderId());
            return OffenderTransformer.assessmentsOf(off, OGRSAssessment);
        });
    }
}
