package uk.gov.justice.digital.delius.jpa.standard.entity;

import org.junit.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class ReleaseTest {

    private Release release = new Release();

    @Test
    public void findLatestRecall_noRecalls_returnsEmpty() {
        release.setRecalls(List.of());

        assertThat(release.findLatestRecall()).isEmpty();
    }

    @Test
    public void findLatestRecall_singleRecall_returnsRecall() {
        LocalDateTime now = LocalDateTime.now();
        release.setRecalls(List.of(Recall.builder().recallDate(now).build()));

        Optional<Recall> actualRecall = release.findLatestRecall();

        assertThat(actualRecall).isPresent();
        assertThat(actualRecall.get().getRecallDate()).isEqualTo(now);
    }

    @Test
    public void findLatestRecall_multipleRecalls_returnsLatest() {
        LocalDateTime now = LocalDateTime.now();
        Recall recall1 = Recall.builder().recallDate(now.minusDays(2L)).build();
        Recall recall2 = Recall.builder().recallDate(now.minusDays(1L)).build();
        release.setRecalls(List.of(recall1, recall2));

        Optional<Recall> actualRecall = release.findLatestRecall();

        assertThat(actualRecall).isPresent();
        assertThat(actualRecall.get().getRecallDate()).isEqualTo(recall2.getRecallDate());
    }
}
