package uk.gov.justice.digital.delius.controller.secure;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import uk.gov.justice.digital.delius.controller.advice.SecureControllerAdvice;
import uk.gov.justice.digital.delius.data.api.PrimaryIdentifiers;
import uk.gov.justice.digital.delius.data.filters.OffenderFilter;
import uk.gov.justice.digital.delius.helpers.CurrentUserSupplier;
import uk.gov.justice.digital.delius.service.AlfrescoService;
import uk.gov.justice.digital.delius.service.ContactService;
import uk.gov.justice.digital.delius.service.ConvictionService;
import uk.gov.justice.digital.delius.service.CustodyService;
import uk.gov.justice.digital.delius.service.DocumentService;
import uk.gov.justice.digital.delius.service.NsiService;
import uk.gov.justice.digital.delius.service.OffenderManagerService;
import uk.gov.justice.digital.delius.service.OffenderService;
import uk.gov.justice.digital.delius.service.SentenceService;
import uk.gov.justice.digital.delius.service.UserService;

import java.util.List;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

class OffendersResource_getPrimaryIdentifiersTest {

    private final OffenderService offenderService = mock(OffenderService.class);
    private final ArgumentCaptor<OffenderFilter> offenderFilterCaptor = ArgumentCaptor.forClass(OffenderFilter.class);
    private final ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);

    @BeforeEach
    void setUp() {
        RestAssuredMockMvc.standaloneSetup(
                MockMvcBuilders.standaloneSetup(
                        new OffendersResource(offenderService, mock(AlfrescoService.class), mock(DocumentService.class), mock(ContactService.class), mock(ConvictionService.class), mock(NsiService.class), mock(OffenderManagerService.class), mock(SentenceService.class), mock(UserService.class), mock(CurrentUserSupplier.class), mock(CustodyService.class)),
                        new SecureControllerAdvice())
                        .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
        );
        when(offenderService.getAllPrimaryIdentifiers(any(), any()))
                .thenReturn(new PageImpl<>(List.of(PrimaryIdentifiers
                        .builder()
                        .crn("X123456")
                        .offenderId(99L)
                        .build()), PageRequest.of(10, 20), 200));
    }

    @Test
    @DisplayName("Will return primary identifiers and paging information")
    void willReturnPrimaryIdentifiers() {
        given()
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get("/secure/offenders/primaryIdentifiers")
                .then()
                .statusCode(200)
                .body("content.size()", is(1))
                .body("content[0].crn", is("X123456"))
                .body("content[0].offenderId", is(99))
                .body("number", is(10))
                .body("size", is(20))
                .body("numberOfElements", is(1));
    }

    @Test
    @DisplayName("Will return 400 error if an invalid active filter is supplied")
    void willReturn400IfInvalidActiveFilterSupplied() {
        given()
                .contentType(APPLICATION_JSON_VALUE)
                .param("includeActiveOnly", "bananas")
                .when()
                .get("/secure/offenders/primaryIdentifiers")
                .then()
                .statusCode(400);
    }

    @Test
    @DisplayName("Will return 400 error if an invalid deleted filter is supplied")
    void willReturn400IfInvalidDeletedFilterSupplied() {
        given()
                .contentType(APPLICATION_JSON_VALUE)
                .param("includeDeleted", "bananas")
                .when()
                .get("/secure/offenders/primaryIdentifiers")
                .then()
                .statusCode(400);
    }

    @Test
    @DisplayName("Will pass active filter parameter to service")
    void activeFilterSuppliedToService() {
        given()
                .contentType(APPLICATION_JSON_VALUE)
                .param("includeActiveOnly", "true")
                .when()
                .get("/secure/offenders/primaryIdentifiers")
                .then()
                .statusCode(200);

        verify(offenderService).getAllPrimaryIdentifiers(offenderFilterCaptor.capture(), pageableCaptor.capture());

        assertThat(offenderFilterCaptor.getValue().isIncludeActiveOnly()).isTrue();
    }

    @Test
    @DisplayName("Active filter defaults to false")
    void activeFilterDefaultsToFalse() {
        given()
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get("/secure/offenders/primaryIdentifiers")
                .then()
                .statusCode(200);

        verify(offenderService).getAllPrimaryIdentifiers(offenderFilterCaptor.capture(), pageableCaptor.capture());

        assertThat(offenderFilterCaptor.getValue().isIncludeActiveOnly()).isFalse();
    }

    @Test
    @DisplayName("Will pass include deleted filter parameter to service")
    void includeDeletedFilterSuppliedToService() {
        given()
                .contentType(APPLICATION_JSON_VALUE)
                .param("includeDeleted", "true")
                .when()
                .get("/secure/offenders/primaryIdentifiers")
                .then()
                .statusCode(200);

        verify(offenderService).getAllPrimaryIdentifiers(offenderFilterCaptor.capture(), pageableCaptor.capture());

        assertThat(offenderFilterCaptor.getValue().isIncludeDeleted()).isTrue();
    }

    @Test
    @DisplayName("Include deleted filter defaults to false")
    void includeDeletedFilterDefaultsToFalse() {
        given()
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get("/secure/offenders/primaryIdentifiers")
                .then()
                .statusCode(200);

        verify(offenderService).getAllPrimaryIdentifiers(offenderFilterCaptor.capture(), pageableCaptor.capture());

        assertThat(offenderFilterCaptor.getValue().isIncludeDeleted()).isFalse();
    }

    @Test
    @DisplayName("Will pass page number parameter to service")
    void pageNumberSuppliedToService() {
        given()
                .contentType(APPLICATION_JSON_VALUE)
                .param("page", "23")
                .when()
                .get("/secure/offenders/primaryIdentifiers")
                .then()
                .statusCode(200);

        verify(offenderService).getAllPrimaryIdentifiers(offenderFilterCaptor.capture(), pageableCaptor.capture());

        assertThat(pageableCaptor.getValue().getPageNumber()).isEqualTo(23);
    }

    @Test
    @DisplayName("Will default to first page")
    void willDefaultToFirstPage() {
        given()
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get("/secure/offenders/primaryIdentifiers")
                .then()
                .statusCode(200);

        verify(offenderService).getAllPrimaryIdentifiers(offenderFilterCaptor.capture(), pageableCaptor.capture());

        assertThat(pageableCaptor.getValue().getPageNumber()).isEqualTo(0);
    }

    @Test
    @DisplayName("Will default to a page size of ten")
    void willDefaultToPageSizeOfTen() {
        given()
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get("/secure/offenders/primaryIdentifiers")
                .then()
                .statusCode(200);

        verify(offenderService).getAllPrimaryIdentifiers(offenderFilterCaptor.capture(), pageableCaptor.capture());

        assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(10);
    }

    @Test
    @DisplayName("Will pass page size parameter to service")
    void pageSizeSuppliedToService() {
        given()
                .contentType(APPLICATION_JSON_VALUE)
                .param("size", "5")
                .when()
                .get("/secure/offenders/primaryIdentifiers")
                .then()
                .statusCode(200);

        verify(offenderService).getAllPrimaryIdentifiers(offenderFilterCaptor.capture(), pageableCaptor.capture());

        assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(5);
    }

    @Test
    @DisplayName("Will default sort to offenderId ascending")
    void willDefaultToOffenderIdOrder() {
        given()
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get("/secure/offenders/primaryIdentifiers")
                .then()
                .statusCode(200);

        verify(offenderService).getAllPrimaryIdentifiers(offenderFilterCaptor.capture(), pageableCaptor.capture());

        assertThat(pageableCaptor.getValue().getSort()).isEqualTo(Sort.by(Sort.Direction.ASC, "offenderId"));
    }
    @Test
    @DisplayName("Can change sort to CRN descending")
    void canChangeOrder() {
        given()
                .contentType(APPLICATION_JSON_VALUE)
                .param("sort", "crn,desc")
                .when()
                .get("/secure/offenders/primaryIdentifiers")
                .then()
                .statusCode(200);

        verify(offenderService).getAllPrimaryIdentifiers(offenderFilterCaptor.capture(), pageableCaptor.capture());

        assertThat(pageableCaptor.getValue().getSort()).isEqualTo(Sort.by(Sort.Direction.DESC, "crn"));
    }

}
