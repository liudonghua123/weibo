package com.zhangqun.apps.weibo.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
public class Weibo {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	private String person;

	private int statuses;
	
	public Weibo(String person, int statuses) {
		this.person = person;
		this.statuses = statuses;
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

	public int getStatuses() {
		return statuses;
	}

	public void setStatuses(int statuses) {
		this.statuses = statuses;
	}

	@Override
	public String toString() {
		return "Follow [id=" + id + ", person=" + person+ ", statuses=" + statuses + "]";
	}

}
