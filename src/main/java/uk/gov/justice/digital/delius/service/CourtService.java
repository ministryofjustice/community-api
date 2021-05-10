package uk.gov.justice.digital.delius.service;

import org.springframework.stereotype.Service;
import uk.gov.justice.digital.delius.data.api.Court;
import uk.gov.justice.digital.delius.data.api.NewCourtDto;
import uk.gov.justice.digital.delius.data.api.UpdateCourtDto;

@Service
public class CourtService {
    public Court updateCourt(String code, UpdateCourtDto court) {
        // TODO check feature switch and update court
        return Court.builder().code(code).courtName(court.getCourtName()).build();
    }

    public Court createNewCourt(NewCourtDto court) {
        // TODO check feature switch and create court
        return Court.builder().code(court.getCode()).courtName(court.getCourtName()).build();
    }

    public Court getCourt(String code) {
        // TODO get court
        return Court.builder().code(code).build();
    }
}
