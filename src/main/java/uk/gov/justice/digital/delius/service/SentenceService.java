package uk.gov.justice.digital.delius.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.justice.digital.delius.data.api.CustodialStatus;
import uk.gov.justice.digital.delius.jpa.standard.repository.DisposalRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.OffenderRepository;
import uk.gov.justice.digital.delius.transformers.CustodialStatusTransformer;

import java.util.Optional;

@Service
@AllArgsConstructor
public class SentenceService {
    private OffenderRepository offenderRepository;
    private DisposalRepository disposalRepository;
    private CustodialStatusTransformer custodialStatusTransformer;

    public Optional<CustodialStatus> getCustodialStatus(String crn, Long convictionId, Long sentenceId) {
        return offenderRepository.findByCrn(crn)
            .map(offender -> disposalRepository.find(offender.getOffenderId(), convictionId, sentenceId))
            .flatMap(optional ->  optional.map(custodialStatusTransformer::custodialStatusOf));
    }

}
