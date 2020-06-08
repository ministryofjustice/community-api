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
            .map(offender -> disposalRepository.findByDisposalId(sentenceId)
                    .filter(disposal -> offender.getOffenderId().equals(disposal.getOffenderId()))
                    .filter(disposal -> convictionId.equals(disposal.getEvent().getEventId()))
                    .filter(disposal -> !disposal.isSoftDeleted())
                    .filter(disposal -> disposal.getCustody() != null)
                    .filter(disposal -> !disposal.getCustody().isSoftDeleted())
            )
            .flatMap(optional ->  optional.map(custodialStatusTransformer::custodialStatusOf));
    }

}
