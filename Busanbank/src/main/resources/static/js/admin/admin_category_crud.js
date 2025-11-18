/**
 * 작성자: 진원
 * 작성일: 2025-11-18
 * 설명: 카테고리 관리 JavaScript
 */

let categories = [];
let filteredCategories = [];
let currentPage = 1;
const pageSize = 10;
let searchKeyword = '';
let parentFilter = '';

// 페이지 로딩 시 실행
document.addEventListener('DOMContentLoaded', () => {
    loadCategoryList();
    initializeEventListeners();
});

// 이벤트 리스너 초기화
function initializeEventListeners() {
    // 검색 버튼
    const searchBtn = document.querySelector('.search_btn');
    const searchInput = document.querySelector('#search_input');

    if (searchBtn) {
        searchBtn.addEventListener('click', () => {
            searchKeyword = searchInput.value.trim();
            currentPage = 1;
            filterAndRenderCategories();
        });
    }

    if (searchInput) {
        searchInput.addEventListener('keypress', (e) => {
            if (e.key === 'Enter') {
                searchKeyword = searchInput.value.trim();
                currentPage = 1;
                filterAndRenderCategories();
            }
        });
    }

    // 부모 카테고리 필터
    const parentFilterSelect = document.querySelector('#parentFilter');
    if (parentFilterSelect) {
        parentFilterSelect.addEventListener('change', () => {
            parentFilter = parentFilterSelect.value;
            currentPage = 1;
            filterAndRenderCategories();
        });
    }

    // 추가 버튼
    const addBtn = document.querySelector('#addCategoryBtn');
    if (addBtn) {
        addBtn.addEventListener('click', openAddModal);
    }

    // 모달 닫기 버튼
    const closeBtn = document.querySelector('.category-modal .close');
    if (closeBtn) {
        closeBtn.addEventListener('click', closeModal);
    }

    // 모달 저장 버튼
    const saveBtn = document.querySelector('#saveCategoryBtn');
    if (saveBtn) {
        saveBtn.addEventListener('click', saveCategory);
    }

    // 모달 외부 클릭 시 닫기
    window.addEventListener('click', (e) => {
        const modal = document.querySelector('#categoryModal');
        if (e.target === modal) {
            closeModal();
        }
    });
}

// 카테고리 목록 조회
async function loadCategoryList() {
    try {
        const response = await fetch('/busanbank/admin/category/list');
        const data = await response.json();

        if (data.success) {
            categories = data.data;
            populateParentCategoryDropdown();
            populateParentFilterDropdown();
            filterAndRenderCategories();
        } else {
            alert('카테고리 목록 조회 실패: ' + data.message);
        }
    } catch (error) {
        console.error('카테고리 목록 조회 오류:', error);
        alert('카테고리 목록 조회 중 오류가 발생했습니다.');
    }
}

// 필터링 및 렌더링
function filterAndRenderCategories() {
    // 검색 및 필터 적용
    filteredCategories = categories.filter(category => {
        let matchSearch = true;
        let matchParent = true;

        // 검색어 필터
        if (searchKeyword) {
            matchSearch = category.categoryName.toLowerCase().includes(searchKeyword.toLowerCase());
        }

        // 부모 카테고리 필터
        if (parentFilter === 'root') {
            // 최상위 카테고리만
            matchParent = !category.parentId;
        } else if (parentFilter) {
            // 특정 부모의 하위 카테고리만
            matchParent = category.parentId && category.parentId === parseInt(parentFilter);
        }

        return matchSearch && matchParent;
    });

    // 페이지네이션 적용
    const totalPages = Math.ceil(filteredCategories.length / pageSize);
    const startIndex = (currentPage - 1) * pageSize;
    const endIndex = startIndex + pageSize;
    const paginatedCategories = filteredCategories.slice(startIndex, endIndex);

    renderCategoryTable(paginatedCategories);
    renderPagination(totalPages, currentPage);
    updateCategoryCount();
}

// 카테고리 수 업데이트
function updateCategoryCount() {
    const countElement = document.querySelector('#categoryCount');
    if (countElement) {
        countElement.textContent = `전체 ${categories.length}개 중 ${filteredCategories.length}개 표시`;
    }
}

