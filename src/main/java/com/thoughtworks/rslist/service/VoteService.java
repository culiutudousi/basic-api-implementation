package com.thoughtworks.rslist.service;

import com.thoughtworks.rslist.domain.Vote;
import com.thoughtworks.rslist.exception.RsEventNotValidException;
import com.thoughtworks.rslist.exception.VoteNotValidException;
import com.thoughtworks.rslist.po.RsEventPO;
import com.thoughtworks.rslist.po.UserPO;
import com.thoughtworks.rslist.po.VotePO;
import com.thoughtworks.rslist.repository.RsEventRepository;
import com.thoughtworks.rslist.repository.UserRepository;
import com.thoughtworks.rslist.repository.VoteRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Optional;
import java.util.TimeZone;

@Service
public class VoteService {
    final RsEventRepository rsEventRepository;
    final UserRepository userRepository;
    final VoteRepository voteRepository;

    public VoteService(RsEventRepository rsEventRepository, UserRepository userRepository, VoteRepository voteRepository) {
        this.rsEventRepository = rsEventRepository;
        this.userRepository = userRepository;
        this.voteRepository = voteRepository;
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
}
