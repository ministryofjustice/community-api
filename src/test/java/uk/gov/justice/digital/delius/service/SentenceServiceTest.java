package uk.gov.justice.digital.delius.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.digital.delius.data.api.CustodialStatus;
import uk.gov.justice.digital.delius.jpa.standard.entity.Disposal;
import uk.gov.justice.digital.delius.jpa.standard.entity.Offender;
import uk.gov.justice.digital.delius.jpa.standard.repository.DisposalRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.OffenderRepository;
import uk.gov.justice.digital.delius.transformers.CustodialStatusTransformer;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SentenceServiceTest {

    public static final String CRN = "X12345";
    public static final long CONVICTION_ID = 1234567L;
    public static final long SENTENCE_ID = 2345678L;
    public static final long OFFENDER_ID = 3456789L;
    private SentenceService sentenceService;
    @Mock
    private OffenderRepository offenderRepository;
    @Mock
    private DisposalRepository disposalRepository;
    @Mock
    private Offender offender;
    @Mock
    private Disposal disposal;
    @Mock
    private CustodialStatus custodialStatus;
    @Mock
    private CustodialStatusTransformer transformer;

    @BeforeEach
    public void setUp(){
        sentenceService = new SentenceService(offenderRepository, disposalRepository, transformer);
    }

    @Test
    public void whenNoOffenderFound_thenReturnEmpty() {
        when(offenderRepository.findByCrn(CRN)).thenReturn(Optional.empty());
        Optional<CustodialStatus> status = sentenceService.getCustodialStatus(CRN, CONVICTION_ID, SENTENCE_ID);

        assertThat(status).isEmpty();
    }

    @Test
    public void whenNoDisposalFound_thenReturnEmpty() {
        when(offenderRepository.findByCrn(CRN)).thenReturn(Optional.of(offender));
        when(disposalRepository.findByDisposalId(SENTENCE_ID)).thenReturn(Optional.empty());
        Optional<CustodialStatus> status = sentenceService.getCustodialStatus(CRN, CONVICTION_ID, SENTENCE_ID);

        assertThat(status).isEmpty();
    }

    @Test
    public void whenStatusReturnedFromRepository_thenMapAndReturnIt() {
        when(offenderRepository.findByCrn(CRN)).thenReturn(Optional.of(offender));
        when(disposalRepository.findByDisposalId(SENTENCE_ID)).thenReturn(Optional.of(disposal));
        when(transformer.custodialStatusOf(disposal)).thenReturn(custodialStatus);

        Optional<CustodialStatus> status = sentenceService.getCustodialStatus(CRN, CONVICTION_ID, SENTENCE_ID);

        assertThat(status).isNotEmpty();
        assertThat(status.get()).isEqualTo(custodialStatus);
    }
}