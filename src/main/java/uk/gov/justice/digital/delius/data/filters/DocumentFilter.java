package uk.gov.justice.digital.delius.data.filters;

import io.vavr.control.Either;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import uk.gov.justice.digital.delius.data.api.OffenderDocumentDetail.Type;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static uk.gov.justice.digital.delius.data.api.OffenderDocumentDetail.Type.COURT_REPORT_DOCUMENT;
import static uk.gov.justice.digital.delius.data.filters.DocumentFilter.SubType.PSR;

public class DocumentFilter {
    private static final DocumentFilter NO_FILTER = new DocumentFilter();
    private static final Map<Type, List<SubType>> typeSubTypeMap = Map.of(COURT_REPORT_DOCUMENT, List.of(PSR));
    private final Type type;
    private final SubType subType;
    private DocumentFilter(Type type, SubType subType) {
        this.type = type;
        this.subType = subType;
    }
    private DocumentFilter(Type type) {
        this.type = type;
        this.subType = null;
    }

    private DocumentFilter() {
        type = null;
        subType = null;
    }

    public static DocumentFilter noFilter() {
        return NO_FILTER;
    }

    public static Either<String, DocumentFilter> of(String type, String subType) {
        if (StringUtils.isBlank(type) && StringUtils.isBlank(subType)) {
            return Either.right(NO_FILTER);
        }
        if (StringUtils.isBlank(type) && StringUtils.isNotBlank(subType)) {
            return Either.left(String.format("subtype of %s was supplied but no type. subtype can only be supplied when a valid type is supplied", subType));
        }

        final var maybeType = enumOf(Type.class, type);
        return maybeType.map(documentType -> {
            if (StringUtils.isNotBlank(subType)) {
                final var maybeSubType = enumOf(SubType.class, subType);
                return maybeSubType
                    .map(documentSubType -> {
                        if (isRelated(documentType, documentSubType)) {
                            return Either.<String, DocumentFilter>right(new DocumentFilter(documentType, documentSubType));
                        } else {
                            return Either.<String, DocumentFilter>left(String.format("subtype of %s was not valid for type %s", subType, type));
                        }
                    })
                    .orElse(Either.left(String.format("subtype of %s was not valid", subType)));
            }

            return Either.<String, DocumentFilter>right(new DocumentFilter(documentType));

        }).orElse(Either.left(String.format("type of %s was not valid", type)));
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

    private static boolean isRelated(Type documentType, SubType documentSubType) {
        return typeSubTypeMap.containsKey(documentType) && typeSubTypeMap
            .get(documentType)
            .contains(documentSubType);
    }

    public <T> List<T> documentsFor(Type type, Supplier<List<T>> documentReader) {
        if (allowType(type)) {
            return documentReader.get();
        }
        return List.of();
    }

    public <T> List<T> documentsFor(Type type, Function<SubType, List<T>> documentReader) {
        if (allowType(type)) {
            return documentReader.apply(subType);
        }
        return List.of();
    }

    public <T> Predicate<T> hasDocument(Type type, Predicate<T> predicate) {
        return (entity) -> allowType(type) && predicate.test(entity);
    }

    private boolean allowType(Type type) {
        return this == NO_FILTER || type == this.type;
    }

    public enum SubType {
        // Pre sentence report
        PSR
    }
}
