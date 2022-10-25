package com.example.application.data.repository;


import com.example.application.data.entity.Role;

public interface RoleDao {

	public Role findRoleByName(String theRoleName);
	
}
