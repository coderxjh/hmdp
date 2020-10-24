package net.hnjd.mapper;

import net.hnjd.pojo.CourseOrder;

import java.util.List;
import java.util.Map;

/**
 * @author simba@onlying.cn
 * @date 2020/10/21 23:17
 */
public interface CourseOrderMapper {

    int findCountByMap(Map<String, Object> map);

    List<CourseOrder> findListByMap(Map<String, Object> map);

    void insert(CourseOrder courseOrder);

    CourseOrder findByOrderId(String orderId);

    void deleteOrderId(String order_id);
}
