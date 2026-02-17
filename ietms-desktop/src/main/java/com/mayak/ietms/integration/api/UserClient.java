package com.mayak.ietms.integration.api;

import com.mayak.ietms.user.dto.UserCreateDto;
import com.mayak.ietms.user.dto.UserLookupDto;
import com.mayak.ietms.user.dto.UserResponseDto;
import com.mayak.ietms.user.dto.UserUpdateDto;

import java.util.List;

public interface UserClient {

    UserResponseDto getMe();

    List<UserResponseDto> findAll();
    List<UserResponseDto> findClientSpecialists();
    List<UserResponseDto> findClientSpecialistsByDepartment(Long departmentId);
    List<UserResponseDto> findColleagues();
    List<UserResponseDto> findColleaguesByDepartment(Long departmentId);

    List<UserLookupDto> findClientSpecialistsLookupByDepartment(Long departmentId);
    List<UserLookupDto> findColleaguesLookupByDepartment(Long departmentId);

    void create(UserCreateDto dto);
    void update(Long id, UserUpdateDto dto);
    void delete(Long id);
    void changePassword(Long userId, String newPassword);
}