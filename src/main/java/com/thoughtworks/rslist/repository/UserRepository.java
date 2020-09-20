package com.thoughtworks.rslist.repository;

import com.thoughtworks.rslist.entity.UserEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface UserRepository extends CrudRepository<UserEntity, Integer> {
    List<UserEntity> findAll();
}
