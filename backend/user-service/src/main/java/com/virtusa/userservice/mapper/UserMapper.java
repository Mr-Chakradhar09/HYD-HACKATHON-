package com.virtusa.userservice.mapper;

import com.virtusa.userservice.dto.request.CreateUserRequestDto;
import com.virtusa.userservice.dto.request.UpdateUserRequestDto;
import com.virtusa.userservice.dto.response.UserResponseDto;
import com.virtusa.userservice.entity.User;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserMapper {

    private final ModelMapper modelMapper;

    public User toEntity(CreateUserRequestDto dto) {
        if (dto == null) {
            return null;
        }
        return modelMapper.map(dto, User.class);
    }

    public UserResponseDto toResponseDto(User user) {
        if (user == null) {
            return null;
        }
        return modelMapper.map(user, UserResponseDto.class);
    }

    public void updateEntity(UpdateUserRequestDto dto, User user) {
        if (dto == null || user == null) {
            return;
        }
        modelMapper.map(dto, user);
    }
}
