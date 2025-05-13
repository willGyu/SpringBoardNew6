package com.itwillbs.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.itwillbs.domain.BoardVO;
import com.itwillbs.domain.Criteria;
import com.itwillbs.domain.PageVO;
import com.itwillbs.service.BoardService;

import net.coobird.thumbnailator.Thumbnails;

@Controller
@RequestMapping(value = "/board/*")
public class BoardController {

	private static final Logger logger = LoggerFactory.getLogger(BoardController.class);
	
	//BoardService 객체 주입
	@Inject
	private BoardService bService;
	
	// http://localhost:8088/controller/time (x)
	// http://localhost:8088/controller/board/time (x) 
	// http://localhost:8088/board/time 
	@RequestMapping(value = "/time",method = RequestMethod.GET)
	public void getServerTimeGET(Model model) {
		logger.info(" DB서버의 시간정보를 가져오기 ");
		
		String time = bService.getServerTime();
		logger.info(" time : {}",time);
		
		model.addAttribute("time", time);
		logger.info(" 연결된 뷰페이지에 정보 전달 ");
		
	}
	
	// http://localhost:8088/board/regist
	// 게시판 글쓰기 - GET
	@RequestMapping(value = "/regist",method = RequestMethod.GET)
	public void registGET() throws Exception {
		logger.info(" registGET() 실행 ");
		logger.info(" 글쓰기 뷰페이지(/board/regist.jsp)를 연결해서 보여줌 ");		
	}
	
	// 게시판 글쓰기 - POST
	@RequestMapping(value = "/regist",method = RequestMethod.POST)
	public String registPOST(BoardVO vo,RedirectAttributes rttr) throws Exception{
		logger.info(" registPOST() 실행! ");
		// 인코딩 데이터 처리 => web.xml 처리
		
		// 폼태그에서 전달된 파라메터를 저장 & 출력
		logger.info("vo : {}",vo);
		
		// 서비스 호출 - 게시판 글쓰기 동작을 처리(DAO호출)
		bService.registBoard(vo);
		
		// 정보를 하나 전달(createOK)
		rttr.addFlashAttribute("result", "createOK");
		// => 1회성 데이터 전달
		
		// 게시판 글목록 페이지로 이동
		//return "/board/listAll";(x)
		//return "redirect:/board/listAll";
		return "redirect:/board/listPage";
	}
	
	// 게시판 목록 - GET
	@RequestMapping(value = "/listAll",method = RequestMethod.GET)
	// @GetMapping(value = "/listAll")
	public void listAllGET(HttpSession session
			 			  ,Model model
			 			  ,@ModelAttribute("result") String result) throws Exception{
		logger.info(" listAllGET() 실행 ");
		// 전달정보 result 저장
		logger.info(" result : "+result);
		
		// 기존의 DB데이터를 가져와서 화면(view)에 출력
		// => 서비스 -> DAO 호출
		List<BoardVO> boardList = bService.getBoardListAll();
		
		logger.info(" boardList : {} 개",boardList.size());
		
		// => 생성된 데이터를 뷰페이지에 전달(Model)
		model.addAttribute("boardList", boardList);
		
		// session 영역에 정보를 저장 & 전달
		session.setAttribute("updateCheck", true);

		// 임시로 로그인 대신하는 정보
		session.setAttribute("id", "ok");
		
		// 연결된 뷰페이지로 이동(/board/listAll.jsp)
	}
	// 게시판 목록 - GET
	@RequestMapping(value = "/listPage",method = RequestMethod.GET)
	public String listPageGET(Criteria cri
			,HttpSession session
			,Model model
			,@ModelAttribute("result") String result) throws Exception{
		logger.info(" listPageGET() 실행 ");
		// 전달정보 result 저장
		logger.info(" result : "+result);
		
		// 기존의 DB데이터를 가져와서 화면(view)에 출력
		// => 서비스 -> DAO 호출
//		Criteria cri = new Criteria();
//		cri.setPage(1);
//		cri.setPageSize(10);
		
		List<BoardVO> boardList 
		         = bService.getBoardListPage(cri);
		
		logger.info(" boardList : {} 개",boardList.size());
		
		// 페이징처리에 필요한 정보 
		PageVO pageVO = new PageVO();
		pageVO.setCri(cri);
		//pageVO.setTotalCount(89);
		pageVO.setTotalCount(bService.getTotalCount());
		
		// => 생성된 데이터를 뷰페이지에 전달(Model)
		model.addAttribute("boardList", boardList);
		model.addAttribute("pageVO", pageVO);
		
		
		// session 영역에 정보를 저장 & 전달
		session.setAttribute("updateCheck", true);
		
		// 임시로 로그인 대신하는 정보
		session.setAttribute("id", "ok");
		
		// 연결된 뷰페이지로 이동(/board/listAll.jsp)
		return "/board/listAll";
	}
	
