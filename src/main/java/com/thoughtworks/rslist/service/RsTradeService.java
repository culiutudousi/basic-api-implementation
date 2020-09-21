package com.thoughtworks.rslist.service;

import com.thoughtworks.rslist.domain.RsEvent;
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

import java.util.List;

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

    public void addTrade(RsTrade rsTrade) {
        if (rsTrade.getAmount() <= rsTradeRepository.findMaxAmountOrZeroByRank(rsTrade.getRank())) {
            throw new RsTradeNotValidException("Trade amount is not enough");
        }
        rsTradeRepository.save(RsTradePO.builder()
                .amount(rsTrade.getAmount())
                .rank(rsTrade.getRank())
                .rsEventPO(rsEventService.getRsEventPO(rsTrade.getRsEventId()))
                .build());
    }

    public RsTradeRecord getLatestRecord(int rsEventId) {
        RsEventPO rsEventPO = rsEventService.getRsEventPO(rsEventId);
        List<RsTradePO> rsTradePOs = rsTradeRepository.findAllByRsEventPO(rsEventPO);
        if (rsTradePOs.size() == 0) {
            return RsTradeRecord.builder().isBought(false).build();
        }
        RsTradePO lastRsTradePO = rsTradePOs.get(rsTradePOs.size() - 1);
        return RsTradeRecord.builder()
                .isBought(true)
                .amount(lastRsTradePO.getAmount())
                .rank(lastRsTradePO.getRank())
                .build();
    }
}
