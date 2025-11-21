/**
 * 작성자: 진원
 * 작성일: 2025-11-16
 * 설명: 상품 관리 JavaScript
 */

let currentPage = 1;
const pageSize = 10;
let searchKeyword = '';
let productTypeFilter = '';
let categories = []; // 카테고리 목록
let currentSortType = ''; // 현재 정렬 타입
let productListData = []; // 전체 상품 데이터 저장

// 페이지 로딩 시 실행
document.addEventListener('DOMContentLoaded', () => {
    loadCategories();
    loadProductList();
    initializeEventListeners();
});

// 이벤트 리스너 초기화
function initializeEventListeners() {
    // 검색 버튼
    const searchBtn = document.querySelector('#productList .search_btn');
    const searchInput = document.querySelector('#productList .search_input');

    if (searchBtn) {
        searchBtn.addEventListener('click', () => {
            searchKeyword = searchInput.value.trim();
            currentPage = 1;
            loadProductList();
        });
    }

    if (searchInput) {
        searchInput.addEventListener('keypress', (e) => {
            if (e.key === 'Enter') {
                searchKeyword = searchInput.value.trim();
                currentPage = 1;
                loadProductList();
            }
        });
    }

    // 상품 유형 필터
    const typeFilter = document.querySelector('#productTypeFilter');
    if (typeFilter) {
        typeFilter.addEventListener('change', () => {
            productTypeFilter = typeFilter.value;
            currentPage = 1;
            loadProductList();
        });
    }

    // 추가 버튼
    const addBtn = document.querySelector('#addProductBtn');
    if (addBtn) {
        addBtn.addEventListener('click', openAddModal);
    }

    // 모달 닫기 버튼
    const closeBtn = document.querySelector('.product-modal .close');
    if (closeBtn) {
        closeBtn.addEventListener('click', closeModal);
    }

    // 모달 저장 버튼
    const saveBtn = document.querySelector('#saveProductBtn');
    if (saveBtn) {
        saveBtn.addEventListener('click', saveProduct);
    }

    // 모달 외부 클릭 시 닫기
    window.addEventListener('click', (e) => {
        const modal = document.querySelector('#productModal');
        if (e.target === modal) {
            closeModal();
        }
    });

    // 상품명 중복 체크
    const productNameInput = document.querySelector('#productName');
    if (productNameInput) {
        productNameInput.addEventListener('blur', checkProductNameDuplicate);
    }

    // 상품 유형 변경 시 필드 토글
    const productTypeSelect = document.querySelector('#productType');
    if (productTypeSelect) {
        productTypeSelect.addEventListener('change', toggleProductTypeFields);
    }

    // 정렬 버튼
    const sortButtons = document.querySelectorAll('#productList .title_btn[data-sort]');
    sortButtons.forEach(btn => {
        btn.addEventListener('click', () => {
            const sortType = btn.getAttribute('data-sort');
            sortProductList(sortType);

            // 활성화 표시
            sortButtons.forEach(b => b.classList.remove('active'));
            btn.classList.add('active');
        });
    });
}

// 카테고리 목록 조회
async function loadCategories() {
    try {
        const response = await fetch('/busanbank/admin/product/categories');
        const data = await response.json();

        if (data.success) {
            categories = data.data;
            populateCategoryDropdown();
        } else {
            console.error('카테고리 목록 조회 실패:', data.message);
        }
    } catch (error) {
        console.error('카테고리 목록 조회 오류:', error);
    }
}

// 카테고리 드롭다운 채우기
function populateCategoryDropdown() {
    const categorySelect = document.querySelector('#categoryId');
    if (!categorySelect || categories.length === 0) return;

    // 기존 옵션 제거 (첫 번째 옵션 제외)
    categorySelect.innerHTML = '<option value="">카테고리 선택 (선택사항)</option>';

    // 카테고리 옵션 추가
    categories.forEach(category => {
        const option = document.createElement('option');
        option.value = category.categoryId;
        option.textContent = category.categoryName;
        categorySelect.appendChild(option);
    });
}

