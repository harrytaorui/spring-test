package com.thoughtworks.rslist.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class RsEvent implements Serializable {
  @NotNull private String eventName;
  @NotNull private String keyword;
  private int voteNum;
  @NotNull private int userId;
  private int rank;
  @JsonIgnore private int amount = 0;
}
