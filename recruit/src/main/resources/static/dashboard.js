(function() {
    'use strict';

    var state = {
        user: null, role: null, activeTab: 'dashboard', pageSize: 10,
        pages: { users: 1, companies: 1, positions: 1, applications: 1 },
        search: { users: '', companies: '', positions: '', applications: '' },
        filters: { positions: '', applications: '' },
        charts: {}, positionMap: {}
    };

    var MENU_CONFIG = {
        ADMIN: [
            { key: 'dashboard', icon: '📊', label: '数据看板' },
            { key: 'users', icon: '👤', label: '用户管理' },
            { key: 'companies', icon: '🏢', label: '企业管理' },
            { key: 'positions', icon: '💼', label: '岗位管理' },
            { key: 'applications', icon: '📨', label: '投递审核' },
            { key: 'rules', icon: '⚙️', label: '筛选规则' },
            { key: 'settings', icon: '🔧', label: '系统设置' }
        ],
        HR: [
            { key: 'dashboard', icon: '🏠', label: '工作台' },
            { key: 'positions', icon: '💼', label: '我的岗位' },
            { key: 'applications', icon: '📨', label: '简历投递' },
            { key: 'rules', icon: '⚙️', label: '筛选规则' },
            { key: 'company-info', icon: '🏢', label: '企业信息' }
        ]
    };

    var STATUS_MAP = {
        PENDING: { cls: 'tag-pending', text: '⏳ 待审核' },
        REVIEWED: { cls: 'tag-reviewed', text: '👁️ 已查看' },
        ACCEPTED: { cls: 'tag-open', text: '✅ 已通过' },
        REJECTED: { cls: 'tag-closed', text: '❌ 已拒绝' },
        INTERVIEWING: { cls: 'tag-reviewed', text: '💬 面试中' },
        OFFER: { cls: 'tag-warning', text: '📨 已发Offer' },
        SUBMITTED: { cls: 'tag-pending', text: '📤 已投递' },
        SCREENING_PASS: { cls: 'tag-reviewed', text: '🔍 筛选中' },
        INTERVIEWED: { cls: 'tag-info', text: '🗣️ 已面试' }
    };

    var POS_STATUS = {
        OPEN: { cls: 'tag-open', text: '🔥 招聘中' },
        CLOSED: { cls: 'tag-closed', text: '已关闭' }
    };

    var SIZE_MAP = { 1: '初创(1-20)', 2: '中小(20-99)', 3: '中型(100-499)', 4: '大型(500+)' };

    window.__editUser = editUser;
    window.__deleteUser = deleteUser;
    window.__resetPwd = resetPassword;
    window.__editCompany = editCompany;
    window.__deleteCompany = deleteCompany;
    window.__editPosition = editPosition;
    window.__deletePosition = deletePosition;
    window.__editApplication = editApplication;
    window.__deleteApplication = deleteApplication;
    window.__updateAppStatus = updateAppStatus;
    window.__editRule = editRule;
    window.__deleteRule = deleteRule;
    window.__toggleRule = toggleRule;

    document.addEventListener('DOMContentLoaded', init);

    function init() {
        var userStr = null;
        try { userStr = localStorage.getItem('ATS_USER'); } catch(e) {}
        if (!userStr) { ATS.requireLogin(); return; }
        try {
            state.user = JSON.parse(userStr);
            state.role = state.user.role || 'ADMIN';
        } catch(e) { ATS.requireLogin(); return; }
        if (state.role === 'CANDIDATE') { window.location.replace('/portal'); return; }
        buildSidebar();
        updateHeader();
        bindEvents();
        switchTab('dashboard');
    }

    function buildSidebar() {
        var menu = MENU_CONFIG[state.role] || MENU_CONFIG.ADMIN;
        var c = document.getElementById('menuItems');
        c.innerHTML = '';
        menu.forEach(function(item) {
            var el = document.createElement('div');
            el.className = 'menu-item';
            el.dataset.key = item.key;
            el.innerHTML = '<span class="menu-icon">' + item.icon + '</span><span>' + item.label + '</span>';
            el.addEventListener('click', function() { switchTab(item.key); });
            c.appendChild(el);
        });
        var tag = document.getElementById('sidebarRoleTag');
        tag.style.display = 'block';
        document.getElementById('sidebarRoleValue').textContent = state.role === 'ADMIN' ? '系统管理员' : '企业HR';
    }

    function updateHeader() {
        var badge = document.getElementById('roleBadge');
        badge.textContent = state.role === 'ADMIN' ? '🛡️ ADMIN' : '💼 HR';
        badge.className = 'role-badge ' + (state.role === 'ADMIN' ? 'role-admin' : 'role-hr');
        var name = state.user.realName || state.user.username || '用户';
        document.getElementById('userName').textContent = name;
        document.getElementById('userAvatar').textContent = name.charAt(0);
        if (state.role === 'HR' && state.user.company) {
            var tag = document.getElementById('companyTag');
            tag.textContent = '🏢 ' + state.user.company;
            tag.style.display = 'inline-flex';
        }
        document.getElementById('todayDate').textContent = '📅 ' + new Date().toISOString().substring(0, 10);
    }

    function bindEvents() {
        document.getElementById('logoutBtn').addEventListener('click', function() { ATS.logout(); });
        document.getElementById('modalClose').addEventListener('click', closeModal);
        document.getElementById('modalOverlay').addEventListener('click', function(e) {
            if (e.target.id === 'modalOverlay') closeModal();
        });
        if (state.role === 'ADMIN') {
            document.getElementById('addUserBtn').addEventListener('click', openUserForm);
            document.getElementById('userSearch').addEventListener('input', debounce(function(e) {
                state.search.users = e.target.value; state.pages.users = 1; loadUsers();
            }, 300));
            document.getElementById('addCompanyBtn').addEventListener('click', openCompanyForm);
            document.getElementById('companySearch').addEventListener('input', debounce(function(e) {
                state.search.companies = e.target.value; state.pages.companies = 1; loadCompanies();
            }, 300));
            document.getElementById('addPositionBtn').addEventListener('click', openPositionForm);
            document.getElementById('positionSearch').addEventListener('input', debounce(function(e) {
                state.search.positions = e.target.value; state.pages.positions = 1; loadPositions();
            }, 300));
            document.getElementById('positionStatusFilter').addEventListener('change', function(e) {
                state.filters.positions = e.target.value; state.pages.positions = 1; loadPositions();
            });
            document.getElementById('applicationSearch').addEventListener('input', debounce(function(e) {
                state.search.applications = e.target.value; state.pages.applications = 1; loadApplications();
            }, 300));
            document.getElementById('applicationStatusFilter').addEventListener('change', function(e) {
                state.filters.applications = e.target.value; state.pages.applications = 1; loadApplications();
            });
            document.getElementById('addRuleBtn').addEventListener('click', openRuleForm);
            document.getElementById('cleanupBtn').addEventListener('click', function() {
                confirmAction('确定要清理已删除数据吗？此操作不可恢复。', function() { showToast('清理任务已提交', 'success'); });
            });
            document.getElementById('exportBtn').addEventListener('click', function() { showToast('数据导出中，请稍候...', 'info'); });
        } else if (state.role === 'HR') {
            document.getElementById('addPositionBtn').addEventListener('click', openPositionForm);
            document.getElementById('positionSearch').addEventListener('input', debounce(function(e) {
                state.search.positions = e.target.value; state.pages.positions = 1; loadPositions();
            }, 300));
            document.getElementById('positionStatusFilter').addEventListener('change', function(e) {
                state.filters.positions = e.target.value; state.pages.positions = 1; loadPositions();
            });
            document.getElementById('applicationSearch').addEventListener('input', debounce(function(e) {
                state.search.applications = e.target.value; state.pages.applications = 1; loadApplications();
            }, 300));
            document.getElementById('applicationStatusFilter').addEventListener('change', function(e) {
                state.filters.applications = e.target.value; state.pages.applications = 1; loadApplications();
            });
            document.getElementById('addRuleBtn').addEventListener('click', openRuleForm);
        }
    }

    function switchTab(key) {
        state.activeTab = key;
        document.querySelectorAll('.menu-item').forEach(function(el) {
            el.classList.toggle('active', el.dataset.key === key);
        });
        document.querySelectorAll('.tab-content').forEach(function(el) {
            el.classList.toggle('active', el.id === 'tab-' + key);
        });
        var labels = {
            'dashboard': '数据看板', 'users': '用户管理', 'companies': '企业管理',
            'positions': state.role === 'HR' ? '我的岗位' : '岗位管理',
            'applications': state.role === 'HR' ? '简历投递' : '投递审核',
            'rules': '筛选规则', 'company-info': '企业信息', 'settings': '系统设置'
        };
        document.getElementById('breadcrumbPage').textContent = labels[key] || '';
        loadTab(key);
    }

    function loadTab(key) {
        switch (key) {
            case 'dashboard': loadDashboard(); break;
            case 'users': loadUsers(); break;
            case 'companies': loadCompanies(); break;
            case 'positions': loadPositions(); break;
            case 'applications': loadApplications(); break;
            case 'rules': loadRules(); break;
            case 'company-info': loadCompanyInfo(); break;
            case 'settings': loadSettings(); break;
        }
    }

    function loadDashboard() {
        if (state.role === 'ADMIN') loadAdminStats();
        else loadHrStats();
    }

    function loadAdminStats() {
        document.getElementById('welcomeTitle').textContent = '欢迎回来，管理员！';
        document.getElementById('welcomeSub').textContent = '这是您的招聘数据概览';
        document.getElementById('statUsers').style.display = 'flex';
        document.getElementById('statPending').style.display = 'flex';
        Promise.all([
            ATS.ajax('/api/users?current=1&size=1'),
            ATS.ajax('/api/companies?current=1&size=1'),
            ATS.ajax('/api/positions?current=1&size=1'),
            ATS.ajax('/api/applications?current=1&size=500')
        ]).then(function(results) {
            var u = results[0].data, c = results[1].data, p = results[2].data, a = results[3].data;
            document.getElementById('statUsersValue').textContent = u.total || 0;
            document.getElementById('statCompaniesValue').textContent = c.total || 0;
            document.getElementById('statPositionsValue').textContent = p.total || 0;
            document.getElementById('statApplicationsValue').textContent = a.total || 0;
            var pending = 0;
            (a.records || []).forEach(function(x) { if (x.status === 'PENDING') pending++; });
            document.getElementById('statPendingValue').textContent = pending;
            buildPositionMap().then(function() {
                loadRecentPositions();
                loadRecentApplications();
                loadPositionChart(a.records || []);
            });
        }).catch(function() { showToast('加载统计数据失败', 'error'); });
    }

    function loadHrStats() {
        document.getElementById('welcomeTitle').textContent = '欢迎回来，' + (state.user.realName || state.user.username) + '！';
        document.getElementById('welcomeSub').textContent = '以下是您公司的招聘数据';
        document.getElementById('statUsers').style.display = 'none';
        document.getElementById('statPending').style.display = 'none';
        buildPositionMap().then(function() {
            return ATS.ajax('/api/applications?current=1&size=500');
        }).then(function(appRes) {
            var allApps = appRes.data.records || [];
            var myPositions = state.user.companyId
                ? Object.values(state.positionMap).filter(function(p) { return p.companyId === state.user.companyId; })
                : Object.values(state.positionMap);
            var myPosIds = {};
            myPositions.forEach(function(p) { myPosIds[p.id] = true; });
            var myApps = allApps.filter(function(a) { return myPosIds[a.positionId]; });
            document.getElementById('statPositionsValue').textContent = myPositions.length;
            document.getElementById('statPositionsLabel').textContent = (state.user.company || '') + ' 岗位';
            document.getElementById('statPositionsSub').textContent = '本公司所有岗位';
            document.getElementById('statApplicationsValue').textContent = myApps.length;
            document.getElementById('statApplicationsLabel').textContent = (state.user.company || '') + ' 投递';
            document.getElementById('statApplicationsSub').textContent = '本公司岗位投递';
            return ATS.ajax('/api/companies/all');
        }).then(function(compRes) {
            var cs = compRes.data || [];
            document.getElementById('statCompaniesValue').textContent = cs.length;
            document.getElementById('statCompaniesLabel').textContent = '合作企业';
            document.getElementById('statCompaniesSub').textContent = '平台注册企业';
            loadRecentPositions();
            loadRecentApplications();
            ATS.ajax('/api/applications?current=1&size=500').then(function(res) {
                loadPositionChart(res.data.records || []);
            });
        }).catch(function() { showToast('加载数据失败', 'error'); });
    }

    function buildPositionMap() {
        return ATS.ajax('/api/positions?current=1&size=500').then(function(res) {
            var map = {};
            (res.data.records || []).forEach(function(p) { map[p.id] = p; });
            state.positionMap = map;
            return map;
        });
    }

    function loadRecentPositions() {
        ATS.ajax('/api/positions?current=1&size=6').then(function(res) {
            var list = res.data.records || [];
            if (state.role === 'HR' && state.user.companyId) {
                list = list.filter(function(p) { return p.companyId === state.user.companyId; });
            }
            var html = '';
            if (!list.length) {
                html = '<div class="empty"><span class="empty-icon">📭</span><div class="empty-text">暂无职位数据</div></div>';
            } else {
                html = '<table><thead><tr><th>职位</th><th>部门</th><th>状态</th><th>创建时间</th></tr></thead><tbody>';
                list.forEach(function(p) {
                    var s = POS_STATUS[p.status] || { cls: 'tag-info', text: p.status };
                    var t = p.createdAt ? p.createdAt.substring(5, 10) + ' ' + p.createdAt.substring(11, 16) : '-';
                    html += '<tr><td style="font-weight:500;">' + ATS.escapeHtml(p.title) + '</td>'
                        + '<td><span class="dept-chip">' + ATS.escapeHtml(p.department || '-') + '</span></td>'
                        + '<td><span class="status-tag ' + s.cls + '">' + s.text + '</span></td>'
                        + '<td>' + t + '</td></tr>';
                });
                html += '</tbody></table>';
            }
            document.getElementById('recentPositionsContent').innerHTML = html;
        });
    }

    function loadRecentApplications() {
        ATS.ajax('/api/applications?current=1&size=5').then(function(res) {
            var list = res.data.records || [];
            if (state.role === 'HR' && state.user.companyId) {
                list = list.filter(function(a) {
                    var p = state.positionMap[a.positionId];
                    return p && p.companyId === state.user.companyId;
                });
            }
            var html = '';
            if (!list.length) {
                html = '<div class="empty"><span class="empty-icon">📭</span><div class="empty-text">暂无投递记录</div></div>';
            } else {
                html = '<table><thead><tr><th>投递ID</th><th>岗位</th><th>状态</th></tr></thead><tbody>';
                list.forEach(function(a) {
                    var s = STATUS_MAP[a.status] || { cls: 'tag-info', text: a.status };
                    var pt = (state.positionMap[a.positionId] && state.positionMap[a.positionId].title) || ('职位#' + a.positionId);
                    html += '<tr><td style="font-weight:500;">#' + a.id + '</td>'
                        + '<td>' + ATS.escapeHtml(pt) + '</td>'
                        + '<td><span class="status-tag ' + s.cls + '">' + s.text + '</span></td></tr>';
                });
                html += '</tbody></table>';
            }
            document.getElementById('recentAppsContent').innerHTML = html;
        });
    }

    function loadPositionChart(apps) {
        apps = apps || [];
        var counter = {};
        apps.forEach(function(a) {
            var title = (state.positionMap[a.positionId] && state.positionMap[a.positionId].title) || ('职位#' + a.positionId);
            counter[title] = (counter[title] || 0) + 1;
        });
        var labels = Object.keys(counter);
        var data = Object.values(counter);
        var palette = ['#409eff','#67c23a','#e6a23c','#f56c6c','#909399','#79bbff','#95d475','#f3d19e','#fab6b6','#c8c9cc'];
        var el = document.getElementById('positionPieChart');
        if (!el) return;
        if (state.charts.pie) state.charts.pie.destroy();
        state.charts.pie = new Chart(el, {
            type: 'doughnut',
            data: { labels: labels.length ? labels : ['暂无数据'], datasets: [{
                data: data.length ? data : [1],
                backgroundColor: data.length ? palette.slice(0, labels.length) : ['#dcdfe6'],
                borderColor: '#fff', borderWidth: 2
            }]},
            options: { responsive: true, maintainAspectRatio: false, cutout: '60%',
                plugins: { legend: { position: 'right', labels: { font: { size: 12 }, padding: 12 } } } }
        });
    }

    function val(id) {
        var el = document.getElementById(id);
        return el ? (el.value || '').trim() : '';
    }

    function debounce(fn, delay) {
        var t;
        return function() { var ctx = this, args = arguments; clearTimeout(t); t = setTimeout(function() { fn.apply(ctx, args); }, delay); };
    }

    function showToast(msg, type) {
        type = type || 'info';
        var icons = { success: '✅', error: '✕', warning: '⚠', info: 'ⓘ' };
        var el = document.createElement('div');
        el.className = 'toast toast-' + type;
        el.innerHTML = '<span>' + (icons[type] || 'ⓘ') + '</span><span>' + ATS.escapeHtml(msg) + '</span>';
        document.getElementById('toastContainer').appendChild(el);
        setTimeout(function() {
            el.style.opacity = '0'; el.style.transition = 'opacity .3s';
            setTimeout(function() { el.remove(); }, 300);
        }, 3000);
    }

    function openModal(title, bodyHtml, buttons) {
        document.getElementById('modalTitle').textContent = title;
        document.getElementById('modalBody').innerHTML = bodyHtml;
        var footer = document.getElementById('modalFooter');
        footer.innerHTML = '';
        (buttons || []).forEach(function(b) {
            var btn = document.createElement('button');
            btn.className = 'btn-' + (b.type || 'ghost');
            btn.textContent = b.label;
            btn.addEventListener('click', function() {
                if (b.closeOnClick !== false) closeModal();
                if (b.onClick) b.onClick();
            });
            footer.appendChild(btn);
        });
        document.getElementById('modalOverlay').classList.add('show');
    }

    function closeModal() {
        document.getElementById('modalOverlay').classList.remove('show');
    }

    function confirmAction(msg, onOk) {
        openModal('确认操作', '<p style="color:#606266;font-size:14px;">' + ATS.escapeHtml(msg) + '</p>', [
            { label: '取消', type: 'ghost' },
            { label: '确定', type: 'danger', onClick: onOk }
        ]);
    }

    function renderPagination(containerId, total, current, callback) {
        var container = document.getElementById(containerId);
        var totalPages = Math.max(1, Math.ceil(total / state.pageSize));
        if (totalPages <= 1) {
            container.innerHTML = '<span class="pagination-info">共 ' + total + ' 条</span><span></span>';
            return;
        }
        var html = '<span class="pagination-info">共 ' + total + ' 条 · 第 ' + current + ' / ' + totalPages + ' 页</span><div class="pagination-buttons">';
        html += '<button class="pagination-btn"' + (current <= 1 ? ' disabled' : '') + ' data-page="' + (current - 1) + '">上一页</button>';
        var start = Math.max(1, current - 2);
        var end = Math.min(totalPages, start + 4);
        if (end - start < 4) start = Math.max(1, end - 4);
        for (var i = start; i <= end; i++) {
            html += '<button class="pagination-btn' + (i === current ? ' active' : '') + '" data-page="' + i + '">' + i + '</button>';
        }
        html += '<button class="pagination-btn"' + (current >= totalPages ? ' disabled' : '') + ' data-page="' + (current + 1) + '">下一页</button>';
        html += '</div>';
        container.innerHTML = html;
        container.querySelectorAll('.pagination-btn').forEach(function(btn) {
            btn.addEventListener('click', function() {
                var p = parseInt(btn.dataset.page, 10);
                if (p >= 1 && p <= totalPages) callback(p);
            });
        });
    }

    function loadUsers() {
        var c = document.getElementById('usersTableContainer');
        var url = '/api/users?current=' + state.pages.users + '&size=' + state.pageSize;
        if (state.search.users) url += '&keyword=' + encodeURIComponent(state.search.users);
        c.innerHTML = '<div style="padding:40px;text-align:center;"><span class="loading-spinner"></span> <span style="margin-left:8px;color:#909399;">加载中...</span></div>';
        ATS.ajax(url).then(function(res) {
            var d = res.data, list = d.records || [];
            var html = '';
            if (!list.length) {
                html = '<div class="empty"><span class="empty-icon">👥</span><div class="empty-text">暂无用户数据</div></div>';
            } else {
                html = '<table><thead><tr><th>ID</th><th>用户名</th><th>姓名</th><th>邮箱</th><th>角色</th><th>企业</th><th>创建时间</th><th>操作</th></tr></thead><tbody>';
                list.forEach(function(u) {
                    var rb = '';
                    if (u.role === 'ADMIN') rb = '<span class="status-tag tag-warning">管理员</span>';
                    else if (u.role === 'HR') rb = '<span class="status-tag tag-reviewed">HR</span>';
                    else rb = '<span class="status-tag tag-info">求职者</span>';
                    var t = u.createdAt ? u.createdAt.substring(0, 10) : '-';
                    html += '<tr><td>#' + u.id + '</td><td style="font-weight:500;">' + ATS.escapeHtml(u.username) + '</td>'
                        + '<td>' + ATS.escapeHtml(u.realName || '-') + '</td>'
                        + '<td>' + ATS.escapeHtml(u.email || '-') + '</td>'
                        + '<td>' + rb + '</td><td>' + ATS.escapeHtml(u.company || '-') + '</td>'
                        + '<td>' + t + '</td>'
                        + '<td><button class="btn-info" onclick="window.__editUser(' + u.id + ')">编辑</button> '
                        + '<button class="btn-warning" onclick="window.__resetPwd(' + u.id + ')">重置密码</button> '
                        + '<button class="btn-danger" onclick="window.__deleteUser(' + u.id + ')">删除</button></td></tr>';
                });
                html += '</tbody></table>';
            }
            c.innerHTML = html;
            renderPagination('usersPagination', d.total || 0, state.pages.users, function(p) { state.pages.users = p; loadUsers(); });
        });
    }

    function loadCompanies() {
        var c = document.getElementById('companiesTableContainer');
        var url = '/api/companies?current=' + state.pages.companies + '&size=' + state.pageSize;
        if (state.search.companies) url += '&keyword=' + encodeURIComponent(state.search.companies);
        c.innerHTML = '<div style="padding:40px;text-align:center;"><span class="loading-spinner"></span> <span style="margin-left:8px;color:#909399;">加载中...</span></div>';
        ATS.ajax(url).then(function(res) {
            var d = res.data, list = d.records || [];
            var html = '';
            if (!list.length) {
                html = '<div class="empty"><span class="empty-icon">🏢</span><div class="empty-text">暂无企业数据</div></div>';
            } else {
                html = '<table><thead><tr><th>ID</th><th>企业名称</th><th>行业</th><th>地点</th><th>规模</th><th>创建时间</th><th>操作</th></tr></thead><tbody>';
                list.forEach(function(c) {
                    var t = c.createdAt ? c.createdAt.substring(0, 10) : '-';
                    html += '<tr><td>#' + c.id + '</td><td style="font-weight:500;">' + ATS.escapeHtml(c.name) + '</td>'
                        + '<td>' + ATS.escapeHtml(c.industry || '-') + '</td>'
                        + '<td>' + ATS.escapeHtml(c.location || '-') + '</td>'
                        + '<td>' + SIZE_MAP[c.size] + '</td><td>' + t + '</td>'
                        + '<td><button class="btn-info" onclick="window.__editCompany(' + c.id + ')">编辑</button> '
                        + '<button class="btn-danger" onclick="window.__deleteCompany(' + c.id + ')">删除</button></td></tr>';
                });
                html += '</tbody></table>';
            }
            c.innerHTML = html;
            renderPagination('companiesPagination', d.total || 0, state.pages.companies, function(p) { state.pages.companies = p; loadCompanies(); });
        });
    }

    function loadPositions() {
        var c = document.getElementById('positionsTableContainer');
        var url = '/api/positions?current=' + state.pages.positions + '&size=' + state.pageSize;
        if (state.search.positions) url += '&keyword=' + encodeURIComponent(state.search.positions);
        c.innerHTML = '<div style="padding:40px;text-align:center;"><span class="loading-spinner"></span> <span style="margin-left:8px;color:#909399;">加载中...</span></div>';
        ATS.ajax(url).then(function(res) {
            var d = res.data, list = d.records || [];
            if (state.role === 'HR' && state.user.companyId) {
                list = list.filter(function(p) { return p.companyId === state.user.companyId; });
            }
            if (state.filters.positions) {
                list = list.filter(function(p) { return p.status === state.filters.positions; });
            }
            var html = '';
            if (!list.length) {
                html = '<div class="empty"><span class="empty-icon">💼</span><div class="empty-text">暂无岗位数据</div></div>';
            } else {
                html = '<table><thead><tr><th>ID</th><th>职位</th><th>企业</th><th>部门</th><th>状态</th><th>发布人</th><th>操作</th></tr></thead><tbody>';
                list.forEach(function(p) {
                    var s = POS_STATUS[p.status] || { cls: 'tag-info', text: p.status };
                    html += '<tr><td>#' + p.id + '</td><td style="font-weight:500;">' + ATS.escapeHtml(p.title) + '</td>'
                        + '<td>' + ATS.escapeHtml(p.companyName || '-') + '</td>'
                        + '<td><span class="dept-chip">' + ATS.escapeHtml(p.department || '-') + '</span></td>'
                        + '<td><span class="status-tag ' + s.cls + '">' + s.text + '</span></td>'
                        + '<td>' + (p.publishUserId ? '用户#' + p.publishUserId : '-') + '</td>'
                        + '<td><button class="btn-info" onclick="window.__editPosition(' + p.id + ')">编辑</button> '
                        + '<button class="btn-danger" onclick="window.__deletePosition(' + p.id + ')">删除</button></td></tr>';
                });
                html += '</tbody></table>';
            }
            c.innerHTML = html;
            renderPagination('positionsPagination', d.total || 0, state.pages.positions, function(p) { state.pages.positions = p; loadPositions(); });
        });
    }

    function loadApplications() {
        var c = document.getElementById('applicationsTableContainer');
        var url = '/api/applications?current=' + state.pages.applications + '&size=' + state.pageSize;
        if (state.filters.applications) url += '&status=' + encodeURIComponent(state.filters.applications);
        c.innerHTML = '<div style="padding:40px;text-align:center;"><span class="loading-spinner"></span> <span style="margin-left:8px;color:#909399;">加载中...</span></div>';
        ATS.ajax(url).then(function(res) {
            var d = res.data, list = d.records || [];
            if (state.role === 'HR' && state.user.companyId) {
                list = list.filter(function(a) {
                    var p = state.positionMap[a.positionId];
                    return p && p.companyId === state.user.companyId;
                });
            }
            if (state.search.applications) {
                var kw = state.search.applications.toLowerCase();
                list = list.filter(function(a) {
                    var pos = state.positionMap[a.positionId];
                    var title = pos ? pos.title : '';
                    return title.toLowerCase().indexOf(kw) >= 0 || String(a.id).indexOf(kw) >= 0;
                });
            }
            var html = '';
            if (!list.length) {
                html = '<div class="empty"><span class="empty-icon">📭</span><div class="empty-text">暂无投递记录</div></div>';
            } else {
                html = '<table><thead><tr><th>ID</th><th>岗位</th><th>状态</th><th>更新时间</th><th>操作</th></tr></thead><tbody>';
                list.forEach(function(a) {
                    var s = STATUS_MAP[a.status] || { cls: 'tag-info', text: a.status };
                    var pt = (state.positionMap[a.positionId] && state.positionMap[a.positionId].title) || ('职位#' + a.positionId);
                    var t = a.updatedAt ? a.updatedAt.substring(0, 10) : '-';
                    var acts = '';
                    if (state.role === 'ADMIN' || state.role === 'HR') {
                        acts = '<button class="btn-success" onclick="window.__updateAppStatus(' + a.id + ',\'ACCEPTED\')">通过</button> '
                            + '<button class="btn-danger" onclick="window.__updateAppStatus(' + a.id + ',\'REJECTED\')">拒绝</button> ';
                    }
                    html += '<tr><td>#' + a.id + '</td><td style="font-weight:500;">' + ATS.escapeHtml(pt) + '</td>'
                        + '<td><span class="status-tag ' + s.cls + '">' + s.text + '</span></td>'
                        + '<td>' + t + '</td><td>' + acts
                        + '<button class="btn-info" onclick="window.__editApplication(' + a.id + ')">详情</button> '
                        + (state.role === 'ADMIN' ? '<button class="btn-danger" onclick="window.__deleteApplication(' + a.id + ')">删除</button>' : '')
                        + '</td></tr>';
                });
                html += '</tbody></table>';
            }
            c.innerHTML = html;
            renderPagination('applicationsPagination', d.total || 0, state.pages.applications, function(p) { state.pages.applications = p; loadApplications(); });
        });
    }

    function loadRules() {
        var c = document.getElementById('rulesContainer');
        c.innerHTML = '<div style="padding:40px;text-align:center;"><span class="loading-spinner"></span> <span style="margin-left:8px;color:#909399;">加载中...</span></div>';
        ATS.ajax('/api/screen-rules?current=1&size=100').then(function(res) {
            var list = res.data.records || [];
            var html = '';
            if (!list.length) {
                html = '<div class="empty"><span class="empty-icon">⚙️</span><div class="empty-text">暂无筛选规则</div></div>';
            } else {
                var typeMap = { KEYWORD: '关键词', SKILL: '技能', EXPERIENCE: '经验' };
                var modeMap = { ANY: '任一匹配', ALL: '全部匹配', MIN: '最小值' };
                list.forEach(function(r) {
                    html += '<div class="rule-card"><div class="rule-card-header">'
                        + '<div class="rule-card-title">' + ATS.escapeHtml(r.name) + '</div><div>'
                        + '<span class="rule-tag">' + (typeMap[r.ruleType] || r.ruleType) + '</span>'
                        + '<span class="rule-tag">' + (modeMap[r.matchMode] || r.matchMode) + '</span>'
                        + '<span class="rule-tag">权重: ' + (r.weight || 0) + '</span>'
                        + (r.enabled ? '<span class="status-tag tag-open">启用</span>' : '<span class="status-tag tag-closed">禁用</span>')
                        + '</div></div><div class="rule-card-body">'
                        + '<div>目标字段: ' + ATS.escapeHtml(r.targetField || '-') + '</div>'
                        + '<div>期望值: ' + ATS.escapeHtml(r.expectedValues || '-') + '</div>'
                        + '</div><div style="margin-top:12px;display:flex;gap:8px;">'
                        + '<button class="btn-info" onclick="window.__editRule(' + r.id + ')">编辑</button>'
                        + '<button class="btn-ghost" onclick="window.__toggleRule(' + r.id + ',' + (r.enabled ? '0' : '1') + ')">' + (r.enabled ? '禁用' : '启用') + '</button>'
                        + '<button class="btn-danger" onclick="window.__deleteRule(' + r.id + ')">删除</button>'
                        + '</div></div>';
                });
            }
            c.innerHTML = html;
        });
    }

    function loadCompanyInfo() {
        var c = document.getElementById('companyInfoContent');
        if (!state.user.companyId) {
            c.innerHTML = '<div class="empty"><span class="empty-icon">🏢</span><div class="empty-text">未关联企业信息，请联系管理员</div></div>';
            return;
        }
        c.innerHTML = '<div style="padding:40px;text-align:center;"><span class="loading-spinner"></span> <span style="margin-left:8px;color:#909399;">加载中...</span></div>';
        ATS.ajax('/api/companies/' + state.user.companyId).then(function(res) {
            var co = res.data;
            if (!co) {
                c.innerHTML = '<div class="empty"><span class="empty-icon">🏢</span><div class="empty-text">企业信息不存在</div></div>';
                return;
            }
            var html = '<div class="company-info-card"><div style="display:flex;justify-content:space-between;align-items:flex-start;">'
                + '<div><h3>' + ATS.escapeHtml(co.name) + '</h3>'
                + '<div style="font-size:13px;opacity:0.9;">行业: ' + ATS.escapeHtml(co.industry || '-') + '</div></div>'
                + '<button class="btn-primary" onclick="window.__editCompany(' + co.id + ')">✏️ 编辑信息</button></div>'
                + '<div style="margin-top:16px;">'
                + '<div class="info-row"><span>📍 地点:</span>' + ATS.escapeHtml(co.location || '-') + '</div>'
                + '<div class="info-row"><span>🌐 网站:</span>' + ATS.escapeHtml(co.website || '-') + '</div>'
                + '<div class="info-row"><span>👥 规模:</span>' + (SIZE_MAP[co.size] || '-') + '</div></div>'
                + '<div class="description">' + ATS.escapeHtml(co.description || '暂无企业描述') + '</div></div>';
            ATS.ajax('/api/positions?current=1&size=500').then(function(pr) {
                var mps = (pr.data.records || []).filter(function(p) { return p.companyId === co.id; });
                html += '<div class="panel"><div class="panel-header"><div class="panel-title">本公司岗位 (' + mps.length + ')</div></div>';
                if (!mps.length) {
                    html += '<div class="empty"><span class="empty-icon">💼</span><div class="empty-text">暂无岗位</div></div>';
                } else {
                    html += '<table><thead><tr><th>职位</th><th>部门</th><th>状态</th><th>操作</th></tr></thead><tbody>';
                    mps.forEach(function(p) {
                        var s = POS_STATUS[p.status] || { cls: 'tag-info', text: p.status };
                        html += '<tr><td style="font-weight:500;">' + ATS.escapeHtml(p.title) + '</td>'
                            + '<td>' + ATS.escapeHtml(p.department || '-') + '</td>'
                            + '<td><span class="status-tag ' + s.cls + '">' + s.text + '</span></td>'
                            + '<td><button class="btn-info" onclick="window.__editPosition(' + p.id + ')">编辑</button></td></tr>';
                    });
                    html += '</tbody></table>';
                }
                html += '</div>';
                c.innerHTML = html;
            });
        });
    }

    function loadSettings() {
        var c = document.getElementById('adminUserInfo');
        c.innerHTML = '<div class="detail-row"><div class="detail-label">用户ID</div><div class="detail-value">' + state.user.userId + '</div></div>'
            + '<div class="detail-row"><div class="detail-label">用户名</div><div class="detail-value">' + ATS.escapeHtml(state.user.username) + '</div></div>'
            + '<div class="detail-row"><div class="detail-label">姓名</div><div class="detail-value">' + ATS.escapeHtml(state.user.realName || '-') + '</div></div>'
            + '<div class="detail-row"><div class="detail-label">角色</div><div class="detail-value">' + state.user.role + '</div></div>'
            + '<div class="detail-row"><div class="detail-label">邮箱</div><div class="detail-value">' + ATS.escapeHtml(state.user.email || '-') + '</div></div>'
            + '<div class="detail-row"><div class="detail-label">手机</div><div class="detail-value">' + ATS.escapeHtml(state.user.phone || '-') + '</div></div>'
            + '<div class="detail-row"><div class="detail-label">企业</div><div class="detail-value">' + ATS.escapeHtml(state.user.company || '-') + '</div></div>';
    }

    function openUserForm() {
        var html = '<div class="form-group"><label>用户名</label><input id="f_username" placeholder="请输入用户名"></div>'
            + '<div class="form-row"><div class="form-group"><label>姓名</label><input id="f_realName" placeholder="请输入姓名"></div>'
            + '<div class="form-group"><label>角色</label><select id="f_role"><option value="ADMIN">管理员</option><option value="HR">HR</option><option value="CANDIDATE">求职者</option></select></div></div>'
            + '<div class="form-row"><div class="form-group"><label>邮箱</label><input id="f_email" placeholder="请输入邮箱"></div>'
            + '<div class="form-group"><label>手机</label><input id="f_phone" placeholder="请输入手机号"></div></div>'
            + '<div class="form-group"><label>企业</label><input id="f_company" placeholder="企业名称"></div>'
            + '<div class="form-group"><label>密码</label><input id="f_password" type="password" placeholder="初始密码"></div>';
        openModal('新建用户', html, [
            { label: '取消', type: 'ghost' },
            { label: '创建', type: 'primary', onClick: function() {
                var p = { username: val('f_username'), realName: val('f_realName'), role: val('f_role'),
                    email: val('f_email'), phone: val('f_phone'), company: val('f_company'), password: val('f_password') };
                if (!p.username || !p.password) { showToast('用户名和密码必填', 'warning'); return; }
                ATS.ajax('/api/users', { method: 'POST', body: p }).then(function(r) {
                    if (r.code === 0) { showToast('用户创建成功', 'success'); loadUsers(); }
                    else showToast(r.message || '创建失败', 'error');
                });
            }}
        ]);
    }

    function editUser(id) {
        ATS.ajax('/api/users/' + id).then(function(res) {
            var u = res.data;
            if (!u) { showToast('用户不存在', 'error'); return; }
            var html = '<div class="form-row"><div class="form-group"><label>用户名</label><input id="f_username" value="' + ATS.escapeHtml(u.username) + '"></div>'
                + '<div class="form-group"><label>姓名</label><input id="f_realName" value="' + ATS.escapeHtml(u.realName || '') + '"></div></div>'
                + '<div class="form-group"><label>角色</label><select id="f_role">'
                + '<option value="ADMIN"' + (u.role === 'ADMIN' ? ' selected' : '') + '>管理员</option>'
                + '<option value="HR"' + (u.role === 'HR' ? ' selected' : '') + '>HR</option>'
                + '<option value="CANDIDATE"' + (u.role === 'CANDIDATE' ? ' selected' : '') + '>求职者</option></select></div>'
                + '<div class="form-row"><div class="form-group"><label>邮箱</label><input id="f_email" value="' + ATS.escapeHtml(u.email || '') + '"></div>'
                + '<div class="form-group"><label>手机</label><input id="f_phone" value="' + ATS.escapeHtml(u.phone || '') + '"></div></div>'
                + '<div class="form-group"><label>企业</label><input id="f_company" value="' + ATS.escapeHtml(u.company || '') + '"></div>';
            openModal('编辑用户', html, [
                { label: '取消', type: 'ghost' },
                { label: '保存', type: 'primary', onClick: function() {
                    var p = { username: val('f_username'), realName: val('f_realName'), role: val('f_role'),
                        email: val('f_email'), phone: val('f_phone'), company: val('f_company') };
                    ATS.ajax('/api/users/' + id, { method: 'PUT', body: p }).then(function(r) {
                        if (r.code === 0) { showToast('保存成功', 'success'); loadUsers(); }
                        else showToast(r.message || '保存失败', 'error');
                    });
                }}
            ]);
        });
    }

    function deleteUser(id) {
        confirmAction('确定删除该用户吗？此操作不可恢复。', function() {
            ATS.ajax('/api/users/' + id, { method: 'DELETE' }).then(function(r) {
                if (r.code === 0) { showToast('删除成功', 'success'); loadUsers(); }
                else showToast(r.message || '删除失败', 'error');
            });
        });
    }

    function resetPassword(id) {
        var html = '<div class="form-group"><label>新密码</label><input id="f_newpwd" type="password" placeholder="请输入新密码"></div>';
        openModal('重置密码', html, [
            { label: '取消', type: 'ghost' },
            { label: '重置', type: 'primary', onClick: function() {
                var pwd = val('f_newpwd');
                if (!pwd) { showToast('请输入新密码', 'warning'); return; }
                ATS.ajax('/api/users/' + id + '/reset-password', { method: 'POST', body: { password: pwd } }).then(function(r) {
                    if (r.code === 0) showToast('密码重置成功', 'success');
                    else showToast(r.message || '重置失败', 'error');
                });
            }}
        ]);
    }

    function openCompanyForm() {
        var html = '<div class="form-row"><div class="form-group"><label>企业名称</label><input id="f_name" placeholder="请输入企业名称"></div>'
            + '<div class="form-group"><label>行业</label><input id="f_industry" placeholder="如：互联网"></div></div>'
            + '<div class="form-row"><div class="form-group"><label>地点</label><input id="f_location" placeholder="城市"></div>'
            + '<div class="form-group"><label>规模</label><select id="f_size"><option value="1">初创(1-20)</option><option value="2">中小(20-99)</option><option value="3">中型(100-499)</option><option value="4">大型(500+)</option></select></div></div>'
            + '<div class="form-group"><label>网站</label><input id="f_website" placeholder="https://"></div>'
            + '<div class="form-group"><label>企业描述</label><textarea id="f_description" placeholder="企业简介"></textarea></div>';
        openModal('新建企业', html, [
            { label: '取消', type: 'ghost' },
            { label: '创建', type: 'primary', onClick: function() {
                var p = { name: val('f_name'), industry: val('f_industry'), location: val('f_location'),
                    size: parseInt(val('f_size'), 10), website: val('f_website'), description: val('f_description') };
                if (!p.name) { showToast('企业名称必填', 'warning'); return; }
                ATS.ajax('/api/companies', { method: 'POST', body: p }).then(function(r) {
                    if (r.code === 0) { showToast('企业创建成功', 'success'); loadCompanies(); }
                    else showToast(r.message || '创建失败', 'error');
                });
            }}
        ]);
    }

    function editCompany(id) {
        ATS.ajax('/api/companies/' + id).then(function(res) {
            var c = res.data;
            if (!c) { showToast('企业不存在', 'error'); return; }
            var html = '<div class="form-row"><div class="form-group"><label>企业名称</label><input id="f_name" value="' + ATS.escapeHtml(c.name) + '"></div>'
                + '<div class="form-group"><label>行业</label><input id="f_industry" value="' + ATS.escapeHtml(c.industry || '') + '"></div></div>'
                + '<div class="form-row"><div class="form-group"><label>地点</label><input id="f_location" value="' + ATS.escapeHtml(c.location || '') + '"></div>'
                + '<div class="form-group"><label>规模</label><select id="f_size">'
                + '<option value="1"' + (c.size === 1 ? ' selected' : '') + '>初创(1-20)</option>'
                + '<option value="2"' + (c.size === 2 ? ' selected' : '') + '>中小(20-99)</option>'
                + '<option value="3"' + (c.size === 3 ? ' selected' : '') + '>中型(100-499)</option>'
                + '<option value="4"' + (c.size === 4 ? ' selected' : '') + '>大型(500+)</option></select></div></div>'
                + '<div class="form-group"><label>网站</label><input id="f_website" value="' + ATS.escapeHtml(c.website || '') + '"></div>'
                + '<div class="form-group"><label>企业描述</label><textarea id="f_description">' + ATS.escapeHtml(c.description || '') + '</textarea></div>';
            openModal('编辑企业', html, [
                { label: '取消', type: 'ghost' },
                { label: '保存', type: 'primary', onClick: function() {
                    var p = { name: val('f_name'), industry: val('f_industry'), location: val('f_location'),
                        size: parseInt(val('f_size'), 10), website: val('f_website'), description: val('f_description') };
                    ATS.ajax('/api/companies/' + id, { method: 'PUT', body: p }).then(function(r) {
                        if (r.code === 0) { showToast('保存成功', 'success'); loadCompanies(); loadCompanyInfo(); }
                        else showToast(r.message || '保存失败', 'error');
                    });
                }}
            ]);
        });
    }

    function deleteCompany(id) {
        confirmAction('确定删除该企业吗？此操作不可恢复。', function() {
            ATS.ajax('/api/companies/' + id, { method: 'DELETE' }).then(function(r) {
                if (r.code === 0) { showToast('删除成功', 'success'); loadCompanies(); }
                else showToast(r.message || '删除失败', 'error');
            });
        });
    }

    function openPositionForm() {
        var cn = state.role === 'HR' ? state.user.company : '';
        var html = '<div class="form-row"><div class="form-group"><label>职位名称</label><input id="f_title" placeholder="如: Java 高级工程师"></div>'
            + '<div class="form-group"><label>部门</label><input id="f_department" placeholder="如: 技术部"></div></div>'
            + '<div class="form-row"><div class="form-group"><label>状态</label><select id="f_status"><option value="OPEN">招聘中</option><option value="CLOSED">已关闭</option></select></div>'
            + '<div class="form-group"><label>招聘类型</label><select id="f_category"><option value="SOCIAL">社招</option><option value="CAMPUS">校招</option><option value="INTERN">实习</option></select></div></div>'
            + '<div class="form-row"><div class="form-group"><label>地点</label><input id="f_location" placeholder="城市"></div>'
            + '<div class="form-group"><label>薪资</label><input id="f_salary" placeholder="如: 15-25K"></div></div>'
            + '<div class="form-row"><div class="form-group"><label>学历</label><select id="f_education"><option value="">不限</option><option value="大专">大专</option><option value="本科">本科</option><option value="硕士">硕士</option><option value="博士">博士</option></select></div>'
            + '<div class="form-group"><label>经验</label><input id="f_experience" placeholder="如: 3-5年"></div></div>'
            + '<div class="form-group"><label>所属企业</label><input id="f_companyName" value="' + ATS.escapeHtml(cn) + '" ' + (state.role === 'HR' ? 'readonly' : '') + '></div>'
            + '<div class="form-group"><label>职位描述</label><textarea id="f_description" placeholder="职位详细描述"></textarea></div>'
            + '<div class="form-group"><label>任职要求</label><textarea id="f_requirements" placeholder="任职要求"></textarea></div>';
        openModal('新建岗位', html, [
            { label: '取消', type: 'ghost' },
            { label: '创建', type: 'primary', onClick: function() {
                var p = { title: val('f_title'), department: val('f_department'), status: val('f_status'),
                    category: val('f_category'), location: val('f_location'), salary: val('f_salary'),
                    education: val('f_education'), experience: val('f_experience'),
                    companyName: val('f_companyName'), description: val('f_description'), requirements: val('f_requirements') };
                if (state.role === 'HR' && state.user.companyId) p.companyId = state.user.companyId;
                if (!p.title) { showToast('职位名称必填', 'warning'); return; }
                ATS.ajax('/api/positions', { method: 'POST', body: p }).then(function(r) {
                    if (r.code === 0) { showToast('岗位创建成功', 'success'); loadPositions(); buildPositionMap(); }
                    else showToast(r.message || '创建失败', 'error');
                });
            }}
        ]);
    }

    function editPosition(id) {
        ATS.ajax('/api/positions/' + id).then(function(res) {
            var p = res.data;
            if (!p) { showToast('岗位不存在', 'error'); return; }
            var html = '<div class="form-row"><div class="form-group"><label>职位名称</label><input id="f_title" value="' + ATS.escapeHtml(p.title) + '"></div>'
                + '<div class="form-group"><label>部门</label><input id="f_department" value="' + ATS.escapeHtml(p.department || '') + '"></div></div>'
                + '<div class="form-row"><div class="form-group"><label>状态</label><select id="f_status">'
                + '<option value="OPEN"' + (p.status === 'OPEN' ? ' selected' : '') + '>招聘中</option>'
                + '<option value="CLOSED"' + (p.status === 'CLOSED' ? ' selected' : '') + '>已关闭</option></select></div>'
                + '<div class="form-group"><label>招聘类型</label><select id="f_category">'
                + '<option value="SOCIAL"' + (p.category === 'SOCIAL' ? ' selected' : '') + '>社招</option>'
                + '<option value="CAMPUS"' + (p.category === 'CAMPUS' ? ' selected' : '') + '>校招</option>'
                + '<option value="INTERN"' + (p.category === 'INTERN' ? ' selected' : '') + '>实习</option></select></div></div>'
                + '<div class="form-row"><div class="form-group"><label>地点</label><input id="f_location" value="' + ATS.escapeHtml(p.location || '') + '"></div>'
                + '<div class="form-group"><label>薪资</label><input id="f_salary" value="' + ATS.escapeHtml(p.salary || '') + '"></div></div>'
                + '<div class="form-group"><label>职位描述</label><textarea id="f_description">' + ATS.escapeHtml(p.description || '') + '</textarea></div>'
                + '<div class="form-group"><label>任职要求</label><textarea id="f_requirements">' + ATS.escapeHtml(p.requirements || '') + '</textarea></div>';
            openModal('编辑岗位', html, [
                { label: '取消', type: 'ghost' },
                { label: '保存', type: 'primary', onClick: function() {
                    var pl = { title: val('f_title'), department: val('f_department'), status: val('f_status'),
                        category: val('f_category'), location: val('f_location'), salary: val('f_salary'),
                        description: val('f_description'), requirements: val('f_requirements') };
                    ATS.ajax('/api/positions/' + id, { method: 'PUT', body: pl }).then(function(r) {
                        if (r.code === 0) { showToast('保存成功', 'success'); loadPositions(); buildPositionMap(); }
                        else showToast(r.message || '保存失败', 'error');
                    });
                }}
            ]);
        });
    }

    function deletePosition(id) {
        confirmAction('确定删除该岗位吗？此操作不可恢复。', function() {
            ATS.ajax('/api/positions/' + id, { method: 'DELETE' }).then(function(r) {
                if (r.code === 0) { showToast('删除成功', 'success'); loadPositions(); }
                else showToast(r.message || '删除失败', 'error');
            });
        });
    }

    function editApplication(id) {
        ATS.ajax('/api/applications/' + id).then(function(res) {
            var a = res.data;
            if (!a) { showToast('投递记录不存在', 'error'); return; }
            var pt = (state.positionMap[a.positionId] && state.positionMap[a.positionId].title) || ('职位#' + a.positionId);
            var s = STATUS_MAP[a.status] || { cls: 'tag-info', text: a.status };
            var t = a.createdAt ? a.createdAt.substring(0, 10) : '-';
            var html = '<div style="background:#f8fafb;border-radius:8px;padding:16px;margin-bottom:16px;">'
                + '<div class="detail-row"><div class="detail-label">投递ID</div><div class="detail-value">#' + a.id + '</div></div>'
                + '<div class="detail-row"><div class="detail-label">岗位</div><div class="detail-value">' + ATS.escapeHtml(pt) + '</div></div>'
                + '<div class="detail-row"><div class="detail-label">候选人ID</div><div class="detail-value">#' + (a.candidateId || '-') + '</div></div>'
                + '<div class="detail-row"><div class="detail-label">当前状态</div><div class="detail-value"><span class="status-tag ' + s.cls + '">' + s.text + '</span></div></div>'
                + '<div class="detail-row"><div class="detail-label">投递时间</div><div class="detail-value">' + t + '</div></div></div>'
                + '<div class="form-group"><label>更新状态</label><select id="f_status">'
                + '<option value="PENDING"' + (a.status === 'PENDING' ? ' selected' : '') + '>⏳ 待审核</option>'
                + '<option value="REVIEWED"' + (a.status === 'REVIEWED' ? ' selected' : '') + '>👁️ 已查看</option>'
                + '<option value="ACCEPTED"' + (a.status === 'ACCEPTED' ? ' selected' : '') + '>✅ 已通过</option>'
                + '<option value="REJECTED"' + (a.status === 'REJECTED' ? ' selected' : '') + '>❌ 已拒绝</option>'
                + '<option value="INTERVIEWING"' + (a.status === 'INTERVIEWING' ? ' selected' : '') + '>💬 面试中</option>'
                + '<option value="OFFER"' + (a.status === 'OFFER' ? ' selected' : '') + '>📨 已发Offer</option></select></div>'
                + '<div class="form-group"><label>HR备注</label><textarea id="f_remark" placeholder="添加备注">' + ATS.escapeHtml(a.hrRemark || '') + '</textarea></div>';
            openModal('投递详情 #' + id, html, [
                { label: '关闭', type: 'ghost' },
                { label: '保存', type: 'primary', onClick: function() {
                    var ns = val('f_status');
                    ATS.ajax('/api/applications/' + id + '/status/body', { method: 'PUT', body: { status: ns } }).then(function(r) {
                        if (r.code === 0) { showToast('状态更新成功', 'success'); loadApplications(); }
                        else showToast(r.message || '更新失败', 'error');
                    });
                }}
            ]);
        });
    }

    function deleteApplication(id) {
        confirmAction('确定删除该投递记录吗？', function() {
            ATS.ajax('/api/applications/' + id, { method: 'DELETE' }).then(function(r) {
                if (r.code === 0) { showToast('删除成功', 'success'); loadApplications(); }
                else showToast(r.message || '删除失败', 'error');
            });
        });
    }

    function updateAppStatus(id, status) {
        var sm = { ACCEPTED: '通过', REJECTED: '拒绝', PENDING: '待审核', REVIEWED: '已查看', INTERVIEWING: '面试中', OFFER: '已发Offer' };
        confirmAction('确定将投递 #' + id + ' 标记为"' + (sm[status] || status) + '"吗？', function() {
            ATS.ajax('/api/applications/' + id + '/status/body', { method: 'PUT', body: { status: status } }).then(function(r) {
                if (r.code === 0) { showToast('状态更新成功', 'success'); loadApplications(); }
                else showToast(r.message || '更新失败', 'error');
            });
        });
    }

    function openRuleForm() {
        var html = '<div class="form-group"><label>规则名称</label><input id="f_name" placeholder="如: Java 技能要求"></div>'
            + '<div class="form-row"><div class="form-group"><label>规则类型</label><select id="f_ruleType"><option value="KEYWORD">关键词</option><option value="SKILL">技能</option><option value="EXPERIENCE">经验</option></select></div>'
            + '<div class="form-group"><label>目标字段</label><select id="f_targetField"><option value="skills">技能</option><option value="resume_text">简历文本</option><option value="experience_years">经验年数</option></select></div></div>'
            + '<div class="form-group"><label>期望值（逗号分隔）</label><input id="f_expectedValues" placeholder="如: Java,Spring"></div>'
            + '<div class="form-row"><div class="form-group"><label>匹配模式</label><select id="f_matchMode"><option value="ANY">任一匹配</option><option value="ALL">全部匹配</option><option value="MIN">最小值</option></select></div>'
            + '<div class="form-group"><label>权重</label><input id="f_weight" type="number" value="10" min="0" max="100"></div></div>'
            + '<div class="form-group"><label>绑定岗位</label><select id="f_positionId"><option value="0">通用规则</option></select></div>'
            + '<div class="form-group"><label><input id="f_enabled" type="checkbox" checked style="width:auto;"> 启用规则</label></div>';
        openModal('新建筛选规则', html, [
            { label: '取消', type: 'ghost' },
            { label: '创建', type: 'primary', onClick: function() {
                var p = { name: val('f_name'), ruleType: val('f_ruleType'), targetField: val('f_targetField'),
                    expectedValues: val('f_expectedValues'), matchMode: val('f_matchMode'),
                    weight: parseInt(val('f_weight') || '0', 10),
                    enabled: document.getElementById('f_enabled').checked ? 1 : 0 };
                if (!p.name) { showToast('规则名称必填', 'warning'); return; }
                ATS.ajax('/api/screen-rules', { method: 'POST', body: p }).then(function(r) {
                    if (r.code === 0) { showToast('规则创建成功', 'success'); loadRules(); }
                    else showToast(r.message || '创建失败', 'error');