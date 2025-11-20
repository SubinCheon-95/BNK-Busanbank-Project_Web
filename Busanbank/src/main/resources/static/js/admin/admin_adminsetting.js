/**
 * 작성자: 진원
 * 작성일: 2025-11-16
 * 설명: 관리자 계정 관리 JavaScript
 */

let currentPage = 1;
const pageSize = 10;
let searchKeyword = '';

// 페이지 로딩 시 실행
document.addEventListener('DOMContentLoaded', () => {
    loadAdminList();
    initializeEventListeners();
});

// 이벤트 리스너 초기화
function initializeEventListeners() {
    // 검색 버튼
    const searchBtn = document.querySelector('#account .search_btn');
    const searchInput = document.querySelector('#account .search_input');

    if (searchBtn) {
        searchBtn.addEventListener('click', () => {
            searchKeyword = searchInput.value.trim();
            currentPage = 1;
            loadAdminList();
        });
    }

    if (searchInput) {
        searchInput.addEventListener('keypress', (e) => {
            if (e.key === 'Enter') {
                searchKeyword = searchInput.value.trim();
                currentPage = 1;
                loadAdminList();
            }
        });
    }

    // 추가 버튼
    const addBtn = document.querySelector('#addAdminBtn');
    if (addBtn) {
        addBtn.addEventListener('click', openAddModal);
    }

    // 모달 닫기 버튼
    const closeBtn = document.querySelector('.admin-modal .close');
    if (closeBtn) {
        closeBtn.addEventListener('click', closeModal);
    }

    // 모달 저장 버튼
    const saveBtn = document.querySelector('#saveAdminBtn');
    if (saveBtn) {
        saveBtn.addEventListener('click', saveAdmin);
    }

    // 모달 외부 클릭 시 닫기
    window.addEventListener('click', (e) => {
        const modal = document.querySelector('#adminModal');
        if (e.target === modal) {
            closeModal();
        }
    });

    // 로그인 ID 중복 체크
    const loginIdInput = document.querySelector('#adminLoginId');
    if (loginIdInput) {
        loginIdInput.addEventListener('blur', checkLoginIdDuplicate);
    }

    // 비밀번호 변경 체크박스
    const passwordChangeCheck = document.querySelector('#passwordChangeCheck');
    if (passwordChangeCheck) {
        passwordChangeCheck.addEventListener('change', function() {
            const passwordInput = document.querySelector('#adminPassword');
            if (this.checked) {
                passwordInput.disabled = false;
                passwordInput.required = true;
                passwordInput.value = '';
                passwordInput.focus();
            } else {
                passwordInput.disabled = true;
                passwordInput.required = false;
                passwordInput.value = '';
            }
        });
    }
}

// 관리자 목록 조회
async function loadAdminList() {
    try {
        const response = await fetch(`/busanbank/admin/setting/admins?page=${currentPage}&size=${pageSize}&searchKeyword=${searchKeyword}`);
        const data = await response.json();

        if (data.success) {
            renderAdminTable(data.data);
            renderPagination(data.totalPages, data.currentPage);
        } else {
            alert('관리자 목록 조회 실패: ' + data.message);
        }
    } catch (error) {
        console.error('관리자 목록 조회 오류:', error);
        alert('관리자 목록 조회 중 오류가 발생했습니다.');
    }
}

