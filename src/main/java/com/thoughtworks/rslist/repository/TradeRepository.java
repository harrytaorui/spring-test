package com.thoughtworks.rslist.repository;

import com.thoughtworks.rslist.entity.TradeEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface TradeRepository extends CrudRepository<TradeEntity, Integer> {

	List<TradeEntity> findAll();
}
