package com.thoughtworks.rslist.service;

import com.thoughtworks.rslist.dto.Trade;
import com.thoughtworks.rslist.dto.Vote;
import com.thoughtworks.rslist.entity.RsEventEntity;
import com.thoughtworks.rslist.entity.TradeEntity;
import com.thoughtworks.rslist.entity.UserEntity;
import com.thoughtworks.rslist.entity.VoteEntity;
import com.thoughtworks.rslist.exception.RequestNotValidException;
import com.thoughtworks.rslist.repository.RsEventRepository;
import com.thoughtworks.rslist.repository.TradeRepository;
import com.thoughtworks.rslist.repository.UserRepository;
import com.thoughtworks.rslist.repository.VoteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class RsService {
	final RsEventRepository rsEventRepository;
	final UserRepository userRepository;
	final VoteRepository voteRepository;
	final TradeRepository tradeRepository;

	public RsService(RsEventRepository rsEventRepository, UserRepository userRepository, VoteRepository voteRepository
			, TradeRepository tradeRepository) {
		this.rsEventRepository = rsEventRepository;
		this.userRepository = userRepository;
		this.voteRepository = voteRepository;
		this.tradeRepository = tradeRepository;
	}

	public void vote(Vote vote, int rsEventId) {
		Optional<RsEventEntity> rsEventDto = rsEventRepository.findById(rsEventId);
		Optional<UserEntity> userDto = userRepository.findById(vote.getUserId());
		if (!rsEventDto.isPresent()
				|| !userDto.isPresent()
				|| vote.getVoteNum() > userDto.get().getVoteNum()) {
			throw new RuntimeException();
		}
		VoteEntity voteEntity =
				VoteEntity.builder()
						.localDateTime(vote.getTime())
						.num(vote.getVoteNum())
						.rsEvent(rsEventDto.get())
						.user(userDto.get())
						.build();
		voteRepository.save(voteEntity);
		UserEntity user = userDto.get();
		user.setVoteNum(user.getVoteNum() - vote.getVoteNum());
		userRepository.save(user);
		RsEventEntity rsEvent = rsEventDto.get();
		rsEvent.setVoteNum(rsEvent.getVoteNum() + vote.getVoteNum());
		rsEventRepository.save(rsEvent);
	}

	@Transactional
	public void buy(Trade trade, int id) {
		Optional<RsEventEntity> rsEventResult = rsEventRepository.findById(id);
		if (!rsEventResult.isPresent()) {
			throw new RequestNotValidException("event not found");
		}
		RsEventEntity starter = rsEventResult.get();
		rsEventResult = rsEventRepository.findByRank(trade.getRank());
		if (!rsEventResult.isPresent()) {
			throw new RequestNotValidException("opponent not found");
		}
		RsEventEntity opponent = rsEventResult.get();
		if (opponent.getAmount() >= trade.getAmount()) {
			throw new RequestNotValidException("amount not enough");
		}
		if (opponent.getAmount() > 0) {
			rsEventRepository.deleteById(opponent.getId());
		}
		starter.setAmount(trade.getAmount());
		starter.setRank(trade.getRank());
		rsEventRepository.save(starter);
		TradeEntity tradeEntity = TradeEntity.builder().amount(trade.getAmount())
				.rank(trade.getRank()).rsEvent(starter).build();
		tradeRepository.save(tradeEntity);
	}


	public List<RsEventEntity> sortEvents() {
		List<RsEventEntity> allEvents = rsEventRepository.findAllByOrderByVoteNumDescIdAsc();
		List<RsEventEntity> paidEventList =
				allEvents.stream().filter(e -> e.getAmount() > 0).collect(Collectors.toList());
		List<RsEventEntity> freeEventList =
				allEvents.stream().filter(e -> e.getAmount() == 0).collect(Collectors.toList());
		List<RsEventEntity> temp = new ArrayList<>();
		IntStream.range(0, allEvents.size()).forEach(i -> temp.add(null));
		paidEventList.forEach(e -> temp.set(e.getRank() - 1, e));
		for (int i = 0, j = 0; i < temp.size(); i++) {
			if (temp.get(i) == null) {
				temp.set(i, freeEventList.get(j++));
			}
			temp.get(i).setRank(i + 1);
		}
		return temp;
	}

	@Transactional
	public void saveEventEntityInOrder() {
		List<RsEventEntity> rsEventEntityList = sortEvents();
		int rank = 1;
		for (RsEventEntity entity : rsEventEntityList) {
			entity.setRank(rank);
			rsEventRepository.save(entity);
			rank++;
		}
	}
}
