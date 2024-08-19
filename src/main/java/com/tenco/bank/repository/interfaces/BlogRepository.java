package com.tenco.bank.repository.interfaces;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.tenco.bank.repository.model.Blog;

@Mapper
public interface BlogRepository {
	public void insert(Blog blog);
	public void updateById(Blog blog);
	public void deleteById(Integer id);
	public List<Blog> select(@Param("limit") Integer limit, @Param("offset") Integer offset);
	public Integer selectCount();
	public Blog selectById(Integer id);
}
