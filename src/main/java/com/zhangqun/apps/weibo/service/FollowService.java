package com.zhangqun.apps.weibo.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.zhangqun.apps.weibo.model.Follow;
import com.zhangqun.apps.weibo.repository.FollowRepository;

@Service
public class FollowService {

	@Autowired
	private FollowRepository repository;

	public void insert(List<Follow> follows) {
		repository.save(follows);
	}

	public int findTotalInteractivesByFollower(String follower) {
		return repository.findTotalInteractivesByFollower(follower);
	}

	public List<String> findFollower(String person) {
		return repository.findFollower(person);
	}

	public List<String> findFollowedPerson(String follower) {
		return repository.findFollowedPerson(follower);
	}

}
