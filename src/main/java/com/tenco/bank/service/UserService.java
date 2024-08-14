package com.tenco.bank.service;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.tenco.bank.dto.SignInDTO;
import com.tenco.bank.dto.SignUpDTO;
import com.tenco.bank.handler.exception.DataDeliveryException;
import com.tenco.bank.handler.exception.RedirectException;
import com.tenco.bank.repository.interfaces.UserRepository;
import com.tenco.bank.repository.model.User;
import com.tenco.bank.utils.Define;

import lombok.RequiredArgsConstructor;

@Service // IoC의 대상 (싱글톤으로 관리) - 제어의 역전
@RequiredArgsConstructor
public class UserService {

	@Autowired // 필요가 없지만 가독성을 위해
	private final UserRepository userRepository;
	@Autowired
	private final PasswordEncoder passwordEncoder;
	
	// 초기 파라메터 가져오는 방법
	@Value("${file.upload-dir}")
	private String uploadDir;
	
	
	
	/**
	 * 회원 등록 서비스 기능 트랜잭션 처리
	 * 
	 * @param dto
	 */

	@Transactional // 트랜잭션 처리는 반드시 습관화
	public void createUser(SignUpDTO dto) {
		int result = 0;
		
		System.out.println(dto.getMFile().getOriginalFilename());
		
		if(!dto.getMFile().isEmpty()) {
			// 파일 업로드 로직 구현
			String[] fileNames = uploadFile(dto.getMFile());
			dto.setOriginFileName(fileNames[0]);
			dto.setUploadFileName(fileNames[1]);
		}
		
		try {
			// 코드 추가 부분
			// 회원 가입 요청시 사용자가 던진 비밀번호 값을 암호화 처리해야 함
			String hashPwd = passwordEncoder.encode(dto.getPassword());
			System.out.println("hashPwd : " + hashPwd);
			dto.setPassword(hashPwd);
			result = userRepository.insert(dto.toUser());
		} catch (DataAccessException e) {
			throw new DataDeliveryException(Define.FAIL_TO_CREATE_USER, HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (Exception e) {
			throw new RedirectException(Define.UNKNOWN, HttpStatus.SERVICE_UNAVAILABLE);
		}
		if (result != 1) {
			throw new DataDeliveryException(Define.FAIL_TO_CREATE_USER, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}


	public User readUser(SignInDTO dto) {
		// 유효성 검사는 Controller 에서 먼저 하자.
		User userEntity = null;

		// 기능 수정
		// username 으로만 --> select 처리
		// 2가지의 경우의 수 --> 객체가 존재, null

		// 객체안에 사용자의 암호화 되어 있는 값 password 가 존재 한다.

		// passwordEncoder 안에 matches 메서드를 사용해서 판별한다. 

		try {
			userEntity = userRepository.findByUsername(dto.getUsername());
		} catch (DataAccessException e) {
			throw new DataDeliveryException(Define.FAILED_PROCESSING, HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (Exception e) {
			throw new RedirectException(Define.UNKNOWN, HttpStatus.SERVICE_UNAVAILABLE);
		}

		if (userEntity == null) {
			throw new DataDeliveryException(Define.INVALID_INPUT, HttpStatus.BAD_REQUEST);
		}
		boolean isPwdMatched = passwordEncoder.matches(dto.getPassword(), userEntity.getPassword());
		if(isPwdMatched == false) {
			throw new DataDeliveryException("비밀번호가 잘못되었습니다.", HttpStatus.BAD_REQUEST);
		}
		return userEntity;
	}
	
	/**
	 * 서버 운영체제에 파일 업로드 기능
	 * MultipartFile getOrifinalFilename : 사용자가 작성한 파일명
	 * uploadFileName : 서버 컴퓨터에 저장될 파일 명
	 * @return
	 */
	private String[] uploadFile(MultipartFile mFile) {
		// 파일 업로드 구현
		if(mFile.getSize() > Define.MAX_FILE_SIZE) {
			throw new DataDeliveryException("파일 크기는 20MB 이상 클 수 없습니다.", HttpStatus.BAD_REQUEST);
		}
		
		// 코드 수정
		// File - getAbsoluthPath() : 파일 시스템의 절대 경로를 나타냅니다.
		// (리눅스 또는 MacOS)에 맞춰서 절대 경로가 생성을 시킬 수 있다.
		// String saveDirectory = new File(uploadDir).getAbsolutePath();
		String saveDirectory = uploadDir;
		
		// 파일 이름 생성(중복 이름 예방)
		String uploadFileName = UUID.randomUUID() + "_" + mFile.getOriginalFilename();
		// 파일 전체경로 + 새로생성한 파일명
		String uploadPath = saveDirectory + File.separator +uploadFileName;
		System.out.println("==--=-=-==--=-==-=--=--=-==-=-=-=-=--=-=-=");
		System.out.println(uploadPath);
		System.out.println("==--=-=-==--=-==-=--=--=-==-=-=-=-=--=-=-=");
		File destination = new File(uploadPath);
		
		// 반드시 수행
		try {
			mFile.transferTo(destination);
		} catch (IllegalStateException | IOException e) {
			e.printStackTrace();
			throw new DataDeliveryException("파일 업로드중에 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		return new String[] {mFile.getOriginalFilename(), uploadFileName};
	}
}
