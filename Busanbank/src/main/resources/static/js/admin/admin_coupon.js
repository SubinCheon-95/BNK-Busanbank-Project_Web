/**
 * 작성자: 진원
 * 작성일: 2025-11-27
 * 설명: 쿠폰 관리 JavaScript
 */

let currentPage = 1;
const pageSize = 10;
let codeChecked = false;
let isEditMode = false;

// 페이지 로드 시 초기화
document.addEventListener('DOMContentLoaded', () => {
    loadCategories();
    loadCouponList();
    initializeEventListeners();
});

/**
 * 이벤트 리스너 초기화
 */
function initializeEventListeners() {
    // 검색 입력 필드에서 Enter 키 처리
    document.getElementById('searchInput').addEventListener('keypress', (e) => {
        if (e.key === 'Enter') {
            searchCoupons();
        }
    });

    // 쿠폰 코드 변경 시 중복 체크 상태 초기화
    document.getElementById('couponCode').addEventListener('input', () => {
        codeChecked = false;
        document.getElementById('codeValidation').innerHTML = '';
    });

    // 모달 외부 클릭 시 닫기
    window.onclick = (event) => {
        const modal = document.getElementById('couponModal');
        if (event.target === modal) {
            closeModal();
        }
    };
}

/**
 * 카테고리 목록 로드 및 체크박스 생성
 */
function loadCategories() {
    const container = document.getElementById('categoryCheckboxes');
    container.innerHTML = '';

    if (window.categoriesData && window.categoriesData.length > 0) {
        window.categoriesData.forEach(category => {
            const checkboxItem = document.createElement('div');
            checkboxItem.className = 'checkbox-item';
            checkboxItem.innerHTML = `
                <input type="checkbox" id="cat_${category.categoryId}" name="categoryIds" value="${category.categoryId}">
                <label for="cat_${category.categoryId}" style="margin: 0; font-weight: normal; cursor: pointer;">
                    ${category.categoryName}
                </label>
            `;
            container.appendChild(checkboxItem);
        });
    } else {
        container.innerHTML = '<p style="color: #999;">카테고리가 없습니다.</p>';
    }
}

/**
 * 쿠폰 목록 조회
 */
async function loadCouponList(page = 1) {
    currentPage = page;
    const searchKeyword = document.getElementById('searchInput').value.trim();
    const isActive = document.getElementById('statusFilter').value;

    try {
        const response = await fetch(`/busanbank/admin/coupon/coupons?page=${page}&size=${pageSize}&searchKeyword=${encodeURIComponent(searchKeyword)}&isActive=${isActive}`);
        const data = await response.json();

        if (data.success) {
            renderCouponTable(data.data);
            renderPagination(data.totalPages, data.currentPage);
        } else {
            alert('쿠폰 목록을 불러오는데 실패했습니다.');
        }
    } catch (error) {
        console.error('Error loading coupon list:', error);
        alert('쿠폰 목록 조회 중 오류가 발생했습니다.');
    }
}

/**
 * 쿠폰 테이블 렌더링
 */
function renderCouponTable(coupons) {
    const tbody = document.getElementById('couponTableBody');
    tbody.innerHTML = '';

    if (!coupons || coupons.length === 0) {
        tbody.innerHTML = '<tr><td colspan="10" style="text-align: center; padding: 50px;">등록된 쿠폰이 없습니다.</td></tr>';
        return;
    }

    coupons.forEach((coupon, index) => {
        const rowNum = (currentPage - 1) * pageSize + index + 1;
        const availableCount = coupon.maxUsageCount === 0 ? '무제한' : `${coupon.currentUsageCount} / ${coupon.maxUsageCount}`;
        const statusBadge = coupon.isActive === 'Y'
            ? '<span class="status-badge status-active">활성</span>'
            : '<span class="status-badge status-inactive">비활성</span>';

        const row = document.createElement('tr');
        row.innerHTML = `
            <td style="text-align: center;">${rowNum}</td>
            <td style="text-align: center;">${coupon.couponCode}</td>
            <td>${coupon.couponName}</td>
            <td style="text-align: center;">+${coupon.rateIncrease}%p</td>
            <td>${coupon.categoryNames || '-'}</td>
            <td style="text-align: center;">${availableCount}</td>
            <td style="text-align: center;">${coupon.validFromStr}</td>
            <td style="text-align: center;">${coupon.validToStr}</td>
            <td style="text-align: center;">${statusBadge}</td>
            <td style="text-align: center;">
                <button class="btn btn-primary btn-toggle" onclick="editCoupon(${coupon.couponId})">수정</button>
                <button class="btn ${coupon.isActive === 'Y' ? 'btn-secondary' : 'btn-primary'} btn-toggle"
                        onclick="toggleCoupon(${coupon.couponId}, '${coupon.isActive === 'Y' ? 'N' : 'Y'}')">
                    ${coupon.isActive === 'Y' ? '비활성' : '활성'}
                </button>
                <button class="btn btn-delete btn-toggle" onclick="deleteCoupon(${coupon.couponId})">삭제</button>
            </td>
        `;
        tbody.appendChild(row);
    });
}

