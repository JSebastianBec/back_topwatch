package com.topwatch.back_topwatch.controller;

import com.topwatch.back_topwatch.domain.User;
import com.topwatch.back_topwatch.domain.enums.Role;
import com.topwatch.back_topwatch.dto.UpdateUserRequest;
import com.topwatch.back_topwatch.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    private UserController userController;

    private User user;

    @BeforeEach
    void setUp() {
        userController = new UserController(userService);
        user = User.builder()
                .id(1L)
                .email("sebas@topwatch.com")
                .nickname("sebas")
                .role(Role.USER)
                .build();
    }

    @Test
    void healthCheck_returnsHelloWorld() {
        ResponseEntity<String> response = userController.healthCheck();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("Hello world");
    }

    @Test
    void getCurrentUser_returnsAuthenticatedUser() {
        ResponseEntity<User> response = userController.getCurrentUser(user);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(user);
    }

    @Test
    void update_returnsUpdatedUser() {
        UpdateUserRequest request = new UpdateUserRequest("New Name", null, null, null, null, null, null);
        when(userService.update(user, request)).thenReturn(user);

        ResponseEntity<User> response = userController.update(request, user);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(user);
    }

    @Test
    void delete_returnsOk_whenUserExists() {
        when(userService.findUserById(1L)).thenReturn(Optional.of(user));

        ResponseEntity<String> response = userController.delete(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void delete_returnsNotFound_whenUserDoesNotExist() {
        when(userService.findUserById(99L)).thenReturn(Optional.empty());

        ResponseEntity<String> response = userController.delete(99L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void getAllUsers_returnsAllUsers() {
        when(userService.findAllUsers()).thenReturn(List.of(user));

        List<User> result = userController.getAllUsers();

        assertThat(result).containsExactly(user);
    }

    @Test
    void getUser_returnsOk_whenFound() {
        when(userService.findUserById(1L)).thenReturn(Optional.of(user));

        ResponseEntity<User> response = userController.getUser(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(user);
    }

    @Test
    void getUser_returnsNotFound_whenMissing() {
        when(userService.findUserById(99L)).thenReturn(Optional.empty());

        ResponseEntity<User> response = userController.getUser(99L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
