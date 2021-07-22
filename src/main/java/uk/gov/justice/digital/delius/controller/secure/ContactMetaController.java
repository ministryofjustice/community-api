package uk.gov.justice.digital.delius.controller.secure;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.digital.delius.controller.advice.ErrorResponse;
import uk.gov.justice.digital.delius.data.api.ContactType;
import uk.gov.justice.digital.delius.service.ContactService;

import java.util.List;

@RestController
@Api(tags = {"Contacts"})
@PreAuthorize("hasRole('ROLE_COMMUNITY')")
@RequestMapping(value = "secure", produces = MediaType.APPLICATION_JSON_VALUE)
@AllArgsConstructor

public class ContactMetaController {


    private ContactService contactService;

    @GetMapping("/contact-types")
    @ApiResponses(
        value = {
            @ApiResponse(code = 200, message = "All valid contact types", response = ContactType.class, responseContainer = "List"),
            @ApiResponse(code = 403, message = "Requires role ROLE_COMMUNITY"),
            @ApiResponse(code = 500, message = "Unrecoverable error whilst processing request.", response = ErrorResponse.class)
        })
    @ApiOperation(value = "Determines all valid contact types ")
    public List<ContactType> getContactTypes(final @RequestParam(value = "categories", required = false) List<String> categories) {
        return this.contactService.getContactTypes(categories);
    }
}
