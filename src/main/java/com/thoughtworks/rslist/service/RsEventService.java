package com.thoughtworks.rslist.service;

import com.thoughtworks.rslist.domain.RsEvent;
import com.thoughtworks.rslist.domain.RsEventWithVote;
import com.thoughtworks.rslist.domain.RsTrade;
import com.thoughtworks.rslist.dto.RsEventWithVoteDTO;
import com.thoughtworks.rslist.exception.RsEventNotValidException;
import com.thoughtworks.rslist.po.RsEventPO;
import com.thoughtworks.rslist.po.RsTradePO;
import com.thoughtworks.rslist.po.UserPO;
import com.thoughtworks.rslist.po.VotePO;
import com.thoughtworks.rslist.repository.RsEventRepository;
import com.thoughtworks.rslist.repository.RsTradeRepository;
import com.thoughtworks.rslist.repository.UserRepository;
import com.thoughtworks.rslist.repository.VoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class RsEventService {
    final RsEventRepository rsEventRepository;
    final UserRepository userRepository;
    final VoteRepository voteRepository;
    final RsTradeRepository rsTradeRepository;
    @Autowired
    VoteService voteService;
    @Autowired
    RsTradeService rsTradeService;

    public RsEventService(RsEventRepository rsEventRepository, UserRepository userRepository, VoteRepository voteRepository, RsTradeRepository rsTradeRepository) {
        this.rsEventRepository = rsEventRepository;
        this.userRepository = userRepository;
        this.voteRepository = voteRepository;
        this.rsTradeRepository = rsTradeRepository;
    }

    public int addRsEvent(RsEvent rsEvent) {
        Optional<UserPO> userPOResult = userRepository.findById(rsEvent.getUserId());
        if (!userPOResult.isPresent()) {
            throw new RsEventNotValidException("Invalid id: User does not exist");
        }
        RsEventPO rsEventPO = RsEventPO.builder()
                .eventName(rsEvent.getEventName())
                .keyWord(rsEvent.getKeyWord())
                .userPO(userPOResult.get())
                .build();
        rsEventRepository.save(rsEventPO);
        return rsEventPO.getId();
    }

    private RsEventWithVote transformToRsEventWithVote(RsEventPO rsEventPO) {
        return RsEventWithVote.builder()
                .eventName(rsEventPO.getEventName())
                .keyWord(rsEventPO.getKeyWord())
                .userId(rsEventPO.getUserPO().getId())
                .votes(voteService.getVoteNumberOf(rsEventPO))
                .build();
    }

    protected RsEventPO getRsEventPO(int rsEventId) {
        Optional<RsEventPO> rsEventPOResult = rsEventRepository.findById(rsEventId);
        if (!rsEventPOResult.isPresent()) {
            throw new RsEventNotValidException("Invalid id: RsEvent does not exist");
        }
        return rsEventPOResult.get();
    }

    public RsEventWithVote getRsEvent(int rsEventId) {
        return transformToRsEventWithVote(getRsEventPO(rsEventId));
    }

    public List<RsEventWithVote> getRsEvents() {
        List<RsEventPO> rsEventPOs = (List<RsEventPO>) rsEventRepository.findAll();
        return rsEventPOs.stream()
                .map(this::transformToRsEventWithVote)
                .collect(Collectors.toList());
    }

    public Map<Integer, RsEventWithVote> getRankedEvents() {
        List<RsTradePO> rsTradeEventPOs = (List<RsTradePO>) rsTradeRepository.findAll();
        List<RsEventPO> rsEventPOs = (List<RsEventPO>) rsEventRepository.findAll();
        Map<Integer, RsEventWithVote> rankedEvents = new HashMap<>();
        rsTradeEventPOs.forEach(rsTradePO -> {
            rankedEvents.put(rsTradePO.getRank(), transformToRsEventWithVote(rsTradePO.getRsEventPO()));
            rsEventPOs.remove(rsTradePO.getRsEventPO());
        });
        rsEventPOs.stream()
                .sorted(Comparator.comparing(rsEventPO -> voteService.getVoteNumberOf((RsEventPO) rsEventPO)).reversed())
                .forEachOrdered(rsEventPO -> rankedEvents.put(
                            IntStream.range(1, rankedEvents.size() + 2)
                                    .filter(i -> !rankedEvents.containsKey(i))
                                    .findFirst()
                                    .orElse(0),
                            transformToRsEventWithVote(rsEventPO))
                );
        return rankedEvents;
    }

    public void updateRsEvent(int rsEventId, RsEvent rsEvent) {
        RsEventPO rsEventPO = getRsEventPO(rsEventId);
        if (rsEvent.getUserId() != rsEventPO.getUserPO().getId()) {
            throw new RsEventNotValidException("Invalid id: The userId does not match the RsEvent");
        }
        if (rsEvent.getEventName() != null) {
            rsEventPO.setEventName(rsEvent.getEventName());
        }
        if (rsEvent.getKeyWord() != null) {
            rsEventPO.setKeyWord(rsEvent.getKeyWord());
        }
        rsEventRepository.save(rsEventPO);
    }

    public void deleteRsEvent(int rsEventId) {
        RsEventPO rsEventPO = getRsEventPO(rsEventId);
        rsEventRepository.delete(rsEventPO);
    }
}