// 상품 목록 조회
async function loadProductList() {
    try {
        const response = await fetch(`/busanbank/admin/product/products?page=${currentPage}&size=${pageSize}&searchKeyword=${searchKeyword}&productType=${productTypeFilter}`);
        const data = await response.json();

        if (data.success) {
            renderProductTable(data.data);
            renderPagination(data.totalPages, data.currentPage);
        } else {
            alert('상품 목록 조회 실패: ' + data.message);
        }
    } catch (error) {
        console.error('상품 목록 조회 오류:', error);
        alert('상품 목록 조회 중 오류가 발생했습니다.');
    }
}

// 상품 테이블 렌더링
function renderProductTable(productList) {
    productListData = productList; // 전역 변수에 저장
    const tbody = document.querySelector('#productTableBody');
    if (!tbody) return;

    if (!productList || productList.length === 0) {
        tbody.innerHTML = '<tr><td colspan="12" style="text-align:center; padding: 20px;">등록된 상품이 없습니다.</td></tr>';
        return;
    }

    tbody.innerHTML = productList.map((product, index) => {
        const rowClass = index === productList.length - 1 ? 'content_tr_last' : 'content_tr';
        const startStyle = index === productList.length - 1 ? 'style="border-radius: 0 0 0 5px;"' : '';
        const endStyle = index === productList.length - 1 ? 'style="border-radius: 0 0 5px 0;"' : '';

        const productTypeName = product.productType === '01' ? '예금' : product.productType === '02' ? '적금' : '-';
        const categoryName = product.categoryName || '-';
        const rate = product.baseRate || '-';
        const term = product.productType === '01' ? '-' : (product.savingTerm ? `${product.savingTerm}개월` : '-');
        const status = product.status === 'Y' ? '판매중' : '판매중지';
        const description = product.description || '설명이 없습니다.';

        return `
            <tr class="${rowClass}">
                <td ${startStyle}>${(currentPage - 1) * pageSize + index + 1}</td>
                <td style="cursor: pointer; color: #E1545A; font-weight: 500;" onclick="toggleDescription(${index})">${product.productName}</td>
                <td>${productTypeName}</td>
                <td>${categoryName}</td>
                <td>${rate}%</td>
                <td>${term}</td>
                <td>${status}</td>
                <td>${product.createdAt ? product.createdAt.substring(0, 10) : '-'}</td>
                <td>${product.updatedAt ? product.updatedAt.substring(0, 10) : '-'}</td>
                <td>-</td>
                <td>-</td>
                <td ${endStyle}>
                    <button class="productList_btn" onclick="openEditModal(${product.productNo})">
                        <img src="/busanbank/images/admin/free-icon-pencil-7175371.png" alt="편집 버튼" style="width: 100%;height: 100%;object-fit: contain;">
                    </button>
                </td>
            </tr>
            <tr id="desc-row-${index}" style="display: none;">
                <td colspan="12" style="background-color: #f9f9f9; padding: 15px; text-align: left; border-top: 1px solid #ddd;">
                    <strong>상품 설명:</strong> ${description}
                </td>
            </tr>
        `;
    }).join('');
}

// 상품 설명 토글
function toggleDescription(index) {
    const descRow = document.getElementById(`desc-row-${index}`);
    if (descRow) {
        descRow.style.display = descRow.style.display === 'none' ? 'table-row' : 'none';
    }
}

// 페이징 렌더링
function renderPagination(totalPages, currentPageNum) {
    const paginationUl = document.querySelector('#productList .pagenation');
    if (!paginationUl) return;

    let html = '';

    // 이전 버튼
    if (currentPageNum > 1) {
        html += `<li><a href="#" class="page2" onclick="changePage(${currentPageNum - 1}); return false;"><span class="prev"></span></a></li>`;
    }

    // 페이지 번호
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
    loadProductList();
}

