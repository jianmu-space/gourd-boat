# OIDC 静态前端样例

本目录用于演示 OIDC 登录交互的前端静态页面。

## 目录结构

```
oidc-static-sample/
├── static/
│   ├── index.html
│   ├── oidc-demo.js
│   └── style.css
├── flask_app.py
└── README.md
```

## 启动本地 HTTP 服务（推荐 Flask）

1. 安装 Flask（如未安装）：
   ```bash
   pip install flask
   ```
2. 启动服务：
   ```bash
   python flask_app.py
   ```
3. 访问首页：
   ```
   http://localhost:9090/
   ```
   首页会自动加载 static/index.html，其他静态资源也会自动加载。

## 说明
- 微信服务器验证接口为 `/wechat`，用于公众平台服务器配置。
- 所有静态前端资源请放在 `static/` 子目录下。
- 如需更换端口，请修改 `flask_app.py` 中的端口配置。 