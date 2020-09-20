package com.thoughtworks.rslist.repository;

import com.thoughtworks.rslist.po.RsEventPO;
import com.thoughtworks.rslist.po.UserPO;
import com.thoughtworks.rslist.po.VotePO;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.Date;
import java.util.List;

public interface VoteRepository extends JpaRepository<VotePO, Integer> {
    @Override
    List<VotePO> findAll();
    List<VotePO> findVotePOByRsEventPO(RsEventPO rsEventPO);
    List<VotePO> findAllByUserPOAndRsEventPO(UserPO userPO, RsEventPO rsEventPO, Pageable pageable);
    List<VotePO> findAllByVoteTimeBetween(Date voteTimeStart, Date voteTimeEnd);
}
