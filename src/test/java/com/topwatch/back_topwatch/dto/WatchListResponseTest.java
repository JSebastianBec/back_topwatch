package com.topwatch.back_topwatch.dto;

import com.topwatch.back_topwatch.domain.WatchList;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WatchListResponseTest {

    private final WatchList watchList = WatchList.builder().id(10L).name("Para el finde").build();

    @Test
    void from_marksListAsStartedWhenViewExists() {
        WatchListResponse response = WatchListResponse.from(watchList, 100L);

        assertThat(response.started()).isTrue();
        assertThat(response.viewId()).isEqualTo(100L);
    }

    @Test
    void from_marksListAsNotStartedWithoutView() {
        WatchListResponse response = WatchListResponse.from(watchList);

        assertThat(response.started()).isFalse();
        assertThat(response.viewId()).isNull();
    }

    @Test
    void detailFrom_carriesStartedState() {
        WatchListDetailResponse started = WatchListDetailResponse.from(watchList, List.of(), 100L);
        WatchListDetailResponse notStarted = WatchListDetailResponse.from(watchList, List.of(), null);

        assertThat(started.started()).isTrue();
        assertThat(started.viewId()).isEqualTo(100L);
        assertThat(notStarted.started()).isFalse();
        assertThat(notStarted.viewId()).isNull();
    }

    @Test
    void owned_isTrueOnlyForTheCreator() {
        com.topwatch.back_topwatch.domain.User creator =
                com.topwatch.back_topwatch.domain.User.builder().id(1L).nickname("sebas").build();
        WatchList created = WatchList.builder().id(11L).name("Mia").creator(creator).build();

        assertThat(WatchListResponse.from(created, null, 1L).owned()).isTrue();
        assertThat(WatchListResponse.from(created, null, 2L).owned()).isFalse();
        assertThat(WatchListResponse.from(created, null, 1L).creator().nickname()).isEqualTo("sebas");
        assertThat(WatchListDetailResponse.from(created, List.of(), null, 1L).owned()).isTrue();
        assertThat(WatchListDetailResponse.from(created, List.of(), null, 2L).owned()).isFalse();
    }

}
