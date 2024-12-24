package cn.cactusli.cursorapi.dto;

import java.util.List;

/**
 * Package: cn.cactusli.cursorapi.dto
 * Description:
 *
 * @Author 仙人球⁶ᴳ |
 * @Date 2024/12/24 15:56
 * @Github https://github.com/lixuanfengs
 */
public class ChatCompletionChunk {

    private String id;
    private String object;
    private long created;
    private String model;
    private List<Choice> choices;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getObject() {
        return object;
    }

    public void setObject(String object) {
        this.object = object;
    }

    public long getCreated() {
        return created;
    }

    public void setCreated(long created) {
        this.created = created;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public List<Choice> getChoices() {
        return choices;
    }

    public void setChoices(List<Choice> choices) {
        this.choices = choices;
    }


    public static class Choice {
        private int index;
        private Delta delta;

        public int getIndex() {
            return index;
        }

        public void setIndex(int index) {
            this.index = index;
        }

        public Delta getDelta() {
            return delta;
        }

        public void setDelta(Delta delta) {
            this.delta = delta;
        }
    }

    public static class Delta {
        private String role;
        private String content;

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


}
