package uk.gov.justice.digital.delius.jpa.standard.repository;

import uk.gov.justice.digital.delius.jpa.standard.entity.ProbationArea;

import java.util.List;

public interface SpgNotificationHelperRepository {
    List<ProbationArea> getInterestedCRCs(String offenderId);
    Long getNextControlSequence(String probationAreaCode);
}
