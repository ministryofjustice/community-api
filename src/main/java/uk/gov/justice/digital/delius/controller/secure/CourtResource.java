package uk.gov.justice.digital.delius.controller.secure;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.digital.delius.controller.BadRequestException;
import uk.gov.justice.digital.delius.controller.NotFoundException;
import uk.gov.justice.digital.delius.controller.advice.ErrorResponse;
import uk.gov.justice.digital.delius.data.api.Court;
import uk.gov.justice.digital.delius.data.api.NewCourtDto;
import uk.gov.justice.digital.delius.data.api.UpdateCourtDto;
import uk.gov.justice.digital.delius.service.CourtService;
import uk.gov.justice.digital.delius.service.CourtService.NotFound;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.List;

@Api(tags = "Courts")
@RestController
@Slf4j
@RequestMapping(value = "secure/courts", produces = MediaType.APPLICATION_JSON_VALUE)
@AllArgsConstructor
@PreAuthorize("hasRole('ROLE_MAINTAIN_REF_DATA') and hasAuthority('SCOPE_write')")
public class CourtResource {
    private final CourtService courtService;

    @ApiOperation(
        value = "Experimental API to update a court entity", notes = "requires ROLE_MAINTAIN_REF_DATA and write scope")
    @ApiResponses(
        value = {
            @ApiResponse(code = 400, message = "Invalid request", response = ErrorResponse.class),
            @ApiResponse(code = 403, message = "Requires role ROLE_MAINTAIN_REF_DATA and write scope"),
            @ApiResponse(code = 404, message = "Court not found", response = ErrorResponse.class),
            @ApiResponse(code = 409, message = "Attempt to retrieve the latest update that is already in progress", response = ErrorResponse.class)
        })
    @PutMapping(value = "/code/{code}")
    public Court updateCourt(@ApiParam(value = "unique code for this court", example = "SALEMC")
                             @NotBlank(message = "Court code is required")
                             @PathVariable String code, @RequestBody @Valid UpdateCourtDto court) {
        return courtService.updateCourt(code, court).getOrElseThrow((e) -> {
            if (e instanceof NotFound) {
                throw new NotFoundException(e.message());
            } else {
                throw new BadRequestException(e.message());
            }
        });
    }

    @ApiOperation(
        value = "Experimental API to insert a court entity", notes = "requires ROLE_MAINTAIN_REF_DATA and write scope")
    @ApiResponses(
        value = {
            @ApiResponse(code = 400, message = "Invalid request", response = ErrorResponse.class),
            @ApiResponse(code = 403, message = "Requires role ROLE_MAINTAIN_REF_DATA and write scope"),
            @ApiResponse(code = 404, message = "Court not found", response = ErrorResponse.class),
            @ApiResponse(code = 409, message = "Attempt to retrieve the latest update that is already in progress", response = ErrorResponse.class)
        })
    @PostMapping
    public Court insertCourt(@RequestBody @Valid NewCourtDto court) {
        return courtService.createNewCourt(court).getOrElseThrow((e) -> {
            throw new BadRequestException(e.message());
        });
    }


    @ApiOperation(
        value = "Experimental API to get a court entity", notes = "requires ROLE_MAINTAIN_REF_DATA and read scope")
    @ApiResponses(
        value = {
            @ApiResponse(code = 400, message = "Invalid request", response = ErrorResponse.class),
            @ApiResponse(code = 403, message = "Requires role ROLE_MAINTAIN_REF_DATA and read scope"),
            @ApiResponse(code = 404, message = "Court not found", response = ErrorResponse.class),
            @ApiResponse(code = 409, message = "Attempt to retrieve the latest update that is already in progress", response = ErrorResponse.class)
        })
    @GetMapping(value = "/code/{code}")
    @PreAuthorize("hasRole('ROLE_MAINTAIN_REF_DATA') and hasAuthority('SCOPE_read')")
    public Court getCourt(@PathVariable String code) {
        return courtService.getCourt(code);
    }
    @ApiOperation(
        value = "Experimental API to get all court entities", notes = "requires ROLE_MAINTAIN_REF_DATA and read scope")
    @ApiResponses(
        value = {
            @ApiResponse(code = 400, message = "Invalid request", response = ErrorResponse.class),
            @ApiResponse(code = 403, message = "Requires role ROLE_MAINTAIN_REF_DATA and read scope"),
            @ApiResponse(code = 409, message = "Attempt to retrieve the latest update that is already in progress", response = ErrorResponse.class)
        })
    @GetMapping
    @PreAuthorize("hasRole('ROLE_MAINTAIN_REF_DATA') and hasAuthority('SCOPE_read')")
    public List<Court> getCourts() {
        return courtService.getCourts();
    }
}
