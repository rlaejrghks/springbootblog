package com.co.kr.controller;

import org.springframework.stereotype.Controller;

import java.util.HashMap;
import java.io.IOException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.co.kr.service.CommentService;
import com.co.kr.service.UploadService;
import com.co.kr.service.UserService;
import com.co.kr.util.CommonUtils;

import lombok.extern.slf4j.Slf4j;

import com.co.kr.vo.FileListVO;
import com.co.kr.vo.LoginVO;

import java.io.PrintWriter;
import java.text.ParseException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.multipart.MultipartHttpServletRequest;
import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.ModelAndView;

import com.co.kr.domain.BoardFileDomain;
import com.co.kr.domain.BoardListDomain;
import com.co.kr.domain.CommentListDomain;
import com.co.kr.domain.LoginDomain;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Slf4j
@Controller
public class FileListController {
	@RequestMapping(value = "detail", method = RequestMethod.GET)
	public ModelAndView bdSelectOneCall(@ModelAttribute("fileListVO") FileListVO fileListVO,
			@RequestParam("bdSeq") String bdSeq, HttpServletRequest request) {
		ModelAndView mav = new ModelAndView();
		HashMap<String, Object> map = new HashMap<String, Object>();
		HttpSession session = request.getSession();

		map.put("bdSeq", Integer.parseInt(bdSeq));
		BoardListDomain boardListDomain = uploadService.boardSelectOne(map);
		List<BoardFileDomain> fileList = uploadService.boardSelectOneFile(map);
		List<CommentListDomain> commentListDomain = commentService.commentList(map);
		
		for (BoardFileDomain list : fileList) {
			String path = list.getUpFilePath().replaceAll("\\\\", "/");
			list.setUpFilePath(path);
		}
		mav.addObject("detail", boardListDomain);
		mav.addObject("comment", commentListDomain);
		mav.addObject("files", fileList);
		mav.setViewName("/board/board.html");
		session.setAttribute("files", fileList);
		return mav;
	}
	
	@Autowired
	private UploadService uploadService;

	@Autowired
	private UserService userService;
	
	@Autowired
	private CommentService commentService;
	
	@RequestMapping(value = "upload")
	public ModelAndView bdUpload(FileListVO fileListVO, MultipartHttpServletRequest request, HttpServletRequest httpReq,
			HttpServletResponse response) throws IOException, ParseException {

		ModelAndView mav = new ModelAndView();
		int bdSeq = uploadService.fileProcess(fileListVO, request, httpReq);
		fileListVO.setContent(""); // 초기화
		fileListVO.setTitle("");
		mav = bdSelectOneCall(fileListVO, String.valueOf(bdSeq), request);
		mav.setViewName("board/board.html");
		return mav;
	}

	@RequestMapping(value = "/remove", method = RequestMethod.GET)
	public ModelAndView remove(BoardFileDomain boardFileDomain, String bdSeq, HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		System.out.println(bdSeq + "번째 게시글 삭제.");
		ModelAndView mav = new ModelAndView();
		HashMap<String, Object> map = new HashMap<String, Object>();
		HttpSession session = request.getSession();

		map.put("bdSeq", Integer.parseInt(bdSeq));
		uploadService.bdContentRemove(map);

		boardFileDomain.setBdSeq(Integer.parseInt(bdSeq));
		System.out.println(bdSeq + "번째 게시글 사진 삭제");
		uploadService.bdFileRemove(boardFileDomain);
		mav.setViewName("/board/board.html");
		String alertText = "게시글이 삭제되었습니다.";
		String redirectPath = "/main/bdList";
		CommonUtils.redirect(alertText, redirectPath, response);
		return mav;
	}

	@RequestMapping(value = "edit", method = RequestMethod.GET)
	public ModelAndView edit(FileListVO fileListVO, @RequestParam("bdSeq") String bdSeq, HttpServletRequest request) throws IOException {
		ModelAndView mav = new ModelAndView();

		HashMap<String, Object> map = new HashMap<String, Object>();
		HttpSession session = request.getSession();
		
		map.put("bdSeq", Integer.parseInt(bdSeq));
		BoardListDomain boardListDomain =uploadService.boardSelectOne(map);
		List<BoardFileDomain> fileList =  uploadService.boardSelectOneFile(map);
		
		for (BoardFileDomain list : fileList) {
			String path = list.getUpFilePath().replaceAll("\\\\", "/");
			list.setUpFilePath(path);
		}

		fileListVO.setSeq(boardListDomain.getBdSeq());
		fileListVO.setContent(boardListDomain.getBdContent());
		fileListVO.setTitle(boardListDomain.getBdTitle());
		fileListVO.setIsEdit("edit");  // upload 재활용하기위해서
		
	
		mav.addObject("detail", boardListDomain);
		mav.addObject("files", fileList);
		mav.addObject("fileLen",fileList.size());
		
		mav.setViewName("board/boardEditList.html");
		return mav;
	}
	@PostMapping(value="/editSave")
	public ModelAndView editSave(BoardFileDomain boardFileDomain, FileListVO fileListVO, MultipartHttpServletRequest request, HttpServletRequest httpReq,
			HttpServletResponse response) throws IOException, ParseException {
		ModelAndView mav = new ModelAndView();
		int bdSeq = uploadService.fileProcess(fileListVO, request, httpReq);
		System.out.println(bdSeq+"번째 게시물 수정완료");
		
		uploadService.bdFileRemove(boardFileDomain);
		fileListVO.setContent("");
		fileListVO.setTitle("");
		mav = bdSelectOneCall(fileListVO, String.valueOf(bdSeq), request);
		mav.setViewName("board/board.html");
		response.setContentType("text/html; charset=euc-kr");
		PrintWriter out = response.getWriter();
		out.println("수정 완료");
		out.flush();

		return mav;
	}

