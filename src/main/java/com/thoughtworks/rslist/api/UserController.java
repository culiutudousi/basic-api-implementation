package com.thoughtworks.rslist.api;

import com.thoughtworks.rslist.component.Error;
import com.thoughtworks.rslist.domain.User;
import com.thoughtworks.rslist.exception.RsEventNotValidException;
import com.thoughtworks.rslist.po.UserPO;
import com.thoughtworks.rslist.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@RestController
public class UserController {
    private List<User> userList = initUserList();
    private Logger logger = LoggerFactory.getLogger(RsController.class);

    @Autowired
    private UserRepository userRepository;

    private List<User> initUserList() {
        List<User> userList = new ArrayList<>();
        userList.add(new User("czc", "male", 24, "czc@xxx.com", "12345678901"));
        return userList;
    }

    @PostMapping("/user")
    public ResponseEntity addUser(@RequestBody @Valid User user) {
        UserPO userPO = UserPO.builder().name(user.getName()).age(user.getAge()).gender(user.getGender())
                .email(user.getEmail()).phone(user.getPhone()).votes(10).build();
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
                        .email(userPO.getEmail()).phone(userPO.getPhone()).votes(userPO.getVotes()).build())
                .collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }

    public Boolean doesUserExist(User user) {
        long sameUserNameNumber = userList.stream()
                .filter(existingUser -> existingUser.getName().equals(user.getName()))
                .count();
        return sameUserNameNumber >= 1;
    }

    @ExceptionHandler({MethodArgumentNotValidException.class})
    public ResponseEntity rsExceptionHandler(Exception exception) {
        Error error = new Error();
        error.setError("invalid user");
        logger.error("invalid user");
        return ResponseEntity.badRequest().body(error);
    }
}
