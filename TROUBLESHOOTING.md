# Troubleshooting Guide - B2B Gaming Platform

## Common Issues and Solutions

### Docker Desktop Not Running

**Error:**
```
unable to get image (e.g. postgres, redis, kafka): error during connect: Get "http://%2F%2F.%2Fpipe%2FdockerDesktopLinuxEngine/v1.49/images/rabbitmq:3-management-alpine/json": open //./pipe/dockerDesktopLinuxEngine: The system cannot find the file specified.
```

**Solution:**
1. Start Docker Desktop application
2. Wait for Docker Desktop to fully start (check system tray icon)
3. Verify Docker is running:
   ```bash
   docker ps
   ```
4. Retry starting infrastructure:
   ```bash
   scripts\start-infrastructure.bat
   ```

### Docker Compose Version Warning

**Warning:**
```
the attribute `version` is obsolete, it will be ignored
```

**Solution:**
This is just a warning and can be ignored. The `version` field has been removed from newer docker-compose.yml files. The file has been updated to remove this warning.

### Port Already in Use

**Error:**
```
Error: bind: address already in use
```

**Solution:**
1. Find the process using the port:
   ```bash
   netstat -ano | findstr :5432  # For PostgreSQL
   netstat -ano | findstr :6379  # For Redis
   netstat -ano | findstr :9092   # For Kafka
   ```

2. Kill the process:
   ```bash
   taskkill /PID <PID> /F
   ```

3. Or change the port in `docker-compose.yml`

### Database Connection Refused

**Error:**
```
Connection refused: connect
```

**Solution:**
1. Check if PostgreSQL container is running:
   ```bash
   docker ps
   ```

2. Check container logs:
   ```bash
   docker logs b2b-postgres
   ```

3. Restart the container:
   ```bash
   docker restart b2b-postgres
   ```

### Maven Build Failures

**Error:**
```
[ERROR] Failed to execute goal...
```

**Solution:**
1. Clean and rebuild:
   ```bash
   mvn clean install -U
   ```

2. Check Java version:
   ```bash
   java -version
   ```
   Should be Java 17+

3. Check Maven version:
   ```bash
   mvn -version
   ```

4. Delete `.m2` cache if corrupted (last resort):
   ```bash
   rmdir /s %USERPROFILE%\.m2\repository
   ```

### Service Won't Start

**Error:**
```
APPLICATION FAILED TO START
```

**Solution:**
1. Check application logs:
   ```bash
   # Look for error messages in console output
   ```

2. Verify database is running:
   ```bash
   docker ps
   ```

3. Check application.yml configuration:
   - Database URL
   - Username/Password
   - Port numbers

4. Verify network connectivity:
   ```bash
   docker exec -it b2b-postgres psql -U b2b_user -d b2b_platform -c "SELECT 1;"
   ```

### Redis Connection Issues

**Error:**
```
Unable to connect to Redis
```

**Solution:**
1. Check Redis is running:
   ```bash
   docker exec -it b2b-redis redis-cli ping
   ```
   Should return: `PONG`

2. Check Redis logs:
   ```bash
   docker logs b2b-redis
   ```

3. Restart Redis:
   ```bash
   docker restart b2b-redis
   ```

### Kafka Connection Issues

**Error:**
```
Connection to Kafka failed
```

**Solution:**
1. Check Kafka is running:
   ```bash
   docker exec -it b2b-kafka kafka-topics.sh --bootstrap-server localhost:9092 --list
   ```

2. Bootstrap server for apps: `localhost:9092`

3. Check Kafka logs:
   ```bash
   docker logs b2b-kafka
   ```

### Container Won't Start

**Error:**
```
Container exited with code 1
```

**Solution:**
1. Check container logs:
   ```bash
   docker logs <container-name>
   ```

2. Check container status:
   ```bash
   docker ps -a
   ```

3. Remove and recreate:
   ```bash
   docker-compose down
   docker-compose up -d
   ```

### Permission Denied Errors

**Error:**
```
Permission denied
```

**Solution:**
1. Run Command Prompt as Administrator
2. Or check file/folder permissions
3. For Docker, ensure Docker Desktop has proper permissions

### Out of Memory Errors

**Error:**
```
OutOfMemoryError
```

**Solution:**
1. Increase Docker Desktop memory:
   - Docker Desktop → Settings → Resources → Memory
   - Increase to at least 4GB

2. Reduce Maven memory usage:
   ```bash
   set MAVEN_OPTS=-Xmx1024m
   ```

### Network Issues

**Error:**
```
Network not found
```

**Solution:**
1. Create the network manually:
   ```bash
   docker network create b2b-network
   ```

2. Or let docker-compose create it automatically

### Windows-Specific Issues

**Path Issues:**
- Use forward slashes or escaped backslashes in paths
- Avoid spaces in directory names

**Line Ending Issues:**
- Ensure scripts use Windows line endings (CRLF)
- Git may need: `git config core.autocrlf true`

## Verification Steps

After fixing any issue, verify everything works:

1. **Check Docker:**
   ```bash
   docker ps
   ```

2. **Check Infrastructure:**
   ```bash
   scripts\check-infrastructure.bat
   ```

3. **Check Service:**
   ```bash
   curl http://localhost:8081/actuator/health
   ```

## Getting Help

If you're still stuck:
1. Check the error message carefully
2. Review container logs: `docker logs <container-name>`
3. Check application logs in console output
4. Verify all prerequisites are installed correctly
5. Review the SRS and Implementation Plan documents

---

**Last Updated:** February 6, 2026
