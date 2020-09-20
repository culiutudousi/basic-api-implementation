package com.thoughtworks.rslist.service;

import com.thoughtworks.rslist.domain.RsEvent;
import com.thoughtworks.rslist.domain.Vote;
import com.thoughtworks.rslist.exception.RsEventNotValidException;
import com.thoughtworks.rslist.exception.VoteNotValidException;
import com.thoughtworks.rslist.po.RsEventPO;
import com.thoughtworks.rslist.po.UserPO;
import com.thoughtworks.rslist.po.VotePO;
import com.thoughtworks.rslist.repository.RsEventRepository;
import com.thoughtworks.rslist.repository.UserRepository;
import com.thoughtworks.rslist.repository.VoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestParam;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class VoteService {
    final RsEventRepository rsEventRepository;
    final UserRepository userRepository;
    final VoteRepository voteRepository;

    @Autowired
    RsEventService rsEventService;
    @Autowired
    UserService userService;

    public VoteService(RsEventRepository rsEventRepository, UserRepository userRepository, VoteRepository voteRepository) {
        this.rsEventRepository = rsEventRepository;
        this.userRepository = userRepository;
        this.voteRepository = voteRepository;
    }

    private Vote transformVotePOToVote(VotePO votePO) {
        return Vote.builder()
                .userId(votePO.getUserPO().getId())
                .voteNum(votePO.getVoteNum())
                .voteTime(votePO.getVoteTime())
                .build();
    }

    @Transactional
    public void vote(int rsEventId, Vote vote) {
        Optional<RsEventPO> rsEventPOResult = rsEventRepository.findById(rsEventId);
        Optional<UserPO> userPOResult = userRepository.findById(vote.getUserId());
        if (!rsEventPOResult.isPresent() || !userPOResult.isPresent() ||
                vote.getVoteNum() > userPOResult.get().getLeftVoteNumber()) {
            throw new VoteNotValidException("Can not vote");
        }
        RsEventPO rsEventPO = rsEventPOResult.get();
        UserPO userPO = userPOResult.get();
        userPO.setLeftVoteNumber(userPO.getLeftVoteNumber() - vote.getVoteNum());
        userRepository.save(userPO);
        voteRepository.save(VotePO.builder()
                .userPO(userPO)
                .rsEventPO(rsEventPO)
                .voteNum(vote.getVoteNum())
                .voteTime(vote.getVoteTime()).build());
    }

    protected int getVoteNumberOf(RsEventPO rsEventPO) {
        List<VotePO> votePOs = voteRepository.findVotePOByRsEventPO(rsEventPO);
        return votePOs.stream()
                .map(VotePO::getVoteNum)
                .reduce(0, Integer::sum);
    }

    public List<Vote> getVotes(int userId, int rsEventId, int pageIndex) {
        Pageable pageable = PageRequest.of(pageIndex - 1, 5);
        UserPO userPO = userService.getUserPO(userId);
        RsEventPO rsEventPO = rsEventService.getRsEventPO(rsEventId);
        return voteRepository.findAllByUserPOAndRsEventPO(userPO, rsEventPO, pageable).stream()
                .map(this::transformVotePOToVote)
                .collect(Collectors.toList());
    }

    public List<Vote> getVotesBetween(Date dateStart, Date dateEnd) {
        return voteRepository.findAllByVoteTimeBetween(dateStart, dateEnd).stream()
                .map(this::transformVotePOToVote)
                .collect(Collectors.toList());
    }
}