// 관리자 테이블 렌더링
function renderAdminTable(adminList) {
    const tbody = document.querySelector('#adminTableBody');
    if (!tbody) return;

    if (!adminList || adminList.length === 0) {
        tbody.innerHTML = '<tr><td colspan="8" style="text-align:center; padding: 20px;">등록된 관리자가 없습니다.</td></tr>';
        return;
    }

    tbody.innerHTML = adminList.map((admin, index) => {
        const rowClass = index === adminList.length - 1 ? 'content_tr_last' : 'content_tr';
        const startStyle = index === adminList.length - 1 ? 'style="border-radius: 0 0 0 5px;"' : '';
        const endStyle = index === adminList.length - 1 ? 'style="border-radius: 0 0 5px 0;"' : '';

        // 상태 표시 (Y: 활성, N: 비활성)
        const statusText = admin.status === 'Y' ? '활성' : '비활성';
        const statusColor = admin.status === 'Y' ? '#2ecc71' : '#e74c3c';

        return `
            <tr class="${rowClass}">
                <td ${startStyle}>${(currentPage - 1) * pageSize + index + 1}</td>
                <td>${admin.adminName || '-'}</td>
                <td>${admin.loginId}</td>
                <td>${admin.adminRole || '-'}</td>
                <td>${admin.createdAt ? admin.createdAt.substring(0, 10) : '-'}</td>
                <td>${admin.updatedAt ? admin.updatedAt.substring(0, 10) : '-'}</td>
                <td><span style="color: ${statusColor}; font-weight: bold;">${statusText}</span></td>
                <td ${endStyle}>
                    <button class="productList_btn" onclick="openEditModal(${admin.adminId})">
                        <img src="/busanbank/images/admin/free-icon-pencil-7175371.png" alt="수정 버튼" style="width: 100%;height: 100%;object-fit: contain;">
                    </button>
                    <button class="productList_btn" onclick="deleteAdmin(${admin.adminId}, '${admin.loginId}')">
                        <img src="/busanbank/images/admin/cross-mark.png" alt="삭제 버튼" style="width: 100%;height: 100%;object-fit: contain;">
                    </button>
                </td>
            </tr>
        `;
    }).join('');
}

// 페이징 렌더링
function renderPagination(totalPages, currentPageNum) {
    const paginationUl = document.querySelector('#account .pagenation');
    if (!paginationUl) return;

    let html = '';

    // 이전 버튼
    if (currentPageNum > 1) {
        html += `<li><a href="#" class="page2" onclick="changePage(${currentPageNum - 1}); return false;"><span class="prev"></span></a></li>`;
    }

    // 페이지 번호 (현재 페이지 기준 ±2 페이지)
    const startPage = Math.max(1, currentPageNum - 2);
    const endPage = Math.min(totalPages, currentPageNum + 2);

    for (let i = startPage; i <= endPage; i++) {
        const activeClass = i === currentPageNum ? 'active' : '';
        html += `<li><a href="#" class="page1 ${activeClass}" onclick="changePage(${i}); return false;">${i}</a></li>`;
    }

    // 다음 버튼
    if (currentPageNum < totalPages) {
        html += `<li><a href="#" class="page2" onclick="changePage(${currentPageNum + 1}); return false;"><span class="next"></span></a></li>`;
    }

    paginationUl.innerHTML = html;
}

// 페이지 변경
function changePage(page) {
    currentPage = page;
    loadAdminList();
}

// 추가 모달 열기
function openAddModal() {
    document.querySelector('#modalTitle').textContent = '관리자 추가';
    document.querySelector('#adminForm').reset();
    document.querySelector('#adminId').value = '';

    // 추가 모드: 비밀번호 변경 체크박스 숨김, 비밀번호 필드는 필수
    document.querySelector('#passwordChangeGroup').style.display = 'none';
    document.querySelector('#passwordChangeCheck').checked = false;
    document.querySelector('#passwordGroup').style.display = 'block';
    document.querySelector('#adminPassword').disabled = false;
    document.querySelector('#adminPassword').required = true;

    document.querySelector('#adminModal').style.display = 'block';
}

