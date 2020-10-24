package net.hnjd.controller;

import net.hnjd.pojo.MonthIncome;
import net.hnjd.service.CourseOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * @author simba@onlying.cn
 * @date 2020/10/24 16:01
 */
@Controller
@RequestMapping("/dataanalysis")
public class DataAnalysisController {

    @Autowired
    private CourseOrderService courseOrderService;

    @RequestMapping("/income")
    public String list() {
        return "dataanalysis/income";
    }

    @RequestMapping("/monthIncomes")
    @ResponseBody
    public List<MonthIncome> monthIncomes() {
        List<MonthIncome> monthIncomes=courseOrderService.getMonthIncomes();
        System.out.println(monthIncomes);
        return monthIncomes;
    }
}
