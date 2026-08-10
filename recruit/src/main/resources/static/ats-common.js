/**
 * ATS 前端公共工具（所有后台页面都应引入）
 *
 * 功能：
 * 1) 未登录自动跳 /login
 * 2) 封装 AJAX：自动带 Authorization 头、401 自动跳登录
 * 3) 提供登出函数、通用 UI 工具
 *
 * 用法：
 *   ATS.ajax('/api/xxx').then(r => { if (r.code === 0) ... });
 *   ATS.logout();
 */
(function (global) {
    'use strict';

    var TOKEN_KEY = 'ATS_TOKEN';
    var USER_KEY  = 'ATS_USER';

    function getToken() {
        var t = '';
        try { t = localStorage.getItem(TOKEN_KEY) || ''; } catch (e) {}
        if (!t) {
            // 从 Cookie 回退
            var pairs = document.cookie.split(';');
            for (var i = 0; i < pairs.length; i++) {
                var kv = pairs[i].trim();
                if (kv.indexOf(TOKEN_KEY + '=') === 0) {
                    t = decodeURIComponent(kv.substring(TOKEN_KEY.length + 1));
                    try { localStorage.setItem(TOKEN_KEY, t); } catch (e) {}
                    break;
                }
            }
        }
        return t;
    }

    function requireLogin() {
        try { localStorage.removeItem(TOKEN_KEY); localStorage.removeItem(USER_KEY); } catch (e) {}
        window.location.replace('/login');
    }

    /**
     * 统一 AJAX 封装，内部已拼 code / data / message
     * @param url
     * @param opt {method, body, headers, rawBody}
     */
    function ajax(url, opt) {
        opt = opt || {};
        var headers = opt.headers ? JSON.parse(JSON.stringify(opt.headers)) : {};
        if (opt.body && !opt.rawBody && !(opt.body instanceof FormData)) {
            headers['Content-Type'] = 'application/json';
        }
        var token = getToken();
        if (token) {
            headers['Authorization'] = 'Bearer ' + token;
        }
        return fetch(url, {
            method: (opt.method || 'GET').toUpperCase(),
            headers: headers,
            body: opt.body ? (headers['Content-Type'] === 'application/json' && !(opt.body instanceof FormData)
                ? JSON.stringify(opt.body) : opt.body) : undefined,
            credentials: 'same-origin'
        }).then(function (resp) {
            if (resp.status === 401) {
                requireLogin();
                throw new Error('401 Unauthorized');
            }
            return resp.json().then(function (json) {
                if (json && (json.code === 401 || json.status === 401)) {
                    requireLogin();
                    throw new Error(json.message || '未登录');
                }
                return json;
            }, function () {
                return { code: -1, message: '响应解析失败' };
            });
        }).catch(function (err) {
            if (err && err.message && err.message.indexOf('401') >= 0) throw err;
            return { code: -1, message: (err && err.message) || '网络异常' };
        });
    }

    function logout() {
        ajax('/api/auth/logout', { method: 'POST' }).finally(function () {
            try { localStorage.removeItem(TOKEN_KEY); localStorage.removeItem(USER_KEY); } catch (e) {}
            window.location.replace('/login');
        });
    }

    function escapeHtml(s) {
        return String(s == null ? '' : s)
            .replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;').replace(/'/g, '&#39;');
    }

    function fmtTime(s) {
        if (!s) return '-';
        return String(s).substring(5, 10) + ' ' + String(s).substring(11, 16);
    }

    // 暴露
    global.ATS = {
        TOKEN_KEY: TOKEN_KEY,
        USER_KEY: USER_KEY,
        getToken: getToken,
        requireLogin: requireLogin,
        ajax: ajax,
        logout: logout,
        escapeHtml: escapeHtml,
        fmtTime: fmtTime
    };
})(window);
