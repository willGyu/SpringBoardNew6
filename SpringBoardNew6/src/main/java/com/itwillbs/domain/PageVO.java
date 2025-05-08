package com.itwillbs.domain;

/* 페이징처리 위한 객체 */
// 페이징블럭 위한 정보를 저장

/**
 *	시작페이지 번호(startPage)
 *	startPage = (endPage - pageBlock) + 1 
 *  끝 페이지 번호(endPage)
 *  endPage = (int)( Meth.ceil(page / (double)pageBlock) ) * pageBlock;
 *  전체 데이터(글) 개수(totalCount)
 *  => DB조회 & endPage 다시 계산하기
 *  
 *  tmpEndPage = (int) Math.ceil(totalCount / (double)pageSize)
 *   => 끝페이지와 비교해서 변경
 *    
 *  
 *  이전 페이지링크(prev)
 *  prev = startPage == 1? false : true;
 *  prev = startPage != 1;
 *  
 *  다음 페이지링크(next)
 *  next = endPage * pageSize >= totalCount? false : true;
 *  next = endPage * pageSize < totalCount;
 * 
 */

/*
	한페이지에 10개씩 출력
	글이 총 122개 => 13페이지 필요 
	페이지 블럭의 크기 10개 ( 1,2,3,4,.....,10 )
							( 11,12,13,14,.....,20 )
							
	- 3 페이지
	  startPage 1, endPage 10, next O, prev X
	  
	- 10 페이지  
	  startPage 1 , endPage 10, next O, prev X
	  
	- 11 페이지 
	  startPage 11 , endPage 20->13, next X, prev O 
	
 * */

public class PageVO {
	
	private int totalCount;			// 총 글의 개수
	private int startPage;			// (페이지블럭)시작 페이지 번호
	private int endPage;			// (페이지블럭)끝 페이지 번호
	private boolean prev;			// 이번버튼
	private boolean next;			// 다음버튼
	
	private int pageBlock = 2;		// 페이지 블럭의 개수
	
//	private int page;				// 페이지정보
//	private int pageSize;			// 한 페이지에 출력하는 개수
	private Criteria cri;

	
	// alt shift s + r
	public void setCri(Criteria cri) {
		this.cri = cri;
	}
	public void setTotalCount(int totalCount) {
		this.totalCount = totalCount;
		calcData();
	}
	
	// 내가만든 페이징처리 계산함수
	private void calcData() {
		System.out.println(" 페이징처리 계산 시작 ");
		
		// endPage = (int)( Math.ceil(page / (double)pageBlock) ) * pageBlock;
		endPage = (int)( Math.ceil(cri.getPage() / (double)pageBlock) ) * pageBlock;
		
		// startPage = (endPage - pageBlock) + 1 
		startPage = (endPage - pageBlock) + 1 ;
		
		//endPage 다시 계산하기
		//tmpEndPage = (int) Math.ceil(totalCount / (double)pageSize)
		int tmpEndPage = (int) Math.ceil(totalCount / (double)cri.getPageSize());
		
		if(endPage > tmpEndPage) {
			// endPage값이 내가 가진 페이지수보다 클때
			endPage = tmpEndPage;
		}
		
		
		// prev = startPage != 1;
		prev = startPage != 1;
		
		// next = endPage * pageSize < totalCount;
		next = endPage * cri.getPageSize() < totalCount;
		
		System.out.println(" 페이징처리 계산 끝 ");
	}
	
	
	
	
	
	public int getTotalCount() {
		return totalCount;
	}



	public int getStartPage() {
		return startPage;
	}

	public void setStartPage(int startPage) {
		this.startPage = startPage;
	}

	public int getEndPage() {
		return endPage;
	}

	public void setEndPage(int endPage) {
		this.endPage = endPage;
	}

	public boolean isPrev() {
		return prev;
	}

	public void setPrev(boolean prev) {
		this.prev = prev;
	}

	public boolean isNext() {
		return next;
	}

	public void setNext(boolean next) {
		this.next = next;
	}

	public int getPageBlock() {
		return pageBlock;
	}

	public void setPageBlock(int pageBlock) {
		this.pageBlock = pageBlock;
	}

	public Criteria getCri() {
		return cri;
	}

	// alt shift s + s
	@Override
	public String toString() {
		return "PageVO [totalCount=" + totalCount + ", startPage=" + startPage + ", endPage=" + endPage + ", prev="
				+ prev + ", next=" + next + ", pageBlock=" + pageBlock + ", cri=" + cri + "]";
	}
	
	
}