// 추가 모달 열기
function openAddModal() {
    document.querySelector('#modalTitle').textContent = '상품 추가';
    document.querySelector('#productForm').reset();
    document.querySelector('#productNo').value = '';
    document.querySelector('#deleteProductBtn').style.display = 'none'; // 삭제 버튼 숨기기
    document.querySelector('#productModal').style.display = 'block';
    toggleProductTypeFields();
}

// 수정 모달 열기
async function openEditModal(productNo) {
    try {
        const response = await fetch(`/busanbank/admin/product/products/${productNo}`);
        const data = await response.json();

        if (data.success) {
            const product = data.data;
            document.querySelector('#modalTitle').textContent = '상품 수정';
            document.querySelector('#productNo').value = product.productNo;
            document.querySelector('#productName').value = product.productName || '';
            document.querySelector('#productType').value = product.productType || '01';
            document.querySelector('#categoryId').value = product.categoryId || '';
            document.querySelector('#description').value = product.description || '';
            document.querySelector('#baseRate').value = product.baseRate || '';
            document.querySelector('#maturityRate').value = product.maturityRate || '';
            document.querySelector('#earlyTerminateRate').value = product.earlyTerminateRate || '';
            document.querySelector('#monthlyAmount').value = product.monthlyAmount || '';
            document.querySelector('#savingTerm').value = product.savingTerm || '';
            document.querySelector('#depositAmount').value = product.depositAmount || '';
            document.querySelector('#interestMethod').value = product.interestMethod || '';
            document.querySelector('#payCycle').value = product.payCycle || '';
            document.querySelector('#endDate').value = product.endDate || '';

            document.querySelector('#deleteProductBtn').style.display = 'block'; // 삭제 버튼 표시
            toggleProductTypeFields();
            document.querySelector('#productModal').style.display = 'block';
        } else {
            alert('상품 정보 조회 실패: ' + data.message);
        }
    } catch (error) {
        console.error('상품 조회 오류:', error);
        alert('상품 정보 조회 중 오류가 발생했습니다.');
    }
}

// 모달 닫기
function closeModal() {
    document.querySelector('#productModal').style.display = 'none';
    document.querySelector('#productForm').reset();
}

// 상품 유형에 따른 필드 토글
function toggleProductTypeFields() {
    const productType = document.querySelector('#productType').value;
    const savingFields = document.querySelector('#savingFields');
    const depositFields = document.querySelector('#depositFields');

    if (productType === '02') { // 적금
        savingFields.style.display = 'block';
        depositFields.style.display = 'none';
        document.querySelector('#monthlyAmount').required = true;
        document.querySelector('#savingTerm').required = true;
        document.querySelector('#depositAmount').required = false;
    } else { // 예금
        savingFields.style.display = 'none';
        depositFields.style.display = 'block';
        document.querySelector('#monthlyAmount').required = false;
        document.querySelector('#savingTerm').required = false;
        document.querySelector('#depositAmount').required = true;
    }
}

// 상품 저장 (추가/수정)
async function saveProduct() {
    const productNo = document.querySelector('#productNo').value;
    const productName = document.querySelector('#productName').value.trim();
    const productType = document.querySelector('#productType').value;
    const categoryId = parseInt(document.querySelector('#categoryId').value) || 0;
    const description = document.querySelector('#description').value.trim();
    const baseRate = parseFloat(document.querySelector('#baseRate').value) || 0;
    const maturityRate = parseFloat(document.querySelector('#maturityRate').value) || 0;
    const earlyTerminateRate = parseFloat(document.querySelector('#earlyTerminateRate').value) || 0;
    const interestMethod = document.querySelector('#interestMethod').value;
    const payCycle = document.querySelector('#payCycle').value;
    const endDate = document.querySelector('#endDate').value;

    // 유효성 검사
    if (!productName) {
        alert('상품명을 입력해주세요.');
        return;
    }

    const productData = {
        productName: productName,
        productType: productType,
        categoryId: categoryId,
        description: description,
        baseRate: baseRate,
        maturityRate: maturityRate,
        earlyTerminateRate: earlyTerminateRate,
        interestMethod: interestMethod,
        payCycle: payCycle,
        endDate: endDate,
        adminId: 1 // TODO: 실제 로그인한 관리자 ID로 변경
    };

    if (productType === '02') { // 적금
        const monthlyAmount = parseFloat(document.querySelector('#monthlyAmount').value) || 0;
        const savingTerm = parseInt(document.querySelector('#savingTerm').value) || 0;
        productData.monthlyAmount = monthlyAmount;
        productData.savingTerm = savingTerm;
    } else { // 예금
        const depositAmount = parseFloat(document.querySelector('#depositAmount').value) || 0;
        productData.depositAmount = depositAmount;
    }

    try {
        let response;
        if (productNo) {
            // 수정
            productData.productNo = parseInt(productNo);
            response = await fetch(`/busanbank/admin/product/products/${productNo}`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(productData)
            });
        } else {
            // 추가
            response = await fetch('/busanbank/admin/product/products', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(productData)
            });
        }

        const data = await response.json();

        if (data.success) {
            alert(data.message);
            closeModal();
            loadProductList();
        } else {
            alert(data.message);
        }
    } catch (error) {
        console.error('상품 저장 오류:', error);
        alert('상품 저장 중 오류가 발생했습니다.');
    }
}

