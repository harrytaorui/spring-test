package com.thoughtworks.rslist.api;

import com.thoughtworks.rslist.entity.RsEventEntity;
import com.thoughtworks.rslist.entity.UserEntity;
import com.thoughtworks.rslist.entity.VoteEntity;
import com.thoughtworks.rslist.repository.RsEventRepository;
import com.thoughtworks.rslist.repository.UserRepository;
import com.thoughtworks.rslist.repository.VoteRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
class VoteControllerTest {
  @Autowired MockMvc mockMvc;
    @Autowired
    UserRepository userRepository;
    @Autowired
    RsEventRepository rsEventRepository;
    @Autowired
    VoteRepository voteRepository;
    UserEntity userEntity;
    RsEventEntity rsEventEntity;

    @BeforeEach
    void setUp() {
        userEntity = UserEntity.builder().userName("idolice").age(19).email("a@b.com").gender("female")
                .phone("18888888888").voteNum(10).build();
        userEntity = userRepository.save(userEntity);
        rsEventEntity = RsEventEntity.builder().user(userEntity).eventName("event name").keyword("keyword").voteNum(0)
                    .build();
        rsEventEntity = rsEventRepository.save(rsEventEntity);
        VoteEntity voteEntity = VoteEntity.builder().user(userEntity).rsEvent(rsEventEntity).localDateTime(LocalDateTime.now())
                .num(5).build();
        voteRepository.save(voteEntity);
      }

    @AfterEach
    void tearDown() {
        voteRepository.deleteAll();
        rsEventRepository.deleteAll();
        userRepository.deleteAll();
      }

    @Test
    public void shouldGetVoteRecord() throws Exception {
      VoteEntity voteEntity = VoteEntity.builder().user(userEntity).rsEvent(rsEventEntity).localDateTime(LocalDateTime.now())
              .num(1).build();
      voteRepository.save(voteEntity);
      voteEntity = VoteEntity.builder().user(userEntity).rsEvent(rsEventEntity).localDateTime(LocalDateTime.now())
              .num(2).build();
      voteRepository.save(voteEntity);

      voteEntity = VoteEntity.builder().user(userEntity).rsEvent(rsEventEntity).localDateTime(LocalDateTime.now())
              .num(3).build();
      voteRepository.save(voteEntity);

      voteEntity = VoteEntity.builder().user(userEntity).rsEvent(rsEventEntity).localDateTime(LocalDateTime.now())
              .num(4).build();
      voteRepository.save(voteEntity);

      voteEntity = VoteEntity.builder().user(userEntity).rsEvent(rsEventEntity).localDateTime(LocalDateTime.now())
              .num(6).build();
      voteRepository.save(voteEntity);

      voteEntity = VoteEntity.builder().user(userEntity).rsEvent(rsEventEntity).localDateTime(LocalDateTime.now())
              .num(7).build();
      voteRepository.save(voteEntity);

      voteEntity = VoteEntity.builder().user(userEntity).rsEvent(rsEventEntity).localDateTime(LocalDateTime.now())
              .num(8).build();
      voteRepository.save(voteEntity);



      mockMvc.perform(get("/voteRecord").param("userId",String.valueOf(userEntity.getId()))
      .param("rsEventId",String.valueOf(rsEventEntity.getId())).param("pageIndex", "1"))
              .andExpect(jsonPath("$", hasSize(5)))
              .andExpect(jsonPath("$[0].userId",is(userEntity.getId())))
              .andExpect(jsonPath("$[0].rsEventId",is(rsEventEntity.getId())))
              .andExpect(jsonPath("$[0].voteNum",is(5)))
              .andExpect(jsonPath("$[1].voteNum",is(1)))
              .andExpect(jsonPath("$[2].voteNum",is(2)))
              .andExpect(jsonPath("$[3].voteNum",is(3)))
              .andExpect(jsonPath("$[4].voteNum",is(4)));

      mockMvc.perform(get("/voteRecord").param("userId",String.valueOf(userEntity.getId()))
              .param("rsEventId",String.valueOf(rsEventEntity.getId())).param("pageIndex", "2"))
              .andExpect(jsonPath("$", hasSize(3)))
              .andExpect(jsonPath("$[0].userId",is(userEntity.getId())))
              .andExpect(jsonPath("$[0].rsEventId",is(rsEventEntity.getId())))
              .andExpect(jsonPath("$[0].voteNum",is(6)))
              .andExpect(jsonPath("$[1].voteNum",is(7)))
              .andExpect(jsonPath("$[2].voteNum",is(8)));


    }
}
