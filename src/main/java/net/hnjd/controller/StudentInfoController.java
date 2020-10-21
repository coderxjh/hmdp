package net.hnjd.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author simba@onlying.cn
 * @date 2020/10/21 19:43
 */
@Controller
@RequestMapping("/studentinfo")
public class StudentInfoController {

    @RequestMapping("/list")
    public String list(){
        return "studentinfo/list";
    }
}
