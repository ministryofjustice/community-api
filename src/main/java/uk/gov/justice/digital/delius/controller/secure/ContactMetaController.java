package uk.gov.justice.digital.delius.controller.secure;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.digital.delius.controller.advice.ErrorResponse;
import uk.gov.justice.digital.delius.data.api.AvailableContactOutcomeTypes;
import uk.gov.justice.digital.delius.data.api.ContactType;
import uk.gov.justice.digital.delius.service.ContactService;

import javax.validation.constraints.NotNull;
import java.util.List;

@RestController
@Api(tags = {"Contact and attendance"})
@PreAuthorize("hasRole('ROLE_COMMUNITY')")
@RequestMapping(value = "secure", produces = MediaType.APPLICATION_JSON_VALUE)
@AllArgsConstructor
public class ContactMetaController {
    private final ContactService contactService;

    @GetMapping("/contact-types")
    @ApiResponses(
        value = {
            @ApiResponse(code = 200, message = "Contact types belonging to specified categories", response = ContactType.class, responseContainer = "List"),
            @ApiResponse(code = 403, message = "Requires role ROLE_COMMUNITY"),
            @ApiResponse(code = 500, message = "Unrecoverable error whilst processing request.", response = ErrorResponse.class)
        })
    @ApiOperation(value = "Returns all selectable contact types or those in specified categories")
    public List<ContactType> getContactTypes(final @ApiParam(name = "categories", value = "Contact category codes", example = "LT", required = false)  @RequestParam(value = "categories", required = false) List<String> categories) {
        return this.contactService.getContactTypes(categories);
    }

    @GetMapping("/contact-types/{contactTypeCode}/outcome-types")
    @ApiResponses(
        value = {
            @ApiResponse(code = 403, message = "Requires role ROLE_COMMUNITY"),
            @ApiResponse(code = 404, message = "The contact type does not exit"),
            @ApiResponse(code = 500, message = "Unrecoverable error whilst processing request.", response = ErrorResponse.class)
        })
    @ApiOperation(value = "Returns all selectable contact types or those in specified categories")
    public AvailableContactOutcomeTypes getContactTypeOutcomes(final @ApiParam(name = "contactTypeCode", value = "contact type code", example = "APAT", required = true) @NotNull @PathVariable("contactTypeCode")  String contactTypeCode) {
        throw new NotImplementedException();
    }
}
