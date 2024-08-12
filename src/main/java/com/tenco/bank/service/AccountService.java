package com.tenco.bank.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tenco.bank.dto.DepositDTO;
import com.tenco.bank.dto.SaveDTO;
import com.tenco.bank.dto.TransferDTO;
import com.tenco.bank.dto.WithdrawalDTO;
import com.tenco.bank.handler.exception.DataDeliveryException;
import com.tenco.bank.handler.exception.RedirectException;
import com.tenco.bank.repository.interfaces.AccountRepository;
import com.tenco.bank.repository.interfaces.HistoryRepository;
import com.tenco.bank.repository.model.Account;
import com.tenco.bank.repository.model.History;
import com.tenco.bank.repository.model.HistoryAccount;
import com.tenco.bank.utils.Define;

@Service
public class AccountService {

	// 유연하게 개발이 가능하다
	private final AccountRepository accountRepository;
	private final HistoryRepository historyRepository;

	public AccountService(AccountRepository accountRepository, HistoryRepository historyRepository) {
		this.accountRepository = accountRepository;
		this.historyRepository = historyRepository;
	}

	/**
	 * 계좌 생성기능
	 * 
	 * @param dto
	 * @param id
	 */

	@Transactional
	public void createAccount(SaveDTO dto, Integer principalId) {

		int result = 0;

		try {
			result = accountRepository.insert(dto.toAccount(principalId));
		} catch (DataAccessException e) {
			throw new DataDeliveryException(Define.FAILED_PROCESSING, HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (Exception e) {
			throw new RedirectException(Define.UNKNOWN, HttpStatus.SERVICE_UNAVAILABLE);
		}

		if (result == 0) {
			throw new DataDeliveryException(Define.FAILED_PROCESSING, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	// select에 트랜잭션쓰는이유 정합성때문에
	// 근데 안쓸거에요

	public List<Account> readAccountListByUserId(Integer userId) {
		List<Account> accountListEntity = null;

		try {
			accountListEntity = accountRepository.findByUserId(userId);
		} catch (DataAccessException e) {
			throw new DataDeliveryException(Define.FAILED_PROCESSING, HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (Exception e) {
			throw new RedirectException(Define.UNKNOWN, HttpStatus.SERVICE_UNAVAILABLE);
		}

		return accountListEntity;
	}

	// 한번에 모든 기능을 생각하는건 힘듬
	// 1. 계좌 존재 여부를 확인 -- select
	// 2. 본인 계좌 여부를 확인 -- 객체 상태값에서 비교
	// 3. 계좌 비밀번호 확인 -- 객체 상태값에서 일치 여부 확인
	// 4. 잔액 여부 확인 -- 객체 상태값에서 확인
	// 5. 출금 처리 -- update
	// 6. 거래 내역 등록 -- insert(history)
	// 7. 트랜잭션 처리

	@Transactional
	public void updateAccountWithdraw(WithdrawalDTO dto, Integer principalId) {
		// 1. 계좌 존재 여부를 확인
		Account accountEntity = accountRepository.findByNumber(dto.getWAccountNumber());
		if (accountEntity == null) {
			throw new DataDeliveryException(Define.NOT_EXIST_ACCOUNT, HttpStatus.BAD_REQUEST);
		}
		// 2. 본인 계좌 여부를 확인
		accountEntity.checkOwner(principalId);

		// 3. 계좌 비번 확인
		accountEntity.checkPassword(dto.getWAccountPassword());

		// 4. 잔액 여부 확인
		accountEntity.checkBalance(dto.getAmount());

		// 5. 출금 처리
		// accountEntity 객체의 잔액을 변경하고 업데이트 처리해야 한다.
		accountEntity.withdraw(dto.getAmount());
		// update 처리
		accountRepository.updateById(accountEntity);

		// 6. 거래 내역 등록
		History history = new History();
		history.setAmount(dto.getAmount());
		history.setWBalance(accountEntity.getBalance());
		history.setDBalance(null);
		history.setWAccountId(accountEntity.getId());
		history.setDAccountId(null);

		int rowResultCount = historyRepository.insert(history);
		if (rowResultCount != 1) {
			throw new DataDeliveryException(Define.FAILED_PROCESSING, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Transactional
	public void updateAccountDeposit(DepositDTO dto, Integer principalId) {
		// 입금 기능만들기
		Account accountEntity = accountRepository.findByNumber(dto.getDAccountNumber());
		// 1. 계좌 존재여부 확인
		if (accountEntity == null) {
			throw new DataDeliveryException(Define.NOT_EXIST_ACCOUNT, HttpStatus.BAD_REQUEST);
		}
		// 2. 본인 계좌 여부 확인
		accountEntity.checkOwner(principalId);

		// 3. 입금처리
		accountEntity.deposit(dto.getAmount());
		accountRepository.updateById(accountEntity);


		// 4. 거래내역 등록
		History history = new History();
		history.setAmount(dto.getAmount());
		history.setWBalance(null);
		history.setDBalance(accountEntity.getBalance());
		history.setWAccountId(null);
		history.setDAccountId(accountEntity.getId());

		int rowResultCount = historyRepository.insert(history);
		if (rowResultCount != 1) {
			throw new DataDeliveryException(Define.FAILED_PROCESSING, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	// 이체 기능 만들기
	@Transactional
	public void updateAccountTransfer(TransferDTO dto,Integer principalId) {
		Account wAccountEntity = accountRepository.findByNumber(dto.getWAccountNumber());
		Account dAccountEntity = accountRepository.findByNumber(dto.getDAccountNumber());
		// 1. 출금 계좌 존재 여부 확인 -- select (객체 리턴 받은 상태)
		if (wAccountEntity == null) {
			throw new DataDeliveryException(Define.NOT_EXIST_ACCOUNT, HttpStatus.BAD_REQUEST);
		}
		// 2. 입금 계좌 존재 여부 확인 -- select (객체 리턴 받은 상태)
		if (dAccountEntity == null) {
			throw new DataDeliveryException(Define.NOT_EXIST_ACCOUNT, HttpStatus.BAD_REQUEST);
		}
		// 3. 출금 계좌 본인 소유 확인 -- 객체 상태값과 세션 아이디 비교
		wAccountEntity.checkOwner(principalId);
		
		// 4. 출금 계좌 비밀 번호 확인 -- 객체 상태값과 dto 비밀번호 비교
		wAccountEntity.checkPassword(dto.getPassword());
		
		// 5. 출금 계좌 잔액 여부 확인 -- 객체 상태값 확인, dto와 비교
		wAccountEntity.checkBalance(dto.getAmount());
		
		// 6. 입금 계좌 객체 상태값 변경 처리 (거래금액 증가)
		dAccountEntity.deposit(dto.getAmount());
		
		// 7. 입금 계좌 -- update 처리
		accountRepository.updateById(dAccountEntity);
		
		// 8. 출금 계좌 객체 상태값 변경 처리 (잔액 - 거래금액)
		wAccountEntity.withdraw(dto.getAmount());
		
		// 9. 출금 계좌 -- update 처리
		accountRepository.updateById(wAccountEntity);
		
		// 10. 거래 내역 등록 처리
		History history = new History();
		history.setAmount(dto.getAmount());
		history.setWBalance(wAccountEntity.getBalance());
		history.setDBalance(dAccountEntity.getBalance());
		history.setWAccountId(wAccountEntity.getId());
		history.setDAccountId(dAccountEntity.getId());

		int rowResultCount = historyRepository.insert(history);
		if (rowResultCount != 1) {
			throw new DataDeliveryException(Define.FAILED_PROCESSING, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		// 11. 트랜잭션 처리                     
		
	}
	
	/**
	 * 단일 계좌 조회 기능
	 * @param accountId (pk)
	 * @return
	 */
	public Account readAccountById(Integer accountId) {
		Account accountEntity = accountRepository.finndByAccountId(accountId);
		if(accountEntity == null) {
			throw new DataDeliveryException(Define.NOT_EXIST_ACCOUNT, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return accountEntity;
	}
	
	/**
	 * 단일 계좌 거래 내역 조회
	 * @param type = [all, deposit, withdrawal]
	 * @param accountId (pk)
	 * @return 전체, 입금, 출금 거래내역 (3가지 타입) 반환
	 */
	// @Transactional
	public List<HistoryAccount> readHistoryByAccountID(String type, Integer accountId) {
		List<HistoryAccount> list = new ArrayList<>();
		list = historyRepository.findByAccountIdAndTypeOfHistory(type, accountId);
		return list;
		
	}
}
