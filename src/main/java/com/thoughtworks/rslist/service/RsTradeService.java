package com.thoughtworks.rslist.service;

import com.thoughtworks.rslist.domain.RsEventWithVote;
import com.thoughtworks.rslist.domain.RsTrade;
import com.thoughtworks.rslist.domain.RsTradeRecord;
import com.thoughtworks.rslist.exception.RsTradeNotValidException;
import com.thoughtworks.rslist.po.RsEventPO;
import com.thoughtworks.rslist.po.RsTradePO;
import com.thoughtworks.rslist.repository.RsEventRepository;
import com.thoughtworks.rslist.repository.RsTradeRepository;
import com.thoughtworks.rslist.repository.UserRepository;
import com.thoughtworks.rslist.repository.VoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RsTradeService {
    final RsEventRepository rsEventRepository;
    final UserRepository userRepository;
    final VoteRepository voteRepository;
    final RsTradeRepository rsTradeRepository;
    @Autowired
    RsEventService rsEventService;

    public RsTradeService(RsEventRepository rsEventRepository, UserRepository userRepository, VoteRepository voteRepository, RsTradeRepository rsTradeRepository) {
        this.rsEventRepository = rsEventRepository;
        this.userRepository = userRepository;
        this.voteRepository = voteRepository;
        this.rsTradeRepository = rsTradeRepository;
    }

    @Transactional
    public void addTrade(RsTrade rsTrade) {
        RsTradePO oldTradePO = rsTradeRepository.findByRank(rsTrade.getRank());
        if (oldTradePO != null && oldTradePO.getAmount() >= rsTrade.getAmount()) {
            throw new RsTradeNotValidException("Trade amount is not enough");
        }
        if (oldTradePO != null) {
            rsEventService.deleteRsEvent(oldTradePO.getRsEventPO().getId());
        }
        rsTradeRepository.save(RsTradePO.builder()
                .amount(rsTrade.getAmount())
                .rank(rsTrade.getRank())
                .rsEventPO(rsEventService.getRsEventPO(rsTrade.getRsEventId()))
                .build());
    }

    public List<RsTrade> getTrades() {
        List<RsTradePO> rsTradePOs = (List<RsTradePO>) rsTradeRepository.findAll();
        return rsTradePOs.stream()
                .map(rsTradePO -> RsTrade.builder()
                        .amount(rsTradePO.getAmount())
                        .rank(rsTradePO.getRank())
                        .rsEventId(rsTradePO.getRsEventPO().getId())
                        .build())
                .collect(Collectors.toList());
    }
}
