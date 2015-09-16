package com.zhangqun.apps.weibo.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
public class Follow {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	private String person;

	private String follower;

	private int interactives;

	public Follow(String person, String follower, int interactives) {
		super();
		this.person = person;
		this.follower = follower;
		this.interactives = interactives;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getPerson() {
		return person;
	}

	public void setPerson(String person) {
		this.person = person;
	}

	public String getFollower() {
		return follower;
	}

	public void setFollower(String follower) {
		this.follower = follower;
	}

	public int getInteractives() {
		return interactives;
	}

	public void setInteractives(int interactives) {
		this.interactives = interactives;
	}

	@Override
	public String toString() {
		return "Follow [id=" + id + ", person=" + person + ", follower="
				+ follower + ", interactives=" + interactives + "]";
	}

}
