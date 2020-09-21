package com.thoughtworks.rslist.repository;

import com.thoughtworks.rslist.po.RsEventPO;
import com.thoughtworks.rslist.po.RsTradePO;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface RsTradeRepository extends CrudRepository<RsTradePO, Integer> {
    List<RsTradePO> findAllByRsEventPO(RsEventPO rsEventPO);
    RsTradePO findByRank(int rank);
}
