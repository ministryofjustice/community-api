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
        @DisplayName("filter is created for valid category")
        void filterIsCreatedForValidCategory() {
            assertThat(DocumentFilter
                .of("ASSESSMENT_DOCUMENT", "")
                .getOrElseThrow(shouldBeRight)).isNotNull().isNotEqualTo(noFilter());
        }

        @Test
        @DisplayName("filter is created for valid category with related type")
        void filterIsCreatedForValidCategoryWithRelatedType() {
            assertThat(DocumentFilter
                .of("COURT_REPORT_DOCUMENT", "PSR")
                .getOrElseThrow(shouldBeRight)).isNotNull().isNotEqualTo(noFilter());
        }

        @Test
        @DisplayName("filter where type is not related to category is rejected")
        void filterWhereTypeIsNotRelatedToCategoryIsRejected() {
            assertThat(DocumentFilter
                .of("ASSESSMENT_DOCUMENT", "PSR")
                .getLeft()).isEqualTo("Type of PSR was not valid for category ASSESSMENT_DOCUMENT");
        }

        @Test
        @DisplayName("Type where no category is supplied is rejected")
        void typeWhereNoCategoryIsSuppliedIsRejected() {
            assertThat(DocumentFilter
                .of(null, "PSR")
                .getLeft()).isEqualTo("Type of PSR was supplied but no category. Type can only be supplied when a valida category is supplied");
        }
    }

    @DisplayName("documents for category")
    @Nested
    class DocumentsForCategory {
        private final Supplier<List<String>> documentSupplier = () -> List.of("one", "two", "three");
        private final DocumentFilter documentFilter = DocumentFilter
            .of("ASSESSMENT_DOCUMENT", null)
            .getOrElseThrow(shouldBeRight);

        @Test
        @DisplayName("documents returned when matching category")
        void documentsReturnedWhenMatchingCategory() {
            assertThat(documentFilter.documentsFor(Type.ASSESSMENT_DOCUMENT, documentSupplier)).containsExactly("one", "two", "three");
        }

        @Test
        @DisplayName("empty list returned when not matching category")
        void emptyListReturnedWhenNotMatchingCategory() {
            assertThat(documentFilter.documentsFor(Type.OFFENDER_DOCUMENT, documentSupplier)).isEmpty();
        }
    }

    @DisplayName("documents for category and type")
    @Nested
    class DocumentsForCategoryAndType {
        private final Function<SubType, List<String>> documentSupplier = (type) -> type == PSR ? List.of("one", "two", "three") : List
            .of("one", "two", "three", "four");

        @Test
        @DisplayName("allows filtering by type")
        void allowsFilteringByType() {
            assertThat(DocumentFilter
                .of("COURT_REPORT_DOCUMENT", "PSR")
                .getOrElseThrow(shouldBeRight)
                .documentsFor(Type.COURT_REPORT_DOCUMENT, documentSupplier)).containsExactly("one", "two", "three");

        }

        @Test
        @DisplayName("allows filtering by just category")
        void allowsFilteringByJustCategory() {
            assertThat(DocumentFilter
                .of("COURT_REPORT_DOCUMENT", null)
                .getOrElseThrow(shouldBeRight)
                .documentsFor(Type.COURT_REPORT_DOCUMENT, documentSupplier)).containsExactly("one", "two", "three", "four");
        }

        @Test
        @DisplayName("empty list returned when not matching category")
        void emptyListReturnedWhenNotMatchingCategory() {
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
        @DisplayName("predicate can be evaluated true when matching category")
        void predicateCanBeEvaluatedTrueWhenMatchingCategory() {
            assertThat(documentFilter.hasDocument(Type.ASSESSMENT_DOCUMENT, predicate).test("CORRECT!")).isTrue();
        }

        @Test
        @DisplayName("predicate can be evaluated false when matching category")
        void predicateCanBeEvaluatedFalseWhenMatchingCategory() {
            assertThat(documentFilter.hasDocument(Type.ASSESSMENT_DOCUMENT, predicate).test("WRONG!")).isFalse();
        }

        @Test
        @DisplayName("predicate can be always false when not matching category")
        void predicateCanBeAlwaysFalseWhenNotMatchingCategory() {
            assertThat(documentFilter.hasDocument(Type.CONTACT_DOCUMENT, predicate).test("CORRECT!")).isFalse();
        }

    }

}