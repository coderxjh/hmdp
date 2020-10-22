package net.hnjd.service;

import net.hnjd.mapper.CourseOrderMapper;
import net.hnjd.pojo.CourseOrder;
import net.hnjd.pojo.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author simba@onlying.cn
 * @date 2020/10/21 23:15
 */
@Service
public class CourseOrderService {

    @Autowired
    private CourseOrderMapper courseOrderMapper;

    public PageResult<CourseOrder> findPageResult(CourseOrder condition, int page, int pageSize) {
        PageResult<CourseOrder> pageResult = new PageResult<>();
        pageResult.setCode(0);
        //查询总记录数
        Map<String, Object> params = new HashMap<>();
        int totalCount = courseOrderMapper.findCountByMap(params);
        List<CourseOrder> list = courseOrderMapper.findListByMap(params);
        //获取查询数据
        pageResult.setCount(totalCount);
        pageResult.setData(list);
        return pageResult;
    }

}
