package com.thoughtworks.rslist.api;

import com.thoughtworks.rslist.RsListApplication;
import com.thoughtworks.rslist.component.Error;
import com.thoughtworks.rslist.domain.Vote;
import com.thoughtworks.rslist.dto.VoteDTO;
import com.thoughtworks.rslist.exception.VoteNotValidException;
import com.thoughtworks.rslist.po.RsEventPO;
import com.thoughtworks.rslist.po.UserPO;
import com.thoughtworks.rslist.po.VotePO;
import com.thoughtworks.rslist.repository.RsEventRepository;
import com.thoughtworks.rslist.repository.UserRepository;
import com.thoughtworks.rslist.repository.VoteRepository;
import com.thoughtworks.rslist.service.VoteService;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Optional;
import java.util.TimeZone;

@RestController
public class VoteController {
    Logger logger = RsListApplication.logger;

    @Autowired
    VoteService voteService;

    SimpleDateFormat dateFormatter = initDateFormatter();

    private SimpleDateFormat initDateFormatter() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
        formatter.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
        return formatter;
    }

    @PostMapping("/rs/vote/{rsEventId}")
    @Transactional
    public ResponseEntity addVote(@PathVariable int rsEventId, @RequestBody VoteDTO voteDTO) throws ParseException {
        voteService.vote(rsEventId, Vote.builder()
                .userId(voteDTO.getUserId())
                .voteNum(voteDTO.getVoteNum())
                .voteTime(dateFormatter.parse(voteDTO.getVoteTime())).build());
        return ResponseEntity.ok(null);
    }

    @ExceptionHandler({VoteNotValidException.class, ParseException.class})
    public ResponseEntity voteExceptionHandler(Exception exception) {
        String errorMessage = exception.getMessage();
        logger.error(errorMessage);
        return ResponseEntity.badRequest().body(new Error(errorMessage));
    }
}
