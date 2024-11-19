package run.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import run.model.Message;
import run.persistence.MessageDao;
import run.persistence.MessageDaoImpl;

import java.util.List;

@Controller
public class MessageController {



    @GetMapping("/viewMessages")
    public String processRequest(Model model) {
        MessageDao messageDao = new MessageDaoImpl("database.properties");
        List<Message> messages = messageDao.getReceivedMessagesForUser("Charles");
        model.addAttribute("messages",messages);

        return "messages";
    }

//    @GetMapping("/greet")
//    public int greet(@RequestParam(name="messageID", required=false, defaultValue="1") int messageID,
//                        Model model) {
//
//
//        MessageDao messageDao = new MessageDaoImpl("database.properties");
//        Message m = messageDao.getById(productCode);
//        Message m = messageDao.getById(productCode);
//        model.addAttribute("product", m);
//        model.addAttribute("messageID", messageID);
//
//        return "greeting";
//    }

}