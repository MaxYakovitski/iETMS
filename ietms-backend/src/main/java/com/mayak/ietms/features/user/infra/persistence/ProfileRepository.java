package com.mayak.ietms.features.user.infra.persistence;

import com.mayak.ietms.features.user.domain.enums.Role;
import com.mayak.ietms.features.user.domain.model.Profile;
import com.mayak.ietms.features.user.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProfileRepository extends JpaRepository<Profile, Long> {

    @Query("select p.user from Profile p")
    List<User> findAllUsers();

    @Query("""
    select p.user from  Profile p
    where p.role = :role or p.user.userType = com.mayak.ietms.features.user.domain.enums.UserType.MANAGER
    """)
    List<User> findUsersByRoleOrManager(@Param("role") Role role);

    @Query("select p.department.id from Profile p where p.user.id = :userId")
    Optional<Long> findDepartmentIdByUserId(@Param("userId") Long userId);

    @Query("select p.user from Profile p where p.department.id = :departmentId")
    List<User> findUsersByDepartmentId(@Param("departmentId") Long departmentId);

    boolean existsByDepartmentId(Long departmentId);
}