# WordOnlineServer

**WordOnlineServer** is the server-side project of the **Apptive Game Team**, responsible for handling the backend of an online word game.

---

## ⚙️ Tech Stack

- **Language**: Java
- **Framework**: Spring Boot
- **Database**: Postgresql
- **Build Tool**: Gradle
- **Deploy Tool**: Github Action

---

## 🗂️ Project Structure

```
WordOnlineServer/
├── build.gradle          # Gradle build configuration
├── deploy.sh             # Server deployment script
├── gradle/               # Gradle wrapper configuration
├── gradlew               # Unix Gradle wrapper
├── gradlew.bat           # Windows Gradle wrapper
├── remote-deploy.sh      # Remote server deployment script
├── settings.gradle       # Gradle settings
├── src/                  # Source code directory
│   └── main/             # Main source code
│       └── java/         # Java source code
├── .gitignore            # Git ignore file
└── README.md             # Project description file
```

---

## 🚀 Installation & Running

### 1. Clone the project

```bash
git clone https://github.com/Apptive-Game-Team/WordOnlineServer.git
cd WordOnlineServer
```

### 2. Build with Gradle

```bash
./gradlew build
```

### 3. Run the server

```bash
java -jar build/libs/word-online-server-0.0.1.jar
```

---

## 📦 Deployment

Deployment can be automated using the provided scripts.

### 1. Local server deployment

```bash
./deploy.sh
```

### 2. Remote server deployment

```bash
./remote-deploy.sh
```

> Note: You may need to adjust the scripts according to your server environment.

---

## 🧪 Testing

Once the server is running, you can test its functionalities by connecting with the client. The client project is available here: [WordOnlineClient](https://github.com/Apptive-Game-Team/WordOnlineClient).

---

## Member
<table>
        <tr>
    <td align="center">
        개발자
      </a>
    </td>
    <td align="center">
        개발자
      </a>
    </td>
  </tr>
  <tr>
    <td align="center" width="200px">
      <a href="https://github.com/Monolong" target="_blank">
        <img src="https://avatars.githubusercontent.com/u/83206119?v=4" alt="문성필 프로필" />
      </a>
    </td>
    <td align="center" width="200px">
      <a href="https://github.com/dev-yunseong" target="_blank">
        <img src="https://avatars.githubusercontent.com/u/88422717?v=4" alt="정윤성 프로필" />
      </a>
    </td>
  </tr>
  <tr>
    <td align="center">
      <a href="https://github.com/Monolong" target="_blank">
        문성필
      </a>
    </td>
    <td align="center">
      <a href="https://github.com/dev-yunseong" target="_blank">
        정윤성
      </a>
    </td>
  </tr>
</table>
