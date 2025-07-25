볼트 권한 설정

해당 컨테이너 내부에서 작업

```shell
export VAULT_ADDR=""
export VAULT_TOKEN=""
```

역할 권한 설정

```shell
vault auth enable approle
```

ROLE 의 목록 조회

```shell
vault list auth/approle/role
```

ROLE 설정

```shell
vault write auth/approle/role/<ROLE_NAME> token_policies=<ROLE_POLICIES> token_ttl="1h" token_max_ttl="4h" secret_id_ttl="24h" secret_id_num_uses="10"
```

기존 등록된 권한된 사용자에게 권한 할당

```shell
vault write auth/approle/role/demo-role token_policies="myapp-policy"

vault write auth/approle/role/<ROLE_NAME> token_policies=<ROLE_POLICIES> 
```

생성한 ROLE 의 ID 조회

```shell
 vault read auth/approle/role/<ROLE_NAME>/role-id
```

생성한 ROLE 의 시크릿 ID 조회
> 설정 변경 시 시크릿 ID 재발급 필수

```shell
vault write -f auth/approle/role/<ROLE_NAME>/secret-id
```

ROLE 의 목록 조회

```shell
vault list auth/approle/role
```

ROLE 역할 파일 수정하여 적용

```shell
vault policy write myapp-policy ./policies/myapp-policy.hcl

vault policy write <ROLE_POLICIES> <ROLE_POLICIES_FILE_PATH>
```

slack API 테스트 위한 URL 생성
ngrok 사용

```shell
ngrok config add-authtoken $YOUR_AUTHTOKEN
```

Slack API 연계 시 참고 문서
https://api.slack.com/apps

https://api.slack.com/methods/chat.postMessage

https://api.slack.com/methods/chat.postMessage/test

https://api.slack.com/events/url_verification

https://tools.slack.dev/java-slack-sdk/guides/supported-web-frameworks/?utm_source=chatgpt.com

slack 테스트 앱 삭제

관리 페이지에서 삭제 버튼 클릭 시 삭제 처리

```
https://app.slack.com/apps-manage/T097A5FR2E8/integrations/installed
```

```shell
/invite @gcal
```
