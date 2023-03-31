package com.bit.reservation.domain.user.mapper;

import com.bit.reservation.domain.user.dto.UserDto;
import com.bit.reservation.domain.user.entity.User;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

    User postDtoToUser(UserDto.PostDto postDto);

    User patchDtoToUser(UserDto.PatchDto patchDto);

    UserDto.SimpleResponseDto userToSimpleResponseDto(User user);

    UserDto.ResponseDto userToResponseDto(User user);

    List<UserDto.ListResponseDto> usersToResponseDto(List<User> user);

}
