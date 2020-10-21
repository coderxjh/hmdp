package net.hnjd.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author simba@onlying.cn
 * @date 2020/10/21 19:39
 */
@Controller
@RequestMapping("/courseorder")
public class CourseOrderController {

    @RequestMapping("/list")
    public String list(){
        return "courseorder/list";
    }
}
