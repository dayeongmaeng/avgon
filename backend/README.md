# AvgOn 백엔드

Java 21과 Spring Boot 4 기반의 모놀리식 백엔드입니다.

현재 단계에는 PostgreSQL 테이블 설계를 반영한 JPA 도메인 모델과 순수 도메인 테스트만 포함합니다. REST API, 인증 설정, Flyway SQL 마이그레이션, 가격 조회 스케줄러와 푸시 발송 구현은 아직 포함하지 않습니다.

## 검증

```bash
./gradlew test
```

Windows에서는 다음 명령을 사용합니다.

```powershell
.\gradlew.bat test
```
