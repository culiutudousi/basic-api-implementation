package com.thoughtworks.rslist.api;

import com.thoughtworks.rslist.component.Error;
import com.thoughtworks.rslist.domain.User;
import com.thoughtworks.rslist.exception.RsEventNotValidException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

@RestController
public class UserController {
    private List<User> userList = initUserList();
    private Logger logger = LoggerFactory.getLogger(RsController.class);

    private List<User> initUserList() {
        List<User> userList = new ArrayList<>();
        userList.add(new User("czc", "male", 24, "czc@xxx.com", "12345678901"));
        return userList;
    }

    @PostMapping("/user")
    public ResponseEntity addUser(@RequestBody @Valid User user) {
        userList.add(user);
        return ResponseEntity.created(null)
                .header("index", Integer.toString(userList.size()))
                .build();
    }

    @GetMapping("/users")
    public ResponseEntity getUserList() {
        return ResponseEntity.ok(userList);
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
