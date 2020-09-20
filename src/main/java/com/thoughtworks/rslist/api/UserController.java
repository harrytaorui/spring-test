package com.thoughtworks.rslist.api;

import com.thoughtworks.rslist.dto.User;
import com.thoughtworks.rslist.entity.UserEntity;
import com.thoughtworks.rslist.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
public class UserController {
  @Autowired UserRepository userRepository;

  @PostMapping("/user")
  public void register(@RequestBody @Valid User user) {
    UserEntity userEntity = new UserEntity();
    userEntity.setGender(user.getGender());
    userEntity.setPhone(user.getPhone());
    userEntity.setVoteNum(user.getVoteNum());
    userEntity.setAge(user.getAge());
    userEntity.setEmail(user.getEmail());
    userEntity.setUserName(user.getUserName());
    userRepository.save(userEntity);
  }

  @DeleteMapping("/user/{id}")
  public ResponseEntity deleteUser(@PathVariable int id) {
    userRepository.deleteById(id);
    return ResponseEntity.ok().build();
  }
}
