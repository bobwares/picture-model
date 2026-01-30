---
**Created by:** Claude (AI Coding Agent)
**Date:** 2026-01-27
**Version:** 2.0
---

# Picture Model App — Backend Design
## Multi-Drive Image Management System

## 1. Overview

The backend is a Java-based application server that connects to multiple remote file drives (local, network shares, SFTP), crawls for image files, stores metadata in a relational database, and exposes RESTful APIs with WebSocket support for a web application. The server runs on a host machine and serves clients over Wi-Fi.

### Key Features
- Multi-drive support (LOCAL, SMB, SFTP, FTP)
- File system abstraction layer
- Connection management with health checks
- Real-time updates via WebSocket
- Directory tree caching
- Incremental crawling
- Tag-based organization
- Full-text search

### Technology Stack
- **Language:** Java 21
- **Framework:** Spring Boot 3.2+
- **Build Tool:** Maven
- **Database:** PostgreSQL (production), H2 (development)
- **ORM:** Spring Data JPA with Hibernate
- **API:** RESTful with Spring Web MVC
- **WebSocket:** Spring WebSocket with STOMP
- **File Systems:**
  - Local: Java NIO.2
  - SMB: jCIFS-ng or SMBJ
  - SFTP: JSch or Apache MINA SSHD
  - FTP: Apache Commons Net
- **Image Processing:** Thumbnailator
- **Metadata Extraction:** metadata-extractor library
- **Encryption:** Jasypt (credentials)

## 2. Architecture

### 2.1 Layered Architecture

```
┌──────────────────────────────────────────────┐
│         Presentation Layer                   │
│  REST Controllers, WebSocket Handlers        │
│  Exception Handlers, DTOs                    │
├──────────────────────────────────────────────┤
│         Application Layer                    │
│  Services, Orchestration, Business Logic     │
│  - DriveService                              │
│  - ImageService                              │
│  - CrawlerService                            │
│  - SearchService                             │
│  - MetadataService                           │
│  - ThumbnailService                          │
│  - ConnectionManager                         │
├──────────────────────────────────────────────┤
│         Domain Layer                         │
│  Entities, Value Objects, Repositories       │
│  - RemoteFileDrive                           │
│  - Image                                     │
│  - ImageMetadata                             │
│  - Tag                                       │
│  - CrawlJob                                  │
│  - DirectoryNode                             │
├──────────────────────────────────────────────┤
│         Infrastructure Layer                 │
│  FileSystemProvider Implementations          │
│  External APIs, Encryption, Scheduling       │
│  - LocalFileSystemProvider                   │
│  - SmbFileSystemProvider                     │
│  - SftpFileSystemProvider                    │
│  - FtpFileSystemProvider                     │
└──────────────────────────────────────────────┘
```

### 2.2 Package Structure

```
com.picturemodel
├── PictureModelApplication.java
├── api
│   ├── controller
│   │   ├── DriveController.java
│   │   ├── ImageController.java
│   │   ├── TagController.java
│   │   ├── CrawlerController.java
│   │   ├── SearchController.java
│   │   ├── FileController.java
│   │   └── SettingsController.java
│   ├── websocket
│   │   ├── WebSocketConfig.java
│   │   ├── CrawlerWebSocketHandler.java
│   │   └── DriveStatusWebSocketHandler.java
│   ├── dto
│   │   ├── request
│   │   └── response
│   └── exception
│       ├── GlobalExceptionHandler.java
│       └── ErrorResponse.java
├── domain
│   ├── entity
│   │   ├── RemoteFileDrive.java
│   │   ├── Image.java
│   │   ├── ImageMetadata.java
│   │   ├── Tag.java
│   │   ├── CrawlJob.java
│   │   └── DirectoryNode.java
│   ├── repository
│   │   ├── DriveRepository.java
│   │   ├── ImageRepository.java
│   │   ├── MetadataRepository.java
│   │   ├── TagRepository.java
│   │   ├── CrawlJobRepository.java
│   │   └── DirectoryNodeRepository.java
│   ├── valueobject
│   │   ├── DriveType.java (enum)
│   │   ├── ConnectionStatus.java (enum)
│   │   ├── CrawlStatus.java (enum)
│   │   └── MetadataSource.java (enum)
│   └── event
│       ├── CrawlProgressEvent.java
│       └── DriveStatusEvent.java
├── service
│   ├── drive
│   │   ├── DriveService.java
│   │   ├── ConnectionManager.java
│   │   └── CredentialEncryptionService.java
│   ├── image
│   │   ├── ImageService.java
│   │   ├── FileService.java
│   │   └── ThumbnailService.java
│   ├── metadata
│   │   ├── MetadataService.java
│   │   └── ExifExtractor.java
│   ├── crawl
│   │   ├── CrawlerService.java
│   │   ├── CrawlExecutor.java
│   │   └── CrawlScheduler.java
│   ├── search
│   │   └── SearchService.java
│   └── tag
│       └── TagService.java
├── infrastructure
│   ├── filesystem
│   │   ├── FileSystemProvider.java (interface)
│   │   ├── FileSystemProviderFactory.java
│   │   ├── LocalFileSystemProvider.java
│   │   ├── SmbFileSystemProvider.java
│   │   ├── SftpFileSystemProvider.java
│   │   └── FtpFileSystemProvider.java
│   ├── config
│   │   ├── DatabaseConfig.java
│   │   ├── WebSocketConfig.java
│   │   ├── AsyncConfig.java
│   │   ├── CorsConfig.java
│   │   └── PictureModelProperties.java
│   └── security
│       └── EncryptionUtil.java
└── util
    ├── HashUtil.java
    └── PathUtil.java
```

## 3. Domain Model

### 3.1 Entity Specifications

#### RemoteFileDrive

