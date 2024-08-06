package com.tenco.bank.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tenco.bank.dto.SignUpDTO;
import com.tenco.bank.handler.exception.DataDeliveryException;
import com.tenco.bank.handler.exception.RedirectException;
import com.tenco.bank.repository.interfaces.UserRepository;

@Service // IoC의 대상 (싱글톤으로 관리) - 제어의 역전
public class UserService {

	// DI - 의존 주입
	@Autowired
	private UserRepository userRepository;
	
//  @Autowired 어노테이션으로 대체가능하다
//  생성자 의존 주입 - DI
//	public UserService(UserRepository userRepository) {
//		this.userRepository = userRepository;
//	}

	
	
	/**
	 * 회원 등록 서비스 기능
	 * 트랜잭션 처리
	 * @param dto
	 */
	
	@Transactional // 트랜잭션 처리는 반드시 습관화
	public void createUser(SignUpDTO dto) {
		
		int result = 0;
		
		try {
			result =  userRepository.insert(dto.toUser());
			
		} catch (DataAccessException e) {
			throw new DataDeliveryException("잘못된 처리입니다.", HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RedirectException("알 수 없는 오류.", HttpStatus.SERVICE_UNAVAILABLE);
		}

		
		if(result != 1) {
			throw new DataDeliveryException("회원가입 실패", HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		
	}
}