	// 게시판 본문보기GET   /board/read
	@RequestMapping(value = "/read",method = RequestMethod.GET)
	public String readGET(HttpSession session,Model model
			,@RequestParam("bno") int bno) throws Exception{
		
		logger.info(" readGET() 실행 ");
		// 서비스 -> DAO -> mapper 호출
		// 전달정보(bno)를 저장
		logger.info(" bno : {}",bno);
		
		// 리스트 -> 본문 이동시마다 조회수가 증가
		// (본문에서 새로고침 수행시 조회수증가 X)
		boolean updateCheck
		    = (boolean)session.getAttribute("updateCheck");
		
		if(updateCheck) {
			// 서비스 -> 글 조회수를 1씩 증가 동작
			bService.increaseViewCnt(bno);
			session.setAttribute("updateCheck", false);
		}
		
		
		// 서비스 -> 글 하나의 정보를 조회하는 동작 호출
		BoardVO vo = bService.getBoard(bno);
		logger.info(" vo : {}",vo);
		
		// DAO에서 받아온 글정보를 연결된 뷰페이지(/board/read.jsp)로 이동
		model.addAttribute(vo);
		//model.addAttribute(bService.getBoard(bno));
		
		return "/board/read";
	}
	
	// 글정보 수정하기-GET
	@RequestMapping(value = "/modify",method = RequestMethod.GET)
	public void modifyGET(Model model,@ModelAttribute("bno") int bno) throws Exception{
		logger.info(" modifyGET() 실행 ");
		
		// 전달정보 저장 (파라메터)
		logger.info(" bno : {}",bno);
		// bno를 사용해서 정보를 DB에서 가져오기
		BoardVO vo = bService.getBoard(bno);
		model.addAttribute(vo);
		// 연결된 뷰페이지에 출력
		
	}
	
	// 글정보 수정하기-POST
	@RequestMapping(value = "/modify",method = RequestMethod.POST)
	public String modifyPOST(Criteria cri,RedirectAttributes rttr,
			 /*@ModelAttribute*/ BoardVO uvo) throws Exception {
		logger.info(" modifyPOST() 실행 ");
		// 한글처리 인코딩(생략- 필터처리)
		// 전달된 정보(수정할 내용-파라메터)를 저장 => 파라메터 자동수집
		
		logger.info(" uvo : "+uvo);
		
		// 서비스 - 사용자 게시판 글 수정하는 메서드 호출
		bService.modifyBoard(uvo);
		logger.info(" 게시판 수정 성공! ");
		
		// 리스트 페이지로 이동 + "수정 완료" alert 출력
		rttr.addFlashAttribute("result","modifyOK");
		
		
		//return "redirect:/board/listAll";
		return "redirect:/board/listPage?page="+cri.getPage();
	}
	