```java
@Entity
@Table(name = "remote_file_drives", indexes = {
    @Index(name = "idx_drive_status", columnList = "status"),
    @Index(name = "idx_drive_name", columnList = "name")
})
public class RemoteFileDrive {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 200)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DriveType type; // LOCAL, SMB, SFTP, FTP

    @Column(nullable = false, length = 1000)
    private String connectionUrl;

    @Column(columnDefinition = "TEXT")
    private String encryptedCredentials; // Encrypted JSON

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ConnectionStatus status; // CONNECTED, DISCONNECTED, ERROR, CONNECTING

    @Column(nullable = false, length = 1000)
    private String rootPath;

    @Column(nullable = false)
    private Boolean autoConnect = false;

    @Column(nullable = false)
    private Boolean autoCrawl = false;

    @Column
    private Instant lastConnected;

    @Column
    private Instant lastCrawled;

    @Column(nullable = false)
    private Integer imageCount = 0;

    @Column(nullable = false)
    private Instant createdDate;

    @Column(nullable = false)
    private Instant modifiedDate;

    @OneToMany(mappedBy = "drive", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Image> images = new ArrayList<>();

    @OneToMany(mappedBy = "drive", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CrawlJob> crawlJobs = new ArrayList<>();

    // Lifecycle callbacks
    @PrePersist
    protected void onCreate() {
        createdDate = Instant.now();
        modifiedDate = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        modifiedDate = Instant.now();
    }
}
```

**DriveType Enum:**
```java
public enum DriveType {
    LOCAL,    // Local file system
    SMB,      // Windows network share (SMB/CIFS)
    SFTP,     // SSH File Transfer Protocol
    FTP       // File Transfer Protocol
}
```

**ConnectionStatus Enum:**
```java
public enum ConnectionStatus {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    ERROR
}
```

#### Image

```java
@Entity
@Table(name = "images", indexes = {
    @Index(name = "idx_image_drive", columnList = "drive_id"),
    @Index(name = "idx_image_hash", columnList = "file_hash"),
    @Index(name = "idx_image_filename", columnList = "file_name"),
    @Index(name = "idx_image_indexed", columnList = "indexed_date"),
    @Index(name = "idx_image_deleted", columnList = "deleted")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uk_drive_path", columnNames = {"drive_id", "file_path"})
})
public class Image {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "drive_id", nullable = false)
    private RemoteFileDrive drive;

    @Column(nullable = false, length = 500)
    private String fileName;

    @Column(nullable = false, length = 2000)
    private String filePath; // Relative path on drive

    @Column(nullable = false)
    private Long fileSize; // Bytes

    @Column(nullable = false, length = 64)
    private String fileHash; // SHA-256

    @Column(nullable = false, length = 50)
    private String mimeType;

    @Column
    private Integer width;

    @Column
    private Integer height;

    @Column
    private Instant capturedAt; // From EXIF if available

    @Column
    private Instant createdDate;

    @Column
    private Instant modifiedDate;

    @Column(nullable = false)
    private Instant indexedDate;

    @Column(length = 2000)
    private String thumbnailPath;

    @Column(nullable = false)
    private Boolean deleted = false;

    @OneToMany(mappedBy = "image", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ImageMetadata> metadata = new HashSet<>();

    @ManyToMany
    @JoinTable(
        name = "image_tags",
        joinColumns = @JoinColumn(name = "image_id"),
        inverseJoinColumns = @JoinColumn(name = "tag_id"),
        indexes = {
            @Index(name = "idx_image_tags_image", columnList = "image_id"),
            @Index(name = "idx_image_tags_tag", columnList = "tag_id")
        }
    )
    private Set<Tag> tags = new HashSet<>();

    // Computed property
    public String getFullPath() {
        return drive.getRootPath() + filePath;
    }
}
```

#### ImageMetadata

```java
@Entity
@Table(name = "image_metadata", indexes = {
    @Index(name = "idx_metadata_image", columnList = "image_id"),
    @Index(name = "idx_metadata_key", columnList = "metadata_key")
})
public class ImageMetadata {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "image_id", nullable = false)
    private Image image;

    @Column(nullable = false, length = 255)
    private String metadataKey; // Normalized lowercase

    @Column(columnDefinition = "TEXT")
    private String metadataValue;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MetadataSource source;

    @Column(nullable = false)
    private Instant lastModified;
}
```

**MetadataSource Enum:**
```java
public enum MetadataSource {
    EXIF,           // Extracted from EXIF data
    USER_ENTERED,   // Manually entered by user
    AUTO_GENERATED  // Generated by system (e.g., face detection)
}
```

#### Tag

```java
@Entity
@Table(name = "tags", indexes = {
    @Index(name = "idx_tag_name", columnList = "name", unique = true),
    @Index(name = "idx_tag_usage", columnList = "usage_count")
})
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(length = 7) // Hex color code
    private String color;

    @Column(nullable = false)
    private Instant createdDate;

    @Column(nullable = false)
    private Integer usageCount = 0;

    @ManyToMany(mappedBy = "tags")
    private Set<Image> images = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        createdDate = Instant.now();
    }
}
```

#### CrawlJob

```java
@Entity
@Table(name = "crawl_jobs", indexes = {
    @Index(name = "idx_crawl_drive", columnList = "drive_id"),
    @Index(name = "idx_crawl_status", columnList = "status"),
    @Index(name = "idx_crawl_started", columnList = "start_time")
})
public class CrawlJob {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "drive_id", nullable = false)
    private RemoteFileDrive drive;

    @Column(nullable = false, length = 2000)
    private String rootPath;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CrawlStatus status;

    @Column(nullable = false)
    private Instant startTime;

    @Column
    private Instant endTime;

    @Column(nullable = false)
    private Integer filesProcessed = 0;

    @Column(nullable = false)
    private Integer filesAdded = 0;

    @Column(nullable = false)
    private Integer filesUpdated = 0;

    @Column(nullable = false)
    private Integer filesDeleted = 0;

    @Column(length = 2000)
    private String currentPath;

    @Column(nullable = false)
    private Boolean isIncremental = false;

    @Column(columnDefinition = "TEXT")
    private String errors; // JSON array of error messages
}
```

**CrawlStatus Enum:**
```java
public enum CrawlStatus {
    PENDING,
    IN_PROGRESS,
    PAUSED,
    COMPLETED,
    FAILED,
    CANCELLED
}
```

#### DirectoryNode (Optional - for caching)

```java
@Entity
@Table(name = "directory_nodes", indexes = {
    @Index(name = "idx_dirnode_drive", columnList = "drive_id"),
    @Index(name = "idx_dirnode_parent", columnList = "parent_path")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uk_drive_path", columnNames = {"drive_id", "path"})
})
public class DirectoryNode {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "drive_id", nullable = false)
    private RemoteFileDrive drive;

    @Column(nullable = false, length = 2000)
    private String path;

    @Column(length = 2000)
    private String parentPath;

    @Column(nullable = false, length = 500)
    private String name;

    @Column(nullable = false)
    private Integer imageCount = 0; // Direct children only

    @Column(nullable = false)
    private Integer totalImageCount = 0; // Including subdirectories

    @Column(nullable = false)
    private Instant lastUpdated;
}
```

