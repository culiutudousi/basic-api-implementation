package com.thoughtworks.rslist.api;

import com.fasterxml.jackson.annotation.JsonView;
import com.thoughtworks.rslist.RsListApplication;
import com.thoughtworks.rslist.component.Error;
import com.thoughtworks.rslist.dto.RsEventDTO;
import com.thoughtworks.rslist.dto.RsEventWithUserIdDTO;
import com.thoughtworks.rslist.dto.RsEventWithVoteDTO;
import com.thoughtworks.rslist.domain.Vote;
import com.thoughtworks.rslist.dto.VoteDTO;
import com.thoughtworks.rslist.exception.RsEventNotValidException;
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
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@RestController
public class RsController {
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

  private RsEventWithVoteDTO transformToRsEventWithVoteDTO(RsEventPO rsEventPO) {
    List<VotePO> votePOs = voteRepository.findVotePOByRsEventPO(rsEventPO);
    int totalVoteNumber = votePOs.stream()
            .map(VotePO::getVoteNum)
            .reduce(0, Integer::sum);
    return new RsEventWithVoteDTO(rsEventPO.getEventName(), rsEventPO.getKeyWord(), totalVoteNumber);
  }

  @GetMapping("/rs/{index}")
  public ResponseEntity getRsListAtIndex(@PathVariable int index) {
    if (index <= 0) {
      throw new RsEventNotValidException("invalid index");
    }
    Optional<RsEventPO> rsEventPOResult = rsEventRepository.findById(index);
    if (!rsEventPOResult.isPresent()) {
      throw new RsEventNotValidException("invalid index");
    }
    RsEventPO rsEventPO = rsEventPOResult.get();
    return ResponseEntity.ok(transformToRsEventWithVoteDTO(rsEventPO));
  }

  @GetMapping("/rs/list")
  public ResponseEntity getRsListBetween(@RequestParam(required = false) Integer start, @RequestParam(required = false) Integer end) {
    List<RsEventPO> rsEventPOs = (List<RsEventPO>) rsEventRepository.findAll();
    if (start == null || end == null) {
      return ResponseEntity.ok(rsEventPOs.stream()
              .map(this::transformToRsEventWithVoteDTO)
              .collect(Collectors.toList()));
    }
    if (start <= 0 || start > end) {
      throw new RsEventNotValidException("invalid request param");
    }
    return ResponseEntity.ok(rsEventPOs.stream()
            .filter(rsEventPO -> rsEventPO.getUserPO().getId() >= start && rsEventPO.getUserPO().getId() <= end)
            .map(this::transformToRsEventWithVoteDTO)
            .collect(Collectors.toList()));
  }

  @PostMapping("/rs/event")
  public ResponseEntity addRsEvent(@RequestBody @Valid RsEventWithUserIdDTO rsEventWithUserIdDTO) {
    Optional<UserPO> userPOResult = userRepository.findById(rsEventWithUserIdDTO.getUserId());
    if (!userPOResult.isPresent()) {
      return ResponseEntity.badRequest().build();
    }

    RsEventPO rsEventPO = RsEventPO.builder()
            .eventName(rsEventWithUserIdDTO.getEventName())
            .keyWord(rsEventWithUserIdDTO.getKeyWord())
            .userPO(userPOResult.get())
            .build();
    rsEventRepository.save(rsEventPO);
    return ResponseEntity.created(null)
            .header("index", Integer.toString(rsEventPO.getId()))
            .build();
  }

  @PatchMapping("/rs/event/{rsEventId}")
  public ResponseEntity updateRsEvent(@PathVariable int rsEventId, @RequestBody RsEventWithUserIdDTO rsEventWithUserIdDTO) {
    Optional<RsEventPO> rsEventPOResult = rsEventRepository.findById(rsEventId);
    if (!rsEventPOResult.isPresent()) {
      return ResponseEntity.badRequest().build();
    }
    RsEventPO rsEventPO = rsEventPOResult.get();
    if (rsEventWithUserIdDTO.getUserId() != rsEventPO.getUserPO().getId()) {
      return ResponseEntity.badRequest().build();
    }
    if (rsEventWithUserIdDTO.getEventName() != null) {
      rsEventPO.setEventName(rsEventWithUserIdDTO.getEventName());
    }
    if (rsEventWithUserIdDTO.getKeyWord() != null) {
      rsEventPO.setKeyWord(rsEventWithUserIdDTO.getKeyWord());
    }
    rsEventRepository.save(rsEventPO);
    return ResponseEntity.ok(null);
  }

  @DeleteMapping("/rs/event/{rsEventId}")
  public ResponseEntity deleteRsEvent(@PathVariable int rsEventId) {
    Optional<RsEventPO> rsEventPOResult = rsEventRepository.findById(rsEventId);
    if (!rsEventPOResult.isPresent()) {
      return ResponseEntity.badRequest().build();
    }
    rsEventRepository.delete(rsEventPOResult.get());
    return ResponseEntity.ok(null);
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

  @ExceptionHandler({RsEventNotValidException.class, MethodArgumentNotValidException.class})
  public ResponseEntity rsExceptionHandler(Exception exception) {
    String errorString;
    if (exception instanceof MethodArgumentNotValidException) {
      errorString = "invalid param";
      logger.error("An ERROR Message");
    } else {
      errorString = exception.getMessage();
      logger.error(errorString);
    }
    Error error = new Error();
    error.setError(errorString);
    return ResponseEntity.badRequest().body(error);
  }
}
