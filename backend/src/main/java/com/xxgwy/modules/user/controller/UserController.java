package com.xxgwy.modules.user.controller;

import com.xxgwy.common.response.PageResult;
import com.xxgwy.common.response.R;
import com.xxgwy.modules.user.dto.UserInfoResponse;
import com.xxgwy.modules.user.entity.User;
import com.xxgwy.modules.user.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import java.util.stream.Collectors;

@Tag(name = "用户管理")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    private final UserRepository userRepository;

    @Operation(summary = "用户列表（分页）")
    @GetMapping
    public PageResult<UserInfoResponse> list(@RequestParam(defaultValue = "0") int page,
                                             @RequestParam(defaultValue = "10") int size) {
        Page<User> userPage = userRepository.findAll(PageRequest.of(page, size));
        return PageResult.of(
                userPage.getContent().stream().map(UserInfoResponse::from).collect(Collectors.toList()),
                userPage.getTotalElements(),
                page,
                size
        );
    }

    @Operation(summary = "获取单个用户")
    @GetMapping("/{id}")
    public R<UserInfoResponse> getById(@PathVariable UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        return R.ok(UserInfoResponse.from(user));
    }

    @Operation(summary = "修改用户角色")
    @PutMapping("/{id}/role")
    public R<UserInfoResponse> updateRole(@PathVariable UUID id, @RequestBody UpdateRoleRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        user.setRole(request.role());
        userRepository.save(user);
        return R.ok(UserInfoResponse.from(user));
    }

    @Operation(summary = "删除用户")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable UUID id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("用户不存在");
        }
        userRepository.deleteById(id);
        return R.ok();
    }

    public record UpdateRoleRequest(User.Role role) {}
}
