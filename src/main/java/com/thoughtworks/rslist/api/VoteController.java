package com.thoughtworks.rslist.api;

import com.thoughtworks.rslist.RsListApplication;
import com.thoughtworks.rslist.domain.Vote;
import com.thoughtworks.rslist.dto.VoteDTO;
import com.thoughtworks.rslist.po.RsEventPO;
import com.thoughtworks.rslist.po.UserPO;
import com.thoughtworks.rslist.po.VotePO;
import com.thoughtworks.rslist.repository.RsEventRepository;
import com.thoughtworks.rslist.repository.UserRepository;
import com.thoughtworks.rslist.repository.VoteRepository;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Optional;
import java.util.TimeZone;

@RestController
public class VoteController {
    Logger logger = RsListApplication.logger;

    @Autowired
    RsEventRepository rsEventRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    VoteRepository voteRepository;

    SimpleDateFormat dateFormatter = initDateFormatter();

    private SimpleDateFormat initDateFormatter() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
        formatter.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
        return formatter;
    }

    @PostMapping("/rs/vote/{rsEventId}")
    @Transactional
    public ResponseEntity addVote(@PathVariable int rsEventId, @RequestBody VoteDTO voteDTO) throws ParseException {
        Optional<RsEventPO> rsEventPOResult = rsEventRepository.findById(rsEventId);
        Optional<UserPO> userPOResult = userRepository.findById(voteDTO.getUserId());
        if (!rsEventPOResult.isPresent() || !userPOResult.isPresent() ||
                voteDTO.getVoteNum() > userPOResult.get().getLeftVoteNumber()) {
            return ResponseEntity.badRequest().build();
        }
        RsEventPO rsEventPO = rsEventPOResult.get();
        UserPO userPO = userPOResult.get();
        userPO.setLeftVoteNumber(userPO.getLeftVoteNumber() - voteDTO.getVoteNum());
        userRepository.save(userPO);
        Vote vote = Vote.builder()
                .userId(voteDTO.getUserId())
                .voteNum(voteDTO.getVoteNum())
                .voteTime(dateFormatter.parse(voteDTO.getVoteTime())).build();
        voteRepository.save(VotePO.builder()
                .userPO(userPO)
                .rsEventPO(rsEventPO)
                .voteNum(vote.getVoteNum())
                .voteTime(vote.getVoteTime()).build());
        return ResponseEntity.ok(null);
    }
}
