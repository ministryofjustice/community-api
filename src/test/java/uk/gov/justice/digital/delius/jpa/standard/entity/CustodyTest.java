package uk.gov.justice.digital.delius.jpa.standard.entity;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class CustodyTest {

    private Custody custody = new Custody();

    @Test
    public void findLatestRelease_noReleases_returnsEmpty() {
        custody.setReleases(List.of());

        assertThat(custody.findLatestRelease()).isEmpty();
    }

    @Test
    public void findLatestRelease_singleRelease_isReturned() {
        LocalDateTime now = LocalDateTime.now();
        Release release = Release.builder().actualReleaseDate(now).build();
        custody.setReleases(List.of(release));

        Optional<Release> actualRelease = custody.findLatestRelease();

        assertThat(actualRelease).isPresent();
        assertThat(actualRelease.get().getActualReleaseDate()).isEqualTo(now);
    }

    @Test
    public void findLatestRelease_multipleReleases_latestIsReturned() {
        LocalDateTime now = LocalDateTime.now();
        Release release1 = Release.builder().actualReleaseDate(now.minusDays(2)).build();
        Release release2 = Release.builder().actualReleaseDate(now.minusDays(1)).build();
        custody.setReleases(List.of(release1, release2));

        Optional<Release> actualRelease = custody.findLatestRelease();

        assertThat(actualRelease).isPresent();
        assertThat(actualRelease.get().getActualReleaseDate()).isEqualTo(release2.getActualReleaseDate());
    }

    @Test
    public void findLatestRelease_softDeleted_isIgnored() {
        Release release = Release.builder().softDeleted(1L).build();
        custody.setReleases(List.of(release));

        assertThat(custody.findLatestRelease()).isEmpty();
    }

}
