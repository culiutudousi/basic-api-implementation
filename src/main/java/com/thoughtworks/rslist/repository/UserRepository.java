package com.thoughtworks.rslist.repository;

import com.thoughtworks.rslist.po.UserPO;
import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<UserPO, Integer> {

}
