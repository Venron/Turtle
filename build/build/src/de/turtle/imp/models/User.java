package de.turtle.imp.models;

import java.io.Serializable;

public class User implements Serializable {

	private int userId;
	private String userName;
	private Role role;

	public User() {
		this(0, "anon", new Role());
	}

	public User(int userId, String userName, Role role) {
		this.userId = userId;
		this.userName = userName;
		this.role = role;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
	}

	public String toString() {
		return "User [userId=" + userId + ", userName=" + userName + ", role=" + role + "]";
	}

}
