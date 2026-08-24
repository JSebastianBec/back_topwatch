package com.topwatch.back_topwatch.service;

import com.topwatch.back_topwatch.domain.User;
import com.topwatch.back_topwatch.domain.enums.Gender;
import com.topwatch.back_topwatch.domain.enums.Role;
import com.topwatch.back_topwatch.dto.UpdateUserRequest;
import com.topwatch.back_topwatch.exception.DuplicateResourceException;
import com.topwatch.back_topwatch.repository.UserRepository;
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
        UpdateUserRequest request = new UpdateUserRequest("New Name", null, null, null, null, null, null);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.update(currentUser, request);

        assertThat(result.getName()).isEqualTo("New Name");
        assertThat(result.getLastname()).isEqualTo("Becerra");
        assertThat(result.getNickname()).isEqualTo("sebas");
        assertThat(result.getEmail()).isEqualTo("sebas@topwatch.com");
    }

    @Test
    void update_ignoresBlankStrings() {
        UpdateUserRequest request = new UpdateUserRequest("  ", "", null, null, null, null, null);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.update(currentUser, request);

        assertThat(result.getName()).isEqualTo("Sebastian");
        assertThat(result.getLastname()).isEqualTo("Becerra");
    }

    @Test
    void update_encodesPassword_whenProvided() {
        UpdateUserRequest request = new UpdateUserRequest(null, null, null, null, null, "newPassword123", null);
        when(passwordEncoder.encode("newPassword123")).thenReturn("encoded-new-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.update(currentUser, request);

        assertThat(result.getPassword()).isEqualTo("encoded-new-password");
    }

    @Test
    void update_updatesNickname_whenAvailable() {
        UpdateUserRequest request = new UpdateUserRequest(null, null, "new-nick", null, null, null, null);
        when(userRepository.existsByNicknameAndIdNot("new-nick", 1L)).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.update(currentUser, request);

        assertThat(result.getNickname()).isEqualTo("new-nick");
    }

    @Test
    void update_throwsDuplicateResourceException_whenNicknameTaken() {
        UpdateUserRequest request = new UpdateUserRequest(null, null, "taken-nick", null, null, null, null);
        when(userRepository.existsByNicknameAndIdNot("taken-nick", 1L)).thenReturn(true);

        assertThatThrownBy(() -> userService.update(currentUser, request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Nickname");

        verify(userRepository, never()).save(any());
    }

    @Test
    void update_throwsDuplicateResourceException_whenEmailTaken() {
        UpdateUserRequest request = new UpdateUserRequest(null, null, null, null, "taken@topwatch.com", null, null);
        when(userRepository.existsByEmailAndIdNot("taken@topwatch.com", 1L)).thenReturn(true);

        assertThatThrownBy(() -> userService.update(currentUser, request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Email");

        verify(userRepository, never()).save(any());
    }

    @Test
    void update_doesNotCheckUniqueness_whenNicknameIsUnchanged() {
        UpdateUserRequest request = new UpdateUserRequest(null, null, "sebas", null, null, null, null);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        userService.update(currentUser, request);

        verify(userRepository, never()).existsByNicknameAndIdNot(any(), anyLong());
    }

    @Test
    void update_setsGenderAndAvatar_whenProvided() {
        UpdateUserRequest request = new UpdateUserRequest(null, null, null, Gender.MALE, null, null, "https://cdn/avatar.png");
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
    void delete_delegatesToRepository() {
        userService.delete(currentUser);

        verify(userRepository, times(1)).delete(currentUser);
    }

    @Test
    void findAllUsers_returnsAllUsers() {
        when(userRepository.findAll()).thenReturn(List.of(currentUser));

        List<User> result = userService.findAllUsers();

        assertThat(result).containsExactly(currentUser);
    }
}
