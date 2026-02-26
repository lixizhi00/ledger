package com.ledger.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * 微信小程序：用 getPhoneNumber 返回的 code 兑换用户手机号。
 * 需配置 app.wechat.appid 和 app.wechat.secret（小程序）。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WechatPhoneService {

    private static final String TOKEN_URL = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=%s&secret=%s";
    private static final String PHONE_URL = "https://api.weixin.qq.com/wxa/business/getuserphonenumber?access_token=%s";

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${app.wechat.appid:}")
    private String appid;

    @Value("${app.wechat.secret:}")
    private String secret;

    public boolean isConfigured() {
        return appid != null && !appid.isBlank() && secret != null && !secret.isBlank();
    }

    public String getPhoneByCode(String code) {
        if (!isConfigured()) {
            throw new IllegalStateException("未配置微信小程序 appid/secret，请手动输入手机号");
        }
        String token = getAccessToken();
        String url = String.format(PHONE_URL, token);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> body = new HttpEntity<>("{\"code\":\"" + code + "\"}", headers);
        ResponseEntity<String> resp = restTemplate.exchange(url, HttpMethod.POST, body, String.class);
        if (resp.getStatusCode() != HttpStatus.OK || resp.getBody() == null) {
            throw new IllegalArgumentException("微信接口异常，请稍后重试或手动输入手机号");
        }
        try {
            JsonNode root = objectMapper.readTree(resp.getBody());
            int errcode = root.path("errcode").asInt(0);
            if (errcode != 0) {
                String errmsg = root.path("errmsg").asText("");
                log.warn("WeChat getuserphonenumber errcode={} errmsg={}", errcode, errmsg);
                throw new IllegalArgumentException("获取手机号失败：" + errmsg + "，请手动输入手机号");
            }
            JsonNode info = root.path("phone_info");
            if (info.isMissingNode()) {
                throw new IllegalArgumentException("微信未返回手机号，请手动输入手机号");
            }
            String phone = info.path("purePhoneNumber").asText(null);
            if (phone == null || phone.isBlank()) {
                phone = info.path("phoneNumber").asText("");
            }
            if (phone.isBlank()) {
                throw new IllegalArgumentException("微信未返回手机号，请手动输入手机号");
            }
            return phone;
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("Parse WeChat phone response failed", e);
            throw new IllegalArgumentException("解析手机号失败，请手动输入手机号");
        }
    }

    private String getAccessToken() {
        String url = String.format(TOKEN_URL, appid, secret);
        ResponseEntity<String> resp = restTemplate.getForEntity(url, String.class);
        if (resp.getStatusCode() != HttpStatus.OK || resp.getBody() == null) {
            throw new IllegalStateException("获取微信 access_token 失败");
        }
        try {
            JsonNode root = objectMapper.readTree(resp.getBody());
            if (root.has("errcode") && root.get("errcode").asInt() != 0) {
                throw new IllegalStateException("微信返回错误: " + root.path("errmsg").asText(""));
            }
            String token = root.path("access_token").asText(null);
            if (token == null || token.isBlank()) {
                throw new IllegalStateException("微信未返回 access_token");
            }
            return token;
        } catch (Exception e) {
            if (e instanceof IllegalStateException) throw (IllegalStateException) e;
            throw new IllegalStateException("解析 access_token 失败", e);
        }
    }
}
