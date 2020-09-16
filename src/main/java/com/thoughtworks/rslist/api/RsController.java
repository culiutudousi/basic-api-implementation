package com.thoughtworks.rslist.api;

import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.thoughtworks.rslist.domain.RsEvent;
import com.thoughtworks.rslist.domain.User;
import com.thoughtworks.rslist.exception.RsEventNotValidException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

@RestController
public class RsController {
  private List<RsEvent> rsList = initRsList();

  @Autowired
  UserController userController;

  private List<RsEvent> initRsList() {
    List<RsEvent> rsEventList = new ArrayList<>();
    User user = new User("czc", "male", 24, "czc@xxx.com", "12345678901");
    rsEventList.add(new RsEvent("1st event", "no tag", user));
    rsEventList.add(new RsEvent("2ed event", "no tag", user));
    rsEventList.add(new RsEvent("3rd event", "no tag", user));
    return rsEventList;
  }

  @GetMapping("/rs/{index}")
  @JsonView(PropertyFilter.ReEventShowFilter.class)
  public ResponseEntity getRsListAtIndex(@PathVariable int index) {
    if (index <= 0 || index >= rsList.size()) {
      throw new RsEventNotValidException("invalid index");
    }
    return ResponseEntity.ok(rsList.get(index - 1));
  }

  @GetMapping("/rs/list")
  @JsonView(PropertyFilter.ReEventShowFilter.class)
  public ResponseEntity getRsListBetween(@RequestParam(required = false) Integer start, @RequestParam(required = false) Integer end) {
    if (start == null || end == null) {
      return ResponseEntity.ok(rsList);
    }
    if (start <= 0 || end > rsList.size() || start > end) {
      throw new RsEventNotValidException("invalid request param");
    }
    return ResponseEntity.ok(rsList.subList(start - 1, end));
  }

  @PostMapping("/rs/event")
  public ResponseEntity addRsEvent(@RequestBody @Valid RsEvent rsEvent) {
    if (!userController.doesUserExist(rsEvent.getUser())) {
      userController.addUser(rsEvent.getUser());
      System.out.println("Added new user");
    }
    rsList.add(rsEvent);
    return ResponseEntity.created(null)
            .header("index", Integer.toString(rsList.size()))
            .build();
  }

  @PatchMapping("/rs/event/{index}")
  public ResponseEntity updateRsEvent(@PathVariable int index, @RequestBody RsEvent rsEvent) {
    if (rsEvent.getEventName() != null) {
      rsList.get(index - 1).setEventName(rsEvent.getEventName());
    }
    if (rsEvent.getKeyWord() != null) {
      rsList.get(index - 1).setKeyWord(rsEvent.getKeyWord());
    }
    return ResponseEntity.ok(null);
  }

  @DeleteMapping("/rs/event/{index}")
  public ResponseEntity deleteRsEvent(@PathVariable int index) {
    rsList.remove(index - 1);
    return ResponseEntity.ok(null);
  }
}
