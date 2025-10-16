

## Server Port and Session/Cookie

- Server port
  - Value: `server.port=8080`
  - Source:
```84:92:src/main/resources/application.yml
server:
  port: 8080
  servlet:
    session:
      cookie:
        name: JSESSIONID
        http-only: false
        secure: false
        same-site: lax
        max-age: 3600
```

- `@Value` default for `server.port`
  - Value: `@Value("${server.port:8080}")`
  - Source:
```24:26:src/main/java/com/example/demo/config/SwaggerConfig.java
    @Value("${server.port:8080}")
    private String serverPort;
```

## OAuth2 (Google/Facebook)

- Google client credentials and scopes
  - Values:
    - `client-id=45091665731-js8rgkgu5c662khuebpcieikh47eps6t.apps.googleusercontent.com`
    - `client-secret=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`
    - `scope=profile,email`
  - Source:
```35:41:src/main/resources/application.yml
  security:
    oauth2:
      client:
        registration:
          google:
            client-id: 45091665731-js8rgkgu5c662khuebpcieikh47eps6t.apps.googleusercontent.com
            client-secret: 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
            scope: profile,email
```

- Facebook client credentials, provider URLs
  - Values:
    - `client-id=1220413069770726`
    - `client-secret=c5b7d1d07a1564b8bf592045bafbc144`
    - Provider endpoints (auth/token/user-info): `https://www.facebook.com/v18.0/dialog/oauth`, `https://graph.facebook.com/v18.0/oauth/access_token`, `https://graph.facebook.com/v18.0/me?...`
  - Source:
```41:51:src/main/resources/application.yml
          facebook:
            client-id: 1220413069770726
            client-secret: c5b7d1d07a1564b8bf592045bafbc144
            scope: email,public_profile
            redirect-uri: "{baseUrl}/login/oauth2/code/{registrationId}"
        provider:
          facebook:
            authorization-uri: https://www.facebook.com/v18.0/dialog/oauth
            token-uri: https://graph.facebook.com/v18.0/oauth/access_token
            user-info-uri: https://graph.facebook.com/v18.0/me?fields=id,name,email,picture
```

- Facebook avatar URL construction in code
  - Value: `https://graph.facebook.com/<id>/picture?type=large`
  - Source:
```131:134:src/main/java/com/example/demo/config/CustomOAuth2UserService.java
            String id = (String) attributes.get("id");
            if (id != null) {
                return "https://graph.facebook.com/" + id + "/picture?type=large";
```

## Mail (SMTP)

- Gmail SMTP settings and credentials
  - Values: `host=smtp.gmail.com`, `port=587`, `username=starshop.a.6868@gmail.com`, `password=mqai nigf sweg imek`, `from=starshop.a.6868@gmail.com`
  - Source:
```63:83:src/main/resources/application.yml
mail:
  host: smtp.gmail.com
  port: 587
  username: starshop.a.6868@gmail.com
  password: mqai nigf sweg imek
  ...
  from: starshop.a.6868@gmail.com
```

- Default mail `from` in code
  - Value: `@Value("${spring.mail.from:starshop.a.6868@gmail.com}")`
  - Source:
```20:21:src/main/java/com/example/demo/service/EmailService.java
    @Value("${spring.mail.from:starshop.a.6868@gmail.com}")
    private String fromEmail;
```

## JWT

- Secret and expiration
  - Values: `jwt.secret=starshop_jwt_secret_key_2024_very_secure_key_for_production_use`, `jwt.expiration=86400000`
  - Source:
```84:87:src/main/resources/application.yml
jwt:
  secret: starshop_jwt_secret_key_2024_very_secure_key_for_production_use
  expiration: 86400000
```

## Payment (MoMo Test)

- Base URL, credentials, callback URLs, endpoints
  - Values:
    - `momo.base-url=https://test-payment.momo.vn`
    - `partner-code=MOMO`
    - `access-key=F8BBA842ECF85`
    - `secret-key=K951B6PE1waDMi640xX08PD3vg6EkVlz`
    - `return-url=${VSCODE_FORWARD_URL:http://localhost:8080}/payment/momo/return`
    - `notify-url=${VSCODE_FORWARD_URL:http://localhost:8080}/payment/momo/notify`
    - `endpoint-create=/v2/gateway/api/create`, `endpoint-query=/v2/gateway/api/query`
  - Source:
```120:129:src/main/resources/application.yml
momo:
  base-url: https://test-payment.momo.vn
  partner-code: MOMO
  access-key: F8BBA842ECF85
  secret-key: K951B6PE1waDMi640xX08PD3vg6EkVlz
  return-url: ${VSCODE_FORWARD_URL:http://localhost:8080}/payment/momo/return
  notify-url: ${VSCODE_FORWARD_URL:http://localhost:8080}/payment/momo/notify
  endpoint-create: /v2/gateway/api/create
  endpoint-query: /v2/gateway/api/query
```



## VS Code Forwarding Defaults

- Forward URL default
  - Value: `${VSCODE_FORWARD_URL:http://localhost:8080}`
  - Source:
```131:135:src/main/resources/application.yml
vscode:
  forward:
    url: ${VSCODE_FORWARD_URL:http://localhost:8080}
```

## GHN (Shipping) Configuration

