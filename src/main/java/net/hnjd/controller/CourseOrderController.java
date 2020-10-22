package net.hnjd.controller;

import net.hnjd.pojo.CourseOrder;
import net.hnjd.pojo.PageResult;
import net.hnjd.service.CourseOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;

/**
 * @author simba@onlying.cn
 * @date 2020/10/21 19:39
 */
@Controller
@RequestMapping("/courseorder")
public class CourseOrderController {

    @Autowired
    private CourseOrderService courseOrderService;

    @RequestMapping("/list")
    public String list() {
        return "courseorder/list";
    }

    @RequestMapping("/listJson")
    @ResponseBody
    public PageResult<CourseOrder> listJson() {
        PageResult<CourseOrder> pageResult = courseOrderService.findPageResult(null, 1, 8);
        return pageResult;
    }
}
