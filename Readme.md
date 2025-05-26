# 📚 Digital Library Management System

A comprehensive digital library management system built with **Spring Boot** and **MySQL**, featuring complete CRUD operations, advanced search functionality, and Docker containerization.

## 🚀 Features

- **📖 Book Management**: Add, edit, delete, and search books with inventory tracking
- **👥 Author Management**: Manage author profiles with biographical information
- **🏛️ Member Management**: Handle library member registration and profiles
- **📋 Borrowing System**: Complete book borrowing and returning workflow with fine calculation
- **🔍 Advanced Search**: Multi-criteria search across all entities
- **📊 Statistics & Reporting**: Borrowing statistics and overdue book tracking
- **🐳 Docker Ready**: Fully containerized with Docker Compose
- **☁️ AWS EC2 Compatible**: Production-ready deployment configuration

## 🏗️ Technology Stack

- **Backend**: Java 24, Spring Boot 3.x
- **Database**: MySQL 8.0
- **Build Tool**: Maven 3.9.6
- **Containerization**: Docker & Docker Compose
- **API Documentation**: RESTful APIs with comprehensive endpoint coverage

## 📊 Database Schema

```
Authors (1) ←→ (N) Books (1) ←→ (N) BorrowedBooks (N) ←→ (1) Members
```

**Key Entities:**
- **Authors**: Name, biography, birth year, nationality
- **Books**: Title, category, ISBN, publication year, inventory tracking
- **Members**: Name, email, phone, address, membership status
- **BorrowedBooks**: Borrowing records with dates, status, and fine calculation

## 🔌 API Endpoints

### 👥 Authors
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/authors` | Get all authors |
| GET | `/api/authors/{id}` | Get author by ID |
| GET | `/api/authors/{id}/books` | Get author with books |
| POST | `/api/authors` | Create new author |
| PUT | `/api/authors/{id}` | Update author |
| DELETE | `/api/authors/{id}` | Delete author |
| GET | `/api/authors/search?name={name}` | Search authors by name |

### 📚 Books
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/books` | Get all books |
| GET | `/api/books/{id}` | Get book by ID |
| POST | `/api/books` | Create new book |
| PUT | `/api/books/{id}` | Update book |
| DELETE | `/api/books/{id}` | Delete book |
| POST | `/api/books/search` | Advanced book search |
| GET | `/api/books/available` | Get available books |
| GET | `/api/books/category/{category}` | Get books by category |
| GET | `/api/books/categories` | Get all categories |

### 🏛️ Members
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/members` | Get all members |
| GET | `/api/members/{id}` | Get member by ID |
| GET | `/api/members/{id}/borrowed-books` | Get member with borrowing history |
| POST | `/api/members` | Create new member |
| PUT | `/api/members/{id}` | Update member |
| DELETE | `/api/members/{id}` | Delete member |
| GET | `/api/members/search?searchTerm={term}` | Search members |

### 📖 Borrowed Books
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/borrowed-books` | Get all borrowed books |
| GET | `/api/borrowed-books/{id}` | Get borrowed book by ID |
| POST | `/api/borrowed-books/borrow` | Borrow a book |
| PUT | `/api/borrowed-books/{id}/return` | Return a book |
| PUT | `/api/borrowed-books/{id}/extend?days={n}` | Extend due date |
| POST | `/api/borrowed-books/search` | Advanced search |
| GET | `/api/borrowed-books/member/{id}/current` | Member's current borrowings |
| GET | `/api/borrowed-books/member/{id}/fines` | Member's outstanding fines |
| GET | `/api/borrowed-books/overdue` | Get overdue books |
| GET | `/api/borrowed-books/due-today` | Get books due today |
| GET | `/api/borrowed-books/stats` | Get borrowing statistics |

## 🚀 Quick Start

### Prerequisites
- Java 24
- Maven 3.9+
- Docker & Docker Compose

### 1. Clone Repository
```bash
git clone https://github.com/your-username/digital-library.git
cd digital-library
```

### 2. Run with Docker (Recommended)
```bash
# Start the complete stack (MySQL + Application)
docker-compose up --build

# Run in background
docker-compose up --build -d
```

### 3. Access the Application
- **API Base URL**: http://localhost:5051/api
- **Health Check**: http://localhost:5051/actuator/health
- **MySQL Database**: localhost:3038

### 4. Test the API
```bash
# Health check
curl http://localhost:5051/actuator/health

# Get all authors (sample data included)
curl http://localhost:5051/api/authors

# Get all books
curl http://localhost:5051/api/books
```

## 🔧 Configuration

### Environment Variables
```bash
# Database Configuration
SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/digital_library
SPRING_DATASOURCE_USERNAME=library_user
SPRING_DATASOURCE_PASSWORD=library123

# Application Settings
SPRING_PROFILES_ACTIVE=prod
SERVER_PORT=8080

# Business Rules
APP_LIBRARY_MAX_BOOKS_PER_MEMBER=5
APP_LIBRARY_DEFAULT_BORROW_DAYS=14
APP_LIBRARY_FINE_PER_DAY=1.0
```

