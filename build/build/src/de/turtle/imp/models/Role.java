package de.turtle.imp.models;

import java.io.Serializable;

public class Role implements Serializable {
	private int roleId;
	private String roleName;

	public Role() {
		this(0, "unauthenticated");
	}

	public Role(int roleId, String roleName) {
		this.roleId = roleId;
		this.roleName = roleName;
	}

	public Role(String roleName) {
		if (roleName.equals("user")) {
			this.roleId = 1;
			this.roleName = "user";
		} else if (roleName.equals("admin")) {
			this.roleId = 100;
			this.roleName = "admin";
		} else {
			this.roleId = 0;
			this.roleName = "unauthenticated";
		}
	}

	public int getRoleId() {
		return roleId;
	}

	public void setRoleId(int roleId) {
		this.roleId = roleId;
	}

	public String getRoleName() {
		return roleName;
	}

	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}

	public String toString() {
		return "Role [roleId=" + roleId + ", roleName=" + roleName + "]";
	}

}
