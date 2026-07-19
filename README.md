# 🎨 AI Photo Generator

A full-stack AI-powered photo generator built with **Spring Boot** and vanilla **HTML/CSS/JavaScript**. Generates real AI images from text prompts using **Pollinations.ai** — completely free, no API key needed.

![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3-green)
![License](https://img.shields.io/badge/License-MIT-blue)

---

## ✨ Features

- **Real AI Image Generation** — Generate images from text using Pollinations.ai (free, no API key)
- **Responsive Gallery** — Card-based grid layout, works on all devices
- **Live Search** — Debounced search to filter images by prompt
- **Image Preview** — Full-size modal with metadata
- **Download & Delete** — One-click download or remove images
- **Dark/Light Theme** — Toggle with localStorage persistence
- **Drag & Drop** — Drag prompt text onto the generator
- **JWT Authentication** — Secure API with token-based auth
- **Rate Limiting** — 30 requests/minute per IP
- **Docker Support** — Containerized deployment ready

---

## 🚀 Quick Start

### Prerequisites
- **Java 17+** (JDK)
- **Maven 3.8+**

### 1. Run
```bash
git clone <repo-url>
cd stock-photo-generator
mvn spring-boot:run
```

### 2. Open Browser
Navigate to **http://localhost:8081**

### 3. Generate Images
Type a prompt like *"A golden retriever playing in autumn leaves"* and click **Generate Image**!

> ✅ **No API key needed** — uses Pollinations.ai (free)

---

## 🐳 Docker Deployment

```bash
# Using Docker Compose
docker-compose up -d

# Or Docker only
docker build -t ai-photo-generator .
docker run -p 8081:8081 -v ./generated-images:/app/generated-images ai-photo-generator
```

---

## 📡 API Reference

**Base URL:** `http://localhost:8081/api/images`

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/generate` | Generate image from prompt |
| `GET` | `/` | List all images (paginated) |
| `GET` | `/{id}` | Get image metadata |
| `GET` | `/{id}/file` | Download image file |
| `GET` | `/search?q={query}` | Search images by prompt |
| `DELETE` | `/{id}` | Delete an image |

### Generate Image
```bash
curl -X POST http://localhost:8081/api/images/generate \
  -H "Content-Type: application/json" \
  -d '{
    "prompt": "A serene mountain landscape at sunset",
    "size": "1024x1024",
    "style": "vivid",
    "quality": "standard"
  }'
```

**Response:**
```json
{
  "id": "a010ba3b-...",
  "prompt": "A serene mountain landscape at sunset",
  "imageUrl": "/api/images/a010ba3b-.../file",
  "size": "1024x1024",
  "provider": "pollinations",
  "createdAt": "2026-07-19T06:53:34"
}
```

### Request Parameters
| Parameter | Type | Default | Options |
|-----------|------|---------|---------|
| `prompt` | string | (required) | 3–500 characters |
| `size` | string | `1024x1024` | `512x512`, `1024x1024`, `1792x1024`, `1024x1792` |
| `style` | string | `vivid` | `vivid`, `natural` |
| `quality` | string | `standard` | `standard`, `hd` |

---

## 🧪 Testing

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=ImageControllerTest
```

---

## 📁 Project Structure

```
stock-photo-generator/
├── pom.xml                              # Maven config
├── Dockerfile                           # Docker build
├── docker-compose.yml                   # Container setup
├── README.md
├── generated-images/                    # Runtime image storage
├── src/main/java/com/mockai/
│   ├── MockAiPhotoApplication.java      # Entry point
│   ├── config/
│   │   ├── SecurityConfig.java          # JWT auth + security
│   │   ├── CorsConfig.java              # CORS config
│   │   ├── RateLimitConfig.java         # Rate limiting
│   │   └── AppConfig.java              # App config
│   ├── controller/
│   │   ├── ImageController.java         # REST API
│   │   ├── AuthController.java          # Login/register
│   │   └── PageController.java          # Frontend routing
│   ├── service/
│   │   ├── ImageGenerationService.java  # Core generation logic
│   │   ├── ImageStorageService.java     # File storage
│   │   ├── MockImageService.java        # Fallback placeholder
│   │   └── AuthService.java            # JWT auth
│   ├── service/provider/
│   │   └── PollinationsProvider.java    # Free AI image API
│   ├── model/
│   │   ├── ImageRequest.java            # Request DTO
│   │   ├── ImageResponse.java           # Response DTO
│   │   └── GeneratedImage.java          # Image entity
│   ├── repository/
│   │   └── ImageRepository.java         # In-memory store
│   └── security/
│       ├── JwtUtil.java                 # JWT token handling
│       └── JwtAuthenticationFilter.java # Auth filter
├── src/main/resources/
│   ├── application.yml                  # Config
│   └── static/
│       ├── index.html                   # SPA page
│       ├── css/style.css               # Styles
│       └── js/app.js                   # Frontend logic
└── src/test/java/com/mockai/           # Tests
```

---

## ⚙️ Configuration

### application.yml
```yaml
server:
  port: 8081

app:
  image:
    storage-path: ./generated-images
  jwt:
    secret: ${JWT_SECRET:myDefaultSecretKey}
    expiration: ${JWT_EXPIRATION:86400000}
```

### Environment Variables
| Variable | Description | Default |
|----------|-------------|---------|
| `JWT_SECRET` | JWT signing secret | auto-generated |
| `SERVER_PORT` | Server port | `8081` |

---

## 🛡️ Security

- **JWT Authentication** — Token-based API security
- **Rate Limiting** — 30 requests/minute per IP
- **CORS** — Configurable cross-origin policies
- **Input Validation** — Prompt length limits, required fields

---

## 📄 License

MIT License
