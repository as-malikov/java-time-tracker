package ru.timetracker.dto.Mapper;

import org.mapstruct.*;
import ru.timetracker.dto.UserCreateDTO;
import ru.timetracker.dto.UserDTO;
import ru.timetracker.dto.UserUpdateDTO;
import ru.timetracker.model.User;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserDTO toDTO(User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tasks", ignore = true)
    @Mapping(target = "timeEntries", ignore = true)
    User toEntity(UserCreateDTO userCreateDTO);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "tasks", ignore = true)
    @Mapping(target = "timeEntries", ignore = true)
    void updateEntity(UserUpdateDTO userUpdateDTO, @MappingTarget User user);
}
