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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

class RsServiceTest {
  RsService rsService;

  @Mock RsEventRepository rsEventRepository;
  @Mock UserRepository userRepository;
  @Mock VoteRepository voteRepository;
  @Mock TradeRepository tradeRepository;
  LocalDateTime localDateTime;
  Vote vote;

  @BeforeEach
  void setUp() {
    initMocks(this);
    rsService = new RsService(rsEventRepository, userRepository, voteRepository, tradeRepository);
    localDateTime = LocalDateTime.now();
    vote = Vote.builder().voteNum(2).rsEventId(1).time(localDateTime).userId(1).build();
  }

  @Test
  void shouldVoteSuccess() {
    // given

    UserEntity userEntity =
        UserEntity.builder()
            .voteNum(5)
            .phone("18888888888")
            .gender("female")
            .email("a@b.com")
            .age(19)
            .userName("xiaoli")
            .id(2)
            .build();
    RsEventEntity rsEventEntity =
        RsEventEntity.builder()
            .eventName("event name")
            .id(1)
            .keyword("keyword")
            .voteNum(2)
            .user(userEntity)
            .build();

    when(rsEventRepository.findById(anyInt())).thenReturn(Optional.of(rsEventEntity));
    when(userRepository.findById(anyInt())).thenReturn(Optional.of(userEntity));
    // when
    rsService.vote(vote, 1);
    // then
    verify(voteRepository)
        .save(
            VoteEntity.builder()
                .num(2)
                .localDateTime(localDateTime)
                .user(userEntity)
                .rsEvent(rsEventEntity)
                .build());
    verify(userRepository).save(userEntity);
    verify(rsEventRepository).save(rsEventEntity);
  }

  @Test
  void shouldThrowExceptionWhenUserNotExist() {
    // given
    when(rsEventRepository.findById(anyInt())).thenReturn(Optional.empty());
    when(userRepository.findById(anyInt())).thenReturn(Optional.empty());
    //when&then
    assertThrows(
        RuntimeException.class,
        () -> {
          rsService.vote(vote, 1);
        });
  }

  @Test
  void should_buy_event_when_rank_not_sold(){
    //give
    RsEventEntity opponent =
            RsEventEntity.builder()
                    .eventName("event name")
                    .id(1)
                    .keyword("keyword")
                    .voteNum(2)
                    .amount(0)
                    .build();
    RsEventEntity starter =
            RsEventEntity.builder()
                    .eventName("event name")
                    .id(2)
                    .keyword("keyword")
                    .voteNum(2)
                    .amount(0)
                    .build();
    when(rsEventRepository.findById(anyInt())).thenReturn(Optional.of(starter));
    when(rsEventRepository.findByRank(anyInt())).thenReturn(Optional.of(opponent));
    Trade trade = Trade.builder().amount(5).rank(1).build();
    //when
    rsService.buy(trade,2);
    //then
    verify(rsEventRepository).save(starter);
    verify(tradeRepository).save(TradeEntity.builder().amount(trade.getAmount())
            .rank(trade.getRank()).rsEvent(starter).build());
  }

  @Test
  void should_fail_buy_event_if_amount_not_enough(){
    //give
    RsEventEntity opponent =
            RsEventEntity.builder()
                    .eventName("event name")
                    .id(1)
                    .keyword("keyword")
                    .voteNum(2)
                    .amount(10)
                    .build();
    RsEventEntity starter =
            RsEventEntity.builder()
                    .eventName("event name")
                    .id(2)
                    .keyword("keyword")
                    .voteNum(2)
                    .amount(0)
                    .build();
    when(rsEventRepository.findById(anyInt())).thenReturn(Optional.of(starter));
    when(rsEventRepository.findByRank(anyInt())).thenReturn(Optional.of(opponent));
    Trade trade = Trade.builder().amount(5).rank(1).build();
    //when&then
    assertThrows(
            RequestNotValidException.class,
            () -> {
              rsService.buy(trade,2);
            });
  }

  @Test
  void should_replace_event_if_amount_enough(){
    //give
    RsEventEntity opponent =
            RsEventEntity.builder()
                    .eventName("event name")
                    .id(1)
                    .keyword("keyword")
                    .voteNum(2)
                    .amount(1)
                    .build();
    RsEventEntity starter =
            RsEventEntity.builder()
                    .eventName("event name")
                    .id(2)
                    .keyword("keyword")
                    .voteNum(2)
                    .amount(0)
                    .build();
    when(rsEventRepository.findById(anyInt())).thenReturn(Optional.of(starter));
    when(rsEventRepository.findByRank(anyInt())).thenReturn(Optional.of(opponent));
    Trade trade = Trade.builder().amount(2).rank(1).build();
    //when
    rsService.buy(trade,2);
    //then
    verify(rsEventRepository).save(starter);
    verify(tradeRepository).save(TradeEntity.builder().amount(trade.getAmount())
            .rank(trade.getRank()).rsEvent(starter).build());
  }
}