// 카테고리 테이블 렌더링
function renderCategoryTable(categoryList) {
    const tbody = document.querySelector('#categoryTableBody');
    if (!tbody) return;

    if (!categoryList || categoryList.length === 0) {
        tbody.innerHTML = '<tr><td colspan="7" style="text-align:center; padding: 20px;">등록된 카테고리가 없습니다.</td></tr>';
        return;
    }

    tbody.innerHTML = categoryList.map((category, index) => {
        const rowClass = index === categoryList.length - 1 ? 'content_tr_last' : 'content_tr';
        const startStyle = index === categoryList.length - 1 ? 'style="border-radius: 0 0 0 5px;"' : '';
        const endStyle = index === categoryList.length - 1 ? 'style="border-radius: 0 0 5px 0;"' : '';

        const parentName = category.parentId ? findCategoryName(category.parentId) : '-';
        const status = category.status === 'Y' ? '<span style="color: #28a745;">활성</span>' : '<span style="color: #dc3545;">비활성</span>';

        return `
            <tr class="${rowClass}">
                <td ${startStyle}>${category.categoryId}</td>
                <td>${category.categoryName}</td>
                <td>${parentName}</td>
                <td>${category.createdAt ? category.createdAt.substring(0, 10) : '-'}</td>
                <td>${category.updatedAt ? category.updatedAt.substring(0, 10) : '-'}</td>
                <td>${status}</td>
                <td ${endStyle}>
                    <button class="productList_btn" onclick="openEditModal(${category.categoryId})">
                        <img src="/busanbank/images/admin/free-icon-pencil-7175371.png" alt="수정 버튼" style="width: 100%;height: 100%;object-fit: contain;">
                    </button>
                    <button class="productList_btn" onclick="deleteCategory(${category.categoryId}, '${category.categoryName}')">
                        <img src="/busanbank/images/admin/cross-mark.png" alt="삭제 버튼" style="width: 100%;height: 100%;object-fit: contain;">
                    </button>
                </td>
            </tr>
        `;
    }).join('');
}

// 카테고리 ID로 이름 찾기
function findCategoryName(categoryId) {
    const category = categories.find(c => c.categoryId === categoryId);
    return category ? category.categoryName : '알 수 없음';
}

// 부모 카테고리 드롭다운 채우기 (모달용)
function populateParentCategoryDropdown() {
    const parentSelect = document.querySelector('#parentId');
    if (!parentSelect || categories.length === 0) return;

    // 기존 옵션 제거 (첫 번째 옵션 제외)
    parentSelect.innerHTML = '<option value="">최상위 카테고리</option>';

    // 카테고리 옵션 추가
    categories.forEach(category => {
        const option = document.createElement('option');
        option.value = category.categoryId;
        option.textContent = category.categoryName;
        parentSelect.appendChild(option);
    });
}

// 부모 카테고리 필터 드롭다운 채우기
function populateParentFilterDropdown() {
    const filterSelect = document.querySelector('#parentFilter');
    if (!filterSelect || categories.length === 0) return;

    // 기존 옵션 유지하고 카테고리 옵션 추가
    filterSelect.innerHTML = `
        <option value="">전체 카테고리</option>
        <option value="root">최상위 카테고리만</option>
    `;

    // 최상위 카테고리만 필터에 추가
    const rootCategories = categories.filter(c => !c.parentId);

    if (rootCategories.length > 0) {
        const separator = document.createElement('option');
        separator.disabled = true;
        separator.textContent = '───────────';
        filterSelect.appendChild(separator);

        rootCategories.forEach(category => {
            const option = document.createElement('option');
            option.value = category.categoryId;
            option.textContent = `${category.categoryName}의 하위`;
            filterSelect.appendChild(option);
        });
    }
}

