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
    @Query(value = "select coalesce(max(amount),0) from rs_trade where list_rank = :rankNum", nativeQuery = true)
    int findMaxAmountOrZeroByRank(@Param("rankNum") int rank);
}
