package run.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexController {
    @GetMapping("/index")
    public String home(){
        return "index";
    }

    @GetMapping("/")
    public String userIndex(){
        return "user_index";
    }

    @GetMapping("/customer_index")
    public String customerIndex(){
        return "customer_index";
    }

    @GetMapping("/product_index")
    public String employeeIndex(){
        return "product_index";
    }
}
