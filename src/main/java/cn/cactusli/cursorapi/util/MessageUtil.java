package cn.cactusli.cursorapi.util;

import cn.cactusli.cursorapi.dto.ChatCompletion;
import cn.cactusli.cursorapi.dto.ChatCompletionChunk;
import cn.cactusli.cursorapi.dto.ChatMessage;
import cn.cactusli.cursorapi.dto.MessageObject;
import com.google.protobuf.ByteString;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Package: cn.cactusli.cursorapi.util
 * Description:
 *  消息工具类
 *
 * @Author 仙人球⁶ᴳ |
 * @Date 2024/12/23 15:00
 * @Github https://github.com/lixuanfengs
 */
public class MessageUtil {

    private static final String LANGUAGE = "Always respond in 中文";

    /**
     * 构建 ChatMessage.MainMessage 对象
     * @param messageObject
     * @return
     */
    public static ChatMessage.MainMessage buildChatMessage(MessageObject messageObject) {
        LinkedList<MessageObject.Messages> messages = messageObject.getMessages();
        List<ChatMessage.NestedMessage1> nestedMessage1List = new LinkedList<>();
        if (messages != null && !messages.isEmpty()) {
            for (MessageObject.Messages message : messages) {
                nestedMessage1List.add(ChatMessage.NestedMessage1.newBuilder()
                        .setField1(message.getContent())
                        .setField2(1)
                        .setField13(UUID.randomUUID().toString())
                        .build());
            }
        }
        return ChatMessage.MainMessage.newBuilder()
                .addAllField2(nestedMessage1List)
                .setField4(ChatMessage.NestedMessage2.newBuilder()
                        .setField1(LANGUAGE)
                        .build())
                .setField7(ChatMessage.NestedMessage3.newBuilder()
                        .setField1(messageObject.getModel())
                        .setField4(ByteString.EMPTY)
                        .build())
                .setField13(1)
                .setField15(UUID.randomUUID().toString())
                .setField16(1)
                .setField29(1)
                .build();
    }


    /**
     * 合并列表中长度为1的字节数组与其前一个数组
     *
     * @param byteArrays 待处理的字节数组列表
     * @return 处理后的字节数组列表，如果没有需要合并的数组则返回null
     * @throws IllegalArgumentException 如果输入列表为空或null
     */
    public static List<byte[]> mergeByteArrays(List<byte[]> byteArrays) {
        // 参数校验
        if (byteArrays == null || byteArrays.isEmpty()) {
            throw new IllegalArgumentException("Input list cannot be null or empty");
        }

        // 从索引1开始遍历，因为需要访问前一个元素
        for (int i = 1; i < byteArrays.size(); i++) {
            byte[] currentArray = byteArrays.get(i);

            if (currentArray != null && currentArray.length == 1) {
                byte[] previousArray = byteArrays.get(i - 1);

                // 确保前一个数组不为null
                if (previousArray == null) {
                    continue;
                }

                // 创建新数组并合并
                byte[] mergedArray = new byte[previousArray.length + 1];
                System.arraycopy(previousArray, 0, mergedArray, 0, previousArray.length);
                mergedArray[mergedArray.length - 1] = currentArray[0];

                // 更新列表
                byteArrays.set(i - 1, mergedArray);
                byteArrays.remove(i);
            }
        }
        return byteArrays;
    }

