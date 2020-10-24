package net.hnjd.service;

import net.hnjd.mapper.CourseOrderMapper;
import net.hnjd.pojo.CourseOrder;
import net.hnjd.pojo.MonthIncome;
import net.hnjd.pojo.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
        params.put("start", (page - 1) * pageSize);
        params.put("pageSize", pageSize);
        params.put("condition",condition);
        int totalCount = courseOrderMapper.findCountByMap(params);
        List<CourseOrder> list = courseOrderMapper.findListByMap(params);
        //获取查询数据
        pageResult.setCount(totalCount);
        pageResult.setData(list);
        return pageResult;
    }


    public void save (CourseOrder courseOrder){
        courseOrderMapper.insert(courseOrder);
    }

    public CourseOrder findByOrderId(String order_id) {
        return courseOrderMapper.findByOrderId(order_id);
    }

    public void deleteByOrderId(String order_id) {
        courseOrderMapper.deleteOrderId(order_id);
    }

    public void update(CourseOrder order) {
        courseOrderMapper.update(order);
    }

    public List<MonthIncome> getMonthIncomes() {
        return courseOrderMapper.getMonthIncomes();
    }
}
