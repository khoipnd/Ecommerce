package com.fpt.authservice.service;

import com.fpt.authservice.dto.UserResponse;
import com.fpt.authservice.entity.Role;
import com.fpt.authservice.entity.User;
import com.fpt.authservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository);
    }

    @Test
    @DisplayName("Lấy tất cả user thành công (Danh sách có phần tử)")
    void findAllSuccess() {
        Role role = new Role("USER");
        User user1 = new User();
        user1.setId(1L);
        user1.setEmail("user1@test.com");
        user1.setFullName("User 1");
        user1.setRole(role);

        User user2 = new User();
        user2.setId(2L);
        user2.setEmail("user2@test.com");
        user2.setFullName("User 2");
        user2.setRole(role);

        when(userRepository.findAll(Sort.by("id"))).thenReturn(Arrays.asList(user1, user2));

        List<UserResponse> responses = userService.findAll();

        assertEquals(2, responses.size());
        assertEquals(1L, responses.get(0).id());
        assertEquals("user1@test.com", responses.get(0).email());
        assertEquals(2L, responses.get(1).id());
        assertEquals("user2@test.com", responses.get(1).email());
    }

    @Test
    @DisplayName("Lấy danh sách user trả về list rỗng khi không có dữ liệu")
    void findAllReturnsEmptyList() {
        when(userRepository.findAll(Sort.by("id"))).thenReturn(List.of());

        List<UserResponse> responses = userService.findAll();

        assertEquals(0, responses.size());
    }
}
