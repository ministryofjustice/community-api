package uk.gov.justice.digital.delius.service;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.justice.digital.delius.controller.NotFoundException;
import uk.gov.justice.digital.delius.data.api.Court;
import uk.gov.justice.digital.delius.data.api.NewCourtDto;
import uk.gov.justice.digital.delius.data.api.UpdateCourtDto;
import uk.gov.justice.digital.delius.jpa.standard.repository.CourtRepository;
import uk.gov.justice.digital.delius.transformers.CourtTransformer;

import java.util.Optional;

@Service
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class CourtService {
    private final CourtRepository courtRepository;

    public Court updateCourt(String code, UpdateCourtDto court) {
        // TODO check feature switch and update court
        return Court.builder().code(code).courtName(court.getCourtName()).build();
    }

    public Court createNewCourt(NewCourtDto court) {
        // TODO check feature switch and create court
        return Court.builder().code(court.code()).courtName(court.courtName()).build();
    }

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
