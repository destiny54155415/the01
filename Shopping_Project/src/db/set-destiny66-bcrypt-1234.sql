USE petshop;

-- 타이핑하지 마세요. 아래를 통째로 복사 실행하세요.
-- 핵심: H3O4 다음에 반드시 소문자 i 가 들어갑니다 → H3O4iHi6
UPDATE members
SET password = CONCAT(
        '$2a$10$smu7CPUgH3O4',
        'i',
        'Hi6g63SxeI4fkp91FFgo7t6ra71sXxzyLwQzANpS'
    ),
    role = 'ADMIN'
WHERE username = 'destiny66';

-- 검증: mid4 가 반드시 iHi6 , len 이 60, ok 가 1
SELECT
  username,
  role,
  CHAR_LENGTH(password) AS len,
  MID(password, 20, 4) AS mid4,
  password = CONCAT('$2a$10$smu7CPUgH3O4','i','Hi6g63SxeI4fkp91FFgo7t6ra71sXxzyLwQzANpS') AS ok,
  password
FROM members
WHERE username = 'destiny66';

-- 로그인: destiny66 / 1234