### Port Configuration
- **Backend API**: External `5051` → Internal `8080`
- **MySQL Database**: External `3038` → Internal `3306`

## 🧪 Testing

### Sample Data
The application includes pre-loaded sample data:
- **5 Authors** (F. Scott Fitzgerald, George Orwell, Harper Lee, etc.)
- **6 Books** (The Great Gatsby, 1984, Animal Farm, etc.)
- **5 Members** with sample profiles
- **Sample borrowing records**

### Postman Collection
Import the complete API collection for testing:
1. Copy the JSON from `/docs/postman-collection.json`
2. Import into Postman
3. Set base URL to `http://localhost:5051/api`

### cURL Examples
```bash
# Create a new author
curl -X POST http://localhost:5051/api/authors \
  -H "Content-Type: application/json" \
  -d '{
    "name": "J.R.R. Tolkien",
    "biography": "British author and philologist",
    "birthYear": 1892,
    "nationality": "British"
  }'

# Borrow a book
curl -X POST http://localhost:5051/api/borrowed-books/borrow \
  -H "Content-Type: application/json" \
  -d '{
    "bookId": 1,
    "memberId": 1,
    "borrowDate": "2025-05-26"
  }'

# Search books
curl -X POST http://localhost:5051/api/books/search \
  -H "Content-Type: application/json" \
  -d '{
    "bookTitle": "gatsby",
    "category": "Fiction"
  }'
```

## 🌐 AWS EC2 Deployment

### 1. Launch EC2 Instance
- **Instance Type**: t3.medium or larger
- **OS**: Amazon Linux 2
- **Storage**: 20+ GB

### 2. Security Group Rules
```bash
# Add these inbound rules:
Type: Custom TCP, Port: 5051, Source: 0.0.0.0/0    # API access
Type: Custom TCP, Port: 3038, Source: Your-IP/32   # MySQL (restrict to your IP)
Type: SSH, Port: 22, Source: Your-IP/32            # SSH access
```

### 3. Setup Commands
```bash
# SSH into EC2 instance
ssh -i your-key.pem ec2-user@your-ec2-ip

# Install Docker
sudo yum update -y
sudo yum install -y docker
sudo systemctl start docker
sudo usermod -a -G docker ec2-user

# Install Docker Compose
sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose

# Deploy application
git clone https://github.com/your-username/digital-library.git
cd digital-library
docker-compose up --build -d
```

### 4. Access URLs
Replace `YOUR-EC2-IP` with your EC2 public IP:
- **API**: `http://YOUR-EC2-IP:5051/api`
- **Health**: `http://YOUR-EC2-IP:5051/actuator/health`

## 🛠️ Development

### Local Development Setup
```bash
# Run only MySQL via Docker
docker-compose up mysql -d

# Run Spring Boot locally
mvn spring-boot:run

# Access at http://localhost:8080/api
```

### Database Access
```bash
# Connect to MySQL
mysql -h localhost -P 3038 -u library_user -p digital_library
# Password: library123

# Or via Docker
docker-compose exec mysql mysql -u library_user -p digital_library
```

### Project Structure
```
src/main/java/com/digitallibrary/
├── controller/          # REST controllers
├── service/            # Business logic
├── repository/         # Data access layer
├── model/             # Entity classes
├── dto/               # Data transfer objects
├── exception/         # Global exception handling
└── config/            # Configuration classes
```

## 📊 Business Rules

- **Maximum books per member**: 5
- **Default borrowing period**: 14 days
- **Fine calculation**: $1.00 per day after grace period
- **Grace period**: 1 day
- **Maximum extension**: 14 days
- **Outstanding fine limit**: $50.00

## 🔒 Security Features

- **Non-root Docker execution**
- **Environment variable configuration**
- **Production-ready error handling**
- **Database connection pooling**
- **Input validation and sanitization**

## 📈 Monitoring

### Health Checks
- **Application**: `/actuator/health`
- **Database**: Built-in MySQL health checks
- **Container**: Docker health check configuration

### Logging
- **Application logs**: `/app/logs/digital-library.log`
- **Access logs**: Console and file output
- **Error tracking**: Comprehensive exception handling

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 📞 Support

For questions and support:
- **Issues**: [GitHub Issues](https://github.com/your-username/digital-library/issues)
- **Documentation**: [API Documentation](docs/api-documentation.md)
- **Postman Collection**: [Collection File](docs/postman-collection.json)

## 🎉 Acknowledgments

- Built with Spring Boot and Spring Data JPA
- MySQL database with optimized configurations
- Docker containerization for easy deployment
- RESTful API design principles
- Comprehensive error handling and validation

---

**⭐ If this project helps you, please consider giving it a star!**