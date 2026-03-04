# Bug Reporter

A full-stack web application for bug reporting and tracking, developed as a university project. The application provides a comprehensive platform for users to report bugs, comment on existing issues, vote on bug reports, and manage bug resolution through a modern web interface.

## 🚀 Features

### Core Functionality

- **Bug Reporting**: Create detailed bug reports with descriptions, images, and categorization
- **User Authentication**: Secure registration and login system with JWT tokens
- **Commenting System**: Interactive discussion threads on bug reports
- **Voting System**: Community-driven prioritization of bug reports
- **Tagging System**: Organize bugs with custom tags for better categorization
- **Bug Status Tracking**: Monitor bug resolution progress with status updates
- **Accepted Solutions**: Mark comments as accepted solutions for bug resolution

### User Management

- **User Profiles**: Personalized user accounts with profile management
- **Account Settings**: Update personal information and preferences
- **User Moderation**: Administrative tools for user management
- **Ban System**: Moderation capabilities with user banning functionality

### Additional Features

- **Email Integration**: Automated email notifications using Mailtrap
- **Responsive Design**: Modern Material-UI interface with mobile support
- **Protected Routes**: Role-based access control for different user types
- **Dashboard**: Centralized view of user activities and bug statistics

## 🛠️ Technology Stack

### Backend

- **Framework**: Spring Boot 2.5.5
- **Language**: Java 11
- **Security**: Spring Security with JWT authentication
- **Database**: MySQL with JPA/Hibernate ORM
- **Build Tool**: Maven
- **Testing**: JUnit 5, Mockito
- **Email Service**: Spring Mail with Mailtrap integration

### Frontend

- **Framework**: React 19 with TypeScript
- **Build Tool**: Vite
- **UI Library**: Material-UI (@mui/material)
- **Routing**: React Router DOM
- **HTTP Client**: Axios
- **Testing**: Cypress (E2E), Vitest (Unit), Testing Library
- **Development**: Hot Module Replacement (HMR) with Vite

### Database

- **Primary Database**: MySQL
- **Connection Pool**: HikariCP
- **Schema Management**: Hibernate DDL auto-update

## 📁 Project Structure

```
Bug Reporter/
├── src/                          # Backend (Spring Boot)
│   ├── main/
│   │   ├── java/com/utcn/demo/
│   │   │   ├── controller/        # REST API controllers
│   │   │   ├── entity/           # JPA entities
│   │   │   ├── repository/       # Data access layer
│   │   │   ├── service/          # Business logic
│   │   │   ├── dto/              # Data transfer objects
│   │   │   ├── config/           # Application configuration
│   │   │   └── security/         # Security filters and config
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── sql_script/       # Database setup scripts
│   │       └── postman/          # API testing collection
│   └── test/                     # Backend unit tests
├── frontend/                     # Frontend (React + TypeScript)
│   ├── src/
│   │   ├── components/           # Reusable UI components
│   │   ├── pages/                # Page-level components
│   │   ├── contexts/             # React contexts (Auth, etc.)
│   │   ├── services/             # API service layer
│   │   └── tests/                # Frontend tests
│   ├── cypress/                  # E2E tests
│   └── public/                   # Static assets
├── pom.xml                       # Maven configuration
└── README.md                     # Project documentation
```

## 🚦 Getting Started

### Prerequisites

- **Java 11** or higher
- **Node.js 16+** and npm/yarn
- **MySQL 8.0+**
- **Maven 3.6+**

### Database Setup

1. Create a MySQL database named `bugreporter`:

   ```sql
   CREATE DATABASE bugreporter;
   ```

2. Update database credentials in `src/main/resources/application.properties`:

   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/bugreporter?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
   spring.datasource.username=your_username
   spring.datasource.password=your_password
   ```

3. Run the provided SQL script to set up initial data:
   ```bash
   mysql -u your_username -p bugreporter < src/main/resources/sql_script/livecodesession.sql
   ```

### Backend Setup

1. Navigate to the project root directory:

   ```bash
   cd Bug\ Reporter
   ```

2. Install dependencies and run the Spring Boot application:

   ```bash
   ./mvnw clean install
   ./mvnw spring-boot:run
   ```

   The backend server will start on `http://localhost:8080`

### Frontend Setup

1. Navigate to the frontend directory:

   ```bash
   cd frontend
   ```

2. Install dependencies:

   ```bash
   npm install
   ```

3. Start the development server:

   ```bash
   npm run dev
   ```

   The frontend application will be available at `http://localhost:5173`

## 🧪 Testing

### Backend Testing

```bash
# Run unit tests
./mvnw test

# Run specific test class
./mvnw test -Dtest=BugServiceTest
```

### Frontend Testing

```bash
cd frontend

# Run unit tests
npm run test

# Run E2E tests
npm run cypress:open    # Interactive mode
npm run cypress:run     # Headless mode

# Lint code
npm run lint
```

## 📚 API Documentation

The application provides a RESTful API with the following main endpoints:

### Authentication

- `POST /auth/login` - User login
- `POST /auth/register` - User registration
- `POST /auth/logout` - User logout

### Bug Management

- `GET /bugs` - Retrieve all bugs
- `GET /bugs/{id}` - Get bug by ID
- `POST /bugs` - Create new bug report
- `PUT /bugs/{id}` - Update bug report
- `DELETE /bugs/{id}` - Delete bug report

### Comments

- `GET /bugs/{id}/comments` - Get comments for a bug
- `POST /bugs/{id}/comments` - Add comment to bug
- `PUT /comments/{id}/accept` - Mark comment as accepted solution

### User Management

- `GET /users/profile` - Get user profile
- `PUT /users/profile` - Update user profile
- `POST /users/{id}/ban` - Ban user (admin only)

### Tags and Voting

- `GET /tags` - Get all tags
- `POST /bugs/{id}/vote` - Vote on bug report
- `POST /tags` - Create new tag

A Postman collection is available at `src/main/resources/postman/Live Coding.postman_collection.json` for API testing.

## 🔧 Configuration

### Environment Configuration

Key configuration files:

- **Backend**: `src/main/resources/application.properties`
- **Frontend**: `frontend/vite.config.ts`

### Email Configuration

The application uses Mailtrap for email services. Update the following in `application.properties`:

```properties
spring.mail.host=sandbox.smtp.mailtrap.io
spring.mail.port=2525
spring.mail.username=your_mailtrap_username
spring.mail.password=your_mailtrap_password
```

### Security Configuration

JWT authentication is configured in `src/main/java/com/utcn/demo/config/SecurityConfig.java` with customizable token expiration and security rules.

## 🤝 Contributing

This project was developed as a university assignment. For educational purposes, you can:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/new-feature`)
3. Commit your changes (`git commit -am 'Add new feature'`)
4. Push to the branch (`git push origin feature/new-feature`)
5. Create a Pull Request

## 📄 License

This project is part of a university assignment and is intended for educational purposes.

## 👥 Authors

This project was developed as a collaborative university project.

## 🙏 Acknowledgments

- University of Technical Cluj-Napoca (UTCN)
- Spring Boot and React communities for excellent documentation
- Material-UI team for the component library
- All open-source contributors whose libraries made this project possible

---

For any questions or issues, please refer to the codebase documentation or create an issue in the repository.
