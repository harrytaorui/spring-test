package com.thoughtworks.rslist.api;

import com.thoughtworks.rslist.dto.RsEvent;
import com.thoughtworks.rslist.dto.Trade;
import com.thoughtworks.rslist.dto.Vote;
import com.thoughtworks.rslist.entity.RsEventEntity;
import com.thoughtworks.rslist.entity.UserEntity;
import com.thoughtworks.rslist.exception.Error;
import com.thoughtworks.rslist.exception.RequestNotValidException;
import com.thoughtworks.rslist.repository.RsEventRepository;
import com.thoughtworks.rslist.repository.UserRepository;
import com.thoughtworks.rslist.service.RsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@Validated
public class RsController {
	@Autowired
	RsEventRepository rsEventRepository;
	@Autowired
	UserRepository userRepository;
	@Autowired
	RsService rsService;

	@GetMapping("/rs/list")
	public ResponseEntity<List<RsEvent>> getRsEventListBetween(
			@RequestParam(required = false) Integer start, @RequestParam(required = false) Integer end) {
		rsService.saveEventEntityInOrder();
		List<RsEvent> rsEvents =
				rsEventRepository.findAll().stream()
						.map(item -> RsEvent.builder()
								.eventName(item.getEventName())
								.keyword(item.getKeyword())
								.userId(item.getId())
								.voteNum(item.getVoteNum())
								.rank(item.getRank())
								.build())
						.collect(Collectors.toList());
		if (start == null || end == null) {
			return ResponseEntity.ok(rsEvents);
		}
		return ResponseEntity.ok(rsEvents.subList(start - 1, end));
	}

	@GetMapping("/rs/{index}")
	public ResponseEntity<RsEvent> getRsEvent(@PathVariable int index) {
		rsService.saveEventEntityInOrder();
		List<RsEvent> rsEvents =
				rsEventRepository.findAll().stream()
						.map(item -> RsEvent.builder()
								.eventName(item.getEventName())
								.keyword(item.getKeyword())
								.userId(item.getId())
								.voteNum(item.getVoteNum())
								.rank(item.getRank())
								.build())
						.collect(Collectors.toList());
		if (index < 1 || index > rsEvents.size()) {
			throw new RequestNotValidException("invalid index");
		}
		return ResponseEntity.ok(rsEvents.get(index - 1));
	}

	@PostMapping("/rs/event")
	public ResponseEntity addRsEvent(@RequestBody @Valid RsEvent rsEvent) {
		Optional<UserEntity> userDto = userRepository.findById(rsEvent.getUserId());
		if (!userDto.isPresent()) {
			return ResponseEntity.badRequest().build();
		}
		RsEventEntity build =
				RsEventEntity.builder()
						.keyword(rsEvent.getKeyword())
						.eventName(rsEvent.getEventName())
						.voteNum(0)
						.user(userDto.get())
						.build();
		rsEventRepository.save(build);
		rsService.saveEventEntityInOrder();
		return ResponseEntity.created(null).build();
	}

	@PostMapping("/rs/vote/{id}")
	public ResponseEntity vote(@PathVariable int id, @RequestBody Vote vote) {
		rsService.vote(vote, id);
		rsService.saveEventEntityInOrder();
		return ResponseEntity.ok().build();
	}

	@PostMapping("/rs/buy/{id}")
	public ResponseEntity buy(@PathVariable int id, @RequestBody Trade trade) {
		rsService.buy(trade, id);
		rsService.saveEventEntityInOrder();
		return ResponseEntity.ok().build();
	}


	@ExceptionHandler(RequestNotValidException.class)
	public ResponseEntity<Error> handleRequestErrorHandler(RequestNotValidException e) {
		Error error = new Error();
		error.setError(e.getMessage());
		return ResponseEntity.badRequest().body(error);
	}
}
