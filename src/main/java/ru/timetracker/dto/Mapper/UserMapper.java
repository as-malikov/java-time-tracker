package ru.timetracker.dto.Mapper;

import org.mapstruct.*;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import ru.timetracker.dto.UserRequestDTO;
import ru.timetracker.dto.UserResponseDTO;
import ru.timetracker.model.User;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "id", ignore = true)
    User toEntity(UserRequestDTO dto);

    UserResponseDTO toDto(User entity);

    @Mapping(target = "id", ignore = true)
    void updateEntity(UserRequestDTO dto, @MappingTarget User entity);
}
