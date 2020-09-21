package com.thoughtworks.rslist.api;

import com.thoughtworks.rslist.RsListApplication;
import com.thoughtworks.rslist.component.Error;
import com.thoughtworks.rslist.domain.User;
import com.thoughtworks.rslist.dto.UserDTO;
import com.thoughtworks.rslist.exception.UserNotValidException;
import com.thoughtworks.rslist.po.UserPO;
import com.thoughtworks.rslist.repository.UserRepository;
import com.thoughtworks.rslist.service.UserService;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
public class UserController {
    Logger logger = RsListApplication.logger;

    @Autowired
    UserService userService;

    @PostMapping("/user")
    public ResponseEntity addUser(@RequestBody @Valid UserDTO userDTO) {
        int userId = userService.addUser(User.builder()
                .name(userDTO.getName())
                .age(userDTO.getAge())
                .gender(userDTO.getGender())
                .email(userDTO.getEmail())
                .phone(userDTO.getPhone())
                .build());
        return ResponseEntity.created(null)
                .header("index", Integer.toString(userId))
                .build();
    }

    private UserDTO transformToUserDTO(User user) {
        return UserDTO.builder()
                .name(user.getName())
                .age(user.getAge())
                .gender(user.getGender())
                .email(user.getEmail())
                .phone(user.getPhone())
                .build();
    }

    @GetMapping("/users")
    public ResponseEntity getUsers() {
        return ResponseEntity.ok(userService.getUsers().stream()
                .map(this::transformToUserDTO)
        );
    }

    @GetMapping("/user/{id}")
    public ResponseEntity getUser(@PathVariable int id) {
        User user = userService.getUser(id);
        return ResponseEntity.ok(transformToUserDTO(user));
    }

    @DeleteMapping("/user/{id}")
    public ResponseEntity deleteUser(@PathVariable int id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(null);
    }

    @ExceptionHandler({UserNotValidException.class, MethodArgumentNotValidException.class})
    public ResponseEntity rsExceptionHandler(Exception exception) {
        String errorMessage;
        if (exception instanceof MethodArgumentNotValidException) {
            errorMessage = "invalid user";
        } else {
            errorMessage = exception.getMessage();
        }
        logger.error("Exception in UserController: " + errorMessage);
        return ResponseEntity.badRequest().body(new Error(errorMessage));
    }
}