/**
 * 페이징 렌더링
 */
function renderPagination(totalPages, currentPage) {
    const pagination = document.getElementById('pagination');
    pagination.innerHTML = '';

    if (totalPages <= 1) return;

    // 이전 버튼
    if (currentPage > 1) {
        const prevBtn = document.createElement('button');
        prevBtn.textContent = '이전';
        prevBtn.onclick = () => loadCouponList(currentPage - 1);
        pagination.appendChild(prevBtn);
    }

    // 페이지 번호
    const startPage = Math.max(1, currentPage - 2);
    const endPage = Math.min(totalPages, currentPage + 2);

    for (let i = startPage; i <= endPage; i++) {
        const pageBtn = document.createElement('button');
        pageBtn.textContent = i;
        pageBtn.className = i === currentPage ? 'active' : '';
        pageBtn.onclick = () => loadCouponList(i);
        pagination.appendChild(pageBtn);
    }

    // 다음 버튼
    if (currentPage < totalPages) {
        const nextBtn = document.createElement('button');
        nextBtn.textContent = '다음';
        nextBtn.onclick = () => loadCouponList(currentPage + 1);
        pagination.appendChild(nextBtn);
    }
}

/**
 * 검색
 */
function searchCoupons() {
    loadCouponList(1);
}

/**
 * 쿠폰 등록 모달 열기
 */
function openAddModal() {
    isEditMode = false;
    codeChecked = false;
    document.getElementById('modalTitle').textContent = '쿠폰 등록';
    document.getElementById('couponForm').reset();
    document.getElementById('couponId').value = '';
    document.getElementById('isActive').checked = true;
    document.getElementById('codeValidation').innerHTML = '';

    // 모든 카테고리 체크박스 해제
    document.querySelectorAll('input[name="categoryIds"]').forEach(cb => cb.checked = false);

    document.getElementById('couponModal').style.display = 'block';
}

/**
 * 쿠폰 수정 모달 열기
 */
async function editCoupon(couponId) {
    isEditMode = true;
    codeChecked = true;
    document.getElementById('modalTitle').textContent = '쿠폰 수정';
    document.getElementById('codeValidation').innerHTML = '';

    try {
        const response = await fetch(`/busanbank/admin/coupon/coupons/${couponId}`);
        const data = await response.json();

        if (data.success) {
            const coupon = data.data;
            document.getElementById('couponId').value = coupon.couponId;
            document.getElementById('couponCode').value = coupon.couponCode;
            document.getElementById('couponName').value = coupon.couponName;
            document.getElementById('description').value = coupon.description || '';
            document.getElementById('rateIncrease').value = coupon.rateIncrease;
            document.getElementById('maxUsageCount').value = coupon.maxUsageCount;
            document.getElementById('validFrom').value = coupon.validFromStr;
            document.getElementById('validTo').value = coupon.validToStr;
            document.getElementById('isActive').checked = coupon.isActive === 'Y';

            // 카테고리 체크박스 설정
            document.querySelectorAll('input[name="categoryIds"]').forEach(cb => {
                cb.checked = coupon.categoryIds && coupon.categoryIds.includes(parseInt(cb.value));
            });

            document.getElementById('couponModal').style.display = 'block';
        } else {
            alert('쿠폰 정보를 불러오는데 실패했습니다.');
        }
    } catch (error) {
        console.error('Error loading coupon:', error);
        alert('쿠폰 정보 조회 중 오류가 발생했습니다.');
    }
}

/**
 * 모달 닫기
 */
function closeModal() {
    document.getElementById('couponModal').style.display = 'none';
    document.getElementById('couponForm').reset();
    codeChecked = false;
}

/**
 * 쿠폰 코드 중복 체크
 */
async function checkDuplicate() {
    const couponCode = document.getElementById('couponCode').value.trim();
    const validationDiv = document.getElementById('codeValidation');

    if (!couponCode) {
        validationDiv.innerHTML = '<span class="error">쿠폰 코드를 입력해주세요.</span>';
        return;
    }

    try {
        const response = await fetch(`/busanbank/admin/coupon/check-code?couponCode=${encodeURIComponent(couponCode)}`);
        const data = await response.json();

        if (data.success) {
            if (data.isDuplicate && !isEditMode) {
                validationDiv.innerHTML = '<span class="error">이미 사용 중인 쿠폰 코드입니다.</span>';
                codeChecked = false;
            } else {
                validationDiv.innerHTML = '<span class="success">사용 가능한 쿠폰 코드입니다.</span>';
                codeChecked = true;
            }
        } else {
            validationDiv.innerHTML = '<span class="error">중복 체크에 실패했습니다.</span>';
            codeChecked = false;
        }
    } catch (error) {
        console.error('Error checking duplicate:', error);
        validationDiv.innerHTML = '<span class="error">중복 체크 중 오류가 발생했습니다.</span>';
        codeChecked = false;
    }
}

