package uk.gov.justice.digital.delius.jpa.standard.entity;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DisposalTypeTest {

    @Test
    public void isCustodialWhenNonStatutoryCustodySentence() {
        assertThat(DisposalType.builder().sentenceType("NC").build().isCustodial()).isTrue();
    }

    @Test
    public void isCustodialWhenStatutoryCustodySentence() {
        assertThat(DisposalType.builder().sentenceType("SC").build().isCustodial()).isTrue();
    }

    @Test
    public void notCustodialWhenNonStatutoryCommunitySentence() {
        assertThat(DisposalType.builder().sentenceType("NP").build().isCustodial()).isFalse();
    }

    @Test
    public void notCustodialWhenStatutoryCommunitySentence() {
        assertThat(DisposalType.builder().sentenceType("SP").build().isCustodial()).isFalse();
    }

    @Test
    public void notCustodialWhenNotApplicableSentence() {
        assertThat(DisposalType.builder().sentenceType("NA").build().isCustodial()).isFalse();
    }

    @Test
    public void notCustodialWhenNoSentence() {
        assertThat(DisposalType.builder().sentenceType(null).build().isCustodial()).isFalse();
    }
}