## 4. Infrastructure Layer: File System Abstraction

### 4.1 FileSystemProvider Interface

```java
public interface FileSystemProvider extends AutoCloseable {

    /**
     * Establish connection to the file system
     */
    void connect() throws IOException;

    /**
     * Close connection to the file system
     */
    void disconnect();

    /**
     * Check if connection is active
     */
    boolean isConnected();

    /**
     * List files and directories in a given path
     */
    List<FileInfo> listDirectory(String path) throws IOException;

    /**
     * Get directory tree structure
     */
    DirectoryTree getDirectoryTree(String rootPath) throws IOException;

    /**
     * Read file content as InputStream
     */
    InputStream readFile(String path) throws IOException;

    /**
     * Get file metadata (size, dates, etc.)
     */
    FileInfo getFileMetadata(String path) throws IOException;

    /**
     * Check if file exists
     */
    boolean fileExists(String path) throws IOException;

    /**
     * Test connection (for validation)
     */
    ConnectionTestResult testConnection();

    @Override
    void close();
}
```

### 4.2 Implementation: LocalFileSystemProvider

```java
@Component
public class LocalFileSystemProvider implements FileSystemProvider {

    private final FileSystem fileSystem = FileSystems.getDefault();
    private boolean connected = false;

    @Override
    public void connect() throws IOException {
        // Local file system is always available
        connected = true;
    }

    @Override
    public void disconnect() {
        connected = false;
    }

    @Override
    public boolean isConnected() {
        return connected;
    }

    @Override
    public List<FileInfo> listDirectory(String path) throws IOException {
        Path dirPath = Paths.get(path);
        List<FileInfo> files = new ArrayList<>();

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dirPath)) {
            for (Path entry : stream) {
                BasicFileAttributes attrs = Files.readAttributes(entry, BasicFileAttributes.class);
                files.add(FileInfo.from(entry, attrs));
            }
        }

        return files;
    }

    @Override
    public DirectoryTree getDirectoryTree(String rootPath) throws IOException {
        return DirectoryTreeBuilder.build(Paths.get(rootPath));
    }

    @Override
    public InputStream readFile(String path) throws IOException {
        return Files.newInputStream(Paths.get(path));
    }

    @Override
    public FileInfo getFileMetadata(String path) throws IOException {
        Path filePath = Paths.get(path);
        BasicFileAttributes attrs = Files.readAttributes(filePath, BasicFileAttributes.class);
        return FileInfo.from(filePath, attrs);
    }

    @Override
    public boolean fileExists(String path) throws IOException {
        return Files.exists(Paths.get(path));
    }

    @Override
    public ConnectionTestResult testConnection() {
        return new ConnectionTestResult(true, "Local file system available");
    }

    @Override
    public void close() {
        disconnect();
    }
}
```

### 4.3 Implementation: SmbFileSystemProvider

```java
@Component
public class SmbFileSystemProvider implements FileSystemProvider {

    private SmbClient smbClient;
    private CIFSContext context;
    private boolean connected = false;

    public SmbFileSystemProvider(String url, String username, String password) {
        // Configuration for jCIFS-ng
        Properties props = new Properties();
        props.setProperty("jcifs.smb.client.minVersion", "SMB202");
        props.setProperty("jcifs.smb.client.maxVersion", "SMB311");

        this.context = new BaseContext(new PropertyConfiguration(props))
            .withCredentials(new NtlmPasswordAuthenticator(username, password));
    }

    @Override
    public void connect() throws IOException {
        try {
            smbClient = new SmbClient(context);
            connected = true;
        } catch (Exception e) {
            throw new IOException("Failed to connect to SMB share", e);
        }
    }

    @Override
    public List<FileInfo> listDirectory(String path) throws IOException {
        List<FileInfo> files = new ArrayList<>();

        try (SmbFile smbDir = new SmbFile(path, context)) {
            if (smbDir.isDirectory()) {
                for (SmbFile file : smbDir.listFiles()) {
                    files.add(FileInfo.from(file));
                }
            }
        }

        return files;
    }

    @Override
    public InputStream readFile(String path) throws IOException {
        SmbFile smbFile = new SmbFile(path, context);
        return smbFile.getInputStream();
    }

    // ... other methods
}
```

### 4.4 Implementation: SftpFileSystemProvider

```java
@Component
public class SftpFileSystemProvider implements FileSystemProvider {

    private JSch jsch;
    private Session session;
    private ChannelSftp channelSftp;
    private boolean connected = false;

    public SftpFileSystemProvider(String host, int port, String username, String password) {
        this.jsch = new JSch();
        // Configuration
    }

    @Override
    public void connect() throws IOException {
        try {
            session = jsch.getSession(username, host, port);
            session.setPassword(password);
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();

            channelSftp = (ChannelSftp) session.openChannel("sftp");
            channelSftp.connect();

            connected = true;
        } catch (JSchException e) {
            throw new IOException("Failed to connect via SFTP", e);
        }
    }

    @Override
    public List<FileInfo> listDirectory(String path) throws IOException {
        List<FileInfo> files = new ArrayList<>();

        try {
            Vector<ChannelSftp.LsEntry> entries = channelSftp.ls(path);
            for (ChannelSftp.LsEntry entry : entries) {
                if (!entry.getFilename().equals(".") && !entry.getFilename().equals("..")) {
                    files.add(FileInfo.from(entry));
                }
            }
        } catch (SftpException e) {
            throw new IOException("Failed to list directory", e);
        }

        return files;
    }

    @Override
    public InputStream readFile(String path) throws IOException {
        try {
            return channelSftp.get(path);
        } catch (SftpException e) {
            throw new IOException("Failed to read file", e);
        }
    }

    // ... other methods
}
```

### 4.5 FileSystemProviderFactory

