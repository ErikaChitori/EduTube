package com.cqu.edustatistics.controller;


import com.cqu.commonutils.R;
import com.cqu.edustatistics.service.StatisticsDailyService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Member;
import java.util.Map;

/**
 * <p>
 * 网站统计日数据 前端控制器
 * </p>
 *
 * @author CGT
 * @since 2021-07-15
 */
@RestController
@RequestMapping("/edustatistics/statistics")
public class StatisticsDailyController {
    @Autowired
    StatisticsDailyService statisticsDailyService;
    @ApiOperation(value = "生成统计数据")
    @PostMapping("{day}")
    public R createStatisticsByDate(@PathVariable String day) {
        statisticsDailyService.createStatisticsByDay(day);
        return R.ok();
    }

    @ApiOperation(value = "图表显示")
    @GetMapping("showData/{type}/{begin}/{end}")
    public R showData(@PathVariable String type,@PathVariable String begin,
                      @PathVariable String end) {
        Map<String,Object> map = statisticsDailyService.getShowData(type,begin,end);
        return R.ok().data(map);
    }

}
