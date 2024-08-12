package com.tenco.bank.controller;

import java.util.Arrays;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.tenco.bank.dto.DepositDTO;
import com.tenco.bank.dto.SaveDTO;
import com.tenco.bank.dto.TransferDTO;
import com.tenco.bank.dto.WithdrawalDTO;
import com.tenco.bank.handler.exception.DataDeliveryException;
import com.tenco.bank.handler.exception.UnAuthorizedException;
import com.tenco.bank.repository.model.Account;
import com.tenco.bank.repository.model.HistoryAccount;
import com.tenco.bank.repository.model.User;
import com.tenco.bank.service.AccountService;
import com.tenco.bank.utils.Define;

import jakarta.servlet.http.HttpSession;

@Controller // IoC대상 (싱글톤으로 관리)
@RequestMapping("/account")
public class AccountController {

	// 계좌 생성 화면 요청 - DI 처리
	private final HttpSession session;
	private final AccountService accountService; // final - 동작속도가 더빠름

	// @Autowired
	public AccountController(AccountService accountService, HttpSession session) {
		this.session = session;
		this.accountService = accountService;
	}

	/**
	 * 계좌 생성 페이지 요청 주소 설계 : http://localhost:8080/account/save
	 * 
	 * @return
	 */
	@GetMapping("/save")
	public String savePage() {
		
		// 1. 인증 검사가 필요(account 전체 필요함)
		User principal = (User) session.getAttribute(Define.PRINCIPAL);
		if (principal == null) {
			throw new UnAuthorizedException(Define.NOT_AN_AUTHENTICATED_USER, HttpStatus.UNAUTHORIZED);
		}

		return "account/save";
	}

	/**
	 * 계좌 생성 기능 요청 주소설계 : http://localhost:8080/account/save 추후 계좌 목록 페이지 이동 처리
	 * 
	 */

	@PostMapping("/save")
	public String saveProc(SaveDTO dto) {
		// 1. form 데이터 추출 (파싱 전략)
		// 2. 인증 검사
		// 3. 유효성 검사
		// 4. 서비스 호출
		User principal = (User) session.getAttribute(Define.PRINCIPAL);
		if (principal == null) {
			throw new UnAuthorizedException(Define.NOT_AN_AUTHENTICATED_USER, HttpStatus.UNAUTHORIZED);
		}

		if (dto.getNumber() == null || dto.getNumber().isEmpty()) {
			throw new DataDeliveryException(Define.ENTER_YOUR_ACCOUNT_NUMBER, HttpStatus.BAD_REQUEST);
		}

		if (dto.getPassword() == null || dto.getPassword().isEmpty()) {
			throw new DataDeliveryException(Define.FAIL_ACCOUNT_PASSWROD, HttpStatus.BAD_REQUEST);
		}

		if (dto.getBalance() == null || dto.getBalance() < 0) {
			throw new DataDeliveryException(Define.ENTER_YOUR_BALANCE, HttpStatus.BAD_REQUEST);
		}
		
		accountService.createAccount(dto, principal.getId());

		return "redirect:/account/list";
	}

	/**
	 * 계좌 목록 화면 요청 주소설계 : http://localhost:8080/account/list, .../
	 * 
	 * @return
	 */
	@GetMapping({ "/list", "/" })
	public String listPage(Model model) {

		// 1. 인증검사
		User principal = (User) session.getAttribute(Define.PRINCIPAL);
		if (principal == null) {
			throw new UnAuthorizedException(Define.NOT_AN_AUTHENTICATED_USER, HttpStatus.UNAUTHORIZED);
		}
		// 2. 유효성 검사
		// 3. 서비스 호출
		List<Account> accountList = accountService.readAccountListByUserId(principal.getId());
		// 계좌가 하나도 없을때
		if (accountList.isEmpty()) {
			model.addAttribute("accountList", null);
			// 계좌가 있을때
		} else {
			model.addAttribute("accountList", accountList);
		}
		// jsp 데이터를 넣어주는 방법

		return "account/list";
	}

	/**
	 * 출금 페이지 요청
	 * 
	 * @return withdrawal.jsp
	 */
	@GetMapping("/withdrawal")
	public String withdrawalPage() {
		// 1. 인증검사
		User principal = (User) session.getAttribute(Define.PRINCIPAL);
		if (principal == null) {
			throw new UnAuthorizedException(Define.NOT_AN_AUTHENTICATED_USER, HttpStatus.UNAUTHORIZED);
		}
		return "account/withdrawal";
	}

	@PostMapping("/withdrawal")
	public String withdrawalProc(WithdrawalDTO dto) {
		// 1. 인증검사
		User principal = (User) session.getAttribute(Define.PRINCIPAL);
		if (principal == null) {
			throw new UnAuthorizedException(Define.NOT_AN_AUTHENTICATED_USER, HttpStatus.UNAUTHORIZED);
		}

		// 2. 유효성 검사 (자바 코드를 개발) --> 스프링 부트 @Valid 라이브러리가 존재
		if (dto.getAmount() == null) {
			throw new DataDeliveryException(Define.ENTER_YOUR_BALANCE, HttpStatus.BAD_REQUEST);
		}
		if (dto.getAmount().longValue() <= 0) {
			throw new DataDeliveryException(Define.W_BALANCE_VALUE, HttpStatus.BAD_REQUEST);
		}
		if (dto.getWAccountNumber() == null) {
			throw new DataDeliveryException(Define.ENTER_YOUR_ACCOUNT_NUMBER, HttpStatus.BAD_REQUEST);
		}

		if (dto.getWAccountPassword() == null || dto.getWAccountPassword().isEmpty()) {
			throw new DataDeliveryException(Define.ENTER_YOUR_PASSWORD, HttpStatus.BAD_REQUEST);
		}

		accountService.updateAccountWithdraw(dto, principal.getId());

		return "redirect:/account/list";
	}

