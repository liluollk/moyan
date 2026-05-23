package com.liluo.moyan.module.search.controller;

import com.liluo.moyan.common.result.Result;
import com.liluo.moyan.module.search.service.SearchService;
import com.liluo.moyan.module.work.vo.WorkVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 搜索控制器
 */
@Tag(name = "搜索")
@RestController
@RequestMapping("/api/search")
public class SearchController {
    
    @Autowired
    private SearchService searchService;
    
    @Operation(summary = "搜索作品")
    @GetMapping("/works")
    public Result<List<WorkVO>> searchWorks(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size) {
        List<WorkVO> works = searchService.searchWorks(keyword, from, size);
        return Result.success(works);
    }
}