/**
 * 쿠폰 저장 (등록/수정)
 */
async function saveCoupon() {
    // 유효성 검사
    const couponCode = document.getElementById('couponCode').value.trim();
    const couponName = document.getElementById('couponName').value.trim();
    const rateIncrease = document.getElementById('rateIncrease').value;
    const validFrom = document.getElementById('validFrom').value;
    const validTo = document.getElementById('validTo').value;

    if (!couponCode || !couponName || !rateIncrease || !validFrom || !validTo) {
        alert('필수 항목을 모두 입력해주세요.');
        return;
    }

    // 신규 등록 시 중복 체크 확인
    if (!isEditMode && !codeChecked) {
        alert('쿠폰 코드 중복 확인을 해주세요.');
        return;
    }

    // 선택된 카테고리 확인
    const categoryIds = Array.from(document.querySelectorAll('input[name="categoryIds"]:checked'))
        .map(cb => parseInt(cb.value));

    if (categoryIds.length === 0) {
        alert('적용할 카테고리를 하나 이상 선택해주세요.');
        return;
    }

    // 유효기간 검증
    if (new Date(validFrom) > new Date(validTo)) {
        alert('유효기간 종료일은 시작일보다 이후여야 합니다.');
        return;
    }

    // 요청 데이터 구성
    const couponData = {
        couponCode: couponCode,
        couponName: couponName,
        description: document.getElementById('description').value.trim(),
        rateIncrease: parseFloat(rateIncrease),
        maxUsageCount: parseInt(document.getElementById('maxUsageCount').value),
        validFromStr: validFrom,
        validToStr: validTo,
        isActive: document.getElementById('isActive').checked ? 'Y' : 'N',
        categoryIds: categoryIds
    };

    try {
        const couponId = document.getElementById('couponId').value;
        const url = couponId
            ? `/busanbank/admin/coupon/coupons/${couponId}`
            : '/busanbank/admin/coupon/coupons';
        const method = couponId ? 'PUT' : 'POST';

        const response = await fetch(url, {
            method: method,
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(couponData)
        });

        const data = await response.json();

        if (data.success) {
            alert(data.message || '쿠폰이 저장되었습니다.');
            closeModal();
            loadCouponList(currentPage);
        } else {
            alert(data.message || '쿠폰 저장에 실패했습니다.');
        }
    } catch (error) {
        console.error('Error saving coupon:', error);
        alert('쿠폰 저장 중 오류가 발생했습니다.');
    }
}

/**
 * 쿠폰 활성화/비활성화
 */
async function toggleCoupon(couponId, isActive) {
    const action = isActive === 'Y' ? '활성화' : '비활성화';
    if (!confirm(`정말 이 쿠폰을 ${action}하시겠습니까?`)) {
        return;
    }

    try {
        const response = await fetch(`/busanbank/admin/coupon/coupons/${couponId}/toggle?isActive=${isActive}`, {
            method: 'PATCH'
        });

        const data = await response.json();

        if (data.success) {
            alert(data.message || `쿠폰이 ${action}되었습니다.`);
            loadCouponList(currentPage);
        } else {
            alert(data.message || `쿠폰 ${action}에 실패했습니다.`);
        }
    } catch (error) {
        console.error('Error toggling coupon:', error);
        alert(`쿠폰 ${action} 중 오류가 발생했습니다.`);
    }
}

/**
 * 쿠폰 삭제
 */
async function deleteCoupon(couponId) {
    if (!confirm('정말 이 쿠폰을 삭제하시겠습니까?\n삭제된 쿠폰은 복구할 수 없습니다.')) {
        return;
    }

    try {
        const response = await fetch(`/busanbank/admin/coupon/coupons/${couponId}`, {
            method: 'DELETE'
        });

        const data = await response.json();

        if (data.success) {
            alert(data.message || '쿠폰이 삭제되었습니다.');
            loadCouponList(currentPage);
        } else {
            alert(data.message || '쿠폰 삭제에 실패했습니다.');
        }
    } catch (error) {
        console.error('Error deleting coupon:', error);
        alert('쿠폰 삭제 중 오류가 발생했습니다.');
    }
}