```java
@Component
public class FileSystemProviderFactory {

    private final CredentialEncryptionService encryptionService;

    public FileSystemProviderFactory(CredentialEncryptionService encryptionService) {
        this.encryptionService = encryptionService;
    }

    public FileSystemProvider createProvider(RemoteFileDrive drive) {
        Credentials credentials = encryptionService.decrypt(drive.getEncryptedCredentials());

        return switch (drive.getType()) {
            case LOCAL -> new LocalFileSystemProvider();
            case SMB -> new SmbFileSystemProvider(
                drive.getConnectionUrl(),
                credentials.getUsername(),
                credentials.getPassword()
            );
            case SFTP -> new SftpFileSystemProvider(
                credentials.getHost(),
                credentials.getPort(),
                credentials.getUsername(),
                credentials.getPassword()
            );
            case FTP -> new FtpFileSystemProvider(
                credentials.getHost(),
                credentials.getPort(),
                credentials.getUsername(),
                credentials.getPassword()
            );
        };
    }
}
```

## 5. Service Layer

### 5.1 DriveService

```java
@Service
@Transactional
public class DriveService {

    private final DriveRepository driveRepository;
    private final ConnectionManager connectionManager;
    private final CredentialEncryptionService encryptionService;

    /**
     * Create and add a new remote drive
     */
    public RemoteFileDriveDto createDrive(CreateDriveRequest request) {
        // Encrypt credentials
        String encryptedCreds = encryptionService.encrypt(request.getCredentials());

        RemoteFileDrive drive = new RemoteFileDrive();
        drive.setName(request.getName());
        drive.setType(request.getType());
        drive.setConnectionUrl(request.getConnectionUrl());
        drive.setEncryptedCredentials(encryptedCreds);
        drive.setRootPath(request.getRootPath());
        drive.setAutoConnect(request.isAutoConnect());
        drive.setAutoCrawl(request.isAutoCrawl());
        drive.setStatus(ConnectionStatus.DISCONNECTED);

        drive = driveRepository.save(drive);

        return DriveMapper.toDto(drive);
    }

    /**
     * Update drive configuration
     */
    public RemoteFileDriveDto updateDrive(UUID driveId, UpdateDriveRequest request) {
        RemoteFileDrive drive = getDriveOrThrow(driveId);

        drive.setName(request.getName());
        if (request.getCredentials() != null) {
            drive.setEncryptedCredentials(encryptionService.encrypt(request.getCredentials()));
        }
        drive.setAutoConnect(request.isAutoConnect());
        drive.setAutoCrawl(request.isAutoCrawl());

        drive = driveRepository.save(drive);

        return DriveMapper.toDto(drive);
    }

    /**
     * Connect to a drive
     */
    public RemoteFileDriveDto connectDrive(UUID driveId) {
        RemoteFileDrive drive = getDriveOrThrow(driveId);

        drive.setStatus(ConnectionStatus.CONNECTING);
        driveRepository.save(drive);

        try {
            connectionManager.connect(drive);
            drive.setStatus(ConnectionStatus.CONNECTED);
            drive.setLastConnected(Instant.now());
        } catch (IOException e) {
            drive.setStatus(ConnectionStatus.ERROR);
            throw new DriveConnectionException("Failed to connect to drive", e);
        }

        drive = driveRepository.save(drive);

        return DriveMapper.toDto(drive);
    }

    /**
     * Disconnect from a drive
     */
    public void disconnectDrive(UUID driveId) {
        RemoteFileDrive drive = getDriveOrThrow(driveId);

        connectionManager.disconnect(drive);
        drive.setStatus(ConnectionStatus.DISCONNECTED);
        driveRepository.save(drive);
    }

    /**
     * Test connection to a drive
     */
    public ConnectionTestResult testConnection(UUID driveId) {
        RemoteFileDrive drive = getDriveOrThrow(driveId);
        return connectionManager.testConnection(drive);
    }

    /**
     * Get all drives
     */
    @Transactional(readOnly = true)
    public List<RemoteFileDriveDto> getAllDrives() {
        return driveRepository.findAll().stream()
            .map(DriveMapper::toDto)
            .toList();
    }

    /**
     * Get drive by ID
     */
    @Transactional(readOnly = true)
    public RemoteFileDriveDto getDrive(UUID driveId) {
        return DriveMapper.toDto(getDriveOrThrow(driveId));
    }

    /**
     * Delete drive
     */
    public void deleteDrive(UUID driveId) {
        RemoteFileDrive drive = getDriveOrThrow(driveId);
        connectionManager.disconnect(drive);
        driveRepository.delete(drive);
    }

    private RemoteFileDrive getDriveOrThrow(UUID driveId) {
        return driveRepository.findById(driveId)
            .orElseThrow(() -> new EntityNotFoundException("Drive not found: " + driveId));
    }
}
```

### 5.2 ConnectionManager

```java
@Service
public class ConnectionManager {

    private final FileSystemProviderFactory providerFactory;
    private final Map<UUID, FileSystemProvider> activeConnections = new ConcurrentHashMap<>();
    private final ScheduledExecutorService healthCheckExecutor =
        Executors.newScheduledThreadPool(2);

    public ConnectionManager(FileSystemProviderFactory providerFactory) {
        this.providerFactory = providerFactory;

        // Start health check scheduler
        healthCheckExecutor.scheduleAtFixedRate(
            this::performHealthChecks,
            1, 5, TimeUnit.MINUTES
        );
    }

    /**
     * Connect to a drive and cache the provider
     */
    public void connect(RemoteFileDrive drive) throws IOException {
        FileSystemProvider provider = providerFactory.createProvider(drive);
        provider.connect();
        activeConnections.put(drive.getId(), provider);
    }

    /**
     * Disconnect from a drive and remove from cache
     */
    public void disconnect(RemoteFileDrive drive) {
        FileSystemProvider provider = activeConnections.remove(drive.getId());
        if (provider != null) {
            provider.disconnect();
        }
    }

    /**
     * Get cached provider for a drive
     */
    public FileSystemProvider getProvider(UUID driveId) {
        FileSystemProvider provider = activeConnections.get(driveId);
        if (provider == null) {
            throw new DriveNotConnectedException("Drive is not connected: " + driveId);
        }
        if (!provider.isConnected()) {
            throw new DriveNotConnectedException("Drive connection lost: " + driveId);
        }
        return provider;
    }

    /**
     * Test connection to a drive
     */
    public ConnectionTestResult testConnection(RemoteFileDrive drive) {
        try (FileSystemProvider provider = providerFactory.createProvider(drive)) {
            provider.connect();
            return provider.testConnection();
        } catch (Exception e) {
            return new ConnectionTestResult(false, e.getMessage());
        }
    }

    /**
     * Periodic health checks for all active connections
     */
    private void performHealthChecks() {
        activeConnections.forEach((driveId, provider) -> {
            if (!provider.isConnected()) {
                log.warn("Drive connection lost: {}", driveId);
                activeConnections.remove(driveId);
                // Publish event for WebSocket notification
            }
        });
    }

    @PreDestroy
    public void shutdown() {
        activeConnections.values().forEach(FileSystemProvider::disconnect);
        activeConnections.clear();
        healthCheckExecutor.shutdown();
    }
}
```