    // 工具方法：将字节数组转换为 16 进制字符串
    public static String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b)); // 每个字节转为两位 16 进制
        }
        return result.toString();
    }


    // 将 Hex 字符串转换为字节数组
    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    // 辅助方法：将 List<Byte> 转换为 byte[]
    public static byte[] toByteArray(List<Byte> byteList) {
        byte[] byteArray = new byte[byteList.size()];
        for (int i = 0; i < byteList.size(); i++) {
            byteArray[i] = byteList.get(i);
        }
        return byteArray;
    }

    // 辅助方法：在字节数组中查找某个字节的索引位置
    public static int indexOf(byte[] array, byte value) {
        for (int i = 0; i < array.length; i++) {
            if ((array[i] & 0xFF) == value) {
                return i;
            }
        }
        return -1; // 未找到
    }


    public static byte[] filterBytes(byte[] chunk) {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        for (byte b : chunk) {
            // 跳过 0x0C 字节
            if ((b & 0xFF) != 0x00 && (b & 0xFF) != 0x0C) {
                result.write(b);
            }
        }
        return result.toByteArray();
    }


    public static byte[] filterBytes2(byte[] chunk) {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        for (byte b : chunk) {
            // 跳过 0x0C 字节
            if ((b & 0xFF) >= 0x0A) {
                result.write(b);
            }
        }
        return result.toByteArray();
    }

    /**
     * 过滤字节数组：去除无效字节
     *
     * @param chunk
     * @return
     */
    public static List<Byte> processChunk(byte[] chunk) {
        List<Byte> filteredChunk = new ArrayList<>();
        int i = 0;

        while (i < chunk.length) {
            // 新的条件逻辑：如果遇到连续4个0x00，则移除其之后所有前缀为 0 开头的字节（0x00 到 0x0F）
            if (hasConsecutiveZeros(chunk, i)) {
                i += 4; // 跳过这4个0x00
                while (i < chunk.length && (chunk[i] & 0xFF) >= 0x00 && (chunk[i] & 0xFF) <= 0x0F) {
                    i++; // 跳过所有以 0 开头的字节
                }
                continue;
            }

            if ((chunk[i] & 0xFF) == 0x0C) {
                i++; // 跳过 0x0C
                while (i < chunk.length && (chunk[i] & 0xFF) == 0x0A) {
                    i++; // 跳过所有连续的 0x0A
                }
            } else if (i > 0 &&
                    (chunk[i] & 0xFF) == 0x0A &&
                    (chunk[i - 1] & 0xFF) >= 0x00 &&
                    (chunk[i - 1] & 0xFF) <= 0x09) {
                filteredChunk.remove(filteredChunk.size() - 1);
                i++;
            } else {
                filteredChunk.add(chunk[i]);
                i++;
            }
        }

        return filteredChunk;
    }

    /**
     * 判断字节数组中是否有连续的4个0x00
     *
     * @param chunk      字节数组
     * @param startIndex 起始索引
     * @return
     */
    public static boolean hasConsecutiveZeros(byte[] chunk, int startIndex) {
        if (startIndex + 3 >= chunk.length) {
            return false;
        }
        for (int j = 0; j < 4; j++) {
            if ((chunk[startIndex + j] & 0xFF) != 0x00) {
                return false;
            }
        }
        return true;
    }


    // 生成随机ID的方法
    public static String getRandomIDPro(int size, String dictType, String customDict) {
        String random = "";
        // 如果没有提供自定义字典，使用默认字典
        if (customDict == null || customDict.isEmpty()) {
            switch (dictType) {
                case "alphabet":
                    customDict = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
                    break;
                case "max":
                    customDict = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ_-";
                    break;
                default:
                    customDict = "0123456789";
            }
        }

        // 创建一个Random对象用于生成随机数
        Random randomGenerator = new Random();
        // 生成随机ID
        for (int i = 0; i < size; i++) {
            int index = randomGenerator.nextInt(customDict.length());
            random += customDict.charAt(index);
        }

        return random;
    }


    /**
     * 将字节数组转换为字符串, 并替换掉无效字符
     * @param bytes
     * @return
     */
    public static String replaceText(byte[] bytes) {
        String text = new String(bytes, StandardCharsets.UTF_8);
        String textr1 = text.replaceAll("(?s)^.*<\\|END_USER\\|>", "");
        String textr2 = textr1.replaceFirst("^\\n[a-zA-Z]?", "").trim();
        return textr2;
    }

    /**
     * 创建 ChatCompletion 对象
     * @param messageObject
     * @param responseText
     * @return
     */
    public static ChatCompletion newChatCompletion(MessageObject messageObject, String responseText) {
        ChatCompletion chatCompletion = new ChatCompletion();

        chatCompletion.setId("chatcmpl-" + UUID.randomUUID());
        chatCompletion.setObject("chat.completion");
        chatCompletion.setCreated(System.currentTimeMillis());
        chatCompletion.setModel(messageObject.getModel());

        List<ChatCompletion.Choice> choices = new LinkedList<>();
        ChatCompletion.Choice choice = new ChatCompletion.Choice();
        choice.setIndex(0);

        ChatCompletion.Message message = new ChatCompletion.Message();
        message.setRole("assistant");
        message.setContent(responseText);
        choice.setMessage(message);
        choice.setFinish_reason("stop");
        choices.add(choice);

        chatCompletion.setChoices(choices);

        ChatCompletion.Usage usage = new ChatCompletion.Usage();
        usage.setPrompt_tokens(0);
        usage.setCompletion_tokens(0);
        usage.setTotal_tokens(0);

        chatCompletion.setUsage(usage);
        return chatCompletion;
    }


    /**
     * 创建 ChatCompletionChunk 对象
     * @param messageObject
     * @param responseText
     * @return
     */
    public static ChatCompletionChunk newChatCompletionChunk(MessageObject messageObject, String responseText) {

        ChatCompletionChunk chatCompletionChunk = new ChatCompletionChunk();

        chatCompletionChunk.setId("chatcmpl-" + UUID.randomUUID());
        chatCompletionChunk.setObject("chat.completion.chunk");
        chatCompletionChunk.setCreated(System.currentTimeMillis());
        chatCompletionChunk.setModel(messageObject.getModel());

        List<ChatCompletionChunk.Choice> choices = new LinkedList<>();
        ChatCompletionChunk.Choice choice = new ChatCompletionChunk.Choice();
        choice.setIndex(0);

        ChatCompletionChunk.Delta delta = new ChatCompletionChunk.Delta();
        delta.setRole("assistant");
        delta.setContent(responseText);

        choice.setDelta(delta);

        choices.add(choice);

        chatCompletionChunk.setChoices(choices);

        return chatCompletionChunk;
    }

}
