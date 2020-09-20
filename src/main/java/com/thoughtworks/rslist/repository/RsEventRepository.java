package com.thoughtworks.rslist.repository;

import com.thoughtworks.rslist.po.RsEventPO;
import com.thoughtworks.rslist.po.UserPO;
import org.springframework.data.repository.CrudRepository;

public interface RsEventRepository extends CrudRepository<RsEventPO, Integer> {
}
