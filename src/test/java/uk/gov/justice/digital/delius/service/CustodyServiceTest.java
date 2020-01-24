package uk.gov.justice.digital.delius.service;

import com.microsoft.applicationinsights.TelemetryClient;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import uk.gov.justice.digital.delius.controller.NotFoundException;
import uk.gov.justice.digital.delius.data.api.OffenderDetail;
import uk.gov.justice.digital.delius.data.api.UpdateCustody;
import uk.gov.justice.digital.delius.jpa.standard.repository.InstitutionalRepository;
import uk.gov.justice.digital.delius.transformers.*;
import uk.gov.justice.digital.delius.util.EntityHelper;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class CustodyServiceTest {
    private CustodyService custodyService;

    private TelemetryClient telemetryClient = mock(TelemetryClient.class);
    private OffenderService offenderService = mock(OffenderService.class);
    private ConvictionService convictionService = mock(ConvictionService.class);
    private InstitutionalRepository institutionalRepository = mock(InstitutionalRepository.class);
    private LookupSupplier lookupSupplier = mock(LookupSupplier.class);
    private ArgumentMatcher<Map<String, String>> standardTelemetryAttributes =
            attributes -> Optional.ofNullable(attributes.get("offenderNo")).filter(value -> value.equals("G9542VP")).isPresent() &&
            Optional.ofNullable(attributes.get("bookingNumber")).filter(value -> value.equals("44463B")).isPresent() &&
            Optional.ofNullable(attributes.get("toAgency")).filter(value -> value.equals("MDI")).isPresent();

    @Before
    public void setup() {
        final var convictionTransformer = new ConvictionTransformer(
                new MainOffenceTransformer(lookupSupplier),
                new AdditionalOffenceTransformer(lookupSupplier),
                new CourtAppearanceTransformer(new CourtReportTransformer(new CourtTransformer()), new CourtTransformer(), lookupSupplier),
                lookupSupplier,
                new InstitutionTransformer());
        custodyService = new CustodyService(telemetryClient, offenderService, convictionService, institutionalRepository, convictionTransformer);
        when(offenderService.getOffenderByNomsNumber(anyString())).thenReturn(Optional.of(OffenderDetail.builder().build()));
    }

    @Test
    public void willCreateTelemetryEventWhenOffenderNotFound() {
        when(offenderService.getOffenderByNomsNumber("G9542VP")).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
            custodyService.updateCustody("G9542VP", "44463B", UpdateCustody.builder().nomsPrisonInstitutionCode("MDI").build()));

        verify(telemetryClient).trackEvent(eq("P2PTransferOffenderNotFound"), argThat(standardTelemetryAttributes), isNull());
    }

    @Test
    public void willThrowExceptionWhenOffenderNotFound() {
        when(offenderService.getOffenderByNomsNumber("G9542VP")).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
            custodyService.updateCustody("G9542VP", "44463B", UpdateCustody.builder().nomsPrisonInstitutionCode("MDI").build()))
                .isInstanceOf(NotFoundException.class);

    }

    @Test
    public void willCreateTelemetryEventWhenConvictionNotFound() throws ConvictionService.DuplicateConvictionsForBookingNumberException {
        when(offenderService.getOffenderByNomsNumber("G9542VP")).thenReturn(Optional.of(OffenderDetail.builder().offenderId(99L).build()));
        when(convictionService.getSingleActiveConvictionIdByOffenderIdAndPrisonBookingNumber(99L, "G9542VP")).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
            custodyService.updateCustody("G9542VP", "44463B", UpdateCustody.builder().nomsPrisonInstitutionCode("MDI").build()));

        verify(telemetryClient).trackEvent(eq("P2PTransferBookingNumberNotFound"), argThat(standardTelemetryAttributes), isNull());
    }

    @Test
    public void willThrowExceptionWhenBookingNumberNotFound() throws ConvictionService.DuplicateConvictionsForBookingNumberException {
        when(offenderService.getOffenderByNomsNumber("G9542VP")).thenReturn(Optional.of(OffenderDetail.builder().offenderId(99L).build()));
        when(convictionService.getSingleActiveConvictionIdByOffenderIdAndPrisonBookingNumber(99L, "44463B")).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                custodyService.updateCustody("G9542VP", "44463B", UpdateCustody.builder().nomsPrisonInstitutionCode("MDI").build()))
                .isInstanceOf(NotFoundException.class);
    }


    @Test
    public void willCreateTelemetryEventWhenDuplicateConvictionsFound() throws ConvictionService.DuplicateConvictionsForBookingNumberException {
        when(offenderService.getOffenderByNomsNumber("G9542VP")).thenReturn(Optional.of(OffenderDetail.builder().offenderId(99L).build()));
        when(convictionService.getSingleActiveConvictionIdByOffenderIdAndPrisonBookingNumber(99L, "44463B"))
                .thenThrow(new ConvictionService.DuplicateConvictionsForBookingNumberException(2));

        assertThatThrownBy(() ->
                custodyService.updateCustody("G9542VP", "44463B", UpdateCustody.builder().nomsPrisonInstitutionCode("MDI").build()));

        verify(telemetryClient).trackEvent(eq("P2PTransferBookingNumberHasDuplicates"), argThat(standardTelemetryAttributes), isNull());
    }

    @Test
    public void willThrowExceptionWhenDuplicateConvictionsFound() throws ConvictionService.DuplicateConvictionsForBookingNumberException {
        when(offenderService.getOffenderByNomsNumber("G9542VP")).thenReturn(Optional.of(OffenderDetail.builder().offenderId(99L).build()));
        when(convictionService.getSingleActiveConvictionIdByOffenderIdAndPrisonBookingNumber(99L, "44463B"))
                .thenThrow(new ConvictionService.DuplicateConvictionsForBookingNumberException(2));

        assertThatThrownBy(() ->
                custodyService.updateCustody("G9542VP", "44463B", UpdateCustody.builder().nomsPrisonInstitutionCode("MDI").build()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    public void willCreateTelemetryEventWhenPrisonNotFound() throws ConvictionService.DuplicateConvictionsForBookingNumberException {
        when(offenderService.getOffenderByNomsNumber("G9542VP")).thenReturn(Optional.of(OffenderDetail.builder().offenderId(99L).build()));
        when(convictionService.getSingleActiveConvictionIdByOffenderIdAndPrisonBookingNumber(99L, "44463B"))
                .thenReturn(Optional.of(EntityHelper.aCustodyEvent()));
        when(institutionalRepository.findByNomisCdeCode("MDI")).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                custodyService.updateCustody("G9542VP", "44463B", UpdateCustody.builder().nomsPrisonInstitutionCode("MDI").build()));

        verify(telemetryClient).trackEvent(eq("P2PTransferPrisonNotFound"), argThat(standardTelemetryAttributes), isNull());
    }

    @Test
    public void willThrowExceptionWhenPrisonNotFound() throws ConvictionService.DuplicateConvictionsForBookingNumberException {
        when(offenderService.getOffenderByNomsNumber("G9542VP")).thenReturn(Optional.of(OffenderDetail.builder().offenderId(99L).build()));
        when(convictionService.getSingleActiveConvictionIdByOffenderIdAndPrisonBookingNumber(99L, "44463B"))
                .thenReturn(Optional.of(EntityHelper.aCustodyEvent()));
        when(institutionalRepository.findByNomisCdeCode("MDI")).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                custodyService.updateCustody("G9542VP", "44463B", UpdateCustody.builder().nomsPrisonInstitutionCode("MDI").build()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    public void willCreateTelemetryEventWhenPrisonLocationChanges() throws ConvictionService.DuplicateConvictionsForBookingNumberException {
        when(offenderService.getOffenderByNomsNumber("G9542VP")).thenReturn(Optional.of(OffenderDetail.builder().offenderId(99L).build()));
        when(convictionService.getSingleActiveConvictionIdByOffenderIdAndPrisonBookingNumber(99L, "44463B"))
                .thenReturn(Optional.of(EntityHelper.aCustodyEvent()));
        when(institutionalRepository.findByNomisCdeCode("MDI")).thenReturn(Optional.of(EntityHelper.anInstitution()));

        custodyService.updateCustody("G9542VP", "44463B", UpdateCustody.builder().nomsPrisonInstitutionCode("MDI").build());

        verify(telemetryClient).trackEvent(eq("P2PTransferPrisonUpdated"), argThat(standardTelemetryAttributes), isNull());
    }

}