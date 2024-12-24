package cn.cactusli.cursorapi.controller;

import cn.cactusli.cursorapi.dto.MessageObject;
import cn.cactusli.cursorapi.util.SendRequestUtil;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 * Package: cn.cactusli.cursorapi.controller
 * Description:
 *  聊天控制器
 *
 * @Author 仙人球⁶ᴳ |
 * @Date 2024/12/23 14:39
 * @Github
 */
@RequestMapping("/v1/chat")
@RestController
public class ChatController {

    @PostMapping("/completions")
    public Object chat(@RequestBody MessageObject messageObject) throws IOException {
       return SendRequestUtil.sendRequestApi(messageObject);
    }

}