// 수정 모달 열기
async function openEditModal(adminId) {
    try {
        const response = await fetch(`/busanbank/admin/setting/admins/${adminId}`);
        const data = await response.json();

        if (data.success) {
            const admin = data.data;
            document.querySelector('#modalTitle').textContent = '관리자 수정';
            document.querySelector('#adminId').value = admin.adminId;
            document.querySelector('#adminLoginId').value = admin.loginId;
            document.querySelector('#adminName').value = admin.adminName || '';
            document.querySelector('#adminRole').value = admin.adminRole || 'ADMIN';
            document.querySelector('#adminStatus').value = admin.status || 'Y';

            // 수정 모드: 비밀번호 변경 체크박스 표시, 비밀번호 필드는 기본 비활성화
            document.querySelector('#passwordChangeGroup').style.display = 'block';
            document.querySelector('#passwordChangeCheck').checked = false;
            document.querySelector('#passwordGroup').style.display = 'block';
            document.querySelector('#adminPassword').value = '';
            document.querySelector('#adminPassword').disabled = true;
            document.querySelector('#adminPassword').required = false;

            document.querySelector('#adminModal').style.display = 'block';
        } else {
            alert('관리자 정보 조회 실패: ' + data.message);
        }
    } catch (error) {
        console.error('관리자 조회 오류:', error);
        alert('관리자 정보 조회 중 오류가 발생했습니다.');
    }
}

// 모달 닫기
function closeModal() {
    document.querySelector('#adminModal').style.display = 'none';
    document.querySelector('#adminForm').reset();
}

// 관리자 저장 (추가/수정)
async function saveAdmin() {
    const adminId = document.querySelector('#adminId').value;
    const loginId = document.querySelector('#adminLoginId').value.trim();
    const password = document.querySelector('#adminPassword').value;
    const name = document.querySelector('#adminName').value.trim();
    const role = document.querySelector('#adminRole').value;
    const status = document.querySelector('#adminStatus').value;
    const isPasswordChangeChecked = document.querySelector('#passwordChangeCheck').checked;

    // 유효성 검사
    if (!loginId) {
        alert('로그인 ID를 입력해주세요.');
        return;
    }

    // 추가 모드에서는 비밀번호 필수
    if (!adminId && !password) {
        alert('비밀번호를 입력해주세요.');
        return;
    }

    // 수정 모드에서 비밀번호 변경 체크했는데 입력 안 한 경우
    if (adminId && isPasswordChangeChecked && !password) {
        alert('비밀번호를 입력해주세요.');
        return;
    }

    if (!name) {
        alert('이름을 입력해주세요.');
        return;
    }

    const adminData = {
        loginId: loginId,
        adminName: name,
        adminRole: role,
        status: status
    };

    // 추가 모드이거나, 수정 모드에서 비밀번호 변경 체크한 경우에만 비밀번호 포함
    if (!adminId || (adminId && isPasswordChangeChecked)) {
        if (password) {
            adminData.password = password;
        }
    }

    try {
        let response;
        if (adminId) {
            // 수정
            adminData.adminId = parseInt(adminId);
            response = await fetch(`/busanbank/admin/setting/admins/${adminId}`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(adminData)
            });
        } else {
            // 추가
            response = await fetch('/busanbank/admin/setting/admins', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(adminData)
            });
        }

        const data = await response.json();

        if (data.success) {
            alert(data.message);
            closeModal();
            loadAdminList();
        } else {
            alert(data.message);
        }
    } catch (error) {
        console.error('관리자 저장 오류:', error);
        alert('관리자 저장 중 오류가 발생했습니다.');
    }
}

// 관리자 삭제
async function deleteAdmin(adminId, loginId) {
    if (!confirm(`관리자 '${loginId}'를 삭제하시겠습니까?`)) {
        return;
    }

    try {
        const response = await fetch(`/busanbank/admin/setting/admins/${adminId}`, {
            method: 'DELETE'
        });

        const data = await response.json();

        if (data.success) {
            alert(data.message);
            loadAdminList();
        } else {
            alert('삭제 실패: ' + data.message);
        }
    } catch (error) {
        console.error('관리자 삭제 오류:', error);
        alert('관리자 삭제 중 오류가 발생했습니다.');
    }
}

