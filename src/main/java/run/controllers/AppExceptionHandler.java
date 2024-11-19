package run.controllers;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class AppExceptionHandler {
    @ExceptionHandler(value = NullPointerException.class)
    public String nullPointerHandler(Model model) {
        model.addAttribute("err", "NullPointerException");
        return "error";
    }

    @ExceptionHandler(value = Exception.class)
    public String allOtherExceptionHandler(Model model, Exception ex) {
        model.addAttribute("errType", ex.getClass());
        model.addAttribute("errMsg", ex.getMessage());
        return "error";
    }
}
