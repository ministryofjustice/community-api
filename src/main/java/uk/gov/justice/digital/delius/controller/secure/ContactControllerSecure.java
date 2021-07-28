package uk.gov.justice.digital.delius.controller.secure;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.digital.delius.controller.NotFoundException;
import uk.gov.justice.digital.delius.controller.advice.ErrorResponse;
import uk.gov.justice.digital.delius.data.api.Contact;
import uk.gov.justice.digital.delius.service.ContactService;
import uk.gov.justice.digital.delius.service.OffenderService;

@RestController
@Api(tags = {"Contact and attendance"})
@PreAuthorize("hasRole('ROLE_COMMUNITY')")
@RequestMapping(value = "secure", produces = MediaType.APPLICATION_JSON_VALUE)
@AllArgsConstructor
public class ContactControllerSecure {

    private final ContactService contactService;
    private final OffenderService offenderService;

    @GetMapping("/offenders/crn/{crn}/contacts/{contactId}")
    @ApiResponses(
        value = {
            @ApiResponse(code = 200, message = "Contact with matching id and crn", response = Contact.class),
            @ApiResponse(code = 403, message = "Requires role ROLE_COMMUNITY"),
            @ApiResponse(code = 404, message = "Contact or offender not found"),
            @ApiResponse(code = 500, message = "Unrecoverable error whilst processing request.", response = ErrorResponse.class)
        }
    )
    @ApiOperation(value = "Gets offender contact by CRN & contact id")
    public Contact getOffenderContactByCrn(final @PathVariable("crn") String crn, final @PathVariable("contactId") Long contactId) {
        final var offenderId = offenderService.offenderIdOfCrn(crn).orElseThrow(() ->
            new NotFoundException(String.format("Offender with crn %s not found", crn)));
        return contactService.getContact(offenderId, contactId).orElseThrow(() ->
            new NotFoundException(String.format("Contact with id %s not found", contactId)));
    }

}