// 상품 삭제
async function deleteProduct(productNo, productName) {
    if (!confirm(`상품 '${productName}'를 삭제하시겠습니까?`)) {
        return;
    }

    try {
        const response = await fetch(`/busanbank/admin/product/products/${productNo}`, {
            method: 'DELETE'
        });

        const data = await response.json();

        if (data.success) {
            alert(data.message);
            loadProductList();
        } else {
            alert('삭제 실패: ' + data.message);
        }
    } catch (error) {
        console.error('상품 삭제 오류:', error);
        alert('상품 삭제 중 오류가 발생했습니다.');
    }
}

// 모달에서 현재 상품 삭제
async function deleteCurrentProduct() {
    const productNo = document.querySelector('#productNo').value;
    const productName = document.querySelector('#productName').value;

    if (!productNo) {
        alert('삭제할 상품이 없습니다.');
        return;
    }

    if (!confirm(`상품 '${productName}'를 삭제하시겠습니까?`)) {
        return;
    }

    try {
        const response = await fetch(`/busanbank/admin/product/products/${productNo}`, {
            method: 'DELETE'
        });

        const data = await response.json();

        if (data.success) {
            alert(data.message);
            closeModal();
            loadProductList();
        } else {
            alert('삭제 실패: ' + data.message);
        }
    } catch (error) {
        console.error('상품 삭제 오류:', error);
        alert('상품 삭제 중 오류가 발생했습니다.');
    }
}

// 상품명 중복 체크
async function checkProductNameDuplicate() {
    const productNo = document.querySelector('#productNo').value;
    const productName = document.querySelector('#productName').value.trim();

    // 수정 모드거나 값이 없으면 체크하지 않음
    if (productNo || !productName) {
        return;
    }

    try {
        const response = await fetch(`/busanbank/admin/product/products/check-name?productName=${encodeURIComponent(productName)}`);
        const data = await response.json();

        if (data.success && data.isDuplicate) {
            alert('이미 존재하는 상품명입니다.');
            document.querySelector('#productName').value = '';
            document.querySelector('#productName').focus();
        }
    } catch (error) {
        console.error('중복 체크 오류:', error);
    }
}

// 상품 목록 정렬
function sortProductList(sortType) {
    if (!productListData || productListData.length === 0) {
        return;
    }

    let sortedList = [...productListData];

    switch(sortType) {
        case 'name':
            // 상품명순 (가나다순)
            sortedList.sort((a, b) => a.productName.localeCompare(b.productName));
            break;
        case 'rate':
            // 금리순 (높은 순)
            sortedList.sort((a, b) => (b.baseRate || 0) - (a.baseRate || 0));
            break;
        case 'date':
            // 등록일순 (최신순)
            sortedList.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));
            break;
    }

    currentSortType = sortType;
    renderProductTable(sortedList);
}
