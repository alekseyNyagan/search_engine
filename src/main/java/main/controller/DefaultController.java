package main.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@Tag(name = "Стартовая страница")
public class DefaultController {
    @RequestMapping("/admin")
    @Operation(summary = "Получение стартовой страницы")
    public String index() {
        return "index";
    }
}
