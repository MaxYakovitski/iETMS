package com.mayak.iet.integration.rest;

import com.mayak.iet.integration.api.UserClient;
import com.mayak.iet.user.dto.*;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class UserRestClient extends AbstractRestClient implements UserClient {

    private static final String BASE_URL = "http://localhost:8080/api/users";

    public UserRestClient(RestTemplate restTemplate) {
        super(restTemplate);
    }

    @Override
    public UserResponseDto getMe() {
        return exchangeSafely(() -> {

            ResponseEntity<UserResponseDto> response =
                    restTemplate.exchange(
                            BASE_URL + "/me",
                            HttpMethod.GET,
                            null,
                            UserResponseDto.class
                    );

            return response.getBody();
        });
    }

    //FULL
    @Override
    public List<UserResponseDto> findAll() {
        return exchangeList(BASE_URL);
    }

    @Override
    public List<UserResponseDto> findClientSpecialists() {
        return exchangeList(BASE_URL + "/client-specialists");
    }

    @Override
    public List<UserResponseDto> findClientSpecialistsByDepartment(Long depId) {
        return exchangeList(BASE_URL + "/client-specialists/by-department/{id}", depId);
    }

    @Override
    public List<UserResponseDto> findColleagues() {
        return exchangeList(BASE_URL + "/colleagues");
    }

    @Override
    public List<UserResponseDto> findColleaguesByDepartment(Long depId) {
        return exchangeList(BASE_URL + "/colleagues/by-department/{id}", depId);
    }

    public void create(UserCreateDto dto) {
        exchangeSafely(() -> {
            RequestEntity<UserCreateDto> request = RequestEntity
                    .post(BASE_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(dto);

            restTemplate.exchange(request, Void.class);
            return null;
        });
    }

    @Override
    public void update(Long id, UserUpdateDto dto) {
        exchangeSafely(() -> {
            RequestEntity<UserUpdateDto> request = RequestEntity
                    .put(BASE_URL + "/{id}", id)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(dto);

            restTemplate.exchange(request, Void.class);
            return null;
        });
    }

    @Override
    public void delete(Long id) {
        exchangeSafely(() -> {
            RequestEntity<Void> request = RequestEntity
                    .delete(BASE_URL + "/{id}", id)
                    .build();

            restTemplate.exchange(request, Void.class);
            return null;
        });
    }

    @Override
    public void changePassword(Long userId, String newPassword) {
        exchangeSafely(() -> {
            RequestEntity<ChangePasswordDto> request =
                    RequestEntity.put(BASE_URL + "/{id}/password", userId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(new ChangePasswordDto(newPassword));

            restTemplate.exchange(request, Void.class);
            return null;
        });
    }

    private List<UserResponseDto> exchangeList(String url, Object... args) {
        return exchangeSafely(() -> {

            ResponseEntity<List<UserResponseDto>> response =
                    restTemplate.exchange(
                            url,
                            HttpMethod.GET,
                            null,
                            new ParameterizedTypeReference<>() {
                            },
                            args
                    );

            return response.getBody() != null
                    ? response.getBody()
                    : List.of();
        });
    }

    @Override
    public List<UserLookupDto> findClientSpecialistsLookupByDepartment(Long depId) {
        return exchangeSafely(() -> {

            ResponseEntity<List<UserLookupDto>> response =
                    restTemplate.exchange(
                            BASE_URL + "/client-specialists/lookup/by-department/{id}",
                            HttpMethod.GET,
                            null,
                            new ParameterizedTypeReference<>() {},
                            depId
                    );

            return response.getBody() != null
                    ? response.getBody()
                    : List.of();
        });
    }

    @Override
    public List<UserLookupDto> findColleaguesLookupByDepartment(Long depId) {
        return exchangeSafely(() -> {

            ResponseEntity<List<UserLookupDto>> response =
                    restTemplate.exchange(
                            BASE_URL + "/colleagues/lookup/by-department/{id}",
                            HttpMethod.GET,
                            null,
                            new ParameterizedTypeReference<>() {},
                            depId
                    );

            return response.getBody() != null
                    ? response.getBody()
                    : List.of();
        });
    }
}