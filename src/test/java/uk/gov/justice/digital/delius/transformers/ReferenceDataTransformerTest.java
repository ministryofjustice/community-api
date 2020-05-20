package uk.gov.justice.digital.delius.transformers;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.justice.digital.delius.jpa.standard.entity.ReferenceDataMaster;
import uk.gov.justice.digital.delius.jpa.standard.entity.StandardReference;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ReferenceDataTransformerTest {

    @Nested
    class ReferenceDataSetsOf {
        @Test
        void wilTransformEachDataItem() {
            final var dataSetsEntities = List.of(
                    ReferenceDataMaster
                            .builder()
                            .codeSetName("CODE SET A")
                            .description("code set a description")
                            .build(),
                    ReferenceDataMaster
                            .builder()
                            .codeSetName("CODE SET B")
                            .description("code set b description")
                            .build(),
                    ReferenceDataMaster
                            .builder()
                            .codeSetName("CODE SET C")
                            .description("code set c description")
                            .build()
            );

            final var dataSets = ReferenceDataTransformer.referenceDataSetsOf(dataSetsEntities);
            assertThat(dataSets).hasSize(3);
        }


        @Test
        void willCopyCodeAndDescription() {
            final var dataSetsEntities = List.of(
                    ReferenceDataMaster
                            .builder()
                            .codeSetName("CODE SET A")
                            .description("code set a description")
                            .build()
            );

            final var referenceDataSet = ReferenceDataTransformer.referenceDataSetsOf(dataSetsEntities).get(0);
            assertThat(referenceDataSet.getCode()).isEqualTo("CODE SET A");
            assertThat(referenceDataSet.getDescription()).isEqualTo("code set a description");
        }
    }

    @Nested
    class ReferenceDataOf {
        @Test
        void wilTransformEachDataItem() {
            final var dataEntities = List.of(
                    StandardReference
                            .builder()
                            .selectable("Y")
                            .codeValue("A")
                            .codeDescription("a description")
                            .standardReferenceListId(1L)
                            .build(),
                    StandardReference
                            .builder()
                            .selectable("Y")
                            .codeValue("B")
                            .codeDescription("b description")
                            .standardReferenceListId(2L)
                            .build(),
                    StandardReference
                            .builder()
                            .selectable("Y")
                            .codeValue("C")
                            .codeDescription("c description")
                            .standardReferenceListId(3L)
                            .build()
            );

            final var data = ReferenceDataTransformer.referenceDataOf(dataEntities);
            assertThat(data).hasSize(3);
        }

        @Test
        void importantAttributeWillBeCopied() {
            final var dataEntities = List.of(
                    StandardReference
                            .builder()
                            .selectable("Y")
                            .codeValue("C")
                            .codeDescription("c description")
                            .standardReferenceListId(3L)
                            .build()
            );

            final var referenceData = ReferenceDataTransformer.referenceDataOf(dataEntities).get(0);
            assertThat(referenceData.getCode()).isEqualTo("C");
            assertThat(referenceData.getDescription()).isEqualTo("c description");
            assertThat(referenceData.getId()).isEqualTo("3");
            assertThat(referenceData.isActive()).isTrue();
        }

        @Test
        void notActiveWhenNotSelectable() {
            final var dataEntities = List.of(
                    StandardReference
                            .builder()
                            .selectable("N")
                            .codeValue("C")
                            .codeDescription("c description")
                            .standardReferenceListId(3L)
                            .build()
            );

            final var referenceData = ReferenceDataTransformer.referenceDataOf(dataEntities).get(0);
            assertThat(referenceData.isActive()).isFalse();
        }
    }
}