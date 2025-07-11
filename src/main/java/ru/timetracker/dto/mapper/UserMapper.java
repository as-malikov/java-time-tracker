package ru.timetracker.dto.mapper;

import org.mapstruct.*;
import ru.timetracker.dto.user.UserCreateDTO;
import ru.timetracker.dto.user.UserDTO;
import ru.timetracker.dto.user.UserUpdateDTO;
import ru.timetracker.model.User;

/**
 * Маппер для преобразования между сущностью User и DTO.
 * Обеспечивает безопасное преобразование с игнорированием чувствительных полей.
 *
 * <p>Основные преобразования:
 * <ul>
 *   <li>User ↔ UserDTO</li>
 *   <li>UserCreateDTO → User</li>
 *   <li>UserUpdateDTO → Обновление User</li>
 * </ul>
 */
@Mapper(componentModel = "spring")
public interface UserMapper {

    /**
     * Преобразует сущность User в UserDTO
     *
     * @param user Сущность пользователя
     * @return DTO пользователя
     */
    UserDTO toDTO(User user);

    /**
     * Преобразует UserCreateDTO в сущность User
     *
     * @param userCreateDTO DTO для создания пользователя
     * @return Сущность пользователя
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tasks", ignore = true)
    @Mapping(target = "timeEntries", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    User toEntity(UserCreateDTO userCreateDTO);

    /**
     * Обновляет сущность User из UserUpdateDTO
     *
     * @param userUpdateDTO DTO с обновленными данными
     * @param user          Сущность для обновления
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "tasks", ignore = true)
    @Mapping(target = "timeEntries", ignore = true)
    void updateEntity(UserUpdateDTO userUpdateDTO, @MappingTarget User user);
}
