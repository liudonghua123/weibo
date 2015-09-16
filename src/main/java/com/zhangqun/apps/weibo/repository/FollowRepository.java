package com.zhangqun.apps.weibo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.zhangqun.apps.weibo.model.Follow;

@Repository
public interface FollowRepository extends CrudRepository<Follow, Long> {

	public Follow findByFollower(String follower);

	@Query(value = "select sum(interactives) from follow where follower = ?1", nativeQuery = true)
	public int findTotalInteractivesByFollower(String follower);

	@Query(value = "select follower from follow where person = ?1", nativeQuery = true)
	public List<String> findFollower(String person);

	@Query(value = "select person from follow where follower = ?1", nativeQuery = true)
	public List<String> findFollowedPerson(String follower);
}
