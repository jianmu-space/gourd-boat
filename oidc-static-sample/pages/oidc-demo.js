function logDebug(msg) {
  const debugDiv = document.getElementById('debug');
  const now = new Date().toLocaleTimeString();
  debugDiv.innerText += `[${now}] ${msg}\n`;
}

const provider = 'WECHAT_MP';
const configId = 'wechat_mp_iceee';
const state = '117788';
const redirectUri = encodeURIComponent('http://110280mczq018.vicp.fun/api/oidc/callback/WECHAT_MP?configId=wechat_mp_iceee');

function getQueryParam(name) {
  const url = new URL(window.location.href);
  return url.searchParams.get(name);
}

window.onload = function() {
  logDebug('页面加载');
  const code = getQueryParam('code');
  const stateParam = getQueryParam('state');
  logDebug('code: ' + code + ', state: ' + stateParam);
  if (code && stateParam) {
    document.getElementById('result').innerHTML = '正在获取用户信息...';
    logDebug('检测到 code 和 state，开始请求后端 /api/oidc/callback');
    fetch(`/api/oidc/callback/WECHAT_MP?configId=${configId}&code=${code}&state=${state}&redirectUri=${redirectUri}`)
      .then(res => {
        logDebug('收到 /api/oidc/callback 响应');
        return res.json();
      })
      .then(data => {
        document.getElementById('result').innerHTML = '登录成功！<br><pre>' + JSON.stringify(data, null, 2) + '</pre>';
        logDebug('登录成功，用户信息已展示');
      })
      .catch(err => {
        document.getElementById('result').innerHTML = '登录失败：' + err;
        logDebug('登录失败：' + err);
      });
  }
};

document.getElementById('loginBtn').onclick = function() {
  document.getElementById('result').innerHTML = '正在获取微信授权地址...';
  logDebug('点击登录按钮，开始请求 /api/oidc/auth');
  fetch(`/api/oidc/auth/WECHAT_MP?configId=${configId}&state=${state}&redirectUri=${redirectUri}`)
    .then(res => {
      logDebug('收到 /api/oidc/auth 响应');
      return res.json();
    })
    .then(data => {
      if (data.authUrl) {
        logDebug('获取到授权地址，跳转到微信授权页');
        logDebug(data.authUrl);
        window.location.href = data.authUrl;
      } else {
        document.getElementById('result').innerHTML = '获取授权地址失败';
        logDebug('获取授权地址失败');
      }
    })
    .catch(err => {
      document.getElementById('result').innerHTML = '请求失败：' + err;
      logDebug('请求失败：' + err);
    });
}; 