package net.hnjd.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author simba@onlying.cn
 * @date 2020/10/21 15:54
 */
@Controller
public class IndexController {

    @RequestMapping("/index")
    public String index() {
        return "index";
    }

//    @RequestMapping("/test")
//    public String test() {
//        return "Hello World";
//    }
}
