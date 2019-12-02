package uk.gov.justice.digital.delius.jpa.standard.repository;

public interface StaffHelperRepository {
    String getNextStaffCode(String probationAreaCode);
}