	/**
	 * 입금 페이지 요청
	 * 
	 * @return
	 */
	@GetMapping("/deposit")
	public String depositPage() {
		// 1. 인증검사
		User principal = (User) session.getAttribute(Define.PRINCIPAL);
		if (principal == null) {
			throw new UnAuthorizedException(Define.NOT_AN_AUTHENTICATED_USER, HttpStatus.UNAUTHORIZED);
		}
		return "account/deposit";
	}

	// 입금 처리 기능 만들기
	@PostMapping("/deposit")
	public String depositProc(DepositDTO dto) {
		// 1. 인증검사
		User principal = (User) session.getAttribute(Define.PRINCIPAL);
		if (principal == null) {
			throw new UnAuthorizedException(Define.NOT_AN_AUTHENTICATED_USER, HttpStatus.UNAUTHORIZED);
		}
		// 2. 유효성 검사
		if (dto.getAmount() == null) {
			throw new DataDeliveryException(Define.ENTER_YOUR_BALANCE, HttpStatus.BAD_REQUEST);
		}
		if (dto.getAmount().longValue() <= 0) {
			throw new DataDeliveryException(Define.D_BALANCE_VALUE, HttpStatus.BAD_REQUEST);
		}
		if (dto.getDAccountNumber() == null || dto.getDAccountNumber().trim().isEmpty()) {
			throw new DataDeliveryException(Define.ENTER_YOUR_ACCOUNT_NUMBER, HttpStatus.BAD_REQUEST);
		}

		accountService.updateAccountDeposit(dto, principal.getId());

		return "redirect:/account/list";
	}

	/**
	 * 이체 페이지 요청
	 */
	@GetMapping("/transfer")
	public String transferPage() {
		// 1. 인증검사
		User principal = (User) session.getAttribute(Define.PRINCIPAL);
		if (principal == null) {
			throw new UnAuthorizedException(Define.NOT_AN_AUTHENTICATED_USER, HttpStatus.UNAUTHORIZED);
		}
		return "account/transfer";
	}
	
	@PostMapping("/transfer")
	public String transferProc(TransferDTO dto) {
		// 1. 인증검사
		User principal = (User) session.getAttribute(Define.PRINCIPAL);
		if (principal == null) {
			throw new UnAuthorizedException(Define.NOT_AN_AUTHENTICATED_USER, HttpStatus.UNAUTHORIZED);
		}
		// 2. 유효성 검사
		if (dto.getAmount() == null) {
			throw new DataDeliveryException(Define.ENTER_YOUR_BALANCE, HttpStatus.BAD_REQUEST);
		}
		if (dto.getAmount().longValue() <= 0) {
			throw new DataDeliveryException(Define.D_BALANCE_VALUE, HttpStatus.BAD_REQUEST);
		}
		if (dto.getDAccountNumber() == null || dto.getDAccountNumber().trim().isEmpty()) {
			throw new DataDeliveryException(Define.ENTER_YOUR_ACCOUNT_NUMBER, HttpStatus.BAD_REQUEST);
		}
		if (dto.getWAccountNumber() == null || dto.getWAccountNumber().trim().isEmpty()) {
			throw new DataDeliveryException(Define.ENTER_YOUR_ACCOUNT_NUMBER, HttpStatus.BAD_REQUEST);
		}
		if (dto.getPassword() == null || dto.getPassword().trim().isEmpty()) {
			throw new DataDeliveryException(Define.ENTER_YOUR_PASSWORD, HttpStatus.BAD_REQUEST);
		}
		
		accountService.updateAccountTransfer(dto, principal.getId());

		return "redirect:/account/list";
	}
	
	/**
	 * 계좌 상세 보기 페이지
	 * 주소 설계 : localhost:8080/account/detail/1?type=all, deposit, withdraw
	 * @return
	 */
	@GetMapping("/detail/{accountId}")
	public String detail(@PathVariable(name = "accountId") Integer accountId, @RequestParam(required = false, name = "type") String type, Model model) {
		// required = false 일 경우 쿼리스트링 없어도 null 로 반환된다.  true 일 경우 쿼리스트링 없으면 오류 뜬다.
		
		// 1. 인증검사
		User principal = (User) session.getAttribute(Define.PRINCIPAL);
		if (principal == null) {
			throw new UnAuthorizedException(Define.NOT_AN_AUTHENTICATED_USER, HttpStatus.UNAUTHORIZED);
		}
		
		// 2. 유효성 검사
		List<String> validTypes = Arrays.asList("all","deposit","withdrawal");
		
		// 여기에 포함되어있는 녀석이 아니라면
		if(!validTypes.contains(type)) {
			throw new DataDeliveryException("유효하지 않은 접근입니다", HttpStatus.BAD_REQUEST);
		}
		
		Account account = accountService.readAccountById(accountId);
		List<HistoryAccount> historyList = accountService.readHistoryByAccountID(type, accountId);
		
		System.out.println("@PathVariable : " + accountId);
		System.out.println("@RequestParam : " + type);
		
		model.addAttribute("account", account);
		model.addAttribute("historyList", historyList);
		return "account/detail";
	}
}
