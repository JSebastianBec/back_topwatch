package com.topwatch.back_topwatch.controller;

import com.topwatch.back_topwatch.domain.Item;
import com.topwatch.back_topwatch.domain.User;
import com.topwatch.back_topwatch.dto.ItemResponse;
import com.topwatch.back_topwatch.service.BlockService;
import com.topwatch.back_topwatch.service.ImageStorageService;
import com.topwatch.back_topwatch.service.ItemService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemControllerTest {

    private static final String AVATAR = "https://cdn.topwatch.com/uploads/items/1/dune.png";

    @Mock
    private ItemService itemService;

    @Mock
    private ImageStorageService imageStorageService;

    @Mock
    private BlockService blockService;

    private ItemController itemController;

    private User user;
    private Item item;

    @BeforeEach
    void setUp() {
        itemController = new ItemController(itemService, imageStorageService, blockService);
        user = User.builder().id(1L).nickname("sebas").build();
        item = Item.builder().id(7L).name("Dune").urlAvatar(AVATAR).creator(user).build();
    }

    @Test
    void deleteItem_returnsNoContentAndDeletesOwnImage() {
        when(itemService.deleteItem(7L, user)).thenReturn(item);

        ResponseEntity<Void> response = itemController.deleteItem(7L, user);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(imageStorageService).deleteIfOwned(AVATAR, 1L);
    }

    @Test
    void getAllItems_hidesItemsFromBlockedUsers() {
        User blocked = User.builder().id(2L).nickname("troll").build();
        Item hidden = Item.builder().id(8L).name("Spam").creator(blocked).build();
        when(itemService.findAllItems()).thenReturn(List.of(item, hidden));
        when(blockService.findBlockedUserIds(user)).thenReturn(Set.of(2L));

        assertThat(itemController.getAllItems(null, user)).extracting(ItemResponse::id).containsExactly(7L);
    }

    @Test
    void deleteItem_stillSucceedsWhenImageDeletionFails() {
        when(itemService.deleteItem(7L, user)).thenReturn(item);
        doThrow(new RuntimeException("S3 down")).when(imageStorageService).deleteIfOwned(AVATAR, 1L);

        ResponseEntity<Void> response = itemController.deleteItem(7L, user);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

}