### 5.3 CrawlerService

```java
@Service
public class CrawlerService {

    private final CrawlJobRepository crawlJobRepository;
    private final DriveRepository driveRepository;
    private final ImageService imageService;
    private final ConnectionManager connectionManager;
    private final SimpMessagingTemplate websocketTemplate;
    private final ExecutorService crawlExecutor = Executors.newFixedThreadPool(4);
    private final Map<UUID, Future<?>> activeCrawls = new ConcurrentHashMap<>();

    /**
     * Start a new crawl job
     */
    public CrawlJobDto startCrawl(UUID driveId, CrawlOptions options) {
        RemoteFileDrive drive = driveRepository.findById(driveId)
            .orElseThrow(() -> new EntityNotFoundException("Drive not found"));

        // Create crawl job
        CrawlJob job = new CrawlJob();
        job.setDrive(drive);
        job.setRootPath(options.getRootPath() != null ? options.getRootPath() : drive.getRootPath());
        job.setStatus(CrawlStatus.PENDING);
        job.setStartTime(Instant.now());
        job.setIsIncremental(options.isIncremental());

        job = crawlJobRepository.save(job);

        // Submit to executor
        UUID jobId = job.getId();
        Future<?> future = crawlExecutor.submit(() -> executeCrawl(jobId));
        activeCrawls.put(jobId, future);

        return CrawlMapper.toDto(job);
    }

    /**
     * Execute crawl job
     */
    @Async
    void executeCrawl(UUID jobId) {
        CrawlJob job = crawlJobRepository.findById(jobId).orElseThrow();

        try {
            job.setStatus(CrawlStatus.IN_PROGRESS);
            crawlJobRepository.save(job);

            FileSystemProvider provider = connectionManager.getProvider(job.getDrive().getId());

            // Get directory tree
            DirectoryTree tree = provider.getDirectoryTree(job.getRootPath());

            // Process each file
            for (FileInfo fileInfo : tree.getAllFiles()) {
                if (Thread.currentThread().isInterrupted()) {
                    job.setStatus(CrawlStatus.CANCELLED);
                    break;
                }

                processFile(job, provider, fileInfo);

                // Update progress periodically
                job.setFilesProcessed(job.getFilesProcessed() + 1);
                job.setCurrentPath(fileInfo.getPath());

                if (job.getFilesProcessed() % 100 == 0) {
                    crawlJobRepository.save(job);
                    publishProgress(job);
                }
            }

            if (job.getStatus() != CrawlStatus.CANCELLED) {
                job.setStatus(CrawlStatus.COMPLETED);
            }

        } catch (Exception e) {
            job.setStatus(CrawlStatus.FAILED);
            job.setErrors(e.getMessage());
            log.error("Crawl job failed: {}", jobId, e);
        } finally {
            job.setEndTime(Instant.now());
            crawlJobRepository.save(job);
            activeCrawls.remove(jobId);
            publishProgress(job);
        }
    }

    /**
     * Process individual file
     */
    private void processFile(CrawlJob job, FileSystemProvider provider, FileInfo fileInfo) {
        try {
            // Check if image file
            if (!isImageFile(fileInfo.getMimeType())) {
                return;
            }

            // Compute hash
            String hash = computeHash(provider, fileInfo.getPath());

            // Check if already exists
            Optional<Image> existingImage = imageService.findByDriveAndPath(
                job.getDrive().getId(),
                fileInfo.getPath()
            );

            if (existingImage.isPresent()) {
                Image image = existingImage.get();
                if (!image.getFileHash().equals(hash)) {
                    // File modified - update
                    imageService.updateImage(image, fileInfo, hash);
                    job.setFilesUpdated(job.getFilesUpdated() + 1);
                }
            } else {
                // New file - add
                imageService.createImage(job.getDrive(), fileInfo, hash);
                job.setFilesAdded(job.getFilesAdded() + 1);
            }

        } catch (Exception e) {
            log.error("Failed to process file: {}", fileInfo.getPath(), e);
            job.setErrorCount(job.getErrorCount() + 1);
        }
    }

    /**
     * Publish crawl progress via WebSocket
     */
    private void publishProgress(CrawlJob job) {
        CrawlProgressEvent event = new CrawlProgressEvent(
            job.getId(),
            job.getStatus(),
            job.getFilesProcessed(),
            job.getFilesAdded(),
            job.getFilesUpdated(),
            job.getCurrentPath()
        );

        websocketTemplate.convertAndSend("/topic/crawler/" + job.getId(), event);
    }

    /**
     * Get crawl job status
     */
    @Transactional(readOnly = true)
    public CrawlJobDto getCrawlStatus(UUID jobId) {
        CrawlJob job = crawlJobRepository.findById(jobId)
            .orElseThrow(() -> new EntityNotFoundException("Crawl job not found"));
        return CrawlMapper.toDto(job);
    }

    /**
     * List recent crawl jobs for a drive
     */
    @Transactional(readOnly = true)
    public List<CrawlJobDto> listRecentCrawls(UUID driveId, int limit) {
        return crawlJobRepository.findByDriveIdOrderByStartTimeDesc(driveId, PageRequest.of(0, limit))
            .stream()
            .map(CrawlMapper::toDto)
            .toList();
    }

    /**
     * Cancel a running crawl job
     */
    public void cancelCrawl(UUID jobId) {
        Future<?> future = activeCrawls.get(jobId);
        if (future != null) {
            future.cancel(true);
        }
    }
}
```

### 5.4 ImageService

