package com.itwillbs.domain;

/**
 *  페이징 처리에 필요한 정보 저장객체
 *  page(시작인덱스),size(크기) 
 *   
 */
public class Criteria {
	
	private int page;		// 페이지 정보 (몇페이지인가?)
	private int pageSize;	// 페이지 크기 (한페이지에 몇개씩 출력)
	
	public Criteria() {
		// 페이징처리 객체 기본생성자
		this.page = 1;
		this.pageSize = 10;
	}
	
	
	// alt shift s + r   get/set 메서드
	public void setPage(int page) {
		if(page <= 0) {
			this.page = 1;
			return;
		}
		this.page = page;
	}
	public void setPageSize(int pageSize) {
		if(pageSize <= 0 || pageSize > 100) {
			this.pageSize = 10;
			return;
		}
		this.pageSize = pageSize;
	}
	// 객체(VO)의 get메서드는 mapper에 # {이름 }코드 연결
	public int getPage() {
		return page;
	}
	public int getPageSize() {
		return pageSize;
	}
	
	// 변수와 상관없이 
	// mapper에 값을 전달하기위한 메서드
	// => 메서드 이름에서 get 뺀 나머지 이름으로 mapper 호출
	//                     (첫글자를 소문자)
	public int getStartPage() {
		// page번호를 조회하는 인덱스 번호로 변경 계산
		return (this.page - 1) * pageSize;
	}
	
	

	// alt shift s + s
	@Override
	public String toString() { 
		return "Criteria [page=" + page + ", pageSize=" + pageSize + "]";
	}
	

}
