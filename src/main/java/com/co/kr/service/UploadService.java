package com.co.kr.service;

import java.util.HashMap;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.co.kr.domain.BoardContentDomain;
import com.co.kr.domain.BoardFileDomain;
import com.co.kr.domain.BoardListDomain;
import com.co.kr.vo.FileListVO;

public interface UploadService {
	
	public int fileProcess(FileListVO fileListVO, MultipartHttpServletRequest request, HttpServletRequest httpReq);
	
	// all list
	public List<BoardListDomain> boardList();
	
	// 하나 삭제
	public void bdContentRemove(HashMap<String, Object> map);
	
	// 전체 삭제
	public void bdContentAllRemove(HashMap<String, String>map);
	
	// 하나 삭제
	public void bdFileRemove(BoardFileDomain boardFileDomain);
	
	// 전체 삭제
	public void bdFileAllRemove(HashMap<String, String> map);
	
	//select one
	public BoardListDomain boardSelectOne(HashMap<String, Object> map);

	//select one file
	public List<BoardFileDomain> boardSelectOneFile(HashMap<String, Object> map);
	
	//content update
	public void bdContentUpdate(BoardContentDomain boardContentDomain);

	//file update
	public void bdFileUpdate(BoardFileDomain boardFileDomain);
}