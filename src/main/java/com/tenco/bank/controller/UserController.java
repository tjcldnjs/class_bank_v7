package com.tenco.bank.controller;

import java.net.URI;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.tenco.bank.dto.SignInDTO;
import com.tenco.bank.dto.SignUpDTO;
import com.tenco.bank.handler.exception.DataDeliveryException;
import com.tenco.bank.repository.model.User;
import com.tenco.bank.service.UserService;
import com.tenco.bank.utils.Define;

import jakarta.servlet.http.HttpSession;

@Controller // Ioc 에 대상 (싱글톤 패턴으로 관리)
@RequestMapping("/user") // 대문 처리
public class UserController {
	
	private UserService userService;
	private final HttpSession session;
	
	@Autowired // DI 처리      // 노란색 경고는 사용할 필요 없음 -  가독성을 위해서 선언해도 됨
	public UserController(UserService service, HttpSession session) {
		this.userService = service;
		this.session = session;
	}
	
	// 주소설계 -> http://localhost:8080/user/sign-up
	
	/**
	 * 회원 가입 페이지 요청
	 * 주소 설계 : http://localhost:8080/user/sign-up 
	 * @return signUp.jsp
	 */
	@GetMapping("/sign-up")
	public String signUpPage() {
		return "user/signUp";
	}
	
	
	/**
	 * 회원가입 로직 처리 요청
	 * 주소 설계 : http://localhost:8080/user/sign-up
	 * @param dto
	 * @return
	 */
	@PostMapping("/sign-up")
	public String signUpProc(SignUpDTO dto) {
		// controller 에서 일반적인 코드 작업
		// 1. 인증검사 (여기서는 인증검사 불 필요)
		// 2. 유효성 검사
		if(dto.getUsername() == null || dto.getUsername().isEmpty()) {
			throw new DataDeliveryException(Define.ENTER_YOUR_USERNAME, HttpStatus.BAD_REQUEST);
		}
		
		if(dto.getPassword() == null || dto.getPassword().isEmpty()) {
			throw new DataDeliveryException(Define.ENTER_YOUR_PASSWORD, HttpStatus.BAD_REQUEST);
		}
		
		if(dto.getFullname() == null || dto.getFullname().isEmpty()) {
			throw new DataDeliveryException(Define.ENTER_YOUR_FULLNAME, HttpStatus.BAD_REQUEST);
		}
		
		// 서비스 객체로 전달
		userService.createUser(dto);
		
		return "redirect:/user/sign-in";
	}
	
	/*
	 * 로그인 화면 요청
	 * 주소설계 : http://localhost:8080/user/sign-in
	 */
	@GetMapping("/sign-in")
	public String signInPage() {
		return "user/signIn";
	}
	
	/**
	 * 로그인 요청처리
	 * 주소설계 : http://localhost:8080/user/sign-in
	 * @return
	 */
	@PostMapping("/sign-in")
	public String signProc(SignInDTO dto) {
		
		// 1. 인증검사 x
		// 2. 유효성 검사
		
		if(dto.getUsername() == null || dto.getUsername().isEmpty()) {
			throw new DataDeliveryException(Define.ENTER_YOUR_USERNAME, HttpStatus.BAD_REQUEST);
		}
		
		if(dto.getPassword() == null || dto.getPassword().isEmpty()) {
			throw new DataDeliveryException(Define.ENTER_YOUR_PASSWORD, HttpStatus.BAD_REQUEST);
			
		}
		
		// 서비스 호출
		User principal =  userService.readUser(dto);
		
		// 세션 메모리에 등록 처리
		session.setAttribute(Define.PRINCIPAL, principal);
		
		// 새로운 페이지로 이동 처리
		// TODO - 계좌 목록 페이지 이동처리 예정
		return "redirect:/account/list";
	}
	
	/**
	 * 로그아웃 처리
	 * @return
	 */
	@GetMapping("/logout")
	public String logout() {
		// HTTP 세션을 무효화 (세션의 모든 데이터가 제거되고 세션이 종료)
		session.invalidate(); 
		return "redirect:/user/sign-in";
	}
	
	@GetMapping("/kakao")
	@ResponseBody
	public ResponseEntity<?> kakaoToken(@RequestParam(name = "code", required = false) String code) {
		System.out.println(code);
		URI uri = UriComponentsBuilder.fromUriString("http://localhost:8080/")
				.path("?" + code).build().toUri();
		RestTemplate restTemplate = new RestTemplate();
		ResponseEntity<String> response = restTemplate.getForEntity(uri, String.class);
		
		
		return ResponseEntity.status(HttpStatus.OK).body(response.getBody());
	}

}
