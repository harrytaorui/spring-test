package com.thoughtworks.rslist.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;

@Entity
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "rsEvent")
public class RsEventEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	private String eventName;
	private String keyword;
	private int voteNum;
	private int rank = 0;
	private int amount = 0;
	@ManyToOne
	@JoinColumn(name = "user_id")
	private UserEntity user;
	@OneToMany(cascade = CascadeType.DETACH, mappedBy = "rsEvent")
	private List<TradeEntity> tradeEntityList;
}
