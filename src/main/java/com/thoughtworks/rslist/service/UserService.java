package com.thoughtworks.rslist.service;

import com.thoughtworks.rslist.domain.User;
import com.thoughtworks.rslist.exception.UserNotValidException;
import com.thoughtworks.rslist.po.UserPO;
import com.thoughtworks.rslist.repository.RsEventRepository;
import com.thoughtworks.rslist.repository.UserRepository;
import com.thoughtworks.rslist.repository.VoteRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {
    final RsEventRepository rsEventRepository;
    final UserRepository userRepository;
    final VoteRepository voteRepository;

    public UserService(RsEventRepository rsEventRepository, UserRepository userRepository, VoteRepository voteRepository) {
        this.rsEventRepository = rsEventRepository;
        this.userRepository = userRepository;
        this.voteRepository = voteRepository;
    }

    private User transformToUser(UserPO userPO) {
        return User.builder()
                .name(userPO.getName())
                .age(userPO.getAge())
                .gender(userPO.getGender())
                .email(userPO.getEmail())
                .phone(userPO.getPhone())
                .leftVoteNumber(userPO.getLeftVoteNumber())
                .build();
    }

    public int addUser(User user) {
        UserPO userPO = UserPO.builder()
                .name(user.getName())
                .age(user.getAge())
                .gender(user.getGender())
                .email(user.getEmail())
                .phone(user.getPhone())
                .leftVoteNumber(10)
                .build();
        userRepository.save(userPO);
        return userPO.getId();
    }

    protected UserPO getUserPO(int userId) {
        Optional<UserPO> userPOResult = userRepository.findById(userId);
        if (!userPOResult.isPresent()) {
            throw new UserNotValidException("User id not exist");
        }
        return userPOResult.get();
    }

    public User getUser(int id) {
        Optional<UserPO> userPOResult = userRepository.findById(id);
        if (!userPOResult.isPresent()) {
            throw new UserNotValidException("User id not exist");
        }
        UserPO userPO = userPOResult.get();
        return transformToUser(userPO);
    }

    public List<User> getUsers() {
        List<UserPO> userPOs = (List<UserPO>) userRepository.findAll();
        return userPOs.stream()
                .map(this::transformToUser)
                .collect(Collectors.toList());
    }

    public void deleteUser(int id) {
        Optional<UserPO> userPOResult = userRepository.findById(id);
        if (!userPOResult.isPresent()) {
            throw new UserNotValidException("User id not exist");
        }
        userRepository.delete(userPOResult.get());
    }
}
