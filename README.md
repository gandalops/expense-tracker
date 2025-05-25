## 🚀 DevOps Journey & Learnings

This project is actively being used to enhance my practical DevOps skills by integrating modern CI/CD and infrastructure practices into a full-stack application.

### 🧠 What I'm Learning (DevOps Perspective)

| 🧩 Area                          | 💡 What I’m Implementing                                                                                                           |
| -------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------- |
| **CI/CD Pipelines**              | Jenkins-based pipeline to build backend (Spring Boot), run tests, build frontend (React), and push Docker images to Docker Hub     |
| **Dockerization**                | Containerizing both backend and frontend using separate Dockerfiles                                                                |
| **Docker Compose**               | Managing full stack as a single service stack for local development                                                                |
| **Kubernetes Deployment**        | Writing and applying `Deployment`, `Service`, and `Ingress` manifests for backend/frontend services on Minikube                    |
| **Secrets Management**           | Injecting DB and email credentials via Kubernetes Secrets and Docker environment variables                                         |
| **Static Code & Image Scanning** | Using SonarQube for code quality and Trivy for Docker image vulnerability scanning                                                 |
| **Cloud Hosting (Planned)**      | Future plans include provisioning AWS EC2 and EKS clusters using Terraform, with infrastructure stored in a GitOps-friendly format |
| **Monitoring (Planned)**         | Adding Prometheus/Grafana for real-time application and infrastructure monitoring                                                  |

---

### 📌 GitHub Collaboration

> This project is **collaboratively developed and maintained** using multiple GitHub roles:
>
> * 👨‍💻 Development work via [`devyogi7579`](https://github.com/devyogi7579)
> * 🛠️ DevOps orchestration and pipeline management via [`gandalops`](https://github.com/gandalops)

---

## 🛠 Troubleshooting Guide

### 1. Java Version Mismatch
**Error**:  
`Fatal error compiling: error: release version 21 not supported`  
or  
`UnsupportedClassVersionError`

**Symptoms**:
- Maven build fails
- Backend won't start (missing "Tomcat started on port 8081" message)
- Spring Boot 3.2+ applications fail to compile

**Root Cause**:  
Project requires Java 21 but system has older version (e.g., Java 17):  
```bash
java --version  # Shows 17.x instead of 21.x
```

#### Solution for Java 17 (Linux):
```bash
# 1. Verify current Java version
java -version  # Should show 17.x.x

# 2. If needed, install Java 17
sudo apt update
sudo apt install openjdk-17-jdk

# 3. Set as default Java version
sudo update-alternatives --config java
# Select Java 17 from the list

# 4. Update pom.xml for Java 17 compatibility
```
Edit `pom.xml`:
```xml
<properties>
    <java.version>17</java.version>
    <!-- Downgrade Spring Boot if needed -->
    <spring-boot.version>2.7.x</spring-boot.version>
</properties>
```

**Verification**:
```bash
# Confirm Java version
mvn --version | grep "Java version"

# Clean and rebuild
./mvnw clean package

# Start with environment variables
export SPRING_PROFILES_ACTIVE=dev
./mvnw spring-boot:run
```

**Important Notes**:
1. Spring Boot 3.x requires Java 17+ (21 recommended)
2. If using Spring Boot 2.x, Java 17 will work perfectly
3. You may need to adjust some dependencies in `pom.xml`

**Common Issues**:
- Mixed Java versions causing conflicts
  ```bash
  # List all Java installations
  sudo update-alternatives --list java
  ```
- Maven cache problems
  ```bash
  # Clear Maven cache if builds fail
  rm -rf ~/.m2/repository
  ```

**Alternative**:  
For production environments, consider using:
```bash
# Run with specific Java version
JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64 ./mvnw spring-boot:run
```

---

### 2. Authentication 401 Errors

#### A) Basic 401 (Endpoint Access)
**Symptoms**:  
- Frontend fails authentication with 401  
- Backend logs: "Full authentication is required"  

**Solution**:  
1. Confirm public endpoints in `WebSecurityConfig.java`:
```java
private static final String[] PUBLIC_ENDPOINTS = {
    "/api/mywallet/auth/**",  // Must include
    "/swagger-ui/**"
};
```

2. Verify frontend base URL:
```javascript
// auth.config.js
const API_BASE_URL = "http://localhost:8081/api/mywallet"; // No trailing slash!
```

---

#### B) JWT-Specific 401 (Token Flow)
**Error Message**:  
`AuthEntryPointJwt: Unauthorized error: Full authentication is required`

**Root Cause**:  
- JWT filter intercepts login requests  
- Deadlock: Need token to auth → Need auth to get token  

**Solution**:  
1. Update `AuthTokenFilter.java`:
```java
protected void doFilterInternal(...) {
    String path = request.getServletPath();
    if (path.startsWith("/auth/")) {  // Skip all auth endpoints
        chain.doFilter(request, response);
        return;
    }
    // ... JWT validation continues
}
```

2. Enhance security config:
```java
.authorizeRequests(auth -> auth
    .antMatchers("/auth/**").permitAll()  // Allow all auth endpoints
    .anyRequest().authenticated()
)
```

3. Verify JWT generation:
```java
public String generateToken(UserDetails user) {
    return Jwts.builder()
        .setSubject(user.getUsername())
        .setIssuedAt(new Date())
        .setExpiration(new Date(System.currentTimeMillis() + 86400000))
        .signWith(SignatureAlgorithm.HS512, jwtSecret)
        .compact();
}
```

**Verification**:  
```bash
# Test token flow
curl -X POST http://localhost:8081/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@example.com","password":"Admin@123"}'

curl -X GET http://localhost:8081/api/protected \
  -H "Authorization: Bearer <token>"
```

**Common Pitfalls**:  
- Trailing slashes in URLs (`/auth/login` ≠ `/auth/login/`)  
- JWT secret mismatch between services  
- CORS blocking auth headers  
- Clock skew affecting token expiration  