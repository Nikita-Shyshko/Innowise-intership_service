package com.innowise.task.controller;

import com.innowise.task.dto.UserDTO;
import com.innowise.task.dto.UserFilterDTO;
import com.innowise.task.dto.UserRequestDTO;
import com.innowise.task.dto.UserUpdateDTO;
import com.innowise.task.entity.UserStatus;
import com.innowise.task.service.UserServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/users")
public class UserController
{
    private final UserServiceImpl usersService;

    @GetMapping("/{id}")
    ResponseEntity<UserDTO> getUserById(@PathVariable Long id)
    {
        return ResponseEntity.ok(usersService.getById(id));
    }

    @PostMapping
    ResponseEntity<UserDTO> createUser(@RequestBody @Valid UserRequestDTO userRequestDTO)
    {
        return ResponseEntity.status(HttpStatus.CREATED).body(usersService.create(userRequestDTO));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<UserDTO> updateById(@PathVariable Long id,
                                              @RequestBody @Valid UserUpdateDTO updateDTO)
    {
        return ResponseEntity.status(HttpStatus.OK).body(usersService.updateById(id, updateDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id)
    {
        usersService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<UserDTO> setActiveStatus(@PathVariable Long id, @RequestParam UserStatus status)
    {
        usersService.setActiveStatus(id, status);
        return ResponseEntity.status(HttpStatus.OK).body(usersService.getById(id));
    }

    @PostMapping("/all")
    public ResponseEntity<Page<UserDTO>> getAllUsers(@RequestBody @Valid UserFilterDTO filter)
    {
        Page<UserDTO> users = usersService.getAll(filter);
        return ResponseEntity.ok(users);
    }
}
