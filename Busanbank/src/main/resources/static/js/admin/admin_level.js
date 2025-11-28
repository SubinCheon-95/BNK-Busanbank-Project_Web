/**
 * 작성자: 진원
 * 작성일: 2025-11-27
 * 설명: 레벨 설정 관리 JavaScript
 */

let isEditMode = false;

// 페이지 로드 시 레벨 목록 로드
document.addEventListener('DOMContentLoaded', function() {
    loadLevelList();
});

// 레벨 목록 로드
async function loadLevelList() {
    try {
        const response = await fetch('/busanbank/admin/point/levels');
        const result = await response.json();

        if (result.success) {
            renderLevelTable(result.data);
        } else {
            alert(result.message || '레벨 목록을 불러오는데 실패했습니다.');
        }
    } catch (error) {
        console.error('Error loading level list:', error);
        alert('레벨 목록을 불러오는 중 오류가 발생했습니다.');
    }
}

// 테이블 렌더링
function renderLevelTable(levels) {
    const tbody = document.getElementById('level-table-body');
    tbody.innerHTML = '';

    if (!levels || levels.length === 0) {
        tbody.innerHTML = '<tr><td colspan="8" style="text-align:center; padding: 20px;">등록된 레벨이 없습니다.</td></tr>';
        return;
    }

    tbody.innerHTML = levels.map((level, index) => `
        <tr class="${index === levels.length - 1 ? 'content_tr_last' : 'content_tr'}">
            <td${index === levels.length - 1 ? ' style="border-radius: 0 0 0 5px; text-align: center;"' : ' style="text-align: center;"'}>${level.levelId}</td>
            <td style="text-align: center;">${level.levelNumber}</td>
            <td style="text-align: center;">${level.levelName}</td>
            <td style="text-align: center;"><strong style="color: #E1545A;">${level.requiredPoints.toLocaleString()}</strong></td>
            <td style="text-align: center; font-size: 20px;">${level.levelIcon || '-'}</td>
            <td style="text-align: left; padding-left: 10px;">${level.levelDescription || '-'}</td>
            <td style="text-align: center;">${formatDate(level.createdAt)}</td>
            <td${index === levels.length - 1 ? ' style="border-radius: 0 0 5px 0; text-align: center;"' : ' style="text-align: center;"'}>
                <button onclick="editLevel(${level.levelId})" class="productList_btn" title="수정">
                    <img src="/busanbank/images/admin/free-icon-pencil-7175371.png" alt="수정 버튼" style="width: 100%;height: 100%;object-fit: contain;">
                </button>
            </td>
        </tr>
    `).join('');
}

// 레벨 추가 모달 열기
function openAddModal() {
    isEditMode = false;
    document.getElementById('modalTitle').textContent = '레벨 추가';
    document.getElementById('levelForm').reset();
    document.getElementById('levelId').value = '';
    document.getElementById('deleteLevelBtn').style.display = 'none';
    document.getElementById('levelModal').style.display = 'block';
}

// 추가 버튼 이벤트 등록
document.addEventListener('DOMContentLoaded', function() {
    const addBtn = document.getElementById('addLevelBtn');
    if (addBtn) {
        addBtn.addEventListener('click', openAddModal);
    }

    // 모달 닫기 버튼
    const closeBtn = document.querySelector('#levelModal .close');
    if (closeBtn) {
        closeBtn.addEventListener('click', closeModal);
    }

    // 취소 버튼
    const cancelBtn = document.querySelector('#levelModal .btn-cancel');
    if (cancelBtn) {
        cancelBtn.addEventListener('click', closeModal);
    }

    // 저장 버튼
    const saveBtn = document.getElementById('saveLevelBtn');
    if (saveBtn) {
        saveBtn.addEventListener('click', function() {
            document.getElementById('levelForm').dispatchEvent(new Event('submit'));
        });
    }

    // 삭제 버튼
    const deleteBtn = document.getElementById('deleteLevelBtn');
    if (deleteBtn) {
        deleteBtn.addEventListener('click', function() {
            const levelId = document.getElementById('levelId').value;
            if (levelId) {
                deleteLevel(parseInt(levelId));
            }
        });
    }
});

// 레벨 수정
async function editLevel(levelId) {
    try {
        const response = await fetch('/busanbank/admin/point/levels');
        const result = await response.json();

        if (result.success) {
            const level = result.data.find(l => l.levelId === levelId);
            if (level) {
                isEditMode = true;
                document.getElementById('modalTitle').textContent = '레벨 수정';
                document.getElementById('levelId').value = level.levelId;
                document.getElementById('levelNumber').value = level.levelNumber;
                document.getElementById('levelName').value = level.levelName;
                document.getElementById('requiredPoints').value = level.requiredPoints;
                document.getElementById('levelIcon').value = level.levelIcon || '';
                document.getElementById('levelDescription').value = level.levelDescription || '';
                document.getElementById('deleteLevelBtn').style.display = 'block';
                document.getElementById('levelModal').style.display = 'block';
            }
        }
    } catch (error) {
        console.error('Error loading level:', error);
        alert('레벨 정보를 불러오는 중 오류가 발생했습니다.');
    }
}

// 레벨 삭제
async function deleteLevel(levelId) {
    if (!confirm('정말로 이 레벨을 삭제하시겠습니까?\n삭제된 레벨은 복구할 수 없습니다.')) {
        return;
    }

    try {
        const response = await fetch(`/busanbank/admin/point/levels/${levelId}`, {
            method: 'DELETE'
        });
        const result = await response.json();

        if (result.success) {
            alert('레벨이 삭제되었습니다.');
            closeModal();
            loadLevelList();
        } else {
            alert(result.message || '레벨 삭제에 실패했습니다.');
        }
    } catch (error) {
        console.error('Error deleting level:', error);
        alert('레벨 삭제 중 오류가 발생했습니다.');
    }
}

// 모달 닫기
function closeModal() {
    document.getElementById('levelModal').style.display = 'none';
    document.getElementById('levelForm').reset();
}

// 폼 제출
document.getElementById('levelForm').addEventListener('submit', async function(e) {
    e.preventDefault();

    const levelData = {
        levelNumber: parseInt(document.getElementById('levelNumber').value),
        levelName: document.getElementById('levelName').value,
        requiredPoints: parseInt(document.getElementById('requiredPoints').value),
        levelIcon: document.getElementById('levelIcon').value,
        levelDescription: document.getElementById('levelDescription').value
    };

    try {
        let url = '/busanbank/admin/point/levels';
        let method = 'POST';

        if (isEditMode) {
            const levelId = document.getElementById('levelId').value;
            url = `/busanbank/admin/point/levels/${levelId}`;
            method = 'PUT';
        }

        const response = await fetch(url, {
            method: method,
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(levelData)
        });

        const result = await response.json();

        if (result.success) {
            alert(result.message);
            closeModal();
            loadLevelList();
        } else {
            alert(result.message || '저장에 실패했습니다.');
        }
    } catch (error) {
        console.error('Error saving level:', error);
        alert('저장 중 오류가 발생했습니다.');
    }
});

// 날짜 포맷 함수
function formatDate(dateString) {
    if (!dateString) return '-';
    const date = new Date(dateString);
    return date.toLocaleDateString('ko-KR');
}

// 모달 외부 클릭 시 닫기
window.onclick = function(event) {
    const modal = document.getElementById('levelModal');
    if (event.target === modal) {
        closeModal();
    }
}
