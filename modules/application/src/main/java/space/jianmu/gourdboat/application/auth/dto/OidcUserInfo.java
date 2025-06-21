package space.jianmu.gourdboat.application.auth.dto;

import lombok.Value;
import lombok.Builder;

/**
 * OIDC用户信息
 */
@Value
@Builder
public class OidcUserInfo {
    String sub;                 // 用户唯一标识
    String name;                // 姓名
    String givenName;           // 名
    String familyName;          // 姓
    String nickname;            // 昵称
    String picture;             // 头像URL
    String email;               // 邮箱
    String phoneNumber;         // 手机号
    String gender;              // 性别
    String locale;              // 地区
    String zoneinfo;            // 时区
    Long updatedAt;             // 更新时间
    String provider;            // 服务商
    String openId;              // 开放ID（微信等特有）
    String unionId;             // 联合ID（微信等特有）
} 