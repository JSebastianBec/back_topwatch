package com.topwatch.back_topwatch.dto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WatchListViewResponseTest {

    @Test
    void progress_isWatchedOverTotal() {
        assertThat(WatchListViewResponse.progress(1, 4)).isEqualTo(25.0);
        assertThat(WatchListViewResponse.progress(4, 4)).isEqualTo(100.0);
        assertThat(WatchListViewResponse.progress(1, 3)).isEqualTo(33.33);
    }

    @Test
    void progress_isZeroForEmptyList() {
        assertThat(WatchListViewResponse.progress(0, 0)).isEqualTo(0.0);
    }

}
