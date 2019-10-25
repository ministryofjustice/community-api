package uk.gov.justice.digital.delius.jpa.standard.repository;

import lombok.val;
import org.hibernate.Session;
import org.hibernate.jdbc.ReturningWork;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import uk.gov.justice.digital.delius.jpa.standard.entity.ProbationArea;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

@Repository
@Profile("oracle")
public class SpgNotificationHelperRepositoryImpl implements SpgNotificationHelperRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<ProbationArea> getInterestedCRCs(String offenderId) {
        final ReturningWork<List<ProbationArea>> work = connection -> {
            val call = connection.prepareCall("{call pkg_search.procGetInterestedCRCs(?, ?)}");
            call.registerOutParameter(2, oracle.jdbc.OracleTypes.CURSOR);
            call.setString(1, offenderId);
            call.execute();
            val result = (ResultSet) call.getObject(2);
            val areas = new ArrayList<ProbationArea>();
            while (result.next()) {
                areas.add(ProbationArea
                        .builder()
                        .probationAreaId(result.getBigDecimal(1).longValue())
                        .code(result.getString(2))
                        .build());
            }
            return areas;
        };
        Session session = (Session) entityManager.getDelegate();
        return session.doReturningWork(work);
    }


    public Long getNextControlSequence(String probationAreaCode) {
        return ((BigDecimal) entityManager
                .createNativeQuery("SELECT spgconfig.getNextControlReference(:probationAreaCode) FROM DUAL")
                .setParameter("probationAreaCode", probationAreaCode)
                .getSingleResult()).longValue();
    }

}