```java
@Service
@Transactional
public class ImageService {

    private final ImageRepository imageRepository;
    private final MetadataRepository metadataRepository;
    private final TagRepository tagRepository;
    private final ExifExtractor exifExtractor;
    private final ThumbnailService thumbnailService;

    /**
     * Create new image record
     */
    public Image createImage(RemoteFileDrive drive, FileInfo fileInfo, String hash) {
        Image image = new Image();
        image.setDrive(drive);
        image.setFileName(fileInfo.getName());
        image.setFilePath(fileInfo.getPath());
        image.setFileSize(fileInfo.getSize());
        image.setFileHash(hash);
        image.setMimeType(fileInfo.getMimeType());
        image.setCreatedDate(fileInfo.getCreatedDate());
        image.setModifiedDate(fileInfo.getModifiedDate());
        image.setIndexedDate(Instant.now());

        // Extract EXIF metadata
        Map<String, String> exifData = exifExtractor.extract(fileInfo);
        if (exifData.containsKey("width")) {
            image.setWidth(Integer.parseInt(exifData.get("width")));
        }
        if (exifData.containsKey("height")) {
            image.setHeight(Integer.parseInt(exifData.get("height")));
        }
        if (exifData.containsKey("capturedAt")) {
            image.setCapturedAt(Instant.parse(exifData.get("capturedAt")));
        }

        image = imageRepository.save(image);

        // Save EXIF metadata
        for (Map.Entry<String, String> entry : exifData.entrySet()) {
            ImageMetadata metadata = new ImageMetadata();
            metadata.setImage(image);
            metadata.setMetadataKey(entry.getKey().toLowerCase());
            metadata.setMetadataValue(entry.getValue());
            metadata.setSource(MetadataSource.EXIF);
            metadata.setLastModified(Instant.now());
            metadataRepository.save(metadata);
        }

        // Generate thumbnail asynchronously
        thumbnailService.generateThumbnailAsync(image.getId());

        return image;
    }

    /**
     * Update existing image
     */
    public void updateImage(Image image, FileInfo fileInfo, String hash) {
        image.setFileSize(fileInfo.getSize());
        image.setFileHash(hash);
        image.setModifiedDate(fileInfo.getModifiedDate());
        image.setIndexedDate(Instant.now());
        imageRepository.save(image);
    }

    /**
     * Find image by drive and path
     */
    @Transactional(readOnly = true)
    public Optional<Image> findByDriveAndPath(UUID driveId, String path) {
        return imageRepository.findByDriveIdAndFilePath(driveId, path);
    }

    /**
     * Get image details
     */
    @Transactional(readOnly = true)
    public ImageDetailsDto getImage(UUID imageId) {
        Image image = imageRepository.findById(imageId)
            .orElseThrow(() -> new EntityNotFoundException("Image not found"));
        return ImageMapper.toDetailsDto(image);
    }

    /**
     * Search images with filters
     */
    @Transactional(readOnly = true)
    public Page<ImageSummaryDto> searchImages(SearchCriteria criteria, Pageable pageable) {
        // Build query with specifications
        Specification<Image> spec = ImageSpecifications.withCriteria(criteria);
        Page<Image> images = imageRepository.findAll(spec, pageable);
        return images.map(ImageMapper::toSummaryDto);
    }

    /**
     * Update image metadata
     */
    public ImageDetailsDto updateMetadata(UUID imageId, Map<String, String> updates) {
        Image image = imageRepository.findById(imageId)
            .orElseThrow(() -> new EntityNotFoundException("Image not found"));

        for (Map.Entry<String, String> entry : updates.entrySet()) {
            String key = entry.getKey().toLowerCase();

            // Find existing or create new
            ImageMetadata metadata = image.getMetadata().stream()
                .filter(m -> m.getMetadataKey().equals(key) && m.getSource() == MetadataSource.USER_ENTERED)
                .findFirst()
                .orElseGet(() -> {
                    ImageMetadata newMetadata = new ImageMetadata();
                    newMetadata.setImage(image);
                    newMetadata.setMetadataKey(key);
                    newMetadata.setSource(MetadataSource.USER_ENTERED);
                    image.getMetadata().add(newMetadata);
                    return newMetadata;
                });

            metadata.setMetadataValue(entry.getValue());
            metadata.setLastModified(Instant.now());
        }

        imageRepository.save(image);

        return ImageMapper.toDetailsDto(image);
    }

    /**
     * Add tags to image
     */
    public ImageDetailsDto addTags(UUID imageId, Set<UUID> tagIds) {
        Image image = imageRepository.findById(imageId)
            .orElseThrow(() -> new EntityNotFoundException("Image not found"));

        Set<Tag> tags = tagRepository.findAllById(tagIds)
            .stream()
            .collect(Collectors.toSet());

        image.getTags().addAll(tags);
        imageRepository.save(image);

        // Update tag usage counts
        tags.forEach(tag -> {
            tag.setUsageCount(tag.getUsageCount() + 1);
            tagRepository.save(tag);
        });

        return ImageMapper.toDetailsDto(image);
    }

    /**
     * Remove tag from image
     */
    public void removeTag(UUID imageId, UUID tagId) {
        Image image = imageRepository.findById(imageId)
            .orElseThrow(() -> new EntityNotFoundException("Image not found"));

        Tag tag = tagRepository.findById(tagId)
            .orElseThrow(() -> new EntityNotFoundException("Tag not found"));

        image.getTags().remove(tag);
        imageRepository.save(image);

        // Update tag usage count
        tag.setUsageCount(Math.max(0, tag.getUsageCount() - 1));
        tagRepository.save(tag);
    }
}
```

### 5.5 SearchService

```java
@Service
@Transactional(readOnly = true)
public class SearchService {

    private final ImageRepository imageRepository;

    /**
     * Full-text search across images
     */
    public Page<ImageSummaryDto> search(SearchQuery query, Pageable pageable) {
        Specification<Image> spec = buildSearchSpecification(query);
        Page<Image> results = imageRepository.findAll(spec, pageable);
        return results.map(ImageMapper::toSummaryDto);
    }

    private Specification<Image> buildSearchSpecification(SearchQuery query) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Text search in filename
            if (query.getText() != null && !query.getText().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("fileName")),
                    "%" + query.getText().toLowerCase() + "%"
                ));
            }

            // Filter by drive
            if (query.getDriveId() != null) {
                predicates.add(criteriaBuilder.equal(
                    root.get("drive").get("id"),
                    query.getDriveId()
                ));
            }

            // Filter by tags
            if (query.getTagIds() != null && !query.getTagIds().isEmpty()) {
                Join<Image, Tag> tagJoin = root.join("tags");
                predicates.add(tagJoin.get("id").in(query.getTagIds()));
            }

            // Filter by date range
            if (query.getFromDate() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                    root.get("indexedDate"),
                    query.getFromDate()
                ));
            }
            if (query.getToDate() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                    root.get("indexedDate"),
                    query.getToDate()
                ));
            }

            // Exclude deleted
            predicates.add(criteriaBuilder.equal(root.get("deleted"), false));

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
```

