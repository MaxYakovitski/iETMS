package com.mayak.ietms.infrastructure.security.user;

import com.mayak.ietms.features.user.domain.model.User;
import com.mayak.ietms.features.user.domain.enums.Permission;
import com.mayak.ietms.features.user.domain.model.UserStatus;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Spring Security principal wrapping a {@link User} domain object.
 * Authorities are composed of the user's {@link com.mayak.ietms.features.user.domain.enums.UserType}
 * and all assigned {@link Permission}s.
 */
@RequiredArgsConstructor
public class UserPrincipal implements UserDetails {

    @Getter
    private final User user;
    private final Set<Permission> permissions;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<GrantedAuthority> authorities = new HashSet<>();

        authorities.add(new SimpleGrantedAuthority(user.getUserType().name()));
        permissions.forEach(p -> authorities.add(new SimpleGrantedAuthority(p.name())));

        return authorities;
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    /**
     * Returns the user's id as a string, used as the Spring Security username.
     */
    @Override
    public String getUsername() {
        return String.valueOf(user.getId());
    }

    @Override public boolean isAccountNonExpired() {
        return true;
    }

    @Override public boolean isAccountNonLocked() {
        return true;
    }

    @Override public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return user.getStatus() == UserStatus.ACTIVE;
    }

}