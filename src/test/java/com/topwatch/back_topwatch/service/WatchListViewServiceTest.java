package com.topwatch.back_topwatch.service;

import com.topwatch.back_topwatch.domain.Item;
import com.topwatch.back_topwatch.domain.User;
import com.topwatch.back_topwatch.domain.WatchList;
import com.topwatch.back_topwatch.domain.WatchListItem;
import com.topwatch.back_topwatch.domain.WatchListView;
import com.topwatch.back_topwatch.domain.WatchListViewItem;
import com.topwatch.back_topwatch.exception.BadRequestException;
import com.topwatch.back_topwatch.exception.DuplicateResourceException;
import com.topwatch.back_topwatch.exception.ResourceNotFoundException;
import com.topwatch.back_topwatch.repository.WatchListItemRepository;
import com.topwatch.back_topwatch.repository.WatchListRepository;
import com.topwatch.back_topwatch.repository.WatchListViewItemRepository;
import com.topwatch.back_topwatch.repository.WatchListViewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WatchListViewServiceTest {

    @Mock
    private WatchListViewRepository watchListViewRepository;

    @Mock
    private WatchListViewItemRepository watchListViewItemRepository;

    @Mock
    private WatchListRepository watchListRepository;

    @Mock
    private WatchListItemRepository watchListItemRepository;

    @InjectMocks
    private WatchListViewService watchListViewService;

    private User owner;
    private User viewer;
    private WatchList watchList;
    private WatchListView view;

    @BeforeEach
    void setUp() {
        owner = User.builder().id(1L).nickname("owner").build();
        viewer = User.builder().id(2L).nickname("viewer").build();
        watchList = WatchList.builder().id(10L).name("Para el finde").creator(owner).build();
        view = WatchListView.builder().id(100L).viewer(viewer).watchList(watchList).build();
    }

    @Test
    void startView_createsViewForSomeoneElsesList() {
        when(watchListRepository.findById(10L)).thenReturn(Optional.of(watchList));
        when(watchListViewRepository.existsByViewer_IdAndWatchList_Id(2L, 10L)).thenReturn(false);
        when(watchListViewRepository.save(any(WatchListView.class))).thenAnswer(invocation -> invocation.getArgument(0));

        WatchListView result = watchListViewService.startView(10L, viewer);

        assertThat(result.getViewer()).isEqualTo(viewer);
        assertThat(result.getWatchList()).isEqualTo(watchList);
        verify(watchListRepository, never()).save(any());
    }

    @Test
    void startView_rejectsSecondViewOfSameList() {
        when(watchListRepository.findById(10L)).thenReturn(Optional.of(watchList));
        when(watchListViewRepository.existsByViewer_IdAndWatchList_Id(2L, 10L)).thenReturn(true);

        assertThatThrownBy(() -> watchListViewService.startView(10L, viewer))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void startView_throwsWhenListMissing() {
        when(watchListRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> watchListViewService.startView(10L, viewer))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void findOwnView_rejectsOtherUsers() {
        when(watchListViewRepository.findById(100L)).thenReturn(Optional.of(view));

        assertThatThrownBy(() -> watchListViewService.findOwnView(100L, owner))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void setWatched_createsViewItemWithDate() {
        WatchListItem watchListItem = WatchListItem.builder().id(50L).watchList(watchList).item(Item.builder().id(7L).build()).build();
        when(watchListViewRepository.findById(100L)).thenReturn(Optional.of(view));
        when(watchListItemRepository.findByIdAndWatchList_Id(50L, 10L)).thenReturn(Optional.of(watchListItem));
        when(watchListViewItemRepository.findByWatchListView_IdAndWatchListItem_Id(100L, 50L)).thenReturn(Optional.empty());

        watchListViewService.setWatched(100L, 50L, true, viewer);

        ArgumentCaptor<WatchListViewItem> captor = ArgumentCaptor.forClass(WatchListViewItem.class);
        verify(watchListViewItemRepository).save(captor.capture());
        assertThat(captor.getValue().getWatched()).isTrue();
        assertThat(captor.getValue().getWatchedDate()).isNotNull();
        assertThat(captor.getValue().getWatchListItem()).isEqualTo(watchListItem);
    }

    @Test
    void setWatched_falseClearsDate() {
        WatchListItem watchListItem = WatchListItem.builder().id(50L).watchList(watchList).build();
        WatchListViewItem existing = WatchListViewItem.builder()
                .id(500L).watched(true).watchedDate(Instant.now()).watchListView(view).watchListItem(watchListItem).build();
        when(watchListViewRepository.findById(100L)).thenReturn(Optional.of(view));
        when(watchListItemRepository.findByIdAndWatchList_Id(50L, 10L)).thenReturn(Optional.of(watchListItem));
        when(watchListViewItemRepository.findByWatchListView_IdAndWatchListItem_Id(100L, 50L)).thenReturn(Optional.of(existing));

        watchListViewService.setWatched(100L, 50L, false, viewer);

        assertThat(existing.getWatched()).isFalse();
        assertThat(existing.getWatchedDate()).isNull();
        verify(watchListViewItemRepository).save(existing);
    }

    @Test
    void setWatched_throwsWhenItemNotInList() {
        when(watchListViewRepository.findById(100L)).thenReturn(Optional.of(view));
        when(watchListItemRepository.findByIdAndWatchList_Id(50L, 10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> watchListViewService.setWatched(100L, 50L, true, viewer))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void setWatched_trueAgainKeepsOriginalDate() {
        Instant original = Instant.parse("2026-01-01T10:00:00Z");
        WatchListItem watchListItem = WatchListItem.builder().id(50L).watchList(watchList).build();
        WatchListViewItem existing = WatchListViewItem.builder()
                .id(500L).watched(true).watchedDate(original).watchListView(view).watchListItem(watchListItem).build();
        when(watchListViewRepository.findById(100L)).thenReturn(Optional.of(view));
        when(watchListItemRepository.findByIdAndWatchList_Id(50L, 10L)).thenReturn(Optional.of(watchListItem));
        when(watchListViewItemRepository.findByWatchListView_IdAndWatchListItem_Id(100L, 50L)).thenReturn(Optional.of(existing));

        watchListViewService.setWatched(100L, 50L, true, viewer);

        assertThat(existing.getWatched()).isTrue();
        assertThat(existing.getWatchedDate()).isEqualTo(original);
    }

    @Test
    void setWatched_trueAfterUnwatchedSetsNewDate() {
        WatchListItem watchListItem = WatchListItem.builder().id(50L).watchList(watchList).build();
        WatchListViewItem existing = WatchListViewItem.builder()
                .id(500L).watched(false).watchedDate(null).watchListView(view).watchListItem(watchListItem).build();
        when(watchListViewRepository.findById(100L)).thenReturn(Optional.of(view));
        when(watchListItemRepository.findByIdAndWatchList_Id(50L, 10L)).thenReturn(Optional.of(watchListItem));
        when(watchListViewItemRepository.findByWatchListView_IdAndWatchListItem_Id(100L, 50L)).thenReturn(Optional.of(existing));

        watchListViewService.setWatched(100L, 50L, true, viewer);

        assertThat(existing.getWatched()).isTrue();
        assertThat(existing.getWatchedDate()).isNotNull();
    }

    @Test
    void findRecentlyWatchedItems_queriesViewerWithLimit() {
        WatchListViewItem recent = WatchListViewItem.builder().id(500L).watched(true).watchedDate(Instant.now()).build();
        when(watchListViewItemRepository.findRecentlyWatchedByViewer(2L, PageRequest.of(0, 5))).thenReturn(List.of(recent));

        assertThat(watchListViewService.findRecentlyWatchedItems(viewer, 5)).containsExactly(recent);
    }

    @Test
    void findRecentlyWatchedItems_rejectsInvalidLimit() {
        assertThatThrownBy(() -> watchListViewService.findRecentlyWatchedItems(viewer, 0))
                .isInstanceOf(BadRequestException.class);
        assertThatThrownBy(() -> watchListViewService.findRecentlyWatchedItems(viewer, 101))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void findViewIdsByWatchListId_mapsStartedListsToViewIds() {
        WatchList other = WatchList.builder().id(11L).name("Otra").creator(owner).build();
        WatchListView otherView = WatchListView.builder().id(101L).viewer(viewer).watchList(other).build();
        when(watchListViewRepository.findByViewer_IdOrderByStartDateDesc(2L)).thenReturn(List.of(view, otherView));

        assertThat(watchListViewService.findViewIdsByWatchListId(viewer))
                .isEqualTo(Map.of(10L, 100L, 11L, 101L));
    }

    @Test
    void findViewIdsByWatchListId_isEmptyWhenNothingStarted() {
        when(watchListViewRepository.findByViewer_IdOrderByStartDateDesc(1L)).thenReturn(List.of());

        assertThat(watchListViewService.findViewIdsByWatchListId(owner)).isEmpty();
    }


    @Test
    void deleteView_removesWatchedStateThenView() {
        when(watchListViewRepository.findById(100L)).thenReturn(Optional.of(view));

        watchListViewService.deleteView(100L, viewer);

        InOrder order = inOrder(watchListViewItemRepository, watchListViewRepository);
        order.verify(watchListViewItemRepository).deleteByViewId(100L);
        order.verify(watchListViewRepository).delete(view);
        verify(watchListRepository, never()).delete(any());
    }

    @Test
    void deleteView_rejectsOtherUsers() {
        when(watchListViewRepository.findById(100L)).thenReturn(Optional.of(view));

        assertThatThrownBy(() -> watchListViewService.deleteView(100L, owner))
                .isInstanceOf(AccessDeniedException.class);
        verify(watchListViewRepository, never()).delete(any());
        verify(watchListViewItemRepository, never()).deleteByViewId(any());
    }

    @Test
    void deleteView_throwsWhenViewMissing() {
        when(watchListViewRepository.findById(100L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> watchListViewService.deleteView(100L, viewer))
                .isInstanceOf(ResourceNotFoundException.class);
    }

}
