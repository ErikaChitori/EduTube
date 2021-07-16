package com.cqu.edustatistics.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cqu.edustatistics.entity.StatisticsDaily;
import com.cqu.edustatistics.mapper.StatisticsDailyMapper;
import com.cqu.edustatistics.service.StatisticsDailyService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cqu.edustatistics.utils.UcenterClient;
import io.swagger.models.auth.In;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 网站统计日数据 服务实现类
 * </p>
 *
 * @author CGT
 * @since 2021-07-15
 */
@Service
public class StatisticsDailyServiceImpl extends ServiceImpl<StatisticsDailyMapper, StatisticsDaily> implements StatisticsDailyService {

    @Autowired
    private UcenterClient ucenterClient;

    @Override
    public void createStatisticsByDay(String day) {
        //删除已存在的统计对象
        QueryWrapper<StatisticsDaily> dayQueryWrapper = new QueryWrapper<>();
        dayQueryWrapper.eq("date_calculated", day);
        baseMapper.delete(dayQueryWrapper);
        //获取统计信息
        Integer registerNum = (Integer) ucenterClient.registerCount(day).getData().get("countRegister");
        Integer loginNum = (Integer) ucenterClient.loginCount(day).getData().get("countLogin");//TODO
        Integer videoViewNum = (Integer) ucenterClient.videoCount(day).getData().get("countVideo");//TODO
        Integer courseNum = (Integer) ucenterClient.courseCount(day).getData().get("countCourse");//TODO
        //创建统计对象
        StatisticsDaily daily = new StatisticsDaily();
        daily.setRegisterNum(registerNum);
        daily.setLoginNum(loginNum);
        daily.setVideoViewNum(videoViewNum);
        daily.setCourseNum(courseNum);
        daily.setDateCalculated(day);
        baseMapper.insert(daily);
    }

    @Override
    public Map<String, Object> getShowData(String type, String begin, String end) {
        //根据条件查询对应数据
        QueryWrapper<StatisticsDaily> wrapper = new QueryWrapper<>();
        wrapper.between("date_calculated",begin,end);
        wrapper.select("date_calculated",type);
        List<StatisticsDaily> staList = baseMapper.selectList(wrapper);

        //因为返回有两部分数据：日期 和 日期对应数量
        //前端要求数组json结构，对应后端java代码是list集合
        //创建两个list集合，一个日期list，一个数量list
        List<String> date_calculatedList = new ArrayList<>();
        List<Integer> numDataList = new ArrayList<>();

        //遍历查询所有数据list集合，进行封装
        for (int i = 0; i < staList.size(); i++) {
            StatisticsDaily daily = staList.get(i);
            //封装日期list集合
            date_calculatedList.add(daily.getDateCalculated());
            //封装对应数量
            switch (type) {
                case "login_num":
                    numDataList.add(daily.getLoginNum());
                    break;
                case "register_num":
                    numDataList.add(daily.getRegisterNum());
                    break;
                case "video_view_num":
                    numDataList.add(daily.getVideoViewNum());
                    break;
                case "course_num":
                    numDataList.add(daily.getCourseNum());
                    break;
                default:
                    break;
            }
        }
        //把封装之后两个list集合放到map集合，进行返回
        Map<String, Object> map = new HashMap<>();
        map.put("date_calculatedList",date_calculatedList);
        map.put("numDataList",numDataList);
        return map;
    }

    @Override
    public void updateLoginNum(String day) {
        QueryWrapper<StatisticsDaily>wrapper=new QueryWrapper<>();
        wrapper.eq("date_calculated",day);
        StatisticsDaily daily=baseMapper.selectOne(wrapper);
        Integer loginNum=daily.getLoginNum();
        loginNum++;
        daily.setLoginNum(loginNum);
        baseMapper.updateById(daily);
    }

    @Override
    public void updateRegisterNum(String day) {
        QueryWrapper<StatisticsDaily>wrapper=new QueryWrapper<>();
        wrapper.eq("date_calculated",day);
        StatisticsDaily daily=baseMapper.selectOne(wrapper);
        Integer registerNum=daily.getRegisterNum();
        registerNum++;
        daily.setRegisterNum(registerNum);
        baseMapper.updateById(daily);
    }

    @Override
    public void updateVideo(String day) {
        QueryWrapper<StatisticsDaily>wrapper=new QueryWrapper<>();
        wrapper.eq("date_calculated",day);
        StatisticsDaily daily=baseMapper.selectOne(wrapper);
        Integer videoNum=daily.getVideoViewNum();
        videoNum++;
        daily.setVideoViewNum(videoNum);
        baseMapper.updateById(daily);
    }

    @Override
    public void updateCourse(String day) {
        QueryWrapper<StatisticsDaily>wrapper=new QueryWrapper<>();
        wrapper.eq("date_calculated",day);
        StatisticsDaily daily=baseMapper.selectOne(wrapper);
        Integer courseNum=daily.getCourseNum();
        courseNum++;
        daily.setCourseNum(courseNum);
        baseMapper.updateById(daily);
    }
}
