package com.itwillbs.service;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.itwillbs.domain.BoardVO;
import com.itwillbs.persistence.BoardDAO;

/**
 *	실제 서비스동작이 수행되는 객체 
 */
@Service
public class BoardServiceImpl implements BoardService{
	//mylog
	private static final Logger logger 
	   = LoggerFactory.getLogger(BoardServiceImpl.class);
	// BoardDAO 객체 주입
	@Inject
	private BoardDAO bDao;
	
	@Override
	public String getServerTime() {
		
		return bDao.getServerTime();
	}

	@Override
	public void registBoard(BoardVO vo) throws Exception {
		logger.info("게시판 글쓰기 - registBoard(BoardVO vo) 실행");
		logger.info(" 전달받은 정보를 DAO로 전달 + DAO동작을 처리 ");
		
		bDao.insertBoard(vo);
		
		logger.info(" DAO 실행완료 -> 컨트롤러로 이동");
		
	}
	
	
	
	
	
	
	
	
	
	

}
