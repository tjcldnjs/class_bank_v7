package com.tenco.bank.repository.interfaces;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.tenco.bank.repository.model.User;

// 마이바티스 설정 확인

// UserRepository 인터페스와 user.xml 파일을 매칭 시킨다.

@Mapper
public interface UserRepository {

	public int insert(User user);
	public int updateById(User user);
	public int deleteById(Integer id);
	public User findbyId(Integer id);
	public List<User> findAll();
	
	// 로그인 기능 x (username, password) --> return User ..
	// 주의 ! - 매개변수 2개 이상 시 반드시 @Param 어노테이션을 사용하자!
	public User findByUsernameAndPassword(@Param("username") String username,@Param("password") String password);
	
	public User findByUsername(@Param("username") String username);
}
