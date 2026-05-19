package com.mayak.ietms.features.user.infra.persistence;

import com.mayak.ietms.features.user.domain.model.User;
import com.mayak.ietms.features.user.domain.enums.UserType;
import com.mayak.ietms.features.user.domain.model.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository <User, Long> {

    @Query("select u.id from User u")
    List<Long> findAllIds();

    int countByStatusAndUserTypeNot(UserStatus status, UserType userType);

    boolean existsByEmail(String email);
    boolean existsByEmailAndIdNot(String email, Long id);
    Optional<User> findByEmail(String email);

    boolean existsByProfileDepartmentIdAndUserType(Long departmentId, UserType type);
    boolean existsByProfileDepartmentIdAndUserTypeAndIdNot(Long departmentId, UserType type, Long excludeId);
}