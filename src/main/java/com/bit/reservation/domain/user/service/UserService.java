package com.bit.reservation.domain.user.service;

import com.bit.reservation.domain.user.entity.User;
import com.bit.reservation.domain.user.repository.UserRepository;
import com.bit.reservation.global.exception.BusinessLogicException;
import com.bit.reservation.global.exception.ExceptionCode;
import com.bit.reservation.global.security.utils.CustomAuthorityUtils;
import com.bit.reservation.global.status.UserLevel;
import com.bit.reservation.global.status.UserProfileImage;
import com.bit.reservation.global.status.UserStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CustomAuthorityUtils authorityUtils;

    public User join(User user) {
        verifiedUser(user.getEmail());

        List<String> roles = authorityUtils.createRoles(user.getEmail(), false);
        user.setRoles(roles);

        String encryptPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encryptPassword);

        UserProfileImage userProfileImage = getRandomProfileImage();
        user.setProfileImage(userProfileImage);

        user.setUserLevel(UserLevel.FAMILY);
        user.setUserStatus(UserStatus.ACTIVE);

        return userRepository.save(user);
    }

    private UserProfileImage getRandomProfileImage() {
        Random random = new Random();
        int randomNum = random.nextInt(5) + 1;

        return Arrays.stream(UserProfileImage.values())
                .filter(image -> image.getNum() == randomNum)
                .findFirst()
                .orElseThrow();
    }

    public User updateUser(User user) {
        checkJwtAndUser(user.getUserId());
        User findUser = existUser(user.getUserId());

        Optional.ofNullable(user.getUserName())
                .ifPresent(findUser::setUserName);
        Optional.ofNullable(user.getPassword())
                .ifPresent(newPassword -> findUser.setPassword(passwordEncoder.encode(newPassword)));
        Optional.ofNullable(user.getAddress())
                .ifPresent(findUser::setAddress);
        Optional.ofNullable(user.getAge())
                .ifPresent(findUser::setAge);
        Optional.ofNullable(user.getPhoneNumber())
                .ifPresent(findUser::setPhoneNumber);

        return userRepository.save(findUser);
    }

    public void deleteUser(long userId) {
        User user = checkJwtAndUser(userId);
        //회원 탈퇴시 예약은 삭제 X
//        user.getReservations().forEach(reservation -> {
//            reservation.setUser(null);
//            reservation.setQuitClientInfo(List.of(user.getUserName(), user.getAge().toString(), user.getProfileImage().getImage()));
//        });
        user.setUserStatus(UserStatus.QUIT);
        userRepository.save(user);
    }

    public User getUser(long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessLogicException(ExceptionCode.MEMBER_NOT_FOUND));
    }

    public Page<User> getAllUser(Pageable pageable) {
        pageable = PageRequest.of(pageable.getPageNumber()-1, pageable.getPageSize());
         return userRepository.findAll(pageable);
    }

    private User existUser(long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new BusinessLogicException(ExceptionCode.MEMBER_NOT_FOUND));
    }

    public void verifiedUser(String email) {
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isPresent()) {
            throw new BusinessLogicException(ExceptionCode.MEMBER_EXISTS);
        }
    }

    private User getLoginUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessLogicException(ExceptionCode.MEMBER_NOT_FOUND));
    }

    //서비스관리자 + 본인만 접근 허용
    public User checkJwtAndUser(long userId) {
        if (!isServiceAdmin()) {
            User user = getLoginUser();
            if (user.getUserId() != userId || user.getUserStatus().equals(UserStatus.QUIT)) {
                throw new BusinessLogicException(ExceptionCode.ACCESS_FORBIDDEN);
            }
        }
        return existUser(userId);
    }

    public User checkUser() {
        User user = getLoginUser();
        if (user.getUserStatus().equals(UserStatus.QUIT)) {
            throw new BusinessLogicException(ExceptionCode.ACCESS_FORBIDDEN);
        }
        return user;
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public boolean isServiceAdmin() {
        Collection<? extends GrantedAuthority> authorities = SecurityContextHolder.getContext().getAuthentication().getAuthorities();
        return authorities.contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }
}
