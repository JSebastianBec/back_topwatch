package com.topwatch.back_topwatch.domain;

import com.topwatch.back_topwatch.domain.enums.Role;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

class UserTest {

    @Test
    void getUsername_returnsEmail() {
        User user = User.builder().email("sebas@topwatch.com").build();

        assertThat(user.getUsername()).isEqualTo("sebas@topwatch.com");
    }

    @Test
    void getAuthorities_returnsRolePrefixedAuthority() {
        User user = User.builder().role(Role.ADMIN).build();

        Collection<? extends GrantedAuthority> authorities = user.getAuthorities();

        assertThat(authorities).extracting(GrantedAuthority::getAuthority).containsExactly("ROLE_ADMIN");
    }

    @Test
    void getAuthorities_returnsEmptyList_whenRoleIsNull() {
        User user = User.builder().role(null).build();

        assertThat(user.getAuthorities()).isEmpty();
    }

    @Test
    void userDetailsDefaults_areAllEnabled() {
        User user = User.builder().email("sebas@topwatch.com").role(Role.USER).build();

        assertThat(user.isAccountNonExpired()).isTrue();
        assertThat(user.isAccountNonLocked()).isTrue();
        assertThat(user.isCredentialsNonExpired()).isTrue();
        assertThat(user.isEnabled()).isTrue();
    }
}
