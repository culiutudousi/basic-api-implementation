package com.thoughtworks.rslist.api;

import com.thoughtworks.rslist.component.Error;
import com.thoughtworks.rslist.domain.User;
import com.thoughtworks.rslist.po.UserPO;
import com.thoughtworks.rslist.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private Logger logger = LoggerFactory.getLogger(RsController.class);

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/user")
    public ResponseEntity addUser(@RequestBody @Valid User user) {
        UserPO userPO = UserPO.builder().name(user.getName()).age(user.getAge()).gender(user.getGender())
                .email(user.getEmail()).phone(user.getPhone()).leftVoteNumber(10).build();
        userRepository.save(userPO);
        return ResponseEntity.created(null)
                .header("index", Integer.toString(userPO.getId()))
                .build();
    }

    @GetMapping("/users")
    public ResponseEntity getUserList() {
        List<UserPO> userPOs = (List<UserPO>) userRepository.findAll();
        List<User> users = userPOs.stream()
                .map(userPO -> User.builder().name(userPO.getName()).age(userPO.getAge()).gender(userPO.getGender())
                        .email(userPO.getEmail()).phone(userPO.getPhone()).votes(userPO.getLeftVoteNumber()).build())
                .collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }

    @GetMapping("/user/{id}")
    public ResponseEntity getUser(@PathVariable int id) {
        Optional<UserPO> userPOResult = userRepository.findById(id);
        if (!userPOResult.isPresent()) {
            return ResponseEntity.badRequest().build();
        }
        UserPO userPO = userPOResult.get();
        return ResponseEntity.ok(User.builder().name(userPO.getName()).age(userPO.getAge()).gender(userPO.getGender())
                .email(userPO.getEmail()).phone(userPO.getPhone()).votes(userPO.getLeftVoteNumber()).build());
    }

    @DeleteMapping("/user/{id}")
    public ResponseEntity deleteUser(@PathVariable int id) {
        Optional<UserPO> userPOResult = userRepository.findById(id);
        if (!userPOResult.isPresent()) {
            return ResponseEntity.badRequest().build();
        }
        userRepository.delete(userPOResult.get());
        return ResponseEntity.ok(null);
    }

    @ExceptionHandler({MethodArgumentNotValidException.class})
    public ResponseEntity rsExceptionHandler(Exception exception) {
        Error error = new Error();
        error.setError("invalid user");
        logger.error("invalid user");
        return ResponseEntity.badRequest().body(error);
    }
}
