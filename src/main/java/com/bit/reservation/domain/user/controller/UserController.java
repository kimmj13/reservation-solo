package com.bit.reservation.domain.user.controller;

import com.bit.reservation.domain.user.dto.UserDto;
import com.bit.reservation.domain.user.entity.User;
import com.bit.reservation.domain.user.mapper.UserMapper;
import com.bit.reservation.domain.user.service.UserService;
import com.bit.reservation.global.dto.MultiResponseDto;
import com.bit.reservation.global.status.ReservationStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
@Validated
public class UserController {

    private final UserService userService;
    private final UserMapper mapper;

    @PostMapping("/sign-up")
    public ResponseEntity postUser(@RequestBody @Valid UserDto.PostDto postDto) {
        User user = userService.join(mapper.postDtoToUser(postDto));
        return new ResponseEntity<>(mapper.userToSimpleResponseDto(user), HttpStatus.CREATED);
    }

    @PatchMapping("/{user-id}")
    public ResponseEntity patchUser(@PathVariable("user-id") @Positive long userId,
                                    @RequestBody @Valid UserDto.PatchDto patchDto) {
        patchDto.setUserId(userId);
        User user = userService.updateUser(mapper.patchDtoToUser(patchDto));
        return new ResponseEntity<>(mapper.userToSimpleResponseDto(user), HttpStatus.OK);

    }

    @DeleteMapping("/{user-id}")
    public ResponseEntity deleteUser(@PathVariable("user-id") @Positive long userId) {
        userService.deleteUser(userId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/{user-id}")
    public ResponseEntity getUser(@PathVariable("user-id") @Positive long userId) {
        User user = userService.getUser(userId);
        return new ResponseEntity<>(mapper.userToResponseDto(user), HttpStatus.OK);
    }

    /* 서비스 관리자용 */
    @GetMapping
    public ResponseEntity getAllUser(@PageableDefault(page = 1, size = 10) Pageable pageable) {
        Page<User> pages = userService.getAllUser(pageable);
        List<User> users = pages.getContent();
        return new ResponseEntity<>(new MultiResponseDto<>(mapper.usersToResponseDto(users), pages), HttpStatus.OK);
    }
}
