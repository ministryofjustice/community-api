package uk.gov.justice.digital.delius.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.justice.digital.delius.data.api.SentenceStatus;
import uk.gov.justice.digital.delius.jpa.standard.entity.Custody;
import uk.gov.justice.digital.delius.jpa.standard.repository.DisposalRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.OffenderRepository;
import uk.gov.justice.digital.delius.transformers.SentenceStatusTransformer;

import java.util.Optional;

@Service
@AllArgsConstructor
public class SentenceService {
    private final OffenderRepository offenderRepository;
    private final DisposalRepository disposalRepository;

    public Optional<SentenceStatus> getSentenceStatus(String crn, Long convictionId, Long sentenceId) {
        return offenderRepository.findByCrn(crn)
            .map(offender -> disposalRepository.findByDisposalId(sentenceId)
                    .filter(disposal -> offender.getOffenderId().equals(disposal.getOffenderId()))
                    .filter(disposal -> convictionId.equals(disposal.getEvent().getEventId()))
                    .filter(disposal -> !disposal.isSoftDeleted())
                    .filter(disposal -> !Optional.ofNullable(disposal.getCustody()).map(Custody::isSoftDeleted).orElse(Boolean.FALSE))
            )
            .flatMap(optionalDisposal ->  optionalDisposal.map(SentenceStatusTransformer::sentenceStatusOf));
    }

}
