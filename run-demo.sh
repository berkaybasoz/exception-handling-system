#!/bin/bash

echo "=== Spring Boot Exception Handling System Demo ==="
echo ""

# Check if Kafka is running
echo "1. Checking Kafka..."
if nc -z localhost 9092 2>/dev/null; then
    echo "✅ Kafka is running on localhost:9092"
else
    echo "❌ Kafka is not running. Starting with Docker Compose..."
    docker-compose up -d
    echo "⏳ Waiting for Kafka to start..."
    sleep 30
fi

echo ""

# Build and install library
echo "2. Building Exception Handler Library..."
cd exception-handler-library
./gradlew clean build publishToMavenLocal
if [ $? -eq 0 ]; then
    echo "✅ Library built and installed to local Maven repository"
else
    echo "❌ Library build failed"
    exit 1
fi

echo ""

# Start Exception Monitor in background
echo "3. Starting Exception Monitor..."
cd ../exception-monitor
nohup ./gradlew bootRun > monitor.log 2>&1 &
MONITOR_PID=$!
echo "✅ Exception Monitor started (PID: $MONITOR_PID)"
echo "   Web UI: http://localhost:8080"
echo "   Logs: tail -f exception-monitor/monitor.log"

echo ""

# Start Demo App in background
echo "4. Starting Demo Application..."
cd ../demo-app
nohup ./gradlew bootRun > demo.log 2>&1 &
DEMO_PID=$!
echo "✅ Demo Application started (PID: $DEMO_PID)"
echo "   API: http://localhost:8092"
echo "   Logs: tail -f demo-app/demo.log"

echo ""
echo "⏳ Waiting for applications to start up..."
sleep 20

echo ""
echo "5. Testing Exception Generation..."

# Test various exception types
echo "   - Testing RuntimeException..."
curl -s "http://localhost:8092/api/throw-exception?type=runtime" > /dev/null

echo "   - Testing IllegalArgumentException..."
curl -s "http://localhost:8092/api/throw-exception?type=illegal-argument" > /dev/null

echo "   - Testing NullPointerException..."
curl -s "http://localhost:8092/api/throw-exception?type=null-pointer" > /dev/null

echo "   - Testing Validation Error..."
curl -s "http://localhost:8092/api/validation-error?email=invalid-email" > /dev/null

echo "   - Testing Database Error..."
curl -s "http://localhost:8092/api/database-error" > /dev/null

echo "   - Testing User Processing Error..."
curl -s -X POST "http://localhost:8092/api/user/999" \
  -H "Content-Type: application/json" \
  -d '{"name": "Test User"}' > /dev/null

echo ""
echo "✅ Demo completed! Generated various exceptions."
echo ""
echo "🔗 URLs:"
echo "   • Exception Monitor Dashboard: http://localhost:8080"
echo "   • Exception List: http://localhost:8080/exceptions"
echo "   • Demo API Docs: http://localhost:8092/api/hello"
echo "   • Kafka UI: http://localhost:8090"
echo "   • H2 Console: http://localhost:8080/h2-console (JDBC URL: jdbc:h2:mem:exceptiondb)"
echo ""
echo "🛠️  Process IDs:"
echo "   • Exception Monitor: $MONITOR_PID"
echo "   • Demo App: $DEMO_PID"
echo ""
echo "📋 To stop applications:"
echo "   kill $MONITOR_PID $DEMO_PID"
echo "   docker-compose down"
echo ""
echo "📊 View real-time logs:"
echo "   tail -f exception-monitor/monitor.log"
echo "   tail -f demo-app/demo.log"
echo ""

# Save PIDs to file for easy cleanup
echo "$MONITOR_PID $DEMO_PID" > .demo_pids

echo "🎉 Demo is now running! Check the Exception Monitor dashboard to see the caught exceptions."