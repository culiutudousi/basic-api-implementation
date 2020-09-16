package com.thoughtworks.rslist.api;

import com.thoughtworks.rslist.domain.RsEvent;
import com.thoughtworks.rslist.domain.User;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
public class RsController {
  private List<RsEvent> rsList = initRsList();

  private List<RsEvent> initRsList() {
    List<RsEvent> rsEventList = new ArrayList<>();
    User user = new User("czc", "male", 24, "czc@xxx.com", "12345678901");
    rsEventList.add(new RsEvent("1st event", "no tag", user));
    rsEventList.add(new RsEvent("2ed event", "no tag", user));
    rsEventList.add(new RsEvent("3rd event", "no tag", user));
    return rsEventList;
  }

  @GetMapping("/rs/{index}")
  public ResponseEntity getRsListAtIndex(@PathVariable int index) {
    return ResponseEntity.ok(rsList.get(index - 1));
  }

  @GetMapping("/rs/list")
  public ResponseEntity getRsListBetween(@RequestParam(required = false) Integer start, @RequestParam(required = false) Integer end) {
    if (start == null || end == null) {
      return ResponseEntity.ok(rsList);
    }
    return ResponseEntity.ok(rsList.subList(start - 1, end));
  }

  @PostMapping("/rs/event")
  public ResponseEntity addRsEvent(@RequestBody RsEvent rsEvent) {
    rsList.add(rsEvent);
    return ResponseEntity.created(null).build();
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
