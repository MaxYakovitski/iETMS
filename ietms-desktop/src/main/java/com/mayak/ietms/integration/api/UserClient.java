package com.mayak.ietms.integration.api;

import com.mayak.ietms.user.dto.UserCreateDto;
import com.mayak.ietms.user.dto.UserLookupDto;
import com.mayak.ietms.user.dto.UserResponseDto;
import com.mayak.ietms.user.dto.UserUpdateDto;
import com.mayak.ietms.user.dto.enums.UserStatusDto;

import java.util.List;

/**
 * Client interface for user management operations against the backend API.
 */
public interface UserClient {

    /** Returns the currently authenticated user's profile. */
    UserResponseDto getMe();

    /** Returns all users. */
    List<UserResponseDto> findAll();

    /** Returns all client specialists (employees with a client-facing role). */
    List<UserResponseDto> findClientSpecialists();

    /** Returns client specialists filtered by department. */
    List<UserResponseDto> findClientSpecialistsByDepartment(Long departmentId);

    /** Returns colleagues of the current user. */
    List<UserResponseDto> findColleagues();

    /** Returns colleagues of the current user filtered by department. */
    List<UserResponseDto> findColleaguesByDepartment(Long departmentId);

    /** Returns a lightweight lookup list of colleagues filtered by department. */
    List<UserLookupDto> findColleaguesLookupByDepartment(Long departmentId);

    /** Creates a new user. */
    void create(UserCreateDto dto);

    /** Updates an existing user. */
    void update(Long id, UserUpdateDto dto);

    /** Deletes a user by id. */
    void delete(Long id);

    /** Changes the password for the given user. */
    void changePassword(Long userId, String newPassword);

    /** Changes the lifecycle status of the given user. */
    void changeStatus(Long id, UserStatusDto status);
}