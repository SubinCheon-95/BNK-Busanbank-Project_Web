const originalTexts = new Map();

// 페이지 로드 시 저장된 언어로 자동 번역 (세션 내에서만 유지)
document.addEventListener('DOMContentLoaded', function() {
    const savedLang = sessionStorage.getItem('selectedLanguage');
    if (savedLang && savedLang !== 'ko') {
        // translatePage(savedLang);
    }
});

async function translatePage(lang) {
    if (lang === 'ko') {
        restoreOriginal();
        sessionStorage.setItem('selectedLanguage', 'ko'); // sessionStorage 사용
        return;
    }

    // 언어 저장 (세션 동안만 유지)
    sessionStorage.setItem('selectedLanguage', lang);

    // 언어 코드 매핑 (DeepL 형식으로)
    const langMap = {
        'en': 'EN',
        'jp': 'JA',
        'cn': 'ZH'
    };

    const targetLang = langMap[lang];

    if (!targetLang) {
        console.error('지원하지 않는 언어:', lang);
        return;
    }

    const textNodes = getTextNodes(document.body);
    const textsToTranslate = [];
    const nodesToUpdate = [];

    for (let node of textNodes) {
        const text = node.textContent.trim();

        if (!text || text.length < 1) continue;

        const parent = node.parentElement;

        // GNB 영역 - a 태그 내부 텍스트만 허용
        if (parent.closest('.gnb') && parent.tagName !== 'A') continue;

        // Util 영역 - a 태그 내부 텍스트만 허용
        if (parent.closest('.util') && parent.tagName !== 'A') continue;

        // Global 드롭다운은 번역하지 않음 (언어 이름 고정)
        if (parent.closest('.global-dropdown')) continue;

        // 원본 텍스트 저장
        if (!originalTexts.has(node)) {
            originalTexts.set(node, text);
        }

        textsToTranslate.push(originalTexts.get(node));
        nodesToUpdate.push(node);
    }

    try {
        const res = await fetch('/busanbank/api/translate', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                texts: textsToTranslate,
                targetLang: targetLang
            })
        });

        if (!res.ok) {
            console.error('번역 API 에러:', res.status);
            return;
        }

        const data = await res.json();

        // 텍스트 노드만 변경 (nodeValue 사용하여 DOM 구조 유지)
        data.translated.forEach((translated, i) => {
            const node = nodesToUpdate[i];
            // textContent 대신 nodeValue 사용 (더 안전)
            node.nodeValue = translated;
        });

        document.body.setAttribute("data-lang", lang);

        // DOM 업데이트 완료를 보장하기 위해 다음 프레임까지 대기
        await new Promise(resolve => requestAnimationFrame(resolve));
        await new Promise(resolve => requestAnimationFrame(resolve));

        // GNB 강제 재초기화
        forceGnbRefresh();

        // 커스텀 이벤트 발생
        window.dispatchEvent(new CustomEvent('translationComplete'));

    } catch (error) {
        console.error('번역 요청 실패:', error);
    }
}

function restoreOriginal() {
    originalTexts.forEach((original, node) => {
        // nodeValue 사용하여 원본 복원
        node.nodeValue = original;
    });

    document.body.removeAttribute("data-lang");

    // GNB 강제 재초기화
    forceGnbRefresh();

    window.dispatchEvent(new CustomEvent('translationComplete'));
}

// GNB 강제 새로고침 함수
function forceGnbRefresh() {
    // 모든 활성 메뉴 닫기
    document.querySelectorAll('.menu-item.active').forEach(item => {
        item.classList.remove('active');
    });

    // DOM 강제 리플로우
    document.querySelector('.gnb').offsetHeight;
}

function getTextNodes(element) {
    const nodes = [];
    const walker = document.createTreeWalker(
        element,
        NodeFilter.SHOW_TEXT,
        {
            acceptNode: function(node) {
                // 부모가 script, style 태그면 제외
                const parent = node.parentElement;
                if (!parent) return NodeFilter.FILTER_REJECT;

                const tagName = parent.tagName.toLowerCase();
                if (tagName === 'script' || tagName === 'style') {
                    return NodeFilter.FILTER_REJECT;
                }

                // 실제 텍스트가 있는 것만
                if (node.textContent.trim()) {
                    return NodeFilter.FILTER_ACCEPT;
                }
                return NodeFilter.FILTER_REJECT;
            }
        }
    );

    while (walker.nextNode()) {
        nodes.push(walker.currentNode);
    }
    return nodes;
}