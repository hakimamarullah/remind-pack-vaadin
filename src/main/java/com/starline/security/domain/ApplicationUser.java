package com.starline.security.domain;

import com.starline.base.domain.BaseEntity;
import com.starline.security.AppUserInfo;
import com.starline.security.AppUserPrincipal;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Comment;
import org.springframework.aot.hint.annotation.RegisterReflection;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;

@Getter
@Setter
@Entity
@Table(name = "USERS")
@RegisterReflection
public class ApplicationUser extends BaseEntity implements AppUserPrincipal, UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "USER_SEQ")
    @SequenceGenerator(name = "USER_SEQ", sequenceName = "USER_SEQ", allocationSize = 1)
    @Column(name = "ID")
    private Long id;


    @Column(name = "MOBILE_PHONE", length = 15, nullable = false, unique = true)
    @Comment(value = "Mobile phone number should be registered on whatsapp", on = "MOBILE_PHONE")
    private String mobilePhone;

    @Column(name = "ENABLED", nullable = false, columnDefinition = "BOOLEAN DEFAULT true")
    @Comment(value = "Is user enabled", on = "ENABLED")
    private Boolean enabled = true;


    @Column(name = "LAST_LOGIN", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    @Comment(value = "Last login time", on = "LAST_LOGIN")
    private LocalDateTime lastLogin;


    @Column(name = "HASHED_PASSWORD", nullable = false)
    @Comment(value = "Hashed password", on = "HASHED_PASSWORD")
    private String hashedPassword;


    @Override
    public AppUserInfo getAppUser() {
        return new AppUserInfo() {
            @Override
            public UserId getUserId() {
                return UserId.of(String.valueOf(id));
            }

            @Override
            public String getPreferredUsername() {
                return mobilePhone;
            }

            @Override
            public String getPictureUrl() {
                return "https://avatar.iran.liara.run/public/job/operator/male";
            }
        };
    }

    @Override
    public String getName() {
        return mobilePhone;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return new HashSet<>();
    }

    @Override
    public String getPassword() {
        return this.hashedPassword;
    }

    @Override
    public String getUsername() {
        return this.mobilePhone;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }
}