- Base URL, token/shop defaults, FROM location defaults, endpoints
  - Values:
    - `ghn.base-url=https://online-gateway.ghn.vn/shiip/public-api`
    - `ghn.token=${GHN_TOKEN:cd8acb1f-9e9c-11f0-b0e8-f2157cd9069b}`
    - `ghn.shop-id=${GHN_SHOP_ID:6040148}`
    - `from.province-id=${GHN_FROM_PROVINCE_ID:202}`, `district-id=${GHN_FROM_DISTRICT_ID:3695}`, `ward-code=${GHN_FROM_WARD_CODE:90745}`
  - Source:
```136:151:src/main/resources/application.yml
ghn:
  base-url: https://online-gateway.ghn.vn/shiip/public-api
  token: ${GHN_TOKEN:cd8acb1f-9e9c-11f0-b0e8-f2157cd9069b}
  shop-id: ${GHN_SHOP_ID:6040148}
  ...
  from:
    province-id: ${GHN_FROM_PROVINCE_ID:202}
    district-id: ${GHN_FROM_DISTRICT_ID:3695}
    ward-code: ${GHN_FROM_WARD_CODE:90745}
```

- Docs note (alternate example defaults)
  - Values: `shop-id default shown as 4983244`, token example repeated
  - Source:
```95:106:docs/GHN_INTEGRATION.md
ghn:
  base-url: https://online-gateway.ghn.vn/shiip/public-api
  token: ${GHN_TOKEN:cd8acb1f-9e9c-11f0-b0e8-f2157cd9069b}
  shop-id: ${GHN_SHOP_ID:4983244}
```

## Gemini (AI) Configuration

- API key default, base URL, model, generation params
  - Values:
    - `gemini.api-key=${GEMINI_API_KEY:AIzaSyB4tCAr_Y5VqAGzpVF01_lc6fEOCbyd9Oo}`
    - `gemini.base-url=https://generativelanguage.googleapis.com/v1beta`
    - `model=gemini-2.5-flash`, `temperature=0.5`, `max-tokens=2048`, `timeout-seconds=15`
  - Source:
```153:160:src/main/resources/application.yml
gemini:
  api-key: ${GEMINI_API_KEY:AIzaSyB4tCAr_Y5VqAGzpVF01_lc6fEOCbyd9Oo}
  base-url: https://generativelanguage.googleapis.com/v1beta
  model: gemini-2.5-flash
  temperature: 0.5
  max-tokens: 2048
  timeout-seconds: 15
```

## Swagger/OpenAPI Server URLs

- Local and production server entries
  - Values: `http://localhost:${server.port}`, `https://api.flowershop.com`
  - Source:
```58:65:src/main/java/com/example/demo/config/SwaggerConfig.java
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + serverPort)
                                .description("Local Development Server"),
                        new Server()
                                .url("https://api.flowershop.com")
                                .description("Production Server (if available)")
                ))
```

## WebSocket/SockJS

- SockJS client library URL
  - Value: `https://cdn.jsdelivr.net/npm/sockjs-client@1/dist/sockjs.min.js`
  - Source:
```35:40:src/main/java/com/example/demo/config/WebSocketConfig.java
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .setHandshakeHandler(new DefaultHandshakeHandler())
                .withSockJS()
                .setClientLibraryUrl("https://cdn.jsdelivr.net/npm/sockjs-client@1/dist/sockjs.min.js");
```

## CORS Allowed Origins

- Allowed origins (frontend dev)
  - Values: `http://localhost:3000`, `http://localhost:4200`
  - Source:
```13:15:src/main/java/com/example/demo/config/WebConfig.java
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:3000", "http://localhost:4200")
```

## File Upload Directory Default

- `@Value` default for upload directory
  - Value: `@Value("${app.upload.dir:uploads}")`
  - Source:
```45:46:src/main/java/com/example/demo/service/ProductService.java
    @Value("${app.upload.dir:uploads}")
    private String uploadDir;
```

## Public/Internal URLs in Docs/Scripts/Templates

- Swagger UI and API docs URLs, app URLs
  - Values: `http://localhost:8080/swagger-ui.html`, `http://localhost:8080/v3/api-docs`, `http://localhost:8080`
  - Sources:
```101:105:README.md
Swagger UI truy cập tại: `http://localhost:8080/swagger-ui.html`
```
```238:241:README.md
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
```
```174:176:README.md
Ứng dụng chạy tại: `http://localhost:8080`
```





## Notes and Risk Assessment

- Items marked above include real-looking credentials and keys within `application.yml` (DB password, SMTP password, OAuth2 client secrets, JWT secret, MoMo keys, GHN token default, Gemini API key default). These should be externalized to environment variables or a secret manager for non-development environments.
- Defaults in `@Value` (e.g., upload dir, mail from, server port) are typical for development but should be reviewed for production suitability.
- Numerous `localhost` URLs appear across configs, scripts, and docs; ensure they are overridden appropriately in deployment.

## Coverage

Sources covered: `src/main/resources/application.yml`, Java configs/services with `@Value`, `docker-compose.yml`, `dev.sh`, `dev-vscode.sh`, `docs/GHN_INTEGRATION.md`, `README.md`, and SQL seeds under `docker/init/`. A quick sweep for `ws://`/`wss://` and inline Bearer tokens found no hardcoded examples in source.

## Controllers & Services Scan

- Additional `@Value` usages in services (linking to config keys already listed above):
  - JWT configuration referenced in service
```26:31:src/main/java/com/example/demo/service/JwtService.java
    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private Long jwtExpiration;
```

- No hardcoded external URLs or tokens found in controllers or other services beyond those already documented. CORS and Swagger server URLs are configured in `WebConfig` and `SwaggerConfig` (see sections above).


