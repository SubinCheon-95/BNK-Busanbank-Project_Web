const originalTexts = new Map();

// 페이지 로드 시 저장된 언어로 자동 번역
document.addEventListener('DOMContentLoaded', function() {
    const savedLang = localStorage.getItem('selectedLanguage');
    if (savedLang && savedLang !== 'ko') {
        translatePage(savedLang);
    }
});

async function translatePage(lang) {
    if (lang === 'ko') {
        restoreOriginal();
        localStorage.setItem('selectedLanguage', 'ko');
        return;
    }

    // 언어 저장
    localStorage.setItem('selectedLanguage', lang);

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
        if (!text || text.length < 2) continue;

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

        data.translated.forEach((translated, i) => {
            nodesToUpdate[i].textContent = translated;
        });
    } catch (error) {
        console.error('번역 요청 실패:', error);
    }
}

function restoreOriginal() {
    originalTexts.forEach((original, node) => {
        node.textContent = original;
    });
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