/**
 * 작성자: 진원
 * 작성일: 2025-11-27
 * 설명: 출석 보상 설정 JavaScript
 */

let isEditMode = false;

// 페이지 로드 시 보상 목록 로드
document.addEventListener('DOMContentLoaded', function() {
    loadRewardList();
});

// 보상 목록 로드
async function loadRewardList() {
    try {
        const response = await fetch('/busanbank/admin/point/attendance-rewards');
        const result = await response.json();

        if (result.success) {
            renderRewardTable(result.data);
        } else {
            alert(result.message || '보상 목록을 불러오는데 실패했습니다.');
        }
    } catch (error) {
        console.error('Error loading reward list:', error);
        alert('보상 목록을 불러오는 중 오류가 발생했습니다.');
    }
}

// 테이블 렌더링
function renderRewardTable(rewards) {
    const tbody = document.getElementById('reward-table-body');
    tbody.innerHTML = '';

    if (!rewards || rewards.length === 0) {
        tbody.innerHTML = '<tr><td colspan="7" style="text-align:center; padding: 20px;">등록된 보상이 없습니다.</td></tr>';
        return;
    }

    tbody.innerHTML = rewards.map((reward, index) => `
        <tr class="${index === rewards.length - 1 ? 'content_tr_last' : 'content_tr'}">
            <td${index === rewards.length - 1 ? ' style="border-radius: 0 0 0 5px; text-align: center;"' : ' style="text-align: center;"'}>${reward.rewardId}</td>
            <td style="text-align: center;"><strong style="color: #E1545A;">${reward.consecutiveDays}일차</strong></td>
            <td style="text-align: center;"><strong style="color: #28a745;">${reward.rewardPoints.toLocaleString()}P</strong></td>
            <td style="text-align: left; padding-left: 10px;">${reward.rewardDescription || '-'}</td>
            <td style="text-align: center;">
                <span style="color: ${reward.isActive === 'Y' ? '#28a745' : '#dc3545'};">
                    ${reward.isActive === 'Y' ? '활성' : '비활성'}
                </span>
            </td>
            <td style="text-align: center;">${formatDate(reward.createdAt)}</td>
            <td${index === rewards.length - 1 ? ' style="border-radius: 0 0 5px 0; text-align: center;"' : ' style="text-align: center;"'}>
                <button onclick="editReward(${reward.rewardId})" class="productList_btn" title="수정">
                    <img src="/busanbank/images/admin/free-icon-pencil-7175371.png" alt="수정 버튼" style="width: 100%;height: 100%;object-fit: contain;">
                </button>
            </td>
        </tr>
    `).join('');
}

// 보상 추가 모달 열기
function openAddModal() {
    isEditMode = false;
    document.getElementById('modalTitle').textContent = '출석 보상 추가';
    document.getElementById('rewardForm').reset();
    document.getElementById('rewardId').value = '';
    document.getElementById('isActive').value = 'Y';
    document.getElementById('deleteRewardBtn').style.display = 'none';
    document.getElementById('rewardModal').style.display = 'block';
}

// 추가 버튼 이벤트 등록
document.addEventListener('DOMContentLoaded', function() {
    const addBtn = document.getElementById('addRewardBtn');
    if (addBtn) {
        addBtn.addEventListener('click', openAddModal);
    }

    // 모달 닫기 버튼
    const closeBtn = document.querySelector('#rewardModal .close');
    if (closeBtn) {
        closeBtn.addEventListener('click', closeModal);
    }

    // 취소 버튼
    const cancelBtn = document.querySelector('#rewardModal .btn-cancel');
    if (cancelBtn) {
        cancelBtn.addEventListener('click', closeModal);
    }

    // 저장 버튼
    const saveBtn = document.getElementById('saveRewardBtn');
    if (saveBtn) {
        saveBtn.addEventListener('click', function() {
            document.getElementById('rewardForm').dispatchEvent(new Event('submit'));
        });
    }

    // 삭제 버튼
    const deleteBtn = document.getElementById('deleteRewardBtn');
    if (deleteBtn) {
        deleteBtn.addEventListener('click', function() {
            const rewardId = document.getElementById('rewardId').value;
            if (rewardId) {
                deleteReward(parseInt(rewardId));
            }
        });
    }
});

// 보상 수정
async function editReward(rewardId) {
    try {
        const response = await fetch('/busanbank/admin/point/attendance-rewards');
        const result = await response.json();

        if (result.success) {
            const reward = result.data.find(r => r.rewardId === rewardId);
            if (reward) {
                isEditMode = true;
                document.getElementById('modalTitle').textContent = '출석 보상 수정';
                document.getElementById('rewardId').value = reward.rewardId;
                document.getElementById('consecutiveDays').value = reward.consecutiveDays;
                document.getElementById('rewardPoints').value = reward.rewardPoints;
                document.getElementById('rewardDescription').value = reward.rewardDescription || '';
                document.getElementById('isActive').value = reward.isActive;
                document.getElementById('deleteRewardBtn').style.display = 'block';
                document.getElementById('rewardModal').style.display = 'block';
            }
        }
    } catch (error) {
        console.error('Error loading reward:', error);
        alert('보상 정보를 불러오는 중 오류가 발생했습니다.');
    }
}

// 보상 삭제
async function deleteReward(rewardId) {
    if (!confirm('정말로 이 보상을 삭제하시겠습니까?\n삭제된 보상은 복구할 수 없습니다.')) {
        return;
    }

    try {
        const response = await fetch(`/busanbank/admin/point/attendance-rewards/${rewardId}`, {
            method: 'DELETE'
        });
        const result = await response.json();

        if (result.success) {
            alert('보상이 삭제되었습니다.');
            closeModal();
            loadRewardList();
        } else {
            alert(result.message || '보상 삭제에 실패했습니다.');
        }
    } catch (error) {
        console.error('Error deleting reward:', error);
        alert('보상 삭제 중 오류가 발생했습니다.');
    }
}

// 모달 닫기
function closeModal() {
    document.getElementById('rewardModal').style.display = 'none';
    document.getElementById('rewardForm').reset();
}

// 폼 제출
document.getElementById('rewardForm').addEventListener('submit', async function(e) {
    e.preventDefault();

    const rewardData = {
        consecutiveDays: parseInt(document.getElementById('consecutiveDays').value),
        rewardPoints: parseInt(document.getElementById('rewardPoints').value),
        rewardDescription: document.getElementById('rewardDescription').value,
        isActive: document.getElementById('isActive').value
    };

    try {
        let url = '/busanbank/admin/point/attendance-rewards';
        let method = 'POST';

        if (isEditMode) {
            const rewardId = document.getElementById('rewardId').value;
            url = `/busanbank/admin/point/attendance-rewards/${rewardId}`;
            method = 'PUT';
        }

        const response = await fetch(url, {
            method: method,
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(rewardData)
        });

        const result = await response.json();

        if (result.success) {
            alert(result.message);
            closeModal();
            loadRewardList();
        } else {
            alert(result.message || '저장에 실패했습니다.');
        }
    } catch (error) {
        console.error('Error saving reward:', error);
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
    const modal = document.getElementById('rewardModal');
    if (event.target === modal) {
        closeModal();
    }
}
