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
 * 쿠폰 테이블 렌더링 (작성자: 진원, 수정일: 2025-12-01)
 */
function renderCouponTable(coupons) {
    const tbody = document.getElementById('couponTableBody');
    tbody.innerHTML = '';

    if (!coupons || coupons.length === 0) {
        tbody.innerHTML = '<tr><td colspan="8" style="text-align: center; padding: 20px;">등록된 쿠폰이 없습니다.</td></tr>';
        return;
    }

    tbody.innerHTML = coupons.map((coupon, index) => {
        const isLast = index === coupons.length - 1;
        const trClass = isLast ? 'content_tr_last' : 'content_tr';
        const rowNum = (currentPage - 1) * pageSize + index + 1;
        const availableCount = coupon.maxUsageCount === 0 ? '무제한' : `${coupon.currentUsageCount} / ${coupon.maxUsageCount}`;
        const statusColor = coupon.isActive === 'Y' ? '#28a745' : '#6c757d';
        const statusText = coupon.isActive === 'Y' ? '활성' : '비활성';
        const validPeriod = `${coupon.validFromStr} ~ ${coupon.validToStr}`;

        return `
            <tr class="${trClass}">
                <td${isLast ? ' style="border-radius: 0 0 0 5px; text-align: center;"' : ' style="text-align: center;"'}>${rowNum}</td>
                <td style="text-align: center;">${coupon.couponCode}</td>
                <td style="padding-left: 10px; text-align: left;">${coupon.couponName}</td>
                <td style="text-align: center;">+${coupon.rateIncrease}%p</td>
                <td style="text-align: center;">${availableCount}</td>
                <td style="text-align: center; font-size: 13px;">${validPeriod}</td>
                <td style="text-align: center;"><span style="color: ${statusColor}; font-weight: bold;">${statusText}</span></td>
                <td${isLast ? ' style="border-radius: 0 0 5px 0; text-align: center;"' : ' style="text-align: center;"'}>
                    <button onclick="editCoupon(${coupon.couponId})" class="productList_btn" title="수정">
                        <img src="/busanbank/images/admin/free-icon-pencil-7175371.png" alt="수정 버튼" style="width: 100%; height: 100%; object-fit: contain;">
                    </button>
                </td>
            </tr>
        `;
    }).join('');
}

/**
 * 페이징 렌더링 (작성자: 진원, 수정일: 2025-12-01)
 */
function renderPagination(totalPages, current) {
    const pagination = document.getElementById('pagination');
    let html = '';

    if (!totalPages || totalPages === 0) {
        pagination.innerHTML = '';
        return;
    }

    // 이전 페이지
    if (current > 1) {
        html += `<li><a href="#" onclick="loadCouponList(${current - 1}); return false;" class="page2"><span class="prev"></span></a></li>`;
    }

    // 페이지 번호
    for (let i = 1; i <= totalPages; i++) {
        if (i === current) {
            html += `<li><a href="#" class="page1" style="font-weight: bold; background-color: #E1545A; color: white;">${i}</a></li>`;
        } else {
            html += `<li><a href="#" onclick="loadCouponList(${i}); return false;" class="page1">${i}</a></li>`;
        }
    }

    // 다음 페이지
    if (current < totalPages) {
        html += `<li><a href="#" onclick="loadCouponList(${current + 1}); return false;" class="page2"><span class="next"></span></a></li>`;
    }

    pagination.innerHTML = html;
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
    document.getElementById('deleteBtn').style.display = 'none'; // 삭제 버튼 숨기기

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
    document.getElementById('deleteBtn').style.display = 'block'; // 삭제 버튼 표시

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
 * 쿠폰 삭제 (작성자: 진원, 수정일: 2025-12-01)
 */
async function deleteCoupon() {
    const couponId = document.getElementById('couponId').value;

    if (!couponId) {
        alert('삭제할 쿠폰이 없습니다.');
        return;
    }

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
            closeModal();
            loadCouponList(currentPage);
        } else {
            alert(data.message || '쿠폰 삭제에 실패했습니다.');
        }
    } catch (error) {
        console.error('Error deleting coupon:', error);
        alert('쿠폰 삭제 중 오류가 발생했습니다.');
    }
}
