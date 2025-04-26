# AI Vibe News

A modern web application that publishes daily articles about AI and Vibe coding, built with Kotlin and Ktor.

## Features

- Responsive design that works on both web and mobile devices
- Clean and professional UI
- RESTful API for article management
- In-memory database for article storage
- Modern tech stack using Kotlin, Ktor, and Gradle

## Tech Stack

- Kotlin 1.9.22
- Ktor 2.3.8
- Gradle
- Exposed ORM
- H2 Database
- HTML/CSS for frontend

## Getting Started

### Prerequisites

- JDK 17 or higher
- Gradle 8.0 or higher

### Installation

1. Clone the repository:
```bash
git clone https://github.com/yourusername/ai-vibe-news.git
cd ai-vibe-news
```

2. Build the project:
```bash
./gradlew build
```

3. Run the application:
```bash
./gradlew run
```

The application will be available at `http://localhost:8080`

## API Endpoints

- `GET /articles` - Get all articles
- `POST /articles` - Create a new article

## Project Structure

```
src/
├── main/
│   ├── kotlin/
│   │   └── com/
│   │       └── aivibes/
│   │           ├── Application.kt
│   │           ├── database/
│   │           │   └── DatabaseFactory.kt
│   │           ├── models/
│   │           │   └── Article.kt
│   │           └── routes/
│   │               └── ArticleRoutes.kt
```

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## License

This project is licensed under the MIT License - see the LICENSE file for details. 