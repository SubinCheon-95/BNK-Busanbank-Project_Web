-- Quiz 모듈 테스트용 초기 데이터
-- 참고: 애플리케이션 실행 시 JPA가 자동으로 테이블을 생성합니다.
-- 이 파일은 테스트 데이터 삽입용입니다.

-- 1. 샘플 퀴즈 데이터 삽입 (금융 상식)
INSERT INTO quiz (question, options_json, correct_answer, explanation, category, difficulty, created_date, updated_date)
VALUES (
    '예금자 보호법에 따라 1인당 보호되는 예금 한도는 얼마인가요?',
    '["3천만원", "5천만원", "7천만원", "1억원"]',
    1,
    '예금자보호법은 금융기관이 파산하거나 영업이 정지될 경우 예금자를 보호하기 위해 1인당 최대 5천만원까지 보호합니다.',
    'FINANCE',
    2,
    SYSTIMESTAMP,
    SYSTIMESTAMP
);

INSERT INTO quiz (question, options_json, correct_answer, explanation, category, difficulty, created_date, updated_date)
VALUES (
    '주식 투자에서 PER(주가수익비율)이 의미하는 것은?',
    '["주가 대비 자산가치", "주가 대비 순이익", "배당 수익률", "자기자본 수익률"]',
    1,
    'PER(Price Earnings Ratio)은 주가를 주당순이익(EPS)으로 나눈 값으로, 주가가 1주당 수익의 몇 배로 거래되는지를 나타냅니다.',
    'INVESTMENT',
    3,
    SYSTIMESTAMP,
    SYSTIMESTAMP
);

INSERT INTO quiz (question, options_json, correct_answer, explanation, category, difficulty, created_date, updated_date)
VALUES (
    '적금과 예금의 차이점은 무엇인가요?',
    '["적금은 한번에, 예금은 나누어 납입", "예금은 한번에, 적금은 나누어 납입", "둘 다 같다", "적금만 이자가 붙는다"]',
    1,
    '예금은 목돈을 한번에 맡기고 만기에 원금과 이자를 받는 것이며, 적금은 매월 일정 금액을 나누어 납입하는 상품입니다.',
    'SAVINGS',
    1,
    SYSTIMESTAMP,
    SYSTIMESTAMP
);

INSERT INTO quiz (question, options_json, correct_answer, explanation, category, difficulty, created_date, updated_date)
VALUES (
    '신용점수를 올리는 방법으로 올바른 것은?',
    '["대출을 많이 받는다", "카드 결제일을 자주 연체한다", "카드 대금을 제때 납부한다", "여러 카드사에 동시 신청한다"]',
    2,
    '신용점수는 대출금과 카드 대금을 제때 납부하면 상승합니다. 연체는 신용점수 하락의 주요 원인입니다.',
    'CREDIT',
    2,
    SYSTIMESTAMP,
    SYSTIMESTAMP
);

INSERT INTO quiz (question, options_json, correct_answer, explanation, category, difficulty, created_date, updated_date)
VALUES (
    '주택담보대출의 LTV 비율이 70%라는 것의 의미는?',
    '["주택 가격의 70%까지 대출 가능", "이자율이 70%", "대출 기간이 70개월", "대출 한도가 70만원"]',
    0,
    'LTV(Loan To Value)는 담보가치 대비 대출비율로, 주택 가격의 70%까지 대출받을 수 있다는 의미입니다.',
    'LOAN',
    3,
    SYSTIMESTAMP,
    SYSTIMESTAMP
);

INSERT INTO quiz (question, options_json, correct_answer, explanation, category, difficulty, created_date, updated_date)
VALUES (
    '복리 효과가 가장 큰 경우는?',
    '["단기 투자", "장기 투자", "투자 기간과 무관", "원금이 클수록"]',
    1,
    '복리는 이자에 이자가 붙는 효과로, 투자 기간이 길수록 복리 효과가 커집니다.',
    'INVESTMENT',
    2,
    SYSTIMESTAMP,
    SYSTIMESTAMP
);

INSERT INTO quiz (question, options_json, correct_answer, explanation, category, difficulty, created_date, updated_date)
VALUES (
    '금리가 상승하면 채권 가격은 어떻게 되나요?',
    '["상승한다", "하락한다", "변동 없다", "두 배가 된다"]',
    1,
    '금리와 채권 가격은 반대로 움직입니다. 금리가 오르면 기존 채권의 가치가 떨어지므로 채권 가격은 하락합니다.',
    'INVESTMENT',
    4,
    SYSTIMESTAMP,
    SYSTIMESTAMP
);

INSERT INTO quiz (question, options_json, correct_answer, explanation, category, difficulty, created_date, updated_date)
VALUES (
    '정기예금의 중도해지 시 어떤 손실이 발생하나요?',
    '["원금 손실", "이자 손실", "수수료 부과", "손실 없음"]',
    1,
    '정기예금을 만기 전에 중도해지하면 약정된 금리보다 낮은 중도해지 금리가 적용되어 이자 손실이 발생합니다.',
    'SAVINGS',
    2,
    SYSTIMESTAMP,
    SYSTIMESTAMP
);

INSERT INTO quiz (question, options_json, correct_answer, explanation, category, difficulty, created_date, updated_date)
VALUES (
    'ISA(개인종합자산관리계좌)의 주요 장점은?',
    '["세금 혜택", "높은 금리 보장", "원금 보장", "무제한 납입"]',
    0,
    'ISA 계좌는 다양한 금융상품을 하나의 계좌로 관리할 수 있으며, 일정 금액까지 비과세 혜택을 받을 수 있습니다.',
    'FINANCE',
    3,
    SYSTIMESTAMP,
    SYSTIMESTAMP
);

INSERT INTO quiz (question, options_json, correct_answer, explanation, category, difficulty, created_date, updated_date)
VALUES (
    '신용카드 리볼빙의 위험성은?',
    '["포인트 적립 감소", "연회비 증가", "높은 이자 부담", "사용 한도 감소"]',
    2,
    '리볼빙은 카드 대금을 분할 납부하는 것으로, 연 15~20%의 높은 이자가 부과되어 장기적으로 큰 부담이 됩니다.',
    'CREDIT',
    3,
    SYSTIMESTAMP,
    SYSTIMESTAMP
);

-- 2. 테스트용 사용자 레벨 데이터 (userId = 1 가정)
-- 실제로는 USERS 테이블에 사용자가 먼저 등록되어야 합니다.
INSERT INTO user_level (user_id, total_points, current_level, tier, interest_bonus, created_date, updated_date)
VALUES (1, 0, 1, 'Rookie', 0.00, SYSTIMESTAMP, SYSTIMESTAMP);

-- 커밋
COMMIT;

-- 확인 쿼리
SELECT COUNT(*) as quiz_count FROM quiz;
SELECT * FROM quiz ORDER BY quiz_id;
SELECT * FROM user_level;
