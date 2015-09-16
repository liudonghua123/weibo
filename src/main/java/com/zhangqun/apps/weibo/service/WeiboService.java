package com.zhangqun.apps.weibo.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.zhangqun.apps.weibo.model.Weibo;
import com.zhangqun.apps.weibo.repository.FollowRepository;
import com.zhangqun.apps.weibo.repository.WeiboRepository;

@Service
public class WeiboService {

	@Autowired
	private WeiboRepository repository;

	public void insert(List<Weibo> weibos) {
		repository.save(weibos);
	}

	public int calculateStatusSum() {
		return repository.calculateStatusSum();
	}

}
