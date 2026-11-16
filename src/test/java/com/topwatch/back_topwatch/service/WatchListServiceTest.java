package com.topwatch.back_topwatch.service;

import com.topwatch.back_topwatch.domain.SavedWatchList;
import com.topwatch.back_topwatch.domain.User;
import com.topwatch.back_topwatch.domain.WatchList;
import com.topwatch.back_topwatch.exception.BadRequestException;
import com.topwatch.back_topwatch.exception.DuplicateResourceException;
import com.topwatch.back_topwatch.exception.ResourceNotFoundException;
import com.topwatch.back_topwatch.repository.ItemRepository;
import com.topwatch.back_topwatch.repository.SavedWatchListRepository;
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
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WatchListServiceTest {

    @Mock
    private WatchListRepository watchListRepository;

    @Mock
    private WatchListItemRepository watchListItemRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private SavedWatchListRepository savedWatchListRepository;

    @Mock
    private WatchListViewRepository watchListViewRepository;

    @Mock
    private WatchListViewItemRepository watchListViewItemRepository;

    @InjectMocks
    private WatchListService watchListService;

    private User me;
    private WatchList mine;
    private WatchList theirs;
    private WatchList theirsToo;

    @BeforeEach
    void setUp() {
        me = User.builder().id(1L).nickname("me").build();
        User other = User.builder().id(2L).nickname("other").build();
        mine = WatchList.builder().id(10L).name("Mine").creator(me).build();
        theirs = WatchList.builder().id(20L).name("Theirs").creator(other).build();
        theirsToo = WatchList.builder().id(21L).name("Theirs too").creator(other).build();
    }

    @Test
    @SuppressWarnings("unchecked")
    void addToMyWatchLists_savesEveryListForTheUser() {
        when(watchListRepository.findAllById(any())).thenReturn(List.of(theirs, theirsToo));
        when(savedWatchListRepository.findByUser_IdAndWatchList_IdIn(any(), any())).thenReturn(List.of());

        List<WatchList> result = watchListService.addToMyWatchLists(List.of(20L, 21L), me);

        assertThat(result).containsExactly(theirs, theirsToo);
        ArgumentCaptor<List<SavedWatchList>> captor = ArgumentCaptor.forClass(List.class);
        verify(savedWatchListRepository).saveAll(captor.capture());
        assertThat(captor.getValue()).allMatch(saved -> saved.getUser() == me);
        assertThat(captor.getValue()).extracting(SavedWatchList::getWatchList).containsExactly(theirs, theirsToo);
    }

    @Test
    void addToMyWatchLists_rejectsOwnLists() {
        when(watchListRepository.findAllById(any())).thenReturn(List.of(mine, theirs));

        assertThatThrownBy(() -> watchListService.addToMyWatchLists(List.of(10L, 20L), me))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("10");
        verify(savedWatchListRepository, never()).saveAll(any());
    }

    @Test
    void addToMyWatchLists_rejectsListsAlreadyAdded() {
        when(watchListRepository.findAllById(any())).thenReturn(List.of(theirs));
        when(savedWatchListRepository.findByUser_IdAndWatchList_IdIn(any(), any()))
                .thenReturn(List.of(SavedWatchList.builder().user(me).watchList(theirs).build()));

        assertThatThrownBy(() -> watchListService.addToMyWatchLists(List.of(20L), me))
                .isInstanceOf(DuplicateResourceException.class);
        verify(savedWatchListRepository, never()).saveAll(any());
    }

    @Test
    void addToMyWatchLists_rejectsUnknownLists() {
        when(watchListRepository.findAllById(any())).thenReturn(List.of(theirs));

        assertThatThrownBy(() -> watchListService.addToMyWatchLists(List.of(20L, 99L), me))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void addToMyWatchLists_rejectsDuplicatedIds() {
        assertThatThrownBy(() -> watchListService.addToMyWatchLists(List.of(20L, 20L), me))
                .isInstanceOf(BadRequestException.class);
    }


    @Test
    void deleteWatchList_removesDependentRowsThenList() {
        when(watchListRepository.findById(10L)).thenReturn(Optional.of(mine));

        watchListService.deleteWatchList(10L, me);

        InOrder order = inOrder(watchListViewItemRepository, watchListViewRepository, savedWatchListRepository,
                watchListItemRepository, watchListRepository);
        order.verify(watchListViewItemRepository).deleteByWatchListId(10L);
        order.verify(watchListViewRepository).deleteByWatchListId(10L);
        order.verify(savedWatchListRepository).deleteByWatchListId(10L);
        order.verify(watchListItemRepository).deleteByWatchListId(10L);
        order.verify(watchListRepository).delete(mine);
        verify(itemRepository, never()).delete(any());
    }

    @Test
    void deleteWatchList_rejectsListsCreatedByOthers() {
        when(watchListRepository.findById(20L)).thenReturn(Optional.of(theirs));

        assertThatThrownBy(() -> watchListService.deleteWatchList(20L, me))
                .isInstanceOf(AccessDeniedException.class);
        verify(watchListRepository, never()).delete(any());
        verify(watchListItemRepository, never()).deleteByWatchListId(any());
    }

    @Test
    void deleteWatchList_throwsWhenListMissing() {
        when(watchListRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> watchListService.deleteWatchList(99L, me))
                .isInstanceOf(ResourceNotFoundException.class);
    }

}
