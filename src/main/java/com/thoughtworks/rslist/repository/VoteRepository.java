package com.thoughtworks.rslist.repository;

import com.thoughtworks.rslist.po.UserPO;
import com.thoughtworks.rslist.po.VotePO;
import org.springframework.data.repository.CrudRepository;

public interface VoteRepository  extends CrudRepository<VotePO, Integer> {
}
