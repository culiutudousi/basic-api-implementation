package com.thoughtworks.rslist.api;

import com.fasterxml.jackson.annotation.JsonView;
import com.thoughtworks.rslist.RsListApplication;
import com.thoughtworks.rslist.component.Error;
import com.thoughtworks.rslist.domain.RsEvent;
import com.thoughtworks.rslist.domain.RsEventWithVote;
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
import com.thoughtworks.rslist.service.RsEventService;
import com.thoughtworks.rslist.service.VoteService;
import jdk.nashorn.internal.objects.LinkedMap;
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
  RsEventService rsEventService;

  private RsEventWithVoteDTO transformToRsEventWithVoteDTO(RsEventWithVote rsEventWithVote) {
    return RsEventWithVoteDTO.builder()
            .eventName(rsEventWithVote.getEventName())
            .keyWord(rsEventWithVote.getKeyWord())
            .voteNumber(rsEventWithVote.getVotes())
            .build();
  }

  @GetMapping("/rs/{id}")
  public ResponseEntity getRsListAtIndex(@PathVariable int id) {
    RsEventWithVote rsEventWithVote = rsEventService.getRsEvent(id);
    return ResponseEntity.ok(transformToRsEventWithVoteDTO(rsEventWithVote));
  }

  @GetMapping("/rs/list")
  public ResponseEntity<List<RsEventWithVoteDTO>> getRsList() {
    return ResponseEntity.ok(rsEventService.getRsEvents().stream()
            .map(this::transformToRsEventWithVoteDTO)
            .collect(Collectors.toList()));
  }

  @GetMapping("/rs/rankedList")
  public ResponseEntity<Map<Integer, RsEventWithVoteDTO>> getRankedRsList() {
    LinkedHashMap<Integer, RsEventWithVoteDTO> orderedRsList = new LinkedHashMap<>();
    rsEventService.getRankedEvents().entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .forEachOrdered(e -> orderedRsList.put(e.getKey(), transformToRsEventWithVoteDTO(e.getValue())));
    return ResponseEntity.ok(orderedRsList);
  }

  @PostMapping("/rs/event")
  public ResponseEntity addRsEvent(@RequestParam int userId, @RequestBody @Valid RsEventDTO rsEventDTO) {
    int rsEventId = rsEventService.addRsEvent(RsEvent.builder()
            .eventName(rsEventDTO.getEventName())
            .keyWord(rsEventDTO.getKeyWord())
            .userId(userId)
            .build());
    return ResponseEntity.created(null)
            .header("index", Integer.toString(rsEventId))
            .build();
  }

  @PatchMapping("/rs/event/{rsEventId}")
  public ResponseEntity updateRsEvent(@PathVariable int rsEventId, @RequestParam int userId, @RequestBody RsEventDTO rsEventDTO) {
    rsEventService.updateRsEvent(rsEventId, RsEvent.builder()
            .eventName(rsEventDTO.getEventName())
            .keyWord(rsEventDTO.getKeyWord())
            .userId(userId)
            .build());
    return ResponseEntity.ok(null);
  }

  @DeleteMapping("/rs/event/{rsEventId}")
  public ResponseEntity deleteRsEvent(@PathVariable int rsEventId) {
    rsEventService.deleteRsEvent(rsEventId);
    return ResponseEntity.ok(null);
  }

  @ExceptionHandler({RsEventNotValidException.class, MethodArgumentNotValidException.class})
  public ResponseEntity rsExceptionHandler(Exception exception) {
    String errorString;
    if (exception instanceof MethodArgumentNotValidException) {
      errorString = "Invalid param";
    } else {
      errorString = exception.getMessage();
    }
    logger.error(errorString);
    return ResponseEntity.badRequest().body(new Error(errorString));
  }
}