// 로그인 ID 중복 체크
async function checkLoginIdDuplicate() {
    const adminId = document.querySelector('#adminId').value;
    const loginId = document.querySelector('#adminLoginId').value.trim();

    // 수정 모드거나 값이 없으면 체크하지 않음
    if (adminId || !loginId) {
        return;
    }

    try {
        const response = await fetch(`/busanbank/admin/setting/admins/check-loginid?loginId=${loginId}`);
        const data = await response.json();

        if (data.success && data.isDuplicate) {
            alert('이미 사용 중인 로그인 ID입니다.');
            document.querySelector('#adminLoginId').value = '';
            document.querySelector('#adminLoginId').focus();
        }
    } catch (error) {
        console.error('중복 체크 오류:', error);
    }
}

// ========== 사이트 기본설정 관련 함수 (작성자: 진원, 2025-11-19) ==========

// 사이트 설정 목록 조회
async function loadSiteSettings() {
    try {
        const response = await fetch('/busanbank/admin/setting/site');
        const data = await response.json();

        if (data.success) {
            renderSiteSettingsTable(data.data);
        } else {
            alert('사이트 설정 조회 실패: ' + data.message);
        }
    } catch (error) {
        console.error('사이트 설정 조회 오류:', error);
        alert('사이트 설정 조회 중 오류가 발생했습니다.');
    }
}

// 사이트 설정 테이블 렌더링
function renderSiteSettingsTable(settings) {
    const tbody = document.querySelector('#siteSettingsTableBody');
    if (!tbody) return;

    if (!settings || settings.length === 0) {
        tbody.innerHTML = '<tr><td colspan="6" style="text-align:center; padding: 20px;">등록된 설정이 없습니다.</td></tr>';
        return;
    }

    // settingkey에 대응하는 한글 이름 매핑
    const keyNameMap = {
        'CSPHONEDOMESTIC1': '고객센터 전화번호1',
        'CSPHONEDOMESTIC2': '고객센터 전화번호2',
        'CSPHONEOVERSEAS': '해외 전화번호',
        'COPYRIGHTTEXT': '저작권 문구',
        'APPNAME': '앱 이름',
        'APPVERSION': '앱 버전',
        'SITENAME': '사이트명',
        'COMPANYADDRESS': '회사 주소',
        'COMPANYPHONE': '회사 전화번호'
    };

    tbody.innerHTML = settings.map((setting, index) => {
        const rowClass = index === settings.length - 1 ? 'content_tr_last' : 'content_tr';
        const startStyle = index === settings.length - 1 ? 'style="border-radius: 0 0 0 5px;"' : '';
        const endStyle = index === settings.length - 1 ? 'style="border-radius: 0 0 5px 0;"' : '';

        const keyName = keyNameMap[setting.settingkey] || setting.settingkey;

        return `
            <tr class="${rowClass}">
                <td ${startStyle}>${index + 1}</td>
                <td>${keyName}</td>
                <td>${setting.settingvalue || '-'}</td>
                <td>${setting.settingdesc || '-'}</td>
                <td>${setting.updateddate ? setting.updateddate.substring(0, 10) : '-'}</td>
                <td ${endStyle}>
                    <button class="productList_btn" onclick="openSiteSettingModal('${setting.settingkey}', '${keyName}', '${setting.settingvalue}', '${setting.settingdesc}')">
                        <img src="/busanbank/images/admin/free-icon-pencil-7175371.png" alt="수정 버튼" style="width: 100%;height: 100%;object-fit: contain;">
                    </button>
                </td>
            </tr>
        `;
    }).join('');
}