// 페이지네이션 렌더링
function renderPagination(totalPages, currentPageNum) {
    const paginationDiv = document.querySelector('.page_div .pagenation');
    if (!paginationDiv) return;

    // 페이지가 1개 이하면 페이지네이션 숨김
    if (totalPages <= 1) {
        paginationDiv.innerHTML = '';
        return;
    }

    let html = '';

    // 이전 버튼
    if (currentPageNum > 1) {
        html += `<li><a href="#" class="page2" onclick="changePage(${currentPageNum - 1}); return false;"><span class="prev"></span></a></li>`;
    } else {
        html += `<li><a href="#" class="page2 disabled" style="opacity: 0.3; cursor: not-allowed;"><span class="prev"></span></a></li>`;
    }

    // 페이지 번호
    const startPage = Math.max(1, currentPageNum - 2);
    const endPage = Math.min(totalPages, currentPageNum + 2);

    // 첫 페이지
    if (startPage > 1) {
        html += `<li><a href="#" class="page2" onclick="changePage(1); return false;">1</a></li>`;
        if (startPage > 2) {
            html += `<li><span style="padding: 8px;">...</span></li>`;
        }
    }

    // 중간 페이지들
    for (let i = startPage; i <= endPage; i++) {
        const activeClass = i === currentPageNum ? 'page1 active' : 'page2';
        html += `<li><a href="#" class="${activeClass}" onclick="changePage(${i}); return false;">${i}</a></li>`;
    }

    // 마지막 페이지
    if (endPage < totalPages) {
        if (endPage < totalPages - 1) {
            html += `<li><span style="padding: 8px;">...</span></li>`;
        }
        html += `<li><a href="#" class="page2" onclick="changePage(${totalPages}); return false;">${totalPages}</a></li>`;
    }

    // 다음 버튼
    if (currentPageNum < totalPages) {
        html += `<li><a href="#" class="page2" onclick="changePage(${currentPageNum + 1}); return false;"><span class="next"></span></a></li>`;
    } else {
        html += `<li><a href="#" class="page2 disabled" style="opacity: 0.3; cursor: not-allowed;"><span class="next"></span></a></li>`;
    }

    paginationDiv.innerHTML = html;
}

// 페이지 변경
function changePage(page) {
    currentPage = page;
    filterAndRenderCategories();
}

// 추가 모달 열기
function openAddModal() {
    document.querySelector('#modalTitle').textContent = '카테고리 추가';
    document.querySelector('#categoryForm').reset();
    document.querySelector('#categoryId').value = '';
    populateParentCategoryDropdown();
    document.querySelector('#categoryModal').style.display = 'block';
}

// 수정 모달 열기
async function openEditModal(categoryId) {
    try {
        const response = await fetch(`/busanbank/admin/category/${categoryId}`);
        const data = await response.json();

        if (data.success) {
            const category = data.data;
            document.querySelector('#modalTitle').textContent = '카테고리 수정';
            document.querySelector('#categoryId').value = category.categoryId;
            document.querySelector('#categoryName').value = category.categoryName || '';

            populateParentCategoryDropdown();
            document.querySelector('#parentId').value = category.parentId || '';

            document.querySelector('#categoryModal').style.display = 'block';
        } else {
            alert('카테고리 정보 조회 실패: ' + data.message);
        }
    } catch (error) {
        console.error('카테고리 조회 오류:', error);
        alert('카테고리 정보 조회 중 오류가 발생했습니다.');
    }
}

// 모달 닫기
function closeModal() {
    document.querySelector('#categoryModal').style.display = 'none';
    document.querySelector('#categoryForm').reset();
}

// 카테고리 저장 (추가/수정)
async function saveCategory() {
    const categoryId = document.querySelector('#categoryId').value;
    const categoryName = document.querySelector('#categoryName').value.trim();
    const parentId = document.querySelector('#parentId').value;

    // 유효성 검사
    if (!categoryName) {
        alert('카테고리명을 입력해주세요.');
        return;
    }

    const categoryData = {
        categoryName: categoryName,
        parentId: parentId ? parseInt(parentId) : null
    };

    try {
        let response;
        if (categoryId) {
            // 수정
            categoryData.categoryId = parseInt(categoryId);
            response = await fetch(`/busanbank/admin/category/${categoryId}`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(categoryData)
            });
        } else {
            // 추가
            response = await fetch('/busanbank/admin/category', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(categoryData)
            });
        }

        const data = await response.json();

        if (data.success) {
            alert(data.message);
            closeModal();
            await loadCategoryList();
        } else {
            alert(data.message);
        }
    } catch (error) {
        console.error('카테고리 저장 오류:', error);
        alert('카테고리 저장 중 오류가 발생했습니다.');
    }
}

// 카테고리 삭제
async function deleteCategory(categoryId, categoryName) {
    if (!confirm(`카테고리 '${categoryName}'를 삭제하시겠습니까?\n\n※ 주의: 이 카테고리를 사용하는 상품이 있을 수 있습니다.`)) {
        return;
    }

    try {
        const response = await fetch(`/busanbank/admin/category/${categoryId}`, {
            method: 'DELETE'
        });

        const data = await response.json();

        if (data.success) {
            alert(data.message);
            await loadCategoryList();
        } else {
            alert('삭제 실패: ' + data.message);
        }
    } catch (error) {
        console.error('카테고리 삭제 오류:', error);
        alert('카테고리 삭제 중 오류가 발생했습니다.');
    }
}
