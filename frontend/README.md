# 평단온 프론트엔드

웹과 iOS에서 공유하는 Expo Router 기반 MVP입니다. 현재 다음 흐름을 직접 테스트할 수 있습니다.

- 현재 평단가·수량과 추가 매수가·수량을 이용한 변경 평단가 계산
- 내 종목 생성과 기준 보유 정보 확인
- 종목 상세의 매수 이력 추가·수정·삭제 및 전체 평단가 재계산
- 평단가 도달 알림 설정 UI

내 종목 데이터는 아직 서버 대신 메모리 데모 API를 사용하므로 개발 서버를 다시 시작하면 초기화됩니다. 알림 토글도 실제 푸시를 발송하지 않습니다.

## 실행

Node.js 20.19.4 이상과 pnpm이 필요합니다.

```bash
pnpm install
pnpm web
```

iPhone에서 테스트할 때는 같은 네트워크에서 `pnpm start`를 실행하고 Expo Go로 QR 코드를 스캔합니다.

## 검증

```bash
pnpm typecheck
pnpm test
pnpm build:web
```
