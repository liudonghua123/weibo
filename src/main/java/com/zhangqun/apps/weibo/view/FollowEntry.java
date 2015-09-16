package com.zhangqun.apps.weibo.view;

public class FollowEntry {
	private String person;
	private String follower;

	public FollowEntry(String person, String follower) {
		this.person = person;
		this.follower = follower;
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((follower == null) ? 0 : follower.hashCode());
		result = prime * result + ((person == null) ? 0 : person.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FollowEntry other = (FollowEntry) obj;
		if (follower == null) {
			if (other.follower != null)
				return false;
		} else if (!follower.equals(other.follower))
			return false;
		if (person == null) {
			if (other.person != null)
				return false;
		} else if (!person.equals(other.person))
			return false;
		return true;
	}

}
