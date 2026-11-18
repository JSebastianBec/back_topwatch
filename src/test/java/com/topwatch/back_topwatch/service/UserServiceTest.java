package com.topwatch.back_topwatch.service;

import com.topwatch.back_topwatch.domain.Item;
import com.topwatch.back_topwatch.domain.User;
import com.topwatch.back_topwatch.domain.WatchList;
import com.topwatch.back_topwatch.domain.enums.Gender;
import com.topwatch.back_topwatch.domain.enums.Role;
import com.topwatch.back_topwatch.dto.UpdateUserRequest;
import com.topwatch.back_topwatch.exception.DuplicateResourceException;
import com.topwatch.back_topwatch.repository.ContentReportRepository;
import com.topwatch.back_topwatch.repository.ItemRepository;
import com.topwatch.back_topwatch.repository.SavedWatchListRepository;
import com.topwatch.back_topwatch.repository.UserBlockRepository;
import com.topwatch.back_topwatch.repository.UserRepository;
import com.topwatch.back_topwatch.repository.WatchListItemRepository;
import com.topwatch.back_topwatch.repository.WatchListRepository;
import com.topwatch.back_topwatch.repository.WatchListViewItemRepository;
import com.topwatch.back_topwatch.repository.WatchListViewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Mock
    private ImageStorageService imageStorageService;

    @Mock
    private WatchListRepository watchListRepository;

    @Mock
    private WatchListItemRepository watchListItemRepository;

    @Mock
    private WatchListViewRepository watchListViewRepository;

    @Mock
    private WatchListViewItemRepository watchListViewItemRepository;

    @Mock
    private SavedWatchListRepository savedWatchListRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private UserBlockRepository userBlockRepository;

    @Mock
    private ContentReportRepository contentReportRepository;

    @InjectMocks
    private UserService userService;

    private User currentUser;

    @BeforeEach
    void setUp() {
        currentUser = User.builder()
                .id(1L)
                .name("Sebastian")
                .lastname("Becerra")
                .nickname("sebas")
                .email("sebas@topwatch.com")
                .password("old-encoded-password")
                .role(Role.USER)
                .build();
    }

    @Test
    void update_appliesOnlyProvidedFields() {
        UpdateUserRequest request = new UpdateUserRequest("New Name", null, null, null, null, null);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.update(currentUser, request);

        assertThat(result.getName()).isEqualTo("New Name");
        assertThat(result.getLastname()).isEqualTo("Becerra");
        assertThat(result.getNickname()).isEqualTo("sebas");
        assertThat(result.getEmail()).isEqualTo("sebas@topwatch.com");
    }

    @Test
    void update_ignoresBlankStrings() {
        UpdateUserRequest request = new UpdateUserRequest("  ", "", null, null, null, null);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.update(currentUser, request);

        assertThat(result.getName()).isEqualTo("Sebastian");
        assertThat(result.getLastname()).isEqualTo("Becerra");
    }

    @Test
    void update_updatesNickname_whenAvailable() {
        UpdateUserRequest request = new UpdateUserRequest(null, null, "new-nick", null, null, null);
        when(userRepository.existsByNicknameAndIdNot("new-nick", 1L)).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.update(currentUser, request);

        assertThat(result.getNickname()).isEqualTo("new-nick");
    }

    @Test
    void update_throwsDuplicateResourceException_whenNicknameTaken() {
        UpdateUserRequest request = new UpdateUserRequest(null, null, "taken-nick", null, null, null);
        when(userRepository.existsByNicknameAndIdNot("taken-nick", 1L)).thenReturn(true);

        assertThatThrownBy(() -> userService.update(currentUser, request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Nickname");

        verify(userRepository, never()).save(any());
    }

    @Test
    void update_throwsDuplicateResourceException_whenEmailTaken() {
        UpdateUserRequest request = new UpdateUserRequest(null, null, null, null, "taken@topwatch.com", null);
        when(userRepository.existsByEmailAndIdNot("taken@topwatch.com", 1L)).thenReturn(true);

        assertThatThrownBy(() -> userService.update(currentUser, request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Email");

        verify(userRepository, never()).save(any());
    }

    @Test
    void update_doesNotCheckUniqueness_whenNicknameIsUnchanged() {
        UpdateUserRequest request = new UpdateUserRequest(null, null, "sebas", null, null, null);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        userService.update(currentUser, request);

        verify(userRepository, never()).existsByNicknameAndIdNot(any(), anyLong());
    }

    @Test
    void update_setsGenderAndAvatar_whenProvided() {
        UpdateUserRequest request = new UpdateUserRequest(null, null, null, Gender.MALE, null, "https://cdn/avatar.png");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.update(currentUser, request);

        assertThat(result.getGender()).isEqualTo(Gender.MALE);
        assertThat(result.getAvatarURL()).isEqualTo("https://cdn/avatar.png");
    }

    @Test
    void findUserById_returnsUser_whenFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(currentUser));

        Optional<User> result = userService.findUserById(1L);

        assertThat(result).contains(currentUser);
    }

    @Test
    void findUserById_returnsEmpty_whenNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<User> result = userService.findUserById(99L);

        assertThat(result).isEmpty();
    }

    @Test
    void delete_removesUserAndPersonalData_whenNobodyDependsOnThem() {
        currentUser.setAvatarURL("avatar.png");
        WatchList ownList = WatchList.builder().id(5L).build();
        Item ownItem = Item.builder().id(9L).urlAvatar("item.png").build();
        when(watchListRepository.findByCreator_Id(1L)).thenReturn(List.of(ownList));
        when(itemRepository.findByCreator_Id(1L)).thenReturn(List.of(ownItem));

        List<String> unusedImages = userService.delete(currentUser);

        verify(watchListViewItemRepository).deleteByViewerId(1L);
        verify(watchListViewRepository).deleteByViewerId(1L);
        verify(savedWatchListRepository).deleteByUserId(1L);
        verify(userBlockRepository).deleteByUserId(1L);
        verify(contentReportRepository).deleteByReporterId(1L);
        verify(watchListItemRepository).deleteByWatchListId(5L);
        verify(watchListRepository).delete(ownList);
        verify(itemRepository).delete(ownItem);
        verify(userRepository, times(1)).delete(currentUser);
        assertThat(unusedImages).containsExactly("item.png", "avatar.png");
    }

    @Test
    void delete_keepsSharedContent_andWipesPersonalData_whenOthersDependOnIt() {
        currentUser.setAvatarURL("avatar.png");
        WatchList sharedList = WatchList.builder().id(5L).build();
        Item sharedItem = Item.builder().id(9L).urlAvatar("item.png").build();
        when(watchListRepository.findByCreator_Id(1L)).thenReturn(List.of(sharedList));
        when(watchListViewRepository.existsByWatchList_Id(5L)).thenReturn(true);
        when(itemRepository.findByCreator_Id(1L)).thenReturn(List.of(sharedItem));
        when(watchListItemRepository.existsByItem_Id(9L)).thenReturn(true);
        when(watchListRepository.existsByCreator_Id(1L)).thenReturn(true);
        when(passwordEncoder.encode(any())).thenReturn("random-encoded");

        List<String> unusedImages = userService.delete(currentUser);

        verify(watchListRepository, never()).delete(sharedList);
        verify(itemRepository, never()).delete(sharedItem);
        verify(userRepository, never()).delete(currentUser);
        verify(userRepository).save(currentUser);
        assertThat(currentUser.getName()).isNull();
        assertThat(currentUser.getLastname()).isNull();
        assertThat(currentUser.getAvatarURL()).isNull();
        assertThat(currentUser.getNickname()).isEqualTo("deleted-user-1");
        assertThat(currentUser.getEmail()).isEqualTo("deleted-user-1@deleted.invalid");
        assertThat(currentUser.getPassword()).isEqualTo("random-encoded");
        assertThat(unusedImages).containsExactly("avatar.png");
    }

    @Test
    void findAllUsers_returnsAllUsers() {
        when(userRepository.findAll()).thenReturn(List.of(currentUser));

        List<User> result = userService.findAllUsers();

        assertThat(result).containsExactly(currentUser);
    }
}
