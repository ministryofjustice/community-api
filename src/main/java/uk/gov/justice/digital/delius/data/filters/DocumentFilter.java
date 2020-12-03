package uk.gov.justice.digital.delius.data.filters;

import org.apache.commons.lang3.StringUtils;
import uk.gov.justice.digital.delius.data.api.OffenderDocumentDetail.Type;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class DocumentFilter {
    private static final DocumentFilter NO_FILTER = new DocumentFilter();
    private final String category;
    private final String type;

    public DocumentFilter(String category, String type) {
        this.category = category;
        this.type = type;
    }

    public DocumentFilter() {
        category = null;
        type = null;
    }

    public static DocumentFilter noFilter() {
        return NO_FILTER;
    }

    public static Optional<DocumentFilter> of(String category, String type) {
        if (StringUtils.isBlank(category) && StringUtils.isBlank(type)) {
            return Optional.of(NO_FILTER);
        }
        return Optional.of(new DocumentFilter(category, type));
    }

    public <T> List<T> documentsFor(Type category, Supplier<List<T>> documentReader) {
        if (allowCategory(category)) {
            return documentReader.get();
        }
        return List.of();
    }

    public <T> List<T> documentsFor(Type category, Function<String, List<T>> documentReader) {
        if (allowCategory(category)) {
            return documentReader.apply(type);
        }
        return List.of();
    }

    public <T> Predicate<T> hasDocument(Type category, Predicate<T> predicate) {
        return (entity) -> allowCategory(category) && predicate.test(entity);
    }

    private boolean allowCategory(Type category) {
        return this == NO_FILTER || category.name().equals(this.category);
    }
}
