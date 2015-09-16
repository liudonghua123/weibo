package com.zhangqun.apps.weibo.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.zhangqun.apps.weibo.model.Weibo;

@Repository
public interface WeiboRepository extends CrudRepository<Weibo, Long> {

	public Weibo findByPerson(String person);

	@Query(value = "select sum(statuses) from weibo", nativeQuery = true)
	public int calculateStatusSum();
}
