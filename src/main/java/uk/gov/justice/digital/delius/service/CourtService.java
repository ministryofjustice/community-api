package uk.gov.justice.digital.delius.service;

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
            courtEntity.setCourtType(lookupSupplier.courtTypeSupplier(court.getCourtTypeCode()).orElseThrow());
            courtEntity.setCounty(court.getCounty());
            courtEntity.setFax(court.getFax());
            courtEntity.setLocality(court.getLocality());
            courtEntity.setPostcode(court.getPostcode());
            courtEntity.setStreet(court.getStreet());
            courtEntity.setTelephoneNumber(court.getTelephoneNumber());
            courtEntity.setTown(court.getTown());
            courtEntity.setSelectable(court.isActive() ? "Y" : "N");
        } else {
            log.warn(String.format("This Court Update feature for %s is currently switched off", code));
        }
        return CourtTransformer.courtOf(courtEntity);
    }

    private boolean isAllowedToUpdate(String code) {
        return code.matches(featureSwitches.getRegisters().getCourtCodeAllowedPattern());
    }

    @Transactional
    public Court createNewCourt(NewCourtDto court) {
        // TODO check feature switch and create court
        return Court.builder().code(court.code()).courtName(court.courtName()).build();
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
}
