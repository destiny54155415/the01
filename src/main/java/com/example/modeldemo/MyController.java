package com.example.modeldemo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class MyController {

    private static final Logger log = LoggerFactory.getLogger(MyController.class);

    @GetMapping("/01model")
    public String model1(Model model) {
        return "01model";
    }

    @GetMapping("/02model")
    public ModelAndView model2() {
        log.info("02model");
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("key", "value=model2");
        modelAndView.setViewName("02model");
        return modelAndView;
    }

    // 03model?name=John&age=25&height=175.5&birthday=2023-09-11T15:30:00
    @GetMapping("03model")
    public String model3(
            @RequestParam("name") String name,
            @RequestParam("age") long age,
            @RequestParam("height") double height,
            @RequestParam("birthday") @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
            LocalDateTime birthday,
            Model model) {
        log.info("03model");
        System.out.println("name : " + name);
        System.out.println("age : " + age);
        System.out.println("height : " + height);
        System.out.println("birthday : " + birthday);

        return "03model";
    }

    // 04model?name=John&age=25&height=175.5&birthday=2023-09-11T15:30:01
    @GetMapping("04model")
    public String model4(@ModelAttribute HumanDto humanDto, Model model) {
        log.info("04model");
        System.out.println("name : " + humanDto.getName());
        System.out.println("age : " + humanDto.getAge());
        System.out.println("height : " + humanDto.getHeight());
        System.out.println("birthday : " + humanDto.getBirthday());

        return "04model";
    }

    // 05model?name=John&age=25&height=175.5&birthday=2023-09-11T15:30
    @GetMapping("05model")
    public String model5(@ModelAttribute HumanDto humanDto, Model model) {
        log.info("05model");

        Map<String, Object> map = new HashMap<>();
        map.put("key1", "홍길동");
        map.put("key2", "21");
        map.put("key3", "대전");
        map.put("key4", humanDto);

        model.addAttribute("mapdata", map);

        return "05model";
    }

    @GetMapping("/06model")
    public String model6(Model model) {
        List<HumanDto> list = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            HumanDto dto = new HumanDto();
            dto.setName("이름" + i);
            dto.setAge(25 + i);
            dto.setHeight(179 + i);
            dto.setBirthday(LocalDateTime.of(1993, 7, 21, 10, 30).plusYears(i));
            list.add(dto);
        }
        model.addAttribute("list", list);
        return "06model";
    }
}
