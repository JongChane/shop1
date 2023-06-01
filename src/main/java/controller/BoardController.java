package controller;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.apache.commons.beanutils.MappedPropertyDescriptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import exception.BoardException;
import exception.LoginException;
import logic.Board;
import logic.ShopService;

@Controller
@RequestMapping("board")
public class BoardController {
	@Autowired
	private ShopService service;
	
	@GetMapping("*") //설정되지 않은 모든 요청시 호출되는 메서드
	public ModelAndView write() {
		ModelAndView mav = new ModelAndView();
		mav.addObject(new Board());
		return mav;
	}
	/*
	 * 1. 유효성 검증
	 * 2. db의 board 테이블에 내용 저장, 파일 업로드
	 * 3. 등록 성공 : list 리다이렉트
	 * 	  등록 실패 : write 리다이렉트
	 */
	@PostMapping("write")
	public ModelAndView writePost(@Valid Board board,BindingResult bresult, HttpServletRequest request) {
		ModelAndView mav = new ModelAndView();
		if(bresult.hasErrors()) {
			mav.getModel().putAll(bresult.getModel());
			return mav;
		}
		String boardid = (String) request.getSession().getAttribute("boardid");
		if(boardid == null) boardid="1";
		request.getSession().setAttribute("boardid", boardid);
		board.setBoardid(boardid);
		service.boardWrite(board,request);
		mav.setViewName("redirect:list?boardid="+boardid);
		return mav;
	}
	/*
	 * @RequestParam : 파라미터 값을 매핑하여 저장하는 기능
	 * 		파라미터 값 저장 방식
	 * 		1. 파라미터 명과 매개변수 명이 같은 경우
	 * 		2. Bean 클래스 객체의 프로퍼티 명과 파라미터 명이 같은 경우
	 * 		3. Map 객체를 이용하는 경우 ==> list의 경우 이방식 사용
	 * 
	 * 검색 기능 추가
	 * 		
	 */
	@RequestMapping("list")
	public ModelAndView list(@RequestParam Map<String, String> param, HttpSession session) {
		Integer pageNum = null;
		if (param.get("pageNum") != null)
			pageNum = Integer.parseInt(param.get("pageNum"));
		String boardid = param.get("boardid");
		String searchtype = param.get("searchtype");
		String searchcontent = param.get("searchcontent");
		if(searchtype == null || searchtype.trim().equals("")) {
			searchtype = null;
	    	searchcontent = null;
	    }
	    if(searchcontent == null || searchcontent.trim().equals("")) {
	    	searchtype = null;
	    	searchcontent = null;
	    }
		ModelAndView mav = new ModelAndView();
		if(pageNum == null || pageNum.toString().equals("")) {
			pageNum = 1;
		}
		if(boardid == null || boardid.equals("")) {
			boardid = "1";
		}
		session.setAttribute("boardid", boardid);
		String boardName=null;
		switch(boardid) {
			case "1" : boardName="공지사항"; break;
			case "2" : boardName="자유게시판"; break;
			case "3" : boardName="QnA"; break;
		}
		int limit = 10; //한 페이지당 보여줄 게시물 건수
		int listcount = service.boardcount(boardid,searchtype,searchcontent); //등록 게시물 건수
		//boardlist : 현재 페이지에 보여줄 게시물 목록
		List<Board> boardlist = service.boardlist(pageNum,limit,boardid,searchtype,searchcontent);
		//페이징 처리를 위한 값
		int maxpage = (int)((double)listcount/limit + 0.95); //등록 건수에 따른 최대 페이지
		int startpage = (int)((pageNum/10.0 + 0.9) - 1) * 10 + 1; //페이지의 시작 번호
		int endpage = startpage + 9;							  //페이지의 끝 번호		
		if(endpage > maxpage) endpage = maxpage;				  //페이지의 끝 번호는 최대 페이지를 넘어갈 수 없다.
		int boardno = listcount - (pageNum-1) * limit;			  //화면에 보여지는 게시물 번호
		//20230530
		String today = new SimpleDateFormat("yyyyMMdd").format(new Date()); //오늘 날짜를 문자열로 저장
		mav.addObject("boardid",boardid);
		mav.addObject("boardName",boardName);
		mav.addObject("pageNum",pageNum);
		mav.addObject("maxpage",maxpage);
		mav.addObject("startpage",startpage);
		mav.addObject("endpage",endpage);
		mav.addObject("listcount",listcount);
		mav.addObject("boardlist",boardlist);
		mav.addObject("boardno",boardno);
		mav.addObject("today",today);
		return mav;
	}
	/*    
	   Controller,Shopservice, BoardDao 구현
	   1. num파라미터에 해당하는 게시물 정보를 board 객체 전달. 
	      service.getBoard(num)
	      boardDao.selectOne(num)
	   2. 조회수 증가
	      boardDao.addReadCnt(num)
	*/
	@RequestMapping("detail")
	public ModelAndView detail(Integer num) {
		ModelAndView mav = new ModelAndView();
		Board board = service.getBoard(num);
		service.addReadcnt(num);
		mav.addObject("board",board);
		if(board.getBoardid() == null || board.getBoardid().equals("1"))
			mav.addObject("boardName","공지사항");
		else if (board.getBoardid().equals("2"))
			mav.addObject("boardName","자유게시판");
		else if (board.getBoardid().equals("3"))
			mav.addObject("boardName","QnA");
		return mav;
	}
	@GetMapping({"reply","update","delete"})
	public ModelAndView getBoard(Integer num, HttpSession session) {
		ModelAndView mav = new ModelAndView();
		String boardid = (String)session.getAttribute("boardid");
		Board board = service.getBoard(num);
		mav.addObject("board",board);
		if(boardid == null || boardid.equals("1"))
			mav.addObject("boardName","공지사항");
		else if (boardid.equals("2"))
			mav.addObject("boardName","자유게시판");
		else if (boardid.equals("3"))
			mav.addObject("boardName","QnA");
		return mav;
	}
	/*
	 * 1.유효성 검사하기-파라미터 값 저장.
	 * 		-원글정보 : num,grp,grplevel,grpstep,boardid
	 * 		-답글정보 : writer, pass, title, content
	 * 2.db에 insert => service.boardReply()
	 * 		- 원글의 grpstep보다 큰 이미 등록된 답글의 grpstep 값을 +1
	 * 		  => boardDao.grpStepAdd()
	 * 		- num : maxNum() + 1		  
	 *		- db에 insert => boardDao.insert()
	 *		  grp : 원글과 동일
	 *		  grplevel : 원글의 grplevel + 1
	 *		  grpstep  : 원글의 grpstep + 1
	 * 3.등록 성공 : list로 페이지 이동
	 *   등록 실패 : "답변 등록시 오류 발생" reply 페이지 이동
	 */
	@PostMapping("reply")
	public ModelAndView writeReply(Integer num, @Valid Board board,BindingResult bresult) {
		ModelAndView mav = new ModelAndView();
		//유효성 검증
		if(bresult.hasErrors()) {
			Board dbboard = service.getBoard(board.getNum());
			Map<String,Object> map = bresult.getModel();
			Board b = (Board)map.get("board");//화면에서 입력받은 값을 저장한 Board 객체
			b.setTitle(dbboard.getTitle());//원글의 제목으로 변경
			mav.getModel().putAll(bresult.getModel());
			return mav;
		}
		try {
			service.boardReply(board, num);
			mav.setViewName("redirect:list?boardid="+board.getBoardid());
		} catch(Exception e) {
			e.printStackTrace();
			throw new LoginException ("답변등록시 오류 발생","reply?num="+board.getNum());
		}	
		return mav;
	}
	/*
	 * 1. 유효성 검증.
	 * 2. 비밀번호 검증 => 검증 오류 : 비밀번호가 틀립니다. 메세지 출력. update 페이지 이동
	 * 3. 업로드된 파일이 있는 경우 파일 업로드. db의 내용 수정
	 * 4. 수정 완료 : detail 페이지 이동
	 * 	  수정 실패 : 수정실패 메세지 출력. update 페이지 이동
	 */
	@PostMapping("update")
	public ModelAndView updatePost(@Valid Board board,BindingResult bresult,
			HttpServletRequest request) {
		ModelAndView mav = new ModelAndView();
		//유효성 검증
		if(bresult.hasErrors()) {
			mav.getModel().putAll(bresult.getModel());
			return mav;
		}
		Board dbboard = service.getBoard(board.getNum());  	
		//비밀번호 검증
		if(!dbboard.getPass().equals(board.getPass())) {
			throw new BoardException ("비밀번호가 틀립니다","update?num="+board.getNum());
		}
		//입력값 정상, 비밀번호 일치
		try {
			service.updatepost(board, request);
			mav.setViewName("redirect:detail?num="+board.getNum());
		} catch(Exception e) {
			e.printStackTrace();
			throw new BoardException ("게시글 수정에 실패했습니다.","update?num="+board.getNum());
		}	
		return mav;
	}
	/*
	 * 1. num, pass 파라미터 저장
	 * 2. 비밀번호 검증 : db에서 num게시글 조회. db에 등록된 비밀번호와 입력된 비밀번호 비교
	 * 					비밀번호 오류 : error.board.password 코드값 설정 => delete.jsp로 전달
	 * 3. 비밀번호 일치 : db에서 num 게시글 삭제.
	 * 		삭제 성공 : list페이지 이동
	 * 		삭제 실패 : delete페이지 이동
	 */
	@PostMapping("delete")
	public ModelAndView deletePost(@Valid Board board, BindingResult bresult) {
		ModelAndView mav = new ModelAndView();
		Board dbboard = service.getBoard(board.getNum());
		if(board.getPass()==null || board.getPass().trim().equals("")) {
			bresult.reject("error.required.password");
			return mav;
		}
		if(!dbboard.getPass().equals(board.getPass())) {
			bresult.reject("error.board.password");
			return mav;
		}
		try {
			service.deletepost(board.getNum());
			mav.setViewName("redirect:list?boardid="+dbboard.getBoardid());
	    } catch(Exception e) {
	    	e.printStackTrace();
			bresult.reject("error.board.fail");
			mav.setViewName("redirect:delete?num="+board.getNum());
			return mav;
	    }
		return mav;
	}
	@RequestMapping("imgupload")
	public String imgupload(MultipartFile upload, String CKEditorFuncNum,
			HttpServletRequest request, Model model) {
		/*
		 * upload : CKEditor 모듈에서 업로드되는 이미지의 이름.
		 * 			업로드되는 이미지파일의 내용. 이미지값
		 * CKEditorFuncNum : CKEditor 모듈에서 파라미터로 전달되는 값. 리턴해야 되는 값
		 * model : ModelAndView 중 Model에 해당하는 객체.
		 * 		   뷰에 전달할 데이터 정보 저장할 객체
		 * return 타입 String : 뷰의 이름.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                      
		 */
		//업로드 되는 위치 폴더
		//request.getServletContext().getRealPath("/") : 웹어플리케이션의 절대 경로 값.
		String path = request.getServletContext().getRealPath("/") + "board/imgfile/";
		service.uploadFileCreate(upload,path); //upload(파일의 내용), path(업로드되는 폴더)
		//request.getContextPath() : 프로젝트명(웹어플리케이션 서버이름). shop1/
		//http://localhost:8080/shop1/board/imgfile/cat.jpg
		String fileName = request.getContextPath() //웹어플리케이션 경로. 웹 url 정보
						+ "/board/imgfile/" + upload.getOriginalFilename();
		model.addAttribute("fileName",fileName);
		return "ckedit"; //view 이름. /WEB-INF/view/ckedit.jsp
	}
}
