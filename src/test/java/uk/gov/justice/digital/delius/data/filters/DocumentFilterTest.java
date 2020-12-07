package uk.gov.justice.digital.delius.data.filters;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.justice.digital.delius.data.api.OffenderDocumentDetail.Type;
import uk.gov.justice.digital.delius.data.filters.DocumentFilter.SubType;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.justice.digital.delius.data.filters.DocumentFilter.SubType.PSR;
import static uk.gov.justice.digital.delius.data.filters.DocumentFilter.noFilter;

class DocumentFilterTest {
    private final Supplier<AssertionError> shouldBeRight = () -> new AssertionError("Should have had a right value");

    @DisplayName("Factory of")
    @Nested
    class FactoryOf {
        @Test
        @DisplayName("null filter values creates a no filter")
        void nullFilterValuesCreatesANoFilter() {
            assertThat(DocumentFilter
                .of(null, null)
                .getOrElseThrow(shouldBeRight)).isEqualTo(noFilter());
        }

        @Test
        @DisplayName("empty filter values create a no filter")
        void emptyFilterValuesCreateANoFilter() {
            assertThat(DocumentFilter
                .of(" ", "")
                .getOrElseThrow(shouldBeRight)).isEqualTo(noFilter());
        }

        @Test
        @DisplayName("filter is created for valid type")
        void filterIsCreatedForValidType() {
            assertThat(DocumentFilter
                .of("ASSESSMENT_DOCUMENT", "")
                .getOrElseThrow(shouldBeRight)).isNotNull().isNotEqualTo(noFilter());
        }

        @Test
        @DisplayName("filter is created for valid type with related subtype")
        void filterIsCreatedForValidTypeWithRelatedSubType() {
            assertThat(DocumentFilter
                .of("COURT_REPORT_DOCUMENT", "PSR")
                .getOrElseThrow(shouldBeRight)).isNotNull().isNotEqualTo(noFilter());
        }

        @Test
        @DisplayName("filter where subtype is not related to type is rejected")
        void filterWhereSubTypeIsNotRelatedToTypeIsRejected() {
            assertThat(DocumentFilter
                .of("ASSESSMENT_DOCUMENT", "PSR")
                .getLeft()).isEqualTo("subtype of PSR was not valid for type ASSESSMENT_DOCUMENT");
        }

        @Test
        @DisplayName("SubType where no type is supplied is rejected")
        void subTypeWhereNoTypeIsSuppliedIsRejected() {
            assertThat(DocumentFilter
                .of(null, "PSR")
                .getLeft()).isEqualTo("subtype of PSR was supplied but no type. subtype can only be supplied when a valid type is supplied");
        }
    }

    @DisplayName("documents for type")
    @Nested
    class DocumentsForType {
        private final Supplier<List<String>> documentSupplier = () -> List.of("one", "two", "three");
        private final DocumentFilter documentFilter = DocumentFilter
            .of("ASSESSMENT_DOCUMENT", null)
            .getOrElseThrow(shouldBeRight);

        @Test
        @DisplayName("documents returned when matching type")
        void documentsReturnedWhenMatchingType() {
            assertThat(documentFilter.documentsFor(Type.ASSESSMENT_DOCUMENT, documentSupplier)).containsExactly("one", "two", "three");
        }

        @Test
        @DisplayName("empty list returned when not matching type")
        void emptyListReturnedWhenNotMatchingType() {
            assertThat(documentFilter.documentsFor(Type.OFFENDER_DOCUMENT, documentSupplier)).isEmpty();
        }
    }

    @DisplayName("documents for type and subtype")
    @Nested
    class DocumentsForTypeAndSubType {
        private final Function<SubType, List<String>> documentSupplier = (subType) -> subType == PSR ? List.of("one", "two", "three") : List
            .of("one", "two", "three", "four");

        @Test
        @DisplayName("allows filtering by subtype")
        void allowsFilteringBySubType() {
            assertThat(DocumentFilter
                .of("COURT_REPORT_DOCUMENT", "PSR")
                .getOrElseThrow(shouldBeRight)
                .documentsFor(Type.COURT_REPORT_DOCUMENT, documentSupplier)).containsExactly("one", "two", "three");

        }

        @Test
        @DisplayName("allows filtering by just type")
        void allowsFilteringByJustType() {
            assertThat(DocumentFilter
                .of("COURT_REPORT_DOCUMENT", null)
                .getOrElseThrow(shouldBeRight)
                .documentsFor(Type.COURT_REPORT_DOCUMENT, documentSupplier)).containsExactly("one", "two", "three", "four");
        }

        @Test
        @DisplayName("empty list returned when not matching type")
        void emptyListReturnedWhenNotMatchingType() {
            assertThat(DocumentFilter
                .of("COURT_REPORT_DOCUMENT", "PSR")
                .getOrElseThrow(shouldBeRight)
                .documentsFor(Type.OFFENDER_DOCUMENT, documentSupplier)).isEmpty();
        }

    }

    @DisplayName("hasDocument predicate")
    @Nested
    class HasDocumentPredicate {
        private final Predicate<String> predicate = (value) -> value.equals("CORRECT!");
        private final DocumentFilter documentFilter = DocumentFilter
            .of("ASSESSMENT_DOCUMENT", null)
            .getOrElseThrow(shouldBeRight);

        @Test
        @DisplayName("predicate can be evaluated true when matching type")
        void predicateCanBeEvaluatedTrueWhenMatchingType() {
            assertThat(documentFilter.hasDocument(Type.ASSESSMENT_DOCUMENT, predicate).test("CORRECT!")).isTrue();
        }

        @Test
        @DisplayName("predicate can be evaluated false when matching type")
        void predicateCanBeEvaluatedFalseWhenMatchingType() {
            assertThat(documentFilter.hasDocument(Type.ASSESSMENT_DOCUMENT, predicate).test("WRONG!")).isFalse();
        }

        @Test
        @DisplayName("predicate can be always false when not matching type")
        void predicateCanBeAlwaysFalseWhenNotMatchingType() {
            assertThat(documentFilter.hasDocument(Type.CONTACT_DOCUMENT, predicate).test("CORRECT!")).isFalse();
        }

    }

}