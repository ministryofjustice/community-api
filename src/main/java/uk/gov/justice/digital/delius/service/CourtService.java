package uk.gov.justice.digital.delius.service;

import io.vavr.control.Either;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.digital.delius.config.FeatureSwitches;
import uk.gov.justice.digital.delius.controller.NotFoundException;
import uk.gov.justice.digital.delius.data.api.Court;
import uk.gov.justice.digital.delius.data.api.NewCourtDto;
import uk.gov.justice.digital.delius.data.api.UpdateCourtDto;
import uk.gov.justice.digital.delius.jpa.standard.repository.CourtRepository;
import uk.gov.justice.digital.delius.transformers.CourtTransformer;

import java.util.Optional;

@Service
@AllArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class CourtService {
    private final CourtRepository courtRepository;
    private final LookupSupplier lookupSupplier;
    private final FeatureSwitches featureSwitches;

    @Transactional
    public Court updateCourt(String code, UpdateCourtDto court) {
        final var courtEntity = getMostLikelyCourt(code).orElseThrow(() -> new NotFoundException(String
            .format("Court %s not found", code)));
        if (isAllowedToUpdate(code)) {
            courtEntity.setBuildingName(court.getBuildingName());
            courtEntity.setCourtName(court.getCourtName());
            courtEntity.setCountry(court.getCountry());
            courtEntity.setCourtType(lookupSupplier.courtTypeByCode(court.getCourtTypeCode()).orElseThrow());
            courtEntity.setCounty(court.getCounty());
            courtEntity.setFax(court.getFax());
            courtEntity.setLocality(court.getLocality());
            courtEntity.setPostcode(court.getPostcode());
            courtEntity.setStreet(court.getStreet());
            courtEntity.setTelephoneNumber(court.getTelephoneNumber());
            courtEntity.setTown(court.getTown());
            courtEntity.setSelectable(court.isActive() ? "Y" : "N");
        } else {
            log.warn("This Court Update feature for {} is currently switched off", code);
        }
        return CourtTransformer.courtOf(courtEntity);
    }

    private boolean isAllowedToUpdate(String code) {
        return code.matches(featureSwitches.getRegisters().getCourtCodeAllowedPattern());
    }

    @Transactional
    public Either<CourtAlreadyExists, Court> createNewCourt(NewCourtDto court) {
        var maybeExistingCourt = getMostLikelyCourt(court.code());

        if (maybeExistingCourt.isPresent()) {
            return Either.left(new CourtAlreadyExists(court.code()));
        } else {
            final var courtEntity = uk.gov.justice.digital.delius.jpa.standard.entity.Court
                .builder()
                .code(court.code())
                .buildingName(court.buildingName())
                .courtName(court.courtName())
                .country(court.country())
                .courtType(lookupSupplier.courtTypeByCode(court.courtTypeCode()).orElseThrow())
                .county(court.county())
                .fax(court.fax())
                .locality(court.locality())
                .postcode(court.postcode())
                .probationArea(lookupSupplier.probationAreaByCode(court.probationAreaCode()).orElseThrow())
                .street(court.street())
                .telephoneNumber(court.telephoneNumber())
                .town(court.town())
                .selectable(court.active() ? "Y" : "N")
                .build();

            if (isAllowedToUpdate(court.code())) {
                courtRepository.save(courtEntity);
            } else {
                log.warn("This Court Creation feature for {} is currently switched off", court.code());
            }
            return Either.right(CourtTransformer.courtOf(courtEntity));
        }

    }

    @Transactional(readOnly = true)
    public Court getCourt(String code) {
        return getMostLikelyCourt(code).map(CourtTransformer::courtOf).orElseThrow(() -> new NotFoundException(String
            .format("Court %s not found", code)));
    }

    private Optional<uk.gov.justice.digital.delius.jpa.standard.entity.Court> getMostLikelyCourt(String code) {
        // this diabolic code is needed since the Delius test environment has duplicates for a number of courts (court code has no unique constraint)
        // this is not an issue for prod and I guess the UI constrains the code to be unique
        return courtRepository.findByCode(code).stream().reduce((current, mostLikely) -> {
            if (mostLikely == null) {
                return current;
            } else if (current.getSelectable().equals("Y")) {
                return current;
            }
            return mostLikely;
        });
    }

    public static record CourtAlreadyExists(String courtCode) {
    }
}
