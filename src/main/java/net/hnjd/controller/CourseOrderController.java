package net.hnjd.controller;

import net.hnjd.pojo.CRUDResult;
import net.hnjd.pojo.CourseOrder;
import net.hnjd.pojo.PageResult;
import net.hnjd.service.CourseOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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

    @RequestMapping("/edit")
    public String edit(@RequestParam("order_id") String order_id, Model model) {
        CourseOrder courseOrder = courseOrderService.findByOrderId(order_id);
        model.addAttribute("order", courseOrder);
        return "courseorder/edit";
    }

    @RequestMapping("/list")
    public String list() {
        return "courseorder/list";
    }

    @ResponseBody
    @DeleteMapping("/delete")
    public CRUDResult delete(String order_id) {
        CRUDResult crudResult = new CRUDResult();
        courseOrderService.deleteByOrderId(order_id);
        return crudResult;
    }

    @RequestMapping("/save")
    @ResponseBody
    public CRUDResult save(CourseOrder order) {
        CRUDResult crudResult = new CRUDResult();
        if (order.getOrder_id() != null || order.getOrder_id().length() == 0) {
            courseOrderService.update(order);
        } else {
            courseOrderService.save(order);
            System.out.println(order);
        }
        return crudResult;
    }

    @RequestMapping("/listJson")
    @ResponseBody
    public PageResult<CourseOrder> listJson(CourseOrder condition, int page, int limit) {
        PageResult<CourseOrder> pageResult = courseOrderService.findPageResult(condition, page, limit);
        return pageResult;
    }
}
