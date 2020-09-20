package com.thoughtworks.rslist.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "user")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity {
	@Id
	@GeneratedValue
	private int id;
	@Column(name = "name")
	private String userName;
	private String gender;
	private int age;
	private String email;
	private String phone;
	private int voteNum = 10;

	@OneToMany(cascade = CascadeType.REMOVE, mappedBy = "user")
	private List<RsEventEntity> rsEventEntities;
}
