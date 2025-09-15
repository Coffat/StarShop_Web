# StarShop Login Feature Implementation

## Overview
This implementation provides a complete login system for the StarShop flower e-commerce application, following the strict specifications in `rules.mdc`.

## Features Implemented

### 🎨 Frontend (UI)
- **Login Page**: `src/main/resources/templates/login.html`
  - Bootstrap 5.3.3 responsive design
  - Pink theme (#FFB6C1 primary, #FFE4E1 secondary)
  - Sitemesh decorator integration (`decorators/main.html`)
  - Animated falling flower petals (CSS keyframes)
  - Background flower image placeholder
  - OAuth2 login buttons (Google & Facebook)
  - Client-side validation (email regex, 8+ character password)

### 🔧 Backend (API)
- **AuthController**: `/api/auth/login` endpoint
  - JWT token generation on successful authentication
  - BCrypt password validation
  - JSON response format per rules.mdc
  - Error handling with proper HTTP status codes

- **AuthService**: Business logic layer
  - User lookup by email
  - Password validation using BCrypt
  - JWT token generation with user claims

- **JwtService**: JWT token management
  - JJWT 0.12.6 implementation
  - Token generation with role and userId claims
  - Token validation and parsing
  - Configurable expiration (24 hours)

### 🔐 Security
- **SecurityConfig**: Updated with JWT filter
  - Public endpoints: `/api/auth/login`, `/login`, `/static/**`
  - Protected endpoints with role-based access
  - OAuth2 configuration for Google/Facebook
  - JWT filter integration

- **JwtAuthenticationFilter**: Custom filter extending OncePerRequestFilter
  - Bearer token extraction and validation
  - Security context population
  - Role-based authentication

### 🌐 Real-time Features
- **WebSocket Configuration**: SockJS 1.6.1 + Stomp.js 2.3.3
  - Welcome messages after successful login
  - User-specific topics: `/topic/messages/{userId}`
  - Order status updates: `/topic/orders`

### ⚙️ Configuration
- **Dependencies**: Updated `pom.xml` with all required libraries
- **Application Config**: `application.yml` with JWT and OAuth2 settings
- **Database**: PostgreSQL integration with existing User entity

## File Structure
```
src/main/
├── java/com/example/demo/
│   ├── controller/
│   │   ├── AuthController.java      # Login API endpoint
│   │   └── WebController.java       # Login page controller
│   ├── service/
│   │   ├── AuthService.java         # Authentication business logic
│   │   ├── JwtService.java          # JWT token management
│   │   └── WebSocketService.java    # Real-time messaging
│   ├── security/
│   │   └── JwtAuthenticationFilter.java # JWT authentication filter
│   ├── config/
│   │   ├── SecurityConfig.java      # Security configuration
│   │   ├── WebSocketConfig.java     # WebSocket configuration
│   │   └── CustomOAuth2UserService.java # OAuth2 integration
│   └── dto/
│       ├── LoginRequest.java        # Login request DTO
│       ├── ResponseWrapper.java     # API response wrapper
│       └── UserInfoResponse.java    # User info response DTO
└── resources/
    ├── templates/
    │   ├── login.html              # Login page
    │   ├── dashboard.html          # Post-login dashboard
    │   └── decorators/main.html    # Sitemesh decorator
    ├── static/images/              # Static assets (flower background)
    └── application.yml             # Application configuration
```

## Setup Instructions

### 1. Database Setup
Ensure PostgreSQL is running with the database configured in `application.yml`:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/flower_shop_system
    username: flower_admin
    password: flower_password_2024
```

### 2. OAuth2 Configuration
Update `application.yml` with your OAuth2 client credentials:
```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          google:
            client-id: your-google-client-id
            client-secret: your-google-client-secret
          facebook:
            client-id: your-facebook-client-id
            client-secret: your-facebook-client-secret
```

### 3. JWT Secret
Update the JWT secret in `application.yml` for production:
```yaml
jwt:
  secret: your-secure-jwt-secret-key-here
  expiration: 86400000 # 24 hours
```

### 4. Flower Background Image
Add a flower background image to:
`src/main/resources/static/images/flower-bg.jpg`

### 5. Run the Application
```bash
mvn spring-boot:run
```

## API Endpoints

### Authentication
- `POST /api/auth/login` - User login
- `POST /api/auth/refresh` - Token refresh
- `GET /api/auth/validate` - Token validation
- `POST /api/auth/logout` - Logout (client-side)

### Web Pages
- `GET /login` - Login page
- `GET /dashboard` - Dashboard (after login)
- `GET /` - Redirects to login

### OAuth2
- `GET /oauth2/authorization/google` - Google OAuth2 login
- `GET /oauth2/authorization/facebook` - Facebook OAuth2 login

## Testing

### Manual Testing
1. Start the application
2. Navigate to `http://localhost:8080/login`
3. Test login with existing user credentials
4. Verify JWT token generation
5. Test WebSocket welcome message
6. Test OAuth2 login buttons

### Sample Login Request
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john.doe@gmail.com",
    "password": "password123"
  }'
```

## Features Alignment with rules.mdc

✅ **UI Consistency**: Bootstrap 5.3.3, pink theme, Sitemesh decorator
✅ **Database Adherence**: Uses existing User entity from entity-context.json
✅ **Security**: JWT with JJWT 0.12.6, BCrypt password hashing
✅ **Real-time**: WebSocket with SockJS 1.6.1 and Stomp.js 2.3.3
✅ **API Design**: RESTful endpoints with JSON response format
✅ **Error Handling**: Proper HTTP status codes and JSON error responses
✅ **Logging**: SLF4J with structured error logging
✅ **Validation**: Email regex and password length validation

## Commit Message
```
feat: Added login page with JWT and OAuth2, ref rules.mdc

- Implemented login.html with Bootstrap 5.3.3 and pink theme
- Added AuthController with JWT authentication endpoint
- Created JwtService using JJWT 0.12.6 for token management
- Updated SecurityConfig with JWT filter and OAuth2 support
- Integrated WebSocket for welcome messages after login
- Added falling petals animation and flower background
- Following all specifications in rules.mdc strictly
```
