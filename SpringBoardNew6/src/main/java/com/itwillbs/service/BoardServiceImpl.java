package com.itwillbs.service;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import com.itwillbs.persistence.BoardDAO;

/**
 *	실제 서비스동작이 수행되는 객체 
 */
@Service
public class BoardServiceImpl implements BoardService{

	// BoardDAO 객체 주입
	@Inject
	private BoardDAO bDao;
	
	@Override
	public String getServerTime() {
		
		return bDao.getServerTime();
	}

}
