# Spring Boot Exception Handling System

Bu proje, Spring Boot uygulamaları için kapsamlı bir exception handling ve monitoring sistemi sunar. Sistem üç ana bileşenden oluşur:

## Bileşenler

### 1. Exception Handler Library
- Spring Boot uygulamalarına entegre edilebilen bir library
- Exception'ları yakalar, Kafka'ya gönderir ve loglara yazar
- Otomatik konfigürasyon desteği

### 2. Exception Monitor Microservice
- Kafka'dan exception verilerini consume eder
- Veritabanına kaydeder
- Web tabanlı monitoring arayüzü sunar

### 3. Demo Application
- Library'nin kullanımını gösteren örnek uygulama
- Çeşitli exception tiplerini test etmek için endpoint'ler

## Kurulum ve Çalıştırma

### Önkoşullar
- Java 17+
- Gradle 7+
- Kafka (localhost:9092)

### 1. Library'yi Build Et
```bash
cd exception-handler-library
./gradlew publishToMavenLocal
```

### 2. Exception Monitor'ü Çalıştır
```bash
cd exception-monitor
./gradlew bootRun
```
Web arayüzü: http://localhost:8080

### 3. Demo App'i Çalıştır
```bash
cd demo-app
./gradlew bootRun
```
API: http://localhost:8092

## Konfigürasyon

Demo uygulamasında `bootstrap.yml` ile aşağıdaki parametreler ayarlanabilir:

```yaml
exception:
  handler:
    project-name: ${PROJECT_NAME:demo-application}
    pod-name: ${POD_NAME:demo-pod-001}
    pod-ip: ${POD_IP:192.168.1.100}
    cluster-name: ${CLUSTER_NAME:development-cluster}
    environment: ${ENVIRONMENT:UAT}
    kafka:
      topic: exceptions
      bootstrap-servers: ${KAFKA_BOOTSTRAP_SERVERS:localhost:9092}
```

## Test Endpoint'leri

### Demo App API Endpoint'leri:

1. **Basit Exception Test**
```bash
curl "http://localhost:8092/api/throw-exception?type=runtime"
```

2. **Validation Error Test**
```bash
curl "http://localhost:8092/api/validation-error?email=invalid-email"
```

3. **Database Error Test**
```bash
curl "http://localhost:8092/api/database-error"
```

4. **User Processing Error Test**
```bash
curl -X POST "http://localhost:8092/api/user/999" \
  -H "Content-Type: application/json" \
  -d '{"name": "Test User"}'
```

5. **Exception with HTTP Headers Test**
```bash
curl -H "X-Custom-Header: TestValue" \
     -H "Accept: application/json" \
     "http://localhost:8092/api/throw-exception-with-headers?type=runtime"
```

## Monitoring Arayüzü

Exception Monitor web arayüzü şu özellikleri sunar:

- **Dashboard**: Genel istatistikler ve son exception'lar
- **Exception Listesi**: Filtreleme ve pagination ile exception'ları görüntüleme
- **Exception Detayları**: Stack trace ve ek bilgileri görme
- **İstatistikler**: Exception tiplerini ve projelere göre dağılımı

### Monitoring URL'leri:
- Dashboard: http://localhost:8080
- Exception List: http://localhost:8080/exceptions
- H2 Console: http://localhost:8080/h2-console

## Library Kullanımı

Kendi Spring Boot projenizde kullanmak için:

1. Dependency ekleyin:
```gradle
implementation "com.example:exception-handler-library:1.0.0"
```

2. Konfigürasyon ekleyin:
```yaml
exception:
  handler:
    project-name: my-project
    pod-name: my-pod
    pod-ip: 192.168.1.10
    cluster-name: production
    environment: PROD
```

3. Exception'ları handle edin:
```java
@Autowired
private ExceptionHandler exceptionHandler;

try {
    // Risky operation
} catch (Exception e) {
    // Basit kullanım
    exceptionHandler.handle(e);
    
    // Ek data ile
    Map<String, Object> additionalData = new HashMap<>();
    additionalData.put("userId", userId);
    exceptionHandler.handle(e, additionalData);
    
    // HTTP headers'ı otomatik olarak ekler
    exceptionHandler.handleWithHttpHeaders(e);
    
    // HTTP headers + ek data
    exceptionHandler.handleWithHttpHeaders(e, additionalData);
}
```

## Özellikler

- ✅ Otomatik Kafka producer konfigürasyonu
- ✅ HTTP request bilgilerini otomatik yakalama
- ✅ Environment variable desteği
- ✅ Web tabanlı monitoring arayüzü
- ✅ Exception istatistikleri ve filtreleme
- ✅ H2 in-memory database (geliştirme için)
- ✅ Responsive web tasarımı
- ✅ Bootstrap ile modern UI

## Geliştirme Notları

- Exception Monitor varsayılan olarak H2 in-memory database kullanır
- Production ortamında PostgreSQL veya MySQL kullanılmalı
- Kafka topic'i otomatik oluşturulmaz, manuel oluşturulmalı
- Log seviyesi DEBUG olarak ayarlanmış, production'da INFO'ya çekilmeli