// 사이트 설정 수정 모달 열기
function openSiteSettingModal(key, keyName, value, desc) {
    document.querySelector('#settingkey').value = key;
    document.querySelector('#settingkeyDisplay').value = keyName;
    document.querySelector('#settingvalue').value = value;
    document.querySelector('#settingdesc').value = desc;

    document.querySelector('#siteSettingModal').style.display = 'block';

    // 저장 버튼 이벤트 등록 (중복 방지를 위해 기존 이벤트 제거 후 재등록)
    const saveBtn = document.querySelector('#saveSiteSettingBtn');
    const newSaveBtn = saveBtn.cloneNode(true);
    saveBtn.parentNode.replaceChild(newSaveBtn, saveBtn);
    newSaveBtn.addEventListener('click', saveSiteSetting);
}

// 사이트 설정 모달 닫기
function closeSiteSettingModal() {
    document.querySelector('#siteSettingModal').style.display = 'none';
    document.querySelector('#siteSettingForm').reset();
}

// 사이트 설정 저장
async function saveSiteSetting() {
    const key = document.querySelector('#settingkey').value;
    const value = document.querySelector('#settingvalue').value.trim();

    if (!value) {
        alert('설정 값을 입력해주세요.');
        return;
    }

    const settingData = {
        settingkey: key,
        settingvalue: value
    };

    try {
        const response = await fetch('/busanbank/admin/setting/site', {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(settingData)
        });

        const data = await response.json();

        if (data.success) {
            alert(data.message);
            closeSiteSettingModal();
            loadSiteSettings(); // 목록 새로고침
        } else {
            alert(data.message);
        }
    } catch (error) {
        console.error('사이트 설정 저장 오류:', error);
        alert('사이트 설정 저장 중 오류가 발생했습니다.');
    }
}

// 모달 외부 클릭 시 닫기 (사이트 설정 모달)
window.addEventListener('click', (e) => {
    const siteModal = document.querySelector('#siteSettingModal');
    if (e.target === siteModal) {
        closeSiteSettingModal();
    }

    const securityModal = document.querySelector('#securitySettingModal');
    if (e.target === securityModal) {
        closeSecuritySettingModal();
    }
});

// ========== 보안 설정 관련 함수 (작성자: 진원, 2025-11-20) ==========

// 보안 설정 목록 조회
async function loadSecuritySettings() {
    try {
        const response = await fetch('/busanbank/admin/setting/security');
        const data = await response.json();

        if (data.success) {
            renderSecuritySettingsTable(data.data);
        } else {
            alert('보안 설정 조회 실패: ' + data.message);
        }
    } catch (error) {
        console.error('보안 설정 조회 오류:', error);
        alert('보안 설정 조회 중 오류가 발생했습니다.');
    }
}

// 보안 설정 테이블 렌더링
function renderSecuritySettingsTable(settings) {
    const tbody = document.querySelector('#securitySettingsTableBody');
    if (!tbody) return;

    if (!settings || settings.length === 0) {
        tbody.innerHTML = '<tr><td colspan="6" style="text-align:center; padding: 20px;">등록된 설정이 없습니다.</td></tr>';
        return;
    }

    // settingkey에 대응하는 한글 이름 매핑
    const keyNameMap = {
        'PASSWORD_MIN_LENGTH': '비밀번호 최소길이',
        'LOGIN_FAIL_LIMIT': '로그인 실패 횟수 제한',
        'SESSION_TIMEOUT': '세션 타임아웃',
        'SSL_CERT_EXPIRY': 'SSL 인증서 만료일'
    };

    tbody.innerHTML = settings.map((setting, index) => {
        const rowClass = index === settings.length - 1 ? 'content_tr_last' : 'content_tr';
        const startStyle = index === settings.length - 1 ? 'style="border-radius: 0 0 0 5px;"' : '';
        const endStyle = index === settings.length - 1 ? 'style="border-radius: 0 0 5px 0;"' : '';

        const keyName = keyNameMap[setting.settingkey] || setting.settingkey;

        // 값 포맷팅 (타입에 따라)
        let displayValue = setting.settingvalue || '-';
        let warningIcon = '';

        if (setting.settingtype === 'NUMBER' && setting.settingkey === 'SESSION_TIMEOUT') {
            displayValue = `${setting.settingvalue}분`;
        } else if (setting.settingtype === 'NUMBER' && setting.settingkey === 'PASSWORD_MIN_LENGTH') {
            displayValue = `${setting.settingvalue}자`;
        } else if (setting.settingtype === 'NUMBER' && setting.settingkey === 'LOGIN_FAIL_LIMIT') {
            displayValue = `${setting.settingvalue}회`;
        } else if (setting.settingtype === 'DATE' && setting.settingkey === 'SSL_CERT_EXPIRY') {
            // SSL 인증서 만료일 체크
            const expiryDate = new Date(setting.settingvalue);
            const today = new Date();
            const daysLeft = Math.ceil((expiryDate - today) / (1000 * 60 * 60 * 24));

            if (daysLeft < 0) {
                warningIcon = '<span style="color: #e74c3c; font-weight: bold; margin-left: 10px;">⚠️ 만료됨</span>';
            } else if (daysLeft <= 30) {
                warningIcon = `<span style="color: #f39c12; font-weight: bold; margin-left: 10px;">⚠️ ${daysLeft}일 남음</span>`;
            }
        }

        return `
            <tr class="${rowClass}">
                <td ${startStyle}>${index + 1}</td>
                <td>${keyName}</td>
                <td>${displayValue}${warningIcon}</td>
                <td>${setting.settingdesc || '-'}</td>
                <td>${setting.updateddate ? setting.updateddate.substring(0, 10) : '-'}</td>
                <td ${endStyle}>
                    <button class="productList_btn" onclick="openSecuritySettingModal('${setting.settingkey}', '${keyName}', '${setting.settingvalue}', '${setting.settingdesc}', '${setting.settingtype}')">
                        <img src="/busanbank/images/admin/free-icon-pencil-7175371.png" alt="수정 버튼" style="width: 100%;height: 100%;object-fit: contain;">
                    </button>
                </td>
            </tr>
        `;
    }).join('');
}

// 보안 설정 수정 모달 열기
function openSecuritySettingModal(key, keyName, value, desc, type) {
    document.querySelector('#securitySettingkey').value = key;
    document.querySelector('#securitySettingkeyDisplay').value = keyName;
    document.querySelector('#securitySettingvalue').value = value;
    document.querySelector('#securitySettingdesc').value = desc;

    document.querySelector('#securitySettingModal').style.display = 'block';

    // 저장 버튼 이벤트 등록 (중복 방지를 위해 기존 이벤트 제거 후 재등록)
    const saveBtn = document.querySelector('#saveSecuritySettingBtn');
    const newSaveBtn = saveBtn.cloneNode(true);
    saveBtn.parentNode.replaceChild(newSaveBtn, saveBtn);
    newSaveBtn.addEventListener('click', saveSecuritySetting);
}

// 보안 설정 모달 닫기
function closeSecuritySettingModal() {
    document.querySelector('#securitySettingModal').style.display = 'none';
    document.querySelector('#securitySettingForm').reset();
}

// 보안 설정 저장
async function saveSecuritySetting() {
    const key = document.querySelector('#securitySettingkey').value;
    const value = document.querySelector('#securitySettingvalue').value.trim();

    if (!value) {
        alert('설정 값을 입력해주세요.');
        return;
    }

    const settingData = {
        settingkey: key,
        settingvalue: value
    };

    try {
        const response = await fetch('/busanbank/admin/setting/security', {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(settingData)
        });

        const data = await response.json();

        if (data.success) {
            alert(data.message);
            closeSecuritySettingModal();
            loadSecuritySettings(); // 목록 새로고침
        } else {
            alert(data.message);
        }
    } catch (error) {
        console.error('보안 설정 저장 오류:', error);
        alert('보안 설정 저장 중 오류가 발생했습니다.');
    }
}