	@RequestMapping(value = "profileEditSave", method = RequestMethod.POST)
	public void profileEditSave(LoginVO loginVO, LoginDomain loginDomain,
			MultipartHttpServletRequest request, HttpServletResponse response)
			throws IOException, ParseException {
		HttpSession session = request.getSession();
		ModelAndView mav = new ModelAndView();
		loginDomain.setMbPw(loginVO.getPw().toString());
		loginDomain.setMbUse("Y");
		loginDomain.setMbId(loginVO.getId().toString());
		loginVO.setIsEdit("Y");
		userService.mbUpdate(loginDomain);
		session.setAttribute("pw", loginDomain.getMbPw());
		System.out.println(loginVO.getId().toString() + "님의 프로필을 업데이트 하였습니다.");
		String redirectPath = "bdList";
		CommonUtils.redirect("프로필이 변경되었습니다.", redirectPath, response);
	}
	

	// Comment line
	@PostMapping(value = "bcupload")
	public ModelAndView bcUpload(FileListVO fileListVO, MultipartHttpServletRequest request, HttpServletRequest httpReq,
			HttpServletResponse response) throws IOException, ParseException {

		ModelAndView mav = new ModelAndView();
		fileListVO.setBcseq(httpReq.getParameter("bcSeq"));
		fileListVO.setSeq(httpReq.getParameter("bdSeq"));
		fileListVO.setBccontent(httpReq.getParameter("content"));
		HashMap<String, Object> map = new HashMap<>();
		int bdSeq = commentService.fileProcess(fileListVO, httpReq);
		fileListVO.setContent(""); // 초기화
		map.put("bdSeq", bdSeq);
		CommentListDomain commentListDomain = commentService.commentSelectOne(map);

		mav = bdSelectOneCall(fileListVO, String.valueOf(bdSeq), request);
		mav.addObject("bcitems", commentListDomain);
		String redirectPath = "/main/bdList";
		CommonUtils.redirect("댓글이 추가되었습니다.", redirectPath, response);
		return mav;
	}

	@GetMapping("bcedit")
	public ModelAndView bcedit(FileListVO fileListVO, @RequestParam("bcSeq") String bcSeq,
			@RequestParam("bdSeq") Integer bdSeq, HttpServletRequest request) throws IOException {
		ModelAndView mav = new ModelAndView();

		HashMap<String, Object> map = new HashMap<String, Object>();
		HttpSession session = request.getSession();

		map.put("bdSeq", bdSeq);
		BoardListDomain boardListDomain = uploadService.boardSelectOne(map);
		List<BoardFileDomain> fileList = uploadService.boardSelectOneFile(map);
		map.put("bcSeq", bcSeq);
		CommentListDomain commentListDomain = commentService.commentSelectOne(map);
		for (BoardFileDomain list : fileList) {
			String path = list.getUpFilePath().replaceAll("\\\\", "/");
			list.setUpFilePath(path);
		}
		
		fileListVO.setBcseq(commentListDomain.getBcSeq());
		fileListVO.setBccontent(commentListDomain.getBcContent());
		fileListVO.setSeq(boardListDomain.getBdSeq());
		fileListVO.setContent(boardListDomain.getBdContent());
		fileListVO.setTitle(boardListDomain.getBdTitle());
		fileListVO.setIsEdit("edit"); // upload 재활용하기위해서
		mav.addObject("bcitems", commentListDomain);
		mav.addObject("detail", boardListDomain);
		mav.addObject("bcSeq", commentListDomain.getBcSeq());
		mav.addObject("files", fileList);
		mav.addObject("fileLen", fileList.size());

		mav.setViewName("board/commentEdit.html");
		return mav;
	}

	@PostMapping("bceditSave")
	public ModelAndView bceditSave(@ModelAttribute("fileListVO") FileListVO fileListVO,
			MultipartHttpServletRequest request, HttpServletRequest httpReq) throws IOException {

		HttpSession session = request.getSession();
		ModelAndView mav = new ModelAndView();
		// 저장
		int bdSeq = commentService.fileProcess(fileListVO, httpReq);
		HashMap<String, Object> map = new HashMap<>();
		map.put("bdSeq", bdSeq);
		List<CommentListDomain> commentList = commentService.commentList(map);
		System.out.println("commentList : "+commentList);
		mav = bdSelectOneCall(fileListVO, String.valueOf(bdSeq), request);
		mav.addObject("comment", commentList);
		fileListVO.setContent(""); // 초기화
		fileListVO.setTitle(""); // 초기화
		mav.setViewName("board/board.html");
		return mav;
	}

	@GetMapping("bcremove")
	public ModelAndView bcremove(HttpServletRequest request, HttpServletResponse response) throws IOException {
		ModelAndView mav = new ModelAndView();
		HashMap<String, Object> map = new HashMap<>();
		map.put("bdSeq", request.getParameter("bdSeq"));
		map.put("bcSeq", request.getParameter("bcSeq"));
		commentService.commentRemove(map);
		String redirectPath = "bdList";
		CommonUtils.redirect("댓글이 삭제되었습니다.", redirectPath, response);
		return mav;
	}
}
