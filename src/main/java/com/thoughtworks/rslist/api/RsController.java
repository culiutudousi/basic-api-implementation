package com.thoughtworks.rslist.api;

import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.thoughtworks.rslist.component.Error;
import com.thoughtworks.rslist.domain.RsEvent;
import com.thoughtworks.rslist.domain.User;
import com.thoughtworks.rslist.exception.RsEventNotValidException;
import com.thoughtworks.rslist.po.RsEventPO;
import com.thoughtworks.rslist.po.UserPO;
import com.thoughtworks.rslist.repository.RsEventRepository;
import com.thoughtworks.rslist.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
public class RsController {
  private List<RsEvent> rsList = initRsList();
  private Logger logger = LoggerFactory.getLogger(RsController.class);

  @Autowired
  RsEventRepository rsEventRepository;
  @Autowired
  UserRepository userRepository;

  private List<RsEvent> initRsList() {
    List<RsEvent> rsEventList = new ArrayList<>();
    User user = new User("czc", "male", 24, "czc@xxx.com", "12345678901");
    rsEventList.add(new RsEvent("1st event", "no tag", 1));
    rsEventList.add(new RsEvent("2ed event", "no tag", 1));
    rsEventList.add(new RsEvent("3rd event", "no tag", 1));
    return rsEventList;
  }

  @GetMapping("/rs/{index}")
  @JsonView(PropertyFilter.ReEventShowFilter.class)
  public ResponseEntity getRsListAtIndex(@PathVariable int index) {
    if (index <= 0) {
      throw new RsEventNotValidException("invalid index");
    }
    Optional<RsEventPO> rsEventPOResult = rsEventRepository.findById(index);
    if (!rsEventPOResult.isPresent()) {
      throw new RsEventNotValidException("invalid index");
    }
    RsEventPO rsEventPO = rsEventPOResult.get();
    return ResponseEntity.ok(new RsEvent(rsEventPO.getEventName(), rsEventPO.getKeyWord(), rsEventPO.getUserPO().getId()));
  }

  @GetMapping("/rs/list")
  @JsonView(PropertyFilter.ReEventShowFilter.class)
  public ResponseEntity getRsListBetween(@RequestParam(required = false) Integer start, @RequestParam(required = false) Integer end) {
    List<RsEventPO> rsEventPOs = (List<RsEventPO>) rsEventRepository.findAll();
    List<RsEvent> rsEvents = rsEventPOs.stream()
            .map(rsEventPO -> new RsEvent(rsEventPO.getEventName(), rsEventPO.getKeyWord(), rsEventPO.getUserPO().getId()))
            .collect(Collectors.toList());
    if (start == null || end == null) {
      return ResponseEntity.ok(rsEvents);
    }
    if (start <= 0 || start > end) {
      throw new RsEventNotValidException("invalid request param");
    }
    return ResponseEntity.ok(rsEvents.stream()
            .filter(rsEvent -> rsEvent.getUserId() >= start && rsEvent.getUserId() <= end)
            .collect(Collectors.toList()));
  }

  @PostMapping("/rs/event")
  public ResponseEntity addRsEvent(@RequestBody @Valid RsEvent rsEvent) {
    Optional<UserPO> userPOResult = userRepository.findById(rsEvent.getUserId());
    if (!userPOResult.isPresent()) {
      return ResponseEntity.badRequest().build();
    }

    RsEventPO rsEventPO = RsEventPO.builder()
            .eventName(rsEvent.getEventName())
            .keyWord(rsEvent.getKeyWord())
            .userPO(userPOResult.get())
            .build();
    rsEventRepository.save(rsEventPO);
    return ResponseEntity.created(null)
            .header("index", Integer.toString(rsEventPO.getId()))
            .build();
  }

  @PatchMapping("/rs/event/{id}")
  public ResponseEntity updateRsEvent(@PathVariable int id, @RequestBody RsEvent rsEvent) {
    Optional<RsEventPO> rsEventPOResult = rsEventRepository.findById(id);
    if (!rsEventPOResult.isPresent()) {
      return ResponseEntity.badRequest().build();
    }
    RsEventPO rsEventPO = rsEventPOResult.get();
    if (rsEvent.getUserId() != rsEventPO.getUserPO().getId()) {
      return ResponseEntity.badRequest().build();
    }
    if (rsEvent.getEventName() != null) {
      rsEventPO.setEventName(rsEvent.getEventName());
    }
    if (rsEvent.getKeyWord() != null) {
      rsEventPO.setKeyWord(rsEvent.getKeyWord());
    }
    rsEventRepository.save(rsEventPO);
    return ResponseEntity.ok(null);
  }

  @DeleteMapping("/rs/event/{index}")
  public ResponseEntity deleteRsEvent(@PathVariable int index) {
    rsList.remove(index - 1);
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
