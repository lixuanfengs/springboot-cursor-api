package cn.cactusli.cursorapi.dto;

import lombok.Data;

import java.util.LinkedList;

/**
 * Package: cn.cactusli.cursorapi.dto
 * Description:
 * 消息对象
 *
 * @Author 仙人球⁶ᴳ |
 * @Date 2024/12/23 14:58
 * @Github https://github.com/lixuanfengs
 */
public class MessageObject {

    private LinkedList<Messages> messages;
    private String model;
    private Integer max_tokens;
    private Boolean stream;

    public static class Messages {
        private String role;
        private String content;

        public Messages() {
        }
        public Messages(String role, String content) {
            this.role = role;
            this.content = content;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }

    public MessageObject() {
    }
    public MessageObject(String model, Integer max_tokens, Boolean stream, LinkedList<Messages> messages) {
        this.model = model;
        this.max_tokens = max_tokens;
        this.stream = stream;
        this.messages = messages;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public Integer getMax_tokens() {
        return max_tokens;
    }

    public void setMax_tokens(Integer max_tokens) {
        this.max_tokens = max_tokens;
    }

    public LinkedList<Messages> getMessages() {
        return messages;
    }

    public void setMessages(LinkedList<Messages> messages) {
        this.messages = messages;
    }

    public Boolean getStream() {
        return stream;
    }

    public void setStream(Boolean stream) {
        this.stream = stream;
    }
}
