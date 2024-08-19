package com.tenco.bank.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tenco.bank.handler.exception.DataDeliveryException;
import com.tenco.bank.repository.interfaces.BlogRepository;
import com.tenco.bank.repository.model.Blog;

@Service
public class BlogService {

	private final BlogRepository blogRepository;

	public BlogService(BlogRepository blogRepository) {
		this.blogRepository = blogRepository;
	}

	@Transactional
	public void create(Blog blog) {
		if (blog.getTitle() == null || blog.getTitle().isEmpty()) {
			throw new DataDeliveryException("제목을 입력해주세요", HttpStatus.BAD_REQUEST);
		}
		if (blog.getContent() == null || blog.getContent().isEmpty()) {
			throw new DataDeliveryException("내용을 입력해주세요", HttpStatus.BAD_REQUEST);
		}
		if (blog.getName() == null || blog.getName().isEmpty()) {
			throw new DataDeliveryException("이름을 입력해주세요", HttpStatus.BAD_REQUEST);
		}
		
		try {
			blogRepository.insert(blog);
		} catch (DataAccessException e) {
			throw new DataDeliveryException("작성되지 않았습니다.", HttpStatus.BAD_REQUEST);
		}
	}
	
	public List<Blog> select(int page, int size) {
		List<Blog> blogListEntity = new ArrayList<>();

		int limit = size;
		int offset = (page - 1) * size;
		
		blogListEntity = blogRepository.select(limit, offset); 
		
		return blogListEntity;
	}
	
	@Transactional
	public void update(Blog blog) {
		
		if (blog.getTitle() == null || blog.getTitle().isEmpty()) {
			throw new DataDeliveryException("제목을 입력해주세요", HttpStatus.BAD_REQUEST);
		}
		if (blog.getContent() == null || blog.getContent().isEmpty()) {
			throw new DataDeliveryException("내용을 입력해주세요", HttpStatus.BAD_REQUEST);
		}
		if (blog.getName() == null || blog.getName().isEmpty()) {
			throw new DataDeliveryException("이름을 입력해주세요", HttpStatus.BAD_REQUEST);
		}
		
		try {
			blogRepository.updateById(blog);
		} catch (DataAccessException e) {
			throw new DataDeliveryException("제목과 내용을 20자내로 입력해주세요", HttpStatus.BAD_REQUEST);
		}
	}
	
	@Transactional
	public void delete(Integer id) {
		blogRepository.deleteById(id);
	}

	public Integer selectCount(){
		Integer result = blogRepository.selectCount();
		return result;
	}
	
	public Blog selectById(Integer id) {
		return blogRepository.selectById(id);
	}
}