	// 게시판 글 삭제
	@RequestMapping(value = "/remove",method = RequestMethod.POST)
	public String removePOST(Criteria cri, RedirectAttributes rttr,
			                BoardVO dvo) throws Exception {
		logger.info(" removePOST() 호출 ");
		
		// 전달된 정보(bno) 저장
		logger.info(" dvo : "+dvo);
		
		// 서비스 - 특정 글정보를 삭제기능
		int result = bService.removeBoard(dvo);
		
		if(result == 0) {
			rttr.addFlashAttribute("result", "deleteErr");
			// 삭제 실패
//			return "redirect:/board/read?bno="+dvo.getBno();
			//return "redirect:/board/listAll";
			return "redirect:/board/listPage?page="+cri.getPage();
		}
		
		// 삭제 성공
		rttr.addFlashAttribute("result", "deleteOK");
		//return "redirect:/board/listAll";
		return "redirect:/board/listPage?page="+cri.getPage();
	}
	
	
	// 파일업로드 뷰페이지 GET
	@RequestMapping(value = "/upload",method = RequestMethod.GET)
	public void fileUploadGET() throws Exception{
		logger.info(" fileUploadGET() 실행 ");
	}
	
	// 파일업로드 처리 POST
	@RequestMapping(value = "/upload",method = RequestMethod.POST)
	public String fileUploadPOST(MultipartHttpServletRequest multiRequest,
			                     Model model) throws Exception{
		logger.info(" fileUploadPOST() ");
		
		//  파라메터 데이터
		//logger.info(" title : "+multiRequest.getParameter("title"));
		Map map = new HashMap();
		
		Enumeration enu = multiRequest.getParameterNames();
		while(enu.hasMoreElements()) { // 데이터가 있을때 실행
			String name = (String) enu.nextElement();
			logger.info(" name : "+name); // 파라메터 이름 출력
			String value = multiRequest.getParameter(name);
			logger.info(" value : "+value); // 파라메터 값 출력
			
			map.put(name, value);
		}
		
		logger.info(" 폼태그로 전달된 파라메터정보 저장완료! (파일정보를 제외)");
		logger.info(" map : "+map);
		
		//  파일업로드 데이터
		List fileList = fileProcess(multiRequest);
		
		// 기존의 파라메터 정보를 저장한 map에 
		// 파일의 이름정보도 추가로 저장
		
		map.put("fileList", fileList);
		logger.info(" map : "+map);
		
		model.addAttribute("map", map);
		
		return "/board/fileUploadResult";
	}
	
	
	// 파일업로드를 처리하는 메서드
	private List fileProcess(MultipartHttpServletRequest multiRequest) {
		
		final String FAKE_PATH = "/upload";
		
		// 전송된 파일정보(이름)를 저장
		List<String> fileList = new ArrayList<String>();
		
		// 파일정보(파라메터) 받아오기
		Iterator<String> fileNames =  multiRequest.getFileNames();
		while(fileNames.hasNext()) {//데이터 있을때 처리
			
			String fileName = fileNames.next();
			// => 폼태그의 input -file 이름 정보
			MultipartFile mFile = multiRequest.getFile(fileName);
			// => 임시로 전달받은 파일정보를 저장
			
			// 파일명 구하기
			String oFileName = mFile.getOriginalFilename();
			// 파일의 이름을 리스트에 저장
			fileList.add(oFileName);
			
			// 파일정보 업로드
			// getRealPath()를 통해서 서버의 주소(위치)를 찾는 작업
			//   ~~~~/upload\파일명
			// D:\Shared\workspace_sts\.metadata\.plugins
			//   \org.eclipse.wst.server.core\tmp0\wtpwebapps
			//   \SpringBoardNew6
//			File file = new File(multiRequest.getRealPath(FAKE_PATH)+"\\"+fileName);
			File file 
			= new File(multiRequest.getRealPath(FAKE_PATH)+"\\"+oFileName);
			
			if(mFile.getSize() != 0) { // 파일의 업로드 정보가 있다.
				if(! file.exists()) {
					// file 객체 경로에 정보가 없을경우
					if(file.getParentFile().mkdirs()) {
						try {
							file.createNewFile();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}// mkdirs
				}//exists
				try {
					// 해당 경로의 정보가 있을때
					// 임시 파일의 정보를 해당 파일로 이동
					mFile.transferTo(file);	
					System.out.println(" 파일업로드 성공! ");
				} catch (IllegalStateException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				
			}//size
			
		} //while
		return fileList;
	}
	
	
	// 파일 다운로드 동작
	@RequestMapping(value = "/download",method = RequestMethod.GET)
	public void fileDownloadGET(@RequestParam("fileName") String fileName,
			                    HttpServletRequest request,
			                    HttpServletResponse response
			                    ) throws Exception{
		logger.info(" fileDownloadGET() ");
		
		// 다운로드 하려는 폴더 == 업로드한 폴더
		final String FAKE_PATH = "/upload";
		String downFile = request.getRealPath(FAKE_PATH) + "\\" + fileName ;
		
		// 다운로드할 파일 생성
		File file = new File(downFile);
		String encodedFileName = URLEncoder.encode(fileName, "UTF-8");
		
		/*********************************************************/		
		// 파일 썸네일만들기
		//  ex) 파일테스트.png
		int lastIndex = encodedFileName.lastIndexOf(".");
		String thumbFileName = encodedFileName.substring(0,lastIndex);
		
		File thumbNail = new File(request.getRealPath(FAKE_PATH)+"\\"+"thumbnail"+"\\"+thumbFileName+".png");
		if(file.exists()) {
			thumbNail.getParentFile().mkdirs();
			Thumbnails.of(file).size(50, 50).outputFormat("png").toFile(thumbNail);
			logger.info(" 썸네일 생성 완료! ");
		}
		
		/*********************************************************/		
		
		// 다운로드 정보를 출력할 객체
		OutputStream out = response.getOutputStream();
		response.setHeader("Cache-Control", "no-cache");
		//response.addHeader("Content-disposition", "attachment; fileName="+fileName);
		response.addHeader("Content-disposition", "attachment; fileName="+encodedFileName);
		//=> 모든 파일들이 다운로드 형태로 처리 
		
		// 파일정보를 읽어오기
		FileInputStream fis = new FileInputStream(file);
		
		byte[] buffer = new byte[1024 * 8]; // 8KB 
		
		while(true) {
			int data = fis.read(buffer);
			if(data == -1) break; // -1 (EOF, 파일의 끝)
			
			//파일 출력(다운로드)
			out.write(buffer,0,data);
		}
		
		fis.close();
		out.close();
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
	}
	
	// 파일 다운로드 동작
	@RequestMapping(value = "/thumbnail",method = RequestMethod.GET)
	public void thumbNailDownloadGET(@RequestParam("fileName") String fileName,
			                    HttpServletRequest request,
			                    HttpServletResponse response
			                    ) throws Exception{
		logger.info(" thumbNailDownloadGET() ");
		
		// 다운로드 하려는 폴더 == 업로드한 폴더
		final String FAKE_PATH = "/upload";
		String downFile = request.getRealPath(FAKE_PATH) + "\\" + fileName ;
		
		// 다운로드할 파일 생성
		File file = new File(downFile);
		String encodedFileName = URLEncoder.encode(fileName, "UTF-8");
		
		// 다운로드 정보를 출력할 객체
		OutputStream out = response.getOutputStream(); 
		
		/*********************************************************/		
		// 파일 썸네일만들기
		//  ex) 파일테스트.png
		int lastIndex = encodedFileName.lastIndexOf(".");
		String thumbFileName = encodedFileName.substring(0,lastIndex);
		
		File thumbNail = new File(request.getRealPath(FAKE_PATH)+"\\"+"thumbnail"+"\\"+thumbFileName+".png");
		if(file.exists()) {
			//thumbNail.getParentFile().mkdirs();
			Thumbnails.of(file).size(50, 50).outputFormat("png").toOutputStream(out);
			logger.info(" 썸네일 생성 완료! ");
		}
		
		/*********************************************************/		
		
		out.close();
	}
	
	
	
	
	
}// BoardController
