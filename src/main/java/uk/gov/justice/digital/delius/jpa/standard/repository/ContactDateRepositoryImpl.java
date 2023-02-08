package uk.gov.justice.digital.delius.jpa.standard.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import uk.gov.justice.digital.delius.jpa.standard.entity.Contact;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDate;

import static org.springframework.data.jpa.repository.query.QueryUtils.toOrders;

/**
 * Repository of distinct contact dates.
 */
@Repository
public class ContactDateRepositoryImpl implements ContactDateRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Page<LocalDate> findAll(Specification<Contact> spec, Pageable pageable) {
        final var builder = entityManager.getCriteriaBuilder();
        final var query = builder.createQuery(LocalDate.class);
        final var root = query.from(Contact.class);

        query.select(root.get("contactDate"))
            .distinct(true)
            .where(spec.toPredicate(root, query, builder))
            .orderBy(toOrders(pageable.getSortOr(Sort.by("contactDate")), root, builder));

        final var typedQuery = entityManager.createQuery(query);

        if (pageable.isPaged()) {
            final var resultList = typedQuery
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize())
                .getResultList();
            return PageableExecutionUtils.getPage(resultList, pageable, () -> count(spec));
        }

        return new PageImpl<>(typedQuery.getResultList());
    }

    @Override
    public Long count(Specification<Contact> spec) {
        final var builder = entityManager.getCriteriaBuilder();
        final var query = builder.createQuery(Long.class);
        final var root = query.from(Contact.class);

        query.select(builder.countDistinct(root.get("contactDate")))
            .where(spec.toPredicate(root, query, builder));

        return entityManager.createQuery(query).getResultList().stream().mapToLong(x -> x).sum();
    }
}
