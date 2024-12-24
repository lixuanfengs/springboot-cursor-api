package cn.cactusli.cursorapi.util;

import cn.cactusli.cursorapi.dto.ChatCompletion;
import cn.cactusli.cursorapi.dto.ChatCompletionChunk;
import cn.cactusli.cursorapi.dto.ChatMessage;
import cn.cactusli.cursorapi.dto.MessageObject;
import cn.hutool.json.JSONUtil;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;


/**
 * Package: cn.cactusli.cursorapi.util
 * Description:
 * 发送请求工具类
 *
 * @Author 仙人球⁶ᴳ |
 * @Date 2024/12/23 15:05
 * @Github https://github.com/lixuanfengs
 */
public class SendRequestUtil {

    private static final String API_URL = "https://api2.cursor.sh/aiserver.v1.AiService/StreamChat";
    private static final int BUFFER_SIZE = 1024;

    private static final class Headers {
        static final String AUTHORIZATION = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJhdXRoMHx1c2VyXzAxSkZSTlFQTUNaS00xQjNOUkFUQ0s0NTVLIiwidGltZSI6IjE3MzQ5MjEyMjgiLCJyYW5kb21uZXNzIjoiMDI1NWE3YjYtZmQ0Ny00ZTQ2IiwiZXhwIjo0MzI2OTIxMjI4LCJpc3MiOiJodHRwczovL2F1dGhlbnRpY2F0aW9uLmN1cnNvci5zaCIsInNjb3BlIjoib3BlbmlkIHByb2ZpbGUgZW1haWwgb2ZmbGluZV9hY2Nlc3MiLCJhdWQiOiJodHRwczovL2N1cnNvci5jb20ifQ.iWY9bLE_v1yAWELq2VcG6ZZIZgmJwh1mXewuEQYtUT8";
        static final String CLIENT_KEY = "a496aa69313a4fb9238066e5f2c6229cb1fa78ee4ce753505b1f805a8212c7b8";
        static final String CLIENT_VERSION = "0.43.6";

    }

    public static Object sendRequestApi(MessageObject messageObject) throws IOException {
        byte[] requestData = createRequestData(messageObject);
        HttpResponse<InputStream> response;
        try {
            response = createAndSendHttpRequest(requestData);
            return processResponse(response, messageObject);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static Object processResponse(HttpResponse<InputStream> response, MessageObject messageObject) {
        try (InputStream is = response.body()) {
            if (messageObject.getStream()) {
                return processResponseStream(is, messageObject);
            } else {
                return processResponseText(is, messageObject);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static SseEmitter processResponseStream(InputStream inputStream, MessageObject messageObject) {
        SseEmitter emitter = new SseEmitter();
        try {
            List<byte[]> chunks = processResponseInputStream(inputStream);
            for (byte[] chunk : chunks) {
                String replaceText = MessageUtil.replaceText(chunk);
                ChatCompletionChunk chatCompletionChunk = MessageUtil.newChatCompletionChunk(messageObject, replaceText);
                emitter.send(JSONUtil.toJsonStr(chatCompletionChunk) + "\n\n", MediaType.APPLICATION_JSON);
            }
            emitter.send("data: [DONE]\n\n", MediaType.APPLICATION_JSON);
            emitter.complete();
        } catch (IOException e) {
            emitter.completeWithError(e);
        }
        return emitter;
    }

    private static ChatCompletion processResponseText(InputStream inputStream, MessageObject messageObject) throws IOException {
        StringBuilder responseText = new StringBuilder();
        List<byte[]> chunks = processResponseInputStream(inputStream);
        // 合并节数组数组
        List<byte[]> mergeByteArrays = MessageUtil.mergeByteArrays(chunks);
        if (mergeByteArrays != null) {
            for (byte[] bytes : mergeByteArrays) {
                //System.out.println("16位 Hex 字符串：" + MessageUtil.bytesToHex(bytes2));
                //System.out.println("明文字符串：" + new String(bytes2, StandardCharsets.UTF_8));
                responseText.append(MessageUtil.replaceText(bytes));
            }
        }
        return MessageUtil.newChatCompletion(messageObject, responseText.toString());
    }


    private static List<byte[]> processResponseInputStream(InputStream inputStream) throws IOException {
        List<byte[]> chunks = new ArrayList<>();
        byte[] chunk = new byte[BUFFER_SIZE];
        int bytesRead;
        while ((bytesRead = inputStream.read(chunk)) != -1) {
            // 创建新的字节数组只读取 bytesRead 长度字节数组
            byte[] newChunk = new byte[bytesRead];
            int newChunkIndex = 0;
            for (byte b : chunk) {
                if (newChunkIndex > bytesRead - 1) {
                    break;
                }
                newChunk[newChunkIndex] = b;
                newChunkIndex++;
            }
            if (newChunk.length == 1) {
                chunks.add(newChunk);
                continue;
            }
            // 只处理以 0x00 0x00 0x00 0x00 开头的 chunk，其他不处理，不然会有乱码
            if (!((newChunk[0] & 0xFF) == 0x00 && (newChunk[1] & 0xFF) == 0x00)) {
                continue;
            }
            // 去掉 chunk 中 0x0A 以及之前的字符
            int index = MessageUtil.indexOf(newChunk, (byte) 0x0A);
            if (index != -1) {
                newChunk = Arrays.copyOfRange(newChunk, index + 1, newChunk.length);
            }
            // 新的条件过滤：如果遇到连续4个0x00，则移除其之后所有的以 0 开头的字节（0x00 到 0x0F）
            List<Byte> processChunks = MessageUtil.processChunk(newChunk);
            newChunk = MessageUtil.toByteArray(processChunks);
            // 去除所有的 0x00 和 0x0A
            newChunk = MessageUtil.filterBytes(newChunk);
            // 去除小于 0x0A 的字节
            newChunk = MessageUtil.filterBytes2(newChunk);

            // 打印处理后的数据
            if (newChunk.length > 0) {
                chunks.add(newChunk);
            }
        }
        // 移除第一个字节数组（第一个字节数组是内置的提示词）
        chunks.remove(0);
        return chunks;
    }

    private static HttpResponse<InputStream> createAndSendHttpRequest(byte[] requestData) throws IOException, URISyntaxException, InterruptedException {
        String uuid = UUID.randomUUID().toString();
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(API_URL))
                .POST(HttpRequest.BodyPublishers.ofByteArray(requestData))
                .headers("authorization", Headers.AUTHORIZATION,
                        "content-type", "application/connect+proto",
                        "x-client-key", Headers.CLIENT_KEY,
                        "x-cursor-checksum", "3P8BHmtO615c184c124e7bb4103cf14c55e7345c871a91d6ee87025f7e807cf3ac6b3af6/45654b08705c4c80b0270586451971544d912a82be26a3679d596e4c975701c4ba",
                        "x-cursor-client-version", Headers.CLIENT_VERSION,
                        "x-request-id", uuid,
                        "x-amzn-trace-id", "Root=" + uuid,
                        "x-cursor-timezone", "Asia/Shanghai",
                        "x-ghost-mode", "false")
                .build();

        return client.send(request, HttpResponse.BodyHandlers.ofInputStream());
    }

    private static byte[] createRequestData(MessageObject messageText) {
        ChatMessage.MainMessage message = MessageUtil.buildChatMessage(messageText);
        byte[] messageBytes = message.toByteArray();
        String hexLength = String.format("%010x", messageBytes.length);
        String hexData = MessageUtil.bytesToHex(messageBytes);
        return MessageUtil.hexStringToByteArray(hexLength + hexData);
    }

}