## 6. REST API Design

### 6.1 Drive Management API

```java
@RestController
@RequestMapping("/api/drives")
public class DriveController {

    private final DriveService driveService;

    @GetMapping
    public ResponseEntity<List<RemoteFileDriveDto>> getAllDrives() {
        return ResponseEntity.ok(driveService.getAllDrives());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RemoteFileDriveDto> getDrive(@PathVariable UUID id) {
        return ResponseEntity.ok(driveService.getDrive(id));
    }

    @PostMapping
    public ResponseEntity<RemoteFileDriveDto> createDrive(
        @Valid @RequestBody CreateDriveRequest request
    ) {
        RemoteFileDriveDto drive = driveService.createDrive(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(drive);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RemoteFileDriveDto> updateDrive(
        @PathVariable UUID id,
        @Valid @RequestBody UpdateDriveRequest request
    ) {
        return ResponseEntity.ok(driveService.updateDrive(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDrive(@PathVariable UUID id) {
        driveService.deleteDrive(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/connect")
    public ResponseEntity<RemoteFileDriveDto> connectDrive(@PathVariable UUID id) {
        return ResponseEntity.ok(driveService.connectDrive(id));
    }

    @PostMapping("/{id}/disconnect")
    public ResponseEntity<Void> disconnectDrive(@PathVariable UUID id) {
        driveService.disconnectDrive(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/status")
    public ResponseEntity<DriveStatusDto> getDriveStatus(@PathVariable UUID id) {
        return ResponseEntity.ok(driveService.getDriveStatus(id));
    }

    @PostMapping("/{id}/test")
    public ResponseEntity<ConnectionTestResult> testConnection(@PathVariable UUID id) {
        return ResponseEntity.ok(driveService.testConnection(id));
    }

    @GetMapping("/{id}/tree")
    public ResponseEntity<DirectoryTreeDto> getDirectoryTree(
        @PathVariable UUID id,
        @RequestParam(required = false) String path
    ) {
        return ResponseEntity.ok(driveService.getDirectoryTree(id, path));
    }

    @GetMapping("/{id}/images")
    public ResponseEntity<Page<ImageSummaryDto>> getImagesInDirectory(
        @PathVariable UUID id,
        @RequestParam String path,
        Pageable pageable
    ) {
        return ResponseEntity.ok(driveService.getImagesInDirectory(id, path, pageable));
    }
}
```

### 6.2 Complete API Endpoint List

```
# Drive Management
POST   /api/drives                              Create new drive
GET    /api/drives                              List all drives
GET    /api/drives/{id}                         Get drive details
PUT    /api/drives/{id}                         Update drive
DELETE /api/drives/{id}                         Delete drive
POST   /api/drives/{id}/connect                 Connect to drive
POST   /api/drives/{id}/disconnect              Disconnect from drive
GET    /api/drives/{id}/status                  Get connection status
POST   /api/drives/{id}/test                    Test connection
GET    /api/drives/{id}/tree                    Get directory tree
GET    /api/drives/{id}/images                  Get images in directory

# Images
GET    /api/images                              Search/list images
GET    /api/images/{id}                         Get image details
PUT    /api/images/{id}/metadata                Update metadata
POST   /api/images/{id}/tags                    Add tags
DELETE /api/images/{id}/tags/{tagId}            Remove tag
DELETE /api/images/{id}                         Delete image (soft)

# Tags
GET    /api/tags                                List all tags
POST   /api/tags                                Create tag
PUT    /api/tags/{id}                           Update tag
DELETE /api/tags/{id}                           Delete tag
GET    /api/tags/{id}/images                    Get images with tag

# Crawler
POST   /api/crawler/start                       Start crawl job
GET    /api/crawler/jobs/{id}                   Get job status
POST   /api/crawler/jobs/{id}/pause             Pause job
POST   /api/crawler/jobs/{id}/resume            Resume job
POST   /api/crawler/jobs/{id}/cancel            Cancel job
GET    /api/crawler/jobs                        List crawl jobs

# Search
GET    /api/search                              Search images

# File Serving
GET    /api/files/{driveId}/{imageId}           Serve full image
GET    /api/files/{driveId}/{imageId}/thumbnail Serve thumbnail

# System
GET    /api/system/status                       System status
GET    /api/system/settings                     Get settings
PUT    /api/system/settings                     Update settings

# WebSocket Endpoints
/ws/crawler/{jobId}                             Crawl progress updates
/ws/drives/{driveId}                            Drive status updates
```

## 7. WebSocket Configuration

### 7.1 WebSocket Setup

```java
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic");
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
            .setAllowedOrigins("*")
            .withSockJS();
    }
}
```

### 7.2 WebSocket Handlers

```java
@Component
public class CrawlerWebSocketHandler {

    private final SimpMessagingTemplate messagingTemplate;

    public void publishProgress(CrawlProgressEvent event) {
        messagingTemplate.convertAndSend(
            "/topic/crawler/" + event.getJobId(),
            event
        );
    }
}

@Component
public class DriveStatusWebSocketHandler {

    private final SimpMessagingTemplate messagingTemplate;

    public void publishStatus(DriveStatusEvent event) {
        messagingTemplate.convertAndSend(
            "/topic/drives/" + event.getDriveId(),
            event
        );
    }
}
```

## 8. Configuration

### 8.1 application.yml

```yaml
spring:
  application:
    name: picture-model-server

  datasource:
    url: jdbc:postgresql://localhost:5432/picturemodel
    username: ${DB_USERNAME:pictureuser}
    password: ${DB_PASSWORD:picturepass}
    driver-class-name: org.postgresql.Driver
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5

  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
    properties:
      hibernate:
        format_sql: true
        dialect: org.hibernate.dialect.PostgreSQLDialect

  flyway:
    enabled: true
    locations: classpath:db/migration

server:
  port: 8080
  address: 0.0.0.0

picture-model:
  drives:
    connection-timeout: 30000
    health-check-interval: 300000

  crawler:
    supported-mime-types:
      - image/jpeg
      - image/png
      - image/gif
      - image/webp
      - image/tiff
      - image/bmp
    thread-pool-size: 4
    batch-size: 100

  thumbnail:
    cache-dir: ./data/thumbnails
    max-cache-size-mb: 1000
    sizes:
      small: 150
      medium: 250
      large: 350
    quality: 0.85

  files:
    stream-buffer-size: 8192

  security:
    encryption-key: ${ENCRYPTION_KEY:changeme}

logging:
  level:
    com.picturemodel: INFO
    org.springframework: WARN
  file:
    name: ./logs/picture-model.log
    max-size: 10MB
    max-history: 30
```

