-- petshop 선택 후 실행
USE petshop;

-- 1) 현재 destiny66 비밀번호 상태 확인
SELECT
  username,
  role,
  CASE
    WHEN password IS NULL THEN 'NULL'
    WHEN password = '' THEN 'EMPTY'
    WHEN password LIKE '$2a$%' OR password LIKE '$2b$%' OR password LIKE '$2y$%'
      THEN CONCAT('BCrypt / 길이=', CHAR_LENGTH(password), ' / ', LEFT(password, 29))
    ELSE CONCAT('평문 가능 / 값=', password)
  END AS password_status
FROM members
WHERE username = 'destiny66';

-- 2) 먼저 평문 1234 로 테스트 (MemberService 가 equals 비교면 이게 통함)
UPDATE members
SET password = '1234'
WHERE username = 'destiny66';

-- 로그인 테스트: destiny66 / 1234
-- 성공하면 → 회원 서비스가 평문 비교 중 (BCrypt UPDATE 는 안 맞음)
-- 실패하면 → 아래 3) 실행

-- 3) BCrypt 1234 로 다시 설정 (MemberService 가 passwordEncoder.matches 쓰면 이게 통함)
-- UPDATE members
-- SET password = '$2a$10$smu7CPUgH3O4iHi6g63SxeI4fkp91FFgo7t6ra71sXxzyLwQzANpS'
-- WHERE username = 'destiny66';
