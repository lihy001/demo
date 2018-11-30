package com.heng.lucene.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.heng.lucene.dto.DocDto;
import com.heng.lucene.service.IndexService;

@RestController
public class IndexController {

	@Autowired
	private IndexService indexService;

	@RequestMapping("/search")
	public Map<String, Object> index(@RequestParam("wd") String wd) {

		Map<String, Object> map = new HashMap<>();
		
		List<DocDto> docDtoList = indexService.searchIndex(wd);
		map.put("docList", docDtoList);
		return map;
	}
}
