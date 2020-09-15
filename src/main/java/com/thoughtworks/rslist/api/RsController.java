package com.thoughtworks.rslist.api;

import com.thoughtworks.rslist.domain.RsEvent;
import com.thoughtworks.rslist.domain.RsEventUpdate;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RestController
public class RsController {
  private List<RsEvent> rsList = initRsList();

  private List<RsEvent> initRsList() {
    List<RsEvent> rsEventList = new ArrayList<>();
    rsEventList.add(new RsEvent("1st event", "no tag"));
    rsEventList.add(new RsEvent("2ed event", "no tag"));
    rsEventList.add(new RsEvent("3rd event", "no tag"));
    return rsEventList;
  }

  @GetMapping("/rs/{index}")
  public RsEvent getRsListAtIndex(@PathVariable int index) {
    return rsList.get(index - 1);
  }

  @GetMapping("/rs/list")
  public List<RsEvent> getRsListBetween(@RequestParam(required = false) Integer start, @RequestParam(required = false) Integer end) {
    if (start == null || end == null) {
      return rsList;
    }
    return rsList.subList(start - 1, end);
  }

  @PostMapping("/rs/event")
  public void addRsEvent(@RequestBody RsEvent rsEvent) {
    rsList.add(rsEvent);
  }

  @PostMapping("/rs/update")
  public void updateRsEvent(@RequestBody RsEventUpdate rsEventUpdate) {
    if (rsEventUpdate.getEventName() != null) {
      rsList.get(rsEventUpdate.getIndex() - 1).setEventName(rsEventUpdate.getEventName());
    }
    if (rsEventUpdate.getKeyWord() != null) {
      rsList.get(rsEventUpdate.getIndex() - 1).setKeyWord(rsEventUpdate.getKeyWord());
    }
  }
}
