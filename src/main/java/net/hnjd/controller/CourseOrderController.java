package net.hnjd.controller;

import net.hnjd.pojo.CRUDResult;
import net.hnjd.pojo.CourseOrder;
import net.hnjd.pojo.PageResult;
import net.hnjd.service.CourseOrderService;
import org.apache.ibatis.annotations.Delete;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;


/**
 * @author simba@onlying.cn
 * @date 2020/10/21 19:39
 */
@Controller
@RequestMapping("/courseorder")
public class CourseOrderController {

    @Autowired
    private CourseOrderService courseOrderService;

    @RequestMapping("/save")
    @ResponseBody
    public CRUDResult save(CourseOrder order) {
        CRUDResult crudResult = new CRUDResult();
        System.out.println(order);
        courseOrderService.save(order);
        return crudResult;
    }

    @RequestMapping("add")
    public String add() {
        return "courseorder/add";
    }

    @GetMapping("/detail")
    public String detail(@RequestParam(value = "order_id") String order_id, Model model) {
        CourseOrder courseOrder = courseOrderService.findByOrderId(order_id);
        model.addAttribute("order", courseOrder);
        return "courseorder/detail";
    }

    @ResponseBody
    @DeleteMapping("/delete")
    public CRUDResult delete(String order_id){
        CRUDResult crudResult = new CRUDResult();
        courseOrderService.deleteByOrderId(order_id);
        return crudResult;
    }

    @RequestMapping("/list")
    public String list() {
        return "courseorder/list";
    }

    @RequestMapping("/listJson")
    @ResponseBody
    public PageResult<CourseOrder> listJson(int page, int limit) {
        PageResult<CourseOrder> pageResult = courseOrderService.findPageResult(null, page, limit);
        return pageResult;
    }
}
