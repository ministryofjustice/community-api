package uk.gov.justice.digital.delius.jpa.standard.repository;

import com.google.common.collect.ImmutableList;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import uk.gov.justice.digital.delius.jpa.standard.entity.ProbationArea;

import java.util.List;

@Repository
@Profile("!oracle")
public class SpgNotificationHelperNoProceduresRepositoryImpl implements SpgNotificationHelperRepository {

    @Override
    public List<ProbationArea> getInterestedCRCs(String offenderId) {
        return ImmutableList.of();
    }


    public Long getNextControlSequence(String probationAreaCode) {
        return 0L;
    }
}
