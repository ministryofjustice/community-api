package uk.gov.justice.digital.delius.data.filters;

import io.vavr.control.Either;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import uk.gov.justice.digital.delius.data.api.OffenderDocumentDetail.Type;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static uk.gov.justice.digital.delius.data.api.OffenderDocumentDetail.Type.COURT_REPORT_DOCUMENT;
import static uk.gov.justice.digital.delius.data.filters.DocumentFilter.SubType.PSR;

public class DocumentFilter {
    private static final DocumentFilter NO_FILTER = new DocumentFilter();
    private static final Map<Type, List<SubType>> categoryTypeMap = Map.of(COURT_REPORT_DOCUMENT, List.of(PSR));
    private final Type category;
    private final SubType type;
    private DocumentFilter(Type category, SubType type) {
        this.category = category;
        this.type = type;
    }
    private DocumentFilter(Type category) {
        this.category = category;
        this.type = null;
    }

    private DocumentFilter() {
        category = null;
        type = null;
    }

    public static DocumentFilter noFilter() {
        return NO_FILTER;
    }

    public static Either<String, DocumentFilter> of(String category, String type) {
        if (StringUtils.isBlank(category) && StringUtils.isBlank(type)) {
            return Either.right(NO_FILTER);
        }
        if (StringUtils.isBlank(category) && StringUtils.isNotBlank(type)) {
            return Either.left(String.format("Type of %s was supplied but no category. Type can only be supplied when a valida category is supplied", type));
        }

        final var maybeCategory = enumOf(Type.class, category);
        return maybeCategory.map(documentCategory -> {
            if (StringUtils.isNotBlank(type)) {
                final var maybeType = enumOf(SubType.class, type);
                return maybeType
                    .map(documentType -> {
                        if (isRelated(documentCategory, documentType)) {
                            return Either.<String, DocumentFilter>right(new DocumentFilter(documentCategory, documentType));
                        } else {
                            return Either.<String, DocumentFilter>left(String.format("Type of %s was not valid for category %s", type, category));
                        }
                    })
                    .orElse(Either.left(String.format("Type of %s was not valid", type)));
            }

            return Either.<String, DocumentFilter>right(new DocumentFilter(documentCategory));

        }).orElse(Either.left(String.format("Category of %s was not valid", category)));
    }

    @NotNull
    private static <T extends Enum<T>> Optional<T> enumOf(Class<T> enumType,
                                         String name) {
        try {
            return Optional.of(Enum.valueOf(enumType, name));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    private static boolean isRelated(Type documentCategory, SubType documentType) {
        return categoryTypeMap.containsKey(documentCategory) && categoryTypeMap
            .get(documentCategory)
            .contains(documentType);
    }

    public <T> List<T> documentsFor(Type category, Supplier<List<T>> documentReader) {
        if (allowCategory(category)) {
            return documentReader.get();
        }
        return List.of();
    }

    public <T> List<T> documentsFor(Type category, Function<SubType, List<T>> documentReader) {
        if (allowCategory(category)) {
            return documentReader.apply(type);
        }
        return List.of();
    }

    public <T> Predicate<T> hasDocument(Type category, Predicate<T> predicate) {
        return (entity) -> allowCategory(category) && predicate.test(entity);
    }

    private boolean allowCategory(Type category) {
        return this == NO_FILTER || category == this.category;
    }

    public enum SubType {
        // Pre sentence report
        PSR
    }
}