### 8.2 Maven Dependencies (pom.xml additions)

```xml
<!-- File System Libraries -->
<dependency>
    <groupId>eu.agno3.jcifs</groupId>
    <artifactId>jcifs-ng</artifactId>
    <version>2.1.7</version>
</dependency>

<dependency>
    <groupId>com.jcraft</groupId>
    <artifactId>jsch</artifactId>
    <version>0.1.55</version>
</dependency>

<dependency>
    <groupId>commons-net</groupId>
    <artifactId>commons-net</artifactId>
    <version>3.9.0</version>
</dependency>

<!-- Encryption -->
<dependency>
    <groupId>com.github.ulisesbocchio</groupId>
    <artifactId>jasypt-spring-boot-starter</artifactId>
    <version>3.0.5</version>
</dependency>

<!-- WebSocket -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-websocket</artifactId>
</dependency>

<!-- Image Processing -->
<dependency>
    <groupId>net.coobird</groupId>
    <artifactId>thumbnailator</artifactId>
    <version>0.4.19</version>
</dependency>

<!-- Metadata Extraction -->
<dependency>
    <groupId>com.drewnoakes</groupId>
    <artifactId>metadata-extractor</artifactId>
    <version>2.18.0</version>
</dependency>
```

## 9. Security

### 9.1 Credential Encryption

```java
@Service
public class CredentialEncryptionService {

    private final StandardPBEStringEncryptor encryptor;

    public CredentialEncryptionService(@Value("${picture-model.security.encryption-key}") String key) {
        this.encryptor = new StandardPBEStringEncryptor();
        encryptor.setPassword(key);
        encryptor.setAlgorithm("PBEWithMD5AndDES");
    }

    public String encrypt(Credentials credentials) {
        try {
            String json = objectMapper.writeValueAsString(credentials);
            return encryptor.encrypt(json);
        } catch (JsonProcessingException e) {
            throw new EncryptionException("Failed to encrypt credentials", e);
        }
    }

    public Credentials decrypt(String encrypted) {
        try {
            String json = encryptor.decrypt(encrypted);
            return objectMapper.readValue(json, Credentials.class);
        } catch (IOException e) {
            throw new EncryptionException("Failed to decrypt credentials", e);
        }
    }
}
```

### 9.2 CORS Configuration

```java
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
            .allowedOrigins("*") // Configure for production
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
            .allowedHeaders("*")
            .maxAge(3600);
    }
}
```

## 10. Testing Strategy

### 10.1 Unit Tests
- Service layer business logic
- FileSystemProvider implementations (mocked connections)
- Entity validation
- DTO mapping

### 10.2 Integration Tests
- Repository queries with test database
- REST API endpoints
- WebSocket messaging
- File system operations with test directories

### 10.3 Example Integration Test

```java
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class DriveControllerIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
        .withDatabaseName("test")
        .withUsername("test")
        .withPassword("test");

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldCreateLocalDrive() throws Exception {
        CreateDriveRequest request = new CreateDriveRequest();
        request.setName("Test Drive");
        request.setType(DriveType.LOCAL);
        request.setConnectionUrl("/tmp/test");
        request.setRootPath("/tmp/test");

        mockMvc.perform(post("/api/drives")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value("Test Drive"))
            .andExpect(jsonPath("$.type").value("LOCAL"));
    }
}
```

## 11. Deployment

### 11.1 Packaging
```bash
mvn clean package
```

Produces: `target/picture-model-2.0.0.jar`

### 11.2 Running
```bash
java -jar picture-model-2.0.0.jar \
  --server.port=8080 \
  --spring.datasource.url=jdbc:postgresql://localhost:5432/picturemodel \
  --spring.datasource.username=pictureuser \
  --spring.datasource.password=picturepass \
  --picture-model.security.encryption-key=your-secret-key
```

### 11.3 Docker Support
```dockerfile
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY target/picture-model-2.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

## 12. Performance Considerations

### 12.1 Connection Pooling
- Reuse FileSystemProvider instances
- Limit concurrent connections per drive
- Health checks to detect stale connections

### 12.2 Database Optimization
- Proper indexing on frequently queried columns
- Batch inserts during crawls
- Connection pooling (HikariCP)

### 12.3 Caching Strategy
- DirectoryNode caching for tree views
- Thumbnail caching (in-memory + disk)
- Drive status caching

### 12.4 Async Operations
- Crawl jobs run asynchronously
- Thumbnail generation is async
- WebSocket updates don't block operations

## 13. Next Steps

1. ✅ Domain Design v2.0 completed
2. ✅ Backend Design v2.0 completed
3. **Phase 4:** Set up Spring Boot project structure
4. **Phase 5:** Implement core entities and repositories
5. **Phase 6:** Implement FileSystemProvider for LOCAL drives
6. **Phase 7:** Implement basic REST API and crawler
7. **Phase 8:** Add network drive support (SMB, SFTP)
8. **Phase 9:** Implement WebSocket real-time updates
9. **Phase 10:** Frontend integration

---

## Summary of v2.0 Features

### Core Enhancements
✅ Multi-drive support (LOCAL, SMB, SFTP, FTP)
✅ File System Abstraction Layer
✅ Connection Manager with health checks
✅ Real-time updates via WebSocket
✅ Directory tree caching
✅ Drive-aware REST API
✅ Credential encryption
✅ Incremental crawling
✅ Tag-based organization

### Technology Decisions
- Java 21 + Spring Boot 3.2+
- PostgreSQL for production, H2 for development
- jCIFS-ng for SMB, JSch for SFTP, Commons Net for FTP
- Jasypt for credential encryption
- Spring WebSocket for real-time updates
- Thumbnailator for image processing
- metadata-extractor for EXIF data

### Architecture Patterns
- Layered architecture (Presentation, Application, Domain, Infrastructure)
- Strategy pattern for FileSystemProvider
- Factory pattern for provider creation
- Repository pattern for data access
- Observer pattern for WebSocket events

Ready to begin implementation!
