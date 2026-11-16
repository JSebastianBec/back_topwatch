package com.topwatch.back_topwatch.service;

import com.topwatch.back_topwatch.domain.Item;
import com.topwatch.back_topwatch.domain.User;
import com.topwatch.back_topwatch.domain.enums.Type;
import com.topwatch.back_topwatch.exception.ResourceInUseException;
import com.topwatch.back_topwatch.exception.ResourceNotFoundException;
import com.topwatch.back_topwatch.repository.CategoryRepository;
import com.topwatch.back_topwatch.repository.ItemRepository;
import com.topwatch.back_topwatch.repository.WatchListItemRepository;
import com.topwatch.back_topwatch.repository.WatchListViewItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
class ItemServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private WatchListItemRepository watchListItemRepository;

    @Mock
    private WatchListViewItemRepository watchListViewItemRepository;

    @InjectMocks
    private ItemService itemService;

    private User me;
    private User other;
    private Item mine;

    @BeforeEach
    void setUp() {
        me = User.builder().id(1L).nickname("me").build();
        other = User.builder().id(2L).nickname("other").build();
        mine = Item.builder().id(7L).name("Dune").type(Type.BOOKS).creator(me).build();
    }

    @Test
    void deleteItem_removesItFromOwnListsThenDeletes() {
        when(itemRepository.findById(7L)).thenReturn(Optional.of(mine));
        when(watchListItemRepository.findWatchListIdsWithItemNotCreatedBy(7L, 1L)).thenReturn(List.of());

        Item deleted = itemService.deleteItem(7L, me);

        assertThat(deleted).isEqualTo(mine);
        InOrder order = inOrder(watchListViewItemRepository, watchListItemRepository, itemRepository);
        order.verify(watchListViewItemRepository).deleteByItemId(7L);
        order.verify(watchListItemRepository).deleteByItemId(7L);
        order.verify(itemRepository).delete(mine);
    }

    @Test
    void deleteItem_rejectsWhenUsedInOtherUsersLists() {
        when(itemRepository.findById(7L)).thenReturn(Optional.of(mine));
        when(watchListItemRepository.findWatchListIdsWithItemNotCreatedBy(7L, 1L)).thenReturn(List.of(20L, 21L));

        assertThatThrownBy(() -> itemService.deleteItem(7L, me))
                .isInstanceOf(ResourceInUseException.class)
                .hasMessageContaining("20")
                .hasMessageContaining("21");
        verify(itemRepository, never()).delete(any());
        verify(watchListItemRepository, never()).deleteByItemId(any());
        verify(watchListViewItemRepository, never()).deleteByItemId(any());
    }

    @Test
    void deleteItem_rejectsItemsCreatedByOthers() {
        when(itemRepository.findById(7L)).thenReturn(Optional.of(mine));

        assertThatThrownBy(() -> itemService.deleteItem(7L, other))
                .isInstanceOf(AccessDeniedException.class);
        verify(itemRepository, never()).delete(any());
    }

    @Test
    void deleteItem_throwsWhenItemMissing() {
        when(itemRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemService.deleteItem(99L, me))
                .isInstanceOf(ResourceNotFoundException.class);
    }

}
