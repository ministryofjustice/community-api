package uk.gov.justice.digital.delius.jpa.standard.repository;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.stream.IntStream;

@Repository
@Profile("!oracle")
public class StaffHelperNoProceduresRepositoryImpl implements StaffHelperRepository {
    private final StaffRepository staffRepository;

    public StaffHelperNoProceduresRepositoryImpl(StaffRepository staffRepository) {
        this.staffRepository = staffRepository;
    }

    @Override
    public String getNextStaffCode(String probationAreaCode) {
        final var possibleCodes = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
                .chars()
                .mapToObj(letter -> String.valueOf((char) letter))
                .flatMap(prefix -> IntStream.range(1, 999)
                        .mapToObj(number -> String.format("%s%s%03d", probationAreaCode, prefix, number))).toList();


        for (String possibleCode : possibleCodes) {
            if (staffRepository.findByOfficerCode(possibleCode).isEmpty()) {
                return possibleCode;
            }
        }

        throw new RuntimeException("No usable staff codes found");
    }

    public static void main(String[] args) {
        new StaffHelperNoProceduresRepositoryImpl(null).getNextStaffCode("ABC");
    }
}
