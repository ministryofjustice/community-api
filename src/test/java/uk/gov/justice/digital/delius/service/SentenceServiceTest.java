package uk.gov.justice.digital.delius.service;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.digital.delius.data.api.CustodialStatus;
import uk.gov.justice.digital.delius.jpa.standard.entity.Custody;
import uk.gov.justice.digital.delius.jpa.standard.entity.Disposal;
import uk.gov.justice.digital.delius.jpa.standard.entity.Event;
import uk.gov.justice.digital.delius.jpa.standard.entity.Offender;
import uk.gov.justice.digital.delius.jpa.standard.repository.DisposalRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.OffenderRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.justice.digital.delius.transformers.CustodialStatusTransformer.NO_CUSTODY_CODE;
import static uk.gov.justice.digital.delius.transformers.CustodialStatusTransformer.NO_CUSTODY_DESCRIPTION;

@ExtendWith(MockitoExtension.class)
class SentenceServiceTest {

    public static final String CRN = "X12345";
    public static final long CONVICTION_ID = 1234567L;
    public static final long SENTENCE_ID = 2345678L;
    public static final long OFFENDER_ID = 3456789L;
    @InjectMocks
    private SentenceService sentenceService;
    @Mock
    private OffenderRepository offenderRepository;
    @Mock
    private DisposalRepository disposalRepository;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private Offender offender;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private Disposal disposal;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private Custody custody;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private Event event;

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
    public void whenOffenderDoesNotMatchSentence_thenReturnEmpty() {
        when(offenderRepository.findByCrn(CRN)).thenReturn(Optional.of(offender));
        when(disposalRepository.findByDisposalId(SENTENCE_ID)).thenReturn(Optional.of(disposal));
        when(offender.getOffenderId()).thenReturn(OFFENDER_ID);
        when(disposal.getOffenderId()).thenReturn(999999L);
        Optional<CustodialStatus> status = sentenceService.getCustodialStatus(CRN, CONVICTION_ID, SENTENCE_ID);

        assertThat(status).isEmpty();
    }

    @Test
    public void whenConvictionDoesNotMatchSentence_thenReturnEmpty() {
        when(offenderRepository.findByCrn(CRN)).thenReturn(Optional.of(offender));
        when(disposalRepository.findByDisposalId(SENTENCE_ID)).thenReturn(Optional.of(disposal));
        when(offender.getOffenderId()).thenReturn(OFFENDER_ID);
        when(disposal.getOffenderId()).thenReturn(OFFENDER_ID);
        when(disposal.getEvent()).thenReturn(Event.builder()
                .eventId(9999999L)
                .build());
        Optional<CustodialStatus> status = sentenceService.getCustodialStatus(CRN, CONVICTION_ID, SENTENCE_ID);

        assertThat(status).isEmpty();
    }

    @Test
    public void whenSentenceIsSoftDeleted_thenReturnEmpty() {
        when(offenderRepository.findByCrn(CRN)).thenReturn(Optional.of(offender));
        when(disposalRepository.findByDisposalId(SENTENCE_ID)).thenReturn(Optional.of(disposal));
        when(offender.getOffenderId()).thenReturn(OFFENDER_ID);
        when(disposal.getOffenderId()).thenReturn(OFFENDER_ID);
        when(disposal.getEvent()).thenReturn(event);
        when(disposal.getEvent().getEventId()).thenReturn(CONVICTION_ID);
        when(disposal.isSoftDeleted()).thenReturn(true);
        Optional<CustodialStatus> status = sentenceService.getCustodialStatus(CRN, CONVICTION_ID, SENTENCE_ID);

        assertThat(status).isEmpty();
    }

    @Test
    public void whenCustodyIsNull_thenReturnStatusWithNoCustodyCode() {
        when(offenderRepository.findByCrn(CRN)).thenReturn(Optional.of(offender));
        when(disposalRepository.findByDisposalId(SENTENCE_ID)).thenReturn(Optional.of(disposal));
        when(offender.getOffenderId()).thenReturn(OFFENDER_ID);
        when(disposal.getOffenderId()).thenReturn(OFFENDER_ID);
        when(disposal.getEvent()).thenReturn(event);
        when(disposal.getEvent().getEventId()).thenReturn(CONVICTION_ID);
        when(disposal.isSoftDeleted()).thenReturn(false);
        when(disposal.getCustody()).thenReturn(null);
        when(disposal.getDisposalId()).thenReturn(SENTENCE_ID);

        CustodialStatus status = sentenceService.getCustodialStatus(CRN, CONVICTION_ID, SENTENCE_ID).orElse(null);

        assertThat(status).isNotNull();
        assertThat(status.getSentenceId()).isEqualTo(SENTENCE_ID);
        assertThat(status.getCustodialType().getCode()).isEqualTo(NO_CUSTODY_CODE);
        assertThat(status.getCustodialType().getDescription()).isEqualTo(NO_CUSTODY_DESCRIPTION);
        assertThat(status.getLicenceExpiryDate()).isNull();
        assertThat(status.getActualReleaseDate()).isNull();
    }

    @Test
    public void whenCustodyIsSoftDeleted_thenReturnEmpty() {
        when(offenderRepository.findByCrn(CRN)).thenReturn(Optional.of(offender));
        when(disposalRepository.findByDisposalId(SENTENCE_ID)).thenReturn(Optional.of(disposal));
        when(offender.getOffenderId()).thenReturn(OFFENDER_ID);
        when(disposal.getOffenderId()).thenReturn(OFFENDER_ID);
        when(disposal.getEvent()).thenReturn(event);
        when(disposal.getEvent().getEventId()).thenReturn(CONVICTION_ID);
        when(disposal.isSoftDeleted()).thenReturn(false);
        when(disposal.getCustody()).thenReturn(custody);
        when(custody.isSoftDeleted()).thenReturn(true);
        Optional<CustodialStatus> status = sentenceService.getCustodialStatus(CRN, CONVICTION_ID, SENTENCE_ID);

        assertThat(status).isEmpty();
    }

    @Test
    public void whenStatusReturnedFromRepository_thenMapAndReturnIt() {
        when(offenderRepository.findByCrn(CRN)).thenReturn(Optional.of(offender));
        when(disposalRepository.findByDisposalId(SENTENCE_ID)).thenReturn(Optional.of(disposal));
        when(disposal.getDisposalId()).thenReturn(SENTENCE_ID);
        when(disposal.getOffenderId()).thenReturn(OFFENDER_ID);
        when(offender.getOffenderId()).thenReturn(OFFENDER_ID);
        when(disposal.getEvent()).thenReturn(event);
        when(disposal.getEvent().getEventId()).thenReturn(CONVICTION_ID);
        when(disposal.isSoftDeleted()).thenReturn(false);
        when(disposal.getCustody()).thenReturn(custody);
        when(custody.isSoftDeleted()).thenReturn(false);

        Optional<CustodialStatus> status = sentenceService.getCustodialStatus(CRN, CONVICTION_ID, SENTENCE_ID);

        assertThat(status).isNotEmpty();
        assertThat(status.get().getSentenceId()).isEqualTo(SENTENCE_ID);
    }
}
