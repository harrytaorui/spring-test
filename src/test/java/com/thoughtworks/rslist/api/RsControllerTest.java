package com.thoughtworks.rslist.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thoughtworks.rslist.dto.Trade;
import com.thoughtworks.rslist.dto.Vote;
import com.thoughtworks.rslist.entity.RsEventEntity;
import com.thoughtworks.rslist.entity.TradeEntity;
import com.thoughtworks.rslist.entity.UserEntity;
import com.thoughtworks.rslist.entity.VoteEntity;
import com.thoughtworks.rslist.repository.RsEventRepository;
import com.thoughtworks.rslist.repository.TradeRepository;
import com.thoughtworks.rslist.repository.UserRepository;
import com.thoughtworks.rslist.repository.VoteRepository;
import com.thoughtworks.rslist.service.RsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class RsControllerTest {
	@Autowired
	UserRepository userRepository;
	@Autowired
	RsEventRepository rsEventRepository;
	@Autowired
	VoteRepository voteRepository;
	@Autowired
	TradeRepository tradeRepository;
	@Autowired
	RsService rsService;
	@Autowired
	private MockMvc mockMvc;
	private UserEntity userEntity;
	private ObjectMapper mapper = new ObjectMapper();

	@BeforeEach
	void setUp() {
		voteRepository.deleteAll();
		rsEventRepository.deleteAll();
		userRepository.deleteAll();
		userEntity =
				UserEntity.builder()
						.voteNum(10)
						.phone("188888888888")
						.gender("female")
						.email("a@b.com")
						.age(19)
						.userName("idolice")
						.build();
	}

	@Test
	public void shouldGetRsEventList() throws Exception {
		UserEntity save = userRepository.save(userEntity);

		RsEventEntity rsEventEntity =
				RsEventEntity.builder().keyword("无分类").eventName("第一条事件").user(save).build();

		rsEventRepository.save(rsEventEntity);

		mockMvc
				.perform(get("/rs/list"))
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].eventName", is("第一条事件")))
				.andExpect(jsonPath("$[0].keyword", is("无分类")))
				.andExpect(jsonPath("$[0]", not(hasKey("user"))))
				.andExpect(status().isOk());
	}

	@Test
	public void shouldGetOneEvent() throws Exception {
		UserEntity save = userRepository.save(userEntity);

		RsEventEntity rsEventEntity =
				RsEventEntity.builder().keyword("无分类").eventName("第一条事件").user(save).build();

		rsEventRepository.save(rsEventEntity);
		rsEventEntity = RsEventEntity.builder().keyword("无分类").eventName("第二条事件").user(save).build();
		rsEventRepository.save(rsEventEntity);
		mockMvc.perform(get("/rs/1")).andExpect(jsonPath("$.eventName", is("第一条事件")));
		mockMvc.perform(get("/rs/1")).andExpect(jsonPath("$.keyword", is("无分类")));
		mockMvc.perform(get("/rs/2")).andExpect(jsonPath("$.eventName", is("第二条事件")));
		mockMvc.perform(get("/rs/2")).andExpect(jsonPath("$.keyword", is("无分类")));
	}

	@Test
	public void shouldGetErrorWhenIndexInvalid() throws Exception {
		mockMvc
				.perform(get("/rs/4"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error", is("invalid index")));
	}

	@Test
	public void shouldGetRsListBetween() throws Exception {
		UserEntity save = userRepository.save(userEntity);

		RsEventEntity rsEventEntity =
				RsEventEntity.builder().keyword("无分类").eventName("第一条事件").user(save).build();

		rsEventRepository.save(rsEventEntity);
		rsEventEntity = RsEventEntity.builder().keyword("无分类").eventName("第二条事件").user(save).build();
		rsEventRepository.save(rsEventEntity);
		rsEventEntity = RsEventEntity.builder().keyword("无分类").eventName("第三条事件").user(save).build();
		rsEventRepository.save(rsEventEntity);
		mockMvc
				.perform(get("/rs/list?start=1&end=2"))
				.andExpect(jsonPath("$", hasSize(2)))
				.andExpect(jsonPath("$[0].eventName", is("第一条事件")))
				.andExpect(jsonPath("$[0].keyword", is("无分类")))
				.andExpect(jsonPath("$[1].eventName", is("第二条事件")))
				.andExpect(jsonPath("$[1].keyword", is("无分类")));
		mockMvc
				.perform(get("/rs/list?start=2&end=3"))
				.andExpect(jsonPath("$", hasSize(2)))
				.andExpect(jsonPath("$[0].eventName", is("第二条事件")))
				.andExpect(jsonPath("$[0].keyword", is("无分类")))
				.andExpect(jsonPath("$[1].eventName", is("第三条事件")))
				.andExpect(jsonPath("$[1].keyword", is("无分类")));
		mockMvc
				.perform(get("/rs/list?start=1&end=3"))
				.andExpect(jsonPath("$", hasSize(3)))
				.andExpect(jsonPath("$[0].keyword", is("无分类")))
				.andExpect(jsonPath("$[1].eventName", is("第二条事件")))
				.andExpect(jsonPath("$[1].keyword", is("无分类")))
				.andExpect(jsonPath("$[2].eventName", is("第三条事件")))
				.andExpect(jsonPath("$[2].keyword", is("无分类")));
	}

	@Test
	public void shouldAddRsEventWhenUserExist() throws Exception {

		UserEntity save = userRepository.save(userEntity);

		String jsonValue =
				"{\"eventName\":\"猪肉涨价了\",\"keyword\":\"经济\",\"userId\": " + save.getId() + "}";

		mockMvc
				.perform(post("/rs/event").content(jsonValue).contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated());
		List<RsEventEntity> all = rsEventRepository.findAll();
		assertNotNull(all);
		assertEquals(all.size(), 1);
		assertEquals(all.get(0).getEventName(), "猪肉涨价了");
		assertEquals(all.get(0).getKeyword(), "经济");
		assertEquals(all.get(0).getUser().getUserName(), save.getUserName());
		assertEquals(all.get(0).getUser().getAge(), save.getAge());
	}

	@Test
	public void shouldAddRsEventWhenUserNotExist() throws Exception {
		String jsonValue = "{\"eventName\":\"猪肉涨价了\",\"keyword\":\"经济\",\"userId\": 100}";
		mockMvc
				.perform(post("/rs/event").content(jsonValue).contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}

	@Test
	public void shouldVoteSuccess() throws Exception {
		UserEntity save = userRepository.save(this.userEntity);
		RsEventEntity rsEventEntity =
				RsEventEntity.builder().keyword("无分类").eventName("第一条事件").user(save).build();
		rsEventEntity = rsEventRepository.save(rsEventEntity);

		String jsonValue =
				String.format(
						"{\"userId\":%d,\"time\":\"%s\",\"voteNum\":1}",
						save.getId(), LocalDateTime.now().toString());
		mockMvc
				.perform(
						post("/rs/vote/{id}", rsEventEntity.getId())
								.content(jsonValue)
								.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());

		UserEntity userEntity = userRepository.findById(save.getId()).get();
		RsEventEntity newRsEvent = rsEventRepository.findById(rsEventEntity.getId()).get();
		assertEquals(userEntity.getVoteNum(), 9);
		assertEquals(newRsEvent.getVoteNum(), 1);
		List<VoteEntity> voteEntities = voteRepository.findAll();
		assertEquals(voteEntities.size(), 1);
		assertEquals(voteEntities.get(0).getNum(), 1);
	}

	@Test
	public void should_buy_event_if_rank_not_sold() throws Exception {
		UserEntity save = userRepository.save(this.userEntity);
		RsEventEntity firstEvent =
				RsEventEntity.builder().keyword("无分类").eventName("第一条事件").user(save).rank(1).build();
		rsEventRepository.save(firstEvent);
		RsEventEntity secondEvent =
				RsEventEntity.builder().keyword("无分类").eventName("第二条事件").user(save).rank(2).build();
		rsEventRepository.save(secondEvent);
		Trade trade = Trade.builder().amount(10).rank(1).build();
		String jsonString = mapper.writeValueAsString(trade);
		mockMvc.perform(post("/rs/buy/2").content(jsonString)
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
		assertEquals(rsEventRepository.findById(1).get().getRank(), 2);
		assertEquals(rsEventRepository.findById(2).get().getRank(), 1);
	}

	@Test
	public void should_fail_buy_event_if_amount_not_enough() throws Exception {
		UserEntity save = userRepository.save(this.userEntity);
		RsEventEntity firstEvent =
				RsEventEntity.builder().keyword("无分类").eventName("第一条事件").user(save).rank(1).amount(10).build();
		rsEventRepository.save(firstEvent);
		RsEventEntity secondEvent =
				RsEventEntity.builder().keyword("无分类").eventName("第二条事件").user(save).rank(2).build();
		rsEventRepository.save(secondEvent);
		rsService.saveEventEntityInOrder();
		Trade trade = Trade.builder().amount(5).rank(1).build();
		String jsonString = mapper.writeValueAsString(trade);
		mockMvc.perform(post("/rs/buy/2").content(jsonString)
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}

	@Test
	public void should_replace_event_if_amount_enough() throws Exception {
		UserEntity save = userRepository.save(this.userEntity);
		RsEventEntity firstEvent =
				RsEventEntity.builder().keyword("无分类").eventName("第一条事件").user(save).rank(1).amount(10).build();
		rsEventRepository.save(firstEvent);
		RsEventEntity secondEvent =
				RsEventEntity.builder().keyword("无分类").eventName("第二条事件").user(save).rank(2).build();
		rsEventRepository.save(secondEvent);
		rsService.saveEventEntityInOrder();
		Trade trade = Trade.builder().amount(11).rank(1).build();
		String jsonString = mapper.writeValueAsString(trade);
		mockMvc.perform(post("/rs/buy/2").content(jsonString)
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
		List<RsEventEntity> list = rsEventRepository.findAll();
		assertEquals(list.size(),1);
		assertEquals(list.get(0).getId(),2);
	}

	@Test
	public void should_keep_trade_record_after_buy_event() throws Exception {
		UserEntity save = userRepository.save(this.userEntity);
		RsEventEntity firstEvent =
				RsEventEntity.builder().keyword("无分类").eventName("第一条事件").user(save).rank(1).amount(10).build();
		rsEventRepository.save(firstEvent);
		RsEventEntity secondEvent =
				RsEventEntity.builder().keyword("无分类").eventName("第二条事件").user(save).rank(2).build();
		rsEventRepository.save(secondEvent);
		rsService.saveEventEntityInOrder();
		Trade trade = Trade.builder().amount(11).rank(1).build();
		ObjectMapper mapper = new ObjectMapper();
		String jsonString = mapper.writeValueAsString(trade);
		mockMvc.perform(post("/rs/buy/2").content(jsonString)
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
		TradeEntity tradeEntity = tradeRepository.findAll().get(0);
		assertEquals(tradeEntity.getAmount(),11);
		assertEquals(tradeEntity.getRank(),1);
		assertEquals(tradeEntity.getRsEvent().getId(),2);
	}

	@Test
	void should_return_event_in_order_after_buy_and_vote() throws Exception {
		UserEntity save = userRepository.save(this.userEntity);
		RsEventEntity firstEvent =
				RsEventEntity.builder().keyword("无分类").eventName("第1条事件").user(save).rank(1).voteNum(4).build();
		rsEventRepository.save(firstEvent);
		RsEventEntity secondEvent =
				RsEventEntity.builder().keyword("无分类").eventName("第2条事件").user(save).rank(2).voteNum(0).build();
		rsEventRepository.save(secondEvent);
		RsEventEntity thirdEvent =
				RsEventEntity.builder().keyword("无分类").eventName("第3条事件").user(save).rank(3).voteNum(1).build();
		rsEventRepository.save(thirdEvent);
		rsService.saveEventEntityInOrder();
		Trade trade = Trade.builder().amount(11).rank(2).build();
		mockMvc.perform(post("/rs/buy/3").content(mapper.writeValueAsString(trade))
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
		List<RsEventEntity> list = rsEventRepository.findAllByOrderByRankAsc();
		assertEquals(list.get(0).getId(),1);
		assertEquals(list.get(1).getId(),3);
		assertEquals(list.get(2).getId(),2);
		String jsonValue =
				String.format(
						"{\"userId\":%d,\"time\":\"%s\",\"voteNum\":10}",
						save.getId(), LocalDateTime.now().toString());
		mockMvc.perform(post("/rs/vote/2").content(jsonValue)
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
		list = rsEventRepository.findAllByOrderByRankAsc();
		assertEquals(list.get(0).getId(),2);
		assertEquals(list.get(1).getId(),3);
		assertEquals(list.get(2).getId(),1);

	}


}
