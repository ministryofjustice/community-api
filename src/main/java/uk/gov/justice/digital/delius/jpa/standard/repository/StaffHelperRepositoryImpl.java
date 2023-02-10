package uk.gov.justice.digital.delius.jpa.standard.repository;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Repository
@Profile("oracle")
public class StaffHelperRepositoryImpl implements StaffHelperRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public String getNextStaffCode(String probationAreaCode) {
        return entityManager
                .createNativeQuery("SELECT spgconfig.getNextStaffReference(:probationAreaCode) FROM DUAL")
                .setParameter("probationAreaCode", probationAreaCode)
                .getSingleResult().toString();
    }
}
