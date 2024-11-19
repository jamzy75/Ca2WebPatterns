package run.controllers;

import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import run.model.User;
import run.persistence.UserDao;
import run.persistence.UserDaoImpl;

// Lombok annotation to add a logger to the class
@Slf4j
@Controller
public class UserController {
    @PostMapping("registerUser")
    public String registerUser(
            @RequestParam(name="username") String username,
            @RequestParam(name="password") String password,
            @RequestParam(name="first", required = false) String first,
            @RequestParam(name="last", required = false) String last,
            @RequestParam(name="email") String email,
            Model model, HttpSession session){
        // VALIDATION
        String view = "";
        UserDao userDao = new UserDaoImpl("database.properties");
        User u = new User(username, password, first, last, true);
        boolean added = userDao.register(u);
        if(added){
            view = "registerSuccess";
            model.addAttribute("registeredUser", u);
            log.info("User {} registered", u.getUsername());
        }else{
            view = "registerFailed";
            log.info("Registration failed with username {}", username);
        }

        return view;
    }
    
    @PostMapping("/login")
    public String loginUser(
            @RequestParam(name="username")String username,
            @RequestParam(name="password") String password,
            Model model, HttpSession session){

        if(username.isBlank() || password.isBlank()){
            String errorMsg = "Username and password cannot be blank";
            model.addAttribute("errMsg", errorMsg);
            return "error";
        }

        UserDao userDao = new UserDaoImpl("database.properties");
        User u = userDao.login(username, password);

        if(u == null){
            String message = "No such username/password combination";
            model.addAttribute("message", message);
            return "loginFailed";
        }

        session.setAttribute("loggedInUser", u);
        return "loginSuccessful";
    }
}
