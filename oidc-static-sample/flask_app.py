from flask import Flask, request, make_response, send_from_directory
import hashlib
import os

app = Flask(__name__, static_folder='pages', static_url_path='/pages')

# 配置信息 - 在微信公众平台设置的相同Token
WECHAT_TOKEN = "iceee_wechat_verify_token"  # 替换为你在微信后台设置的Token

@app.route('/wechat', methods=['GET'])
def wechat_verify():
    print("收到 /wechat 请求")
    """微信服务器验证接口"""
    signature = request.args.get('signature', '')
    timestamp = request.args.get('timestamp', '')
    nonce = request.args.get('nonce', '')
    echostr = request.args.get('echostr', '')
    if not all([signature, timestamp, nonce, echostr]):
        return 'Missing verification parameters', 400
    if verify_signature(signature, timestamp, nonce):
        response = make_response(echostr)
        response.content_type = 'text/plain'
        return response
    else:
        return 'Verification failed', 403

def verify_signature(signature, timestamp, nonce):
    tmp_list = sorted([WECHAT_TOKEN, timestamp, nonce])
    tmp_str = ''.join(tmp_list)
    hash_str = hashlib.sha1(tmp_str.encode('utf-8')).hexdigest()
    return hash_str == signature

# 主页路由，返回 static/index.html
@app.route('/')
def index():
    return send_from_directory(app.static_folder, 'index.html')

# 其他静态资源自动处理（如 oidc-demo.js, style.css 等）
# Flask 已自动处理，无需额外代码

if __name__ == '__main__':
    print(app.url_map)
    port = int(os.environ.get('PORT', 9090))
    app.run(host='0.0.0.0', port=port, debug=True) 