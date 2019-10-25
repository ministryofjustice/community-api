package uk.gov.justice.digital.delius.jpa.standard.repository;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import uk.gov.justice.digital.delius.jpa.standard.entity.ProbationArea;

import javax.persistence.EntityManager;
import javax.persistence.ParameterMode;
import javax.persistence.PersistenceContext;
import javax.persistence.StoredProcedureQuery;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Repository
@Profile("oracle")
public class SpgNotificationHelperRepositoryImpl implements SpgNotificationHelperRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<ProbationArea> getInterestedCRCs(String offenderId) {

        StoredProcedureQuery query = entityManager.createStoredProcedureQuery( "pkg_search.procGetInterestedCRCs" );
        query.registerStoredProcedureParameter( 1, String.class, ParameterMode.IN );
        query.registerStoredProcedureParameter( 2, Class.class, ParameterMode.REF_CURSOR );
        query.setParameter( 1, offenderId );
        query.execute();
        List<Object[]> probationAreas = query.getResultList();

        return probationAreas.stream().map(rs -> ProbationArea
                .builder()
                .probationAreaId(((BigDecimal)rs[1]).longValue())
                .code((String)rs[2])
                .build()).collect(Collectors.toList());

    }


    public Long getNextControlSequence(String probationAreaCode) {
        return ((BigDecimal) entityManager
                .createNativeQuery("SELECT spgconfig.getNextControlReference(:probationAreaCode) FROM DUAL")
                .setParameter("probationAreaCode", probationAreaCode)
                .getSingleResult()).longValue();
    }

}
