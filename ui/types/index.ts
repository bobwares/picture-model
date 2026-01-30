// Core domain types based on JSON schemas

export type DriveType = 'LOCAL' | 'SMB' | 'SFTP' | 'FTP';

export type ConnectionStatus = 'DISCONNECTED' | 'CONNECTING' | 'CONNECTED' | 'ERROR';

export type CrawlStatus = 'PENDING' | 'IN_PROGRESS' | 'PAUSED' | 'COMPLETED' | 'FAILED' | 'CANCELLED';

export type MetadataSource = 'EXIF' | 'USER_ENTERED' | 'AUTO_GENERATED';

export interface RemoteFileDrive {
  id: string;
  name: string;
  type: DriveType;
  connectionUrl: string;
  rootPath: string;
  status: ConnectionStatus;
  autoConnect: boolean;
  autoCrawl: boolean;
  lastConnected?: string;
  lastCrawled?: string;
  imageCount: number;
  createdDate: string;
  modifiedDate: string;
}

export interface CreateDriveRequest {
  name: string;
  type: DriveType;
  connectionUrl: string;
  rootPath: string;
  credentials?: {
    username?: string;
    password?: string;
    host?: string;
    port?: number;
    domain?: string;
  };
  autoConnect?: boolean;
  autoCrawl?: boolean;
}

export interface Image {
  id: string;
  driveId: string;
  driveName: string;
  fileName: string;
  filePath: string;
  fullPath: string;
  fileSize: number;
  fileHash: string;
  mimeType: string;
  width?: number;
  height?: number;
  imageUrl: string;
  thumbnailUrl: string;
  capturedAt?: string;
  createdDate: string;
  modifiedDate: string;
  indexedDate: string;
  metadata?: ImageMetadata[];
  tags?: Tag[];
}

export interface ImageMetadata {
  id: string;
  key: string;
  value: string;
  source: MetadataSource;
  lastModified: string;
}

export interface Tag {
  id: string;
  name: string;
  color?: string;
  usageCount: number;
  createdDate: string;
}

export interface CrawlJob {
  id: string;
  driveId: string;
  driveName: string;
  rootPath: string;
  status: CrawlStatus;
  startTime: string;
  endTime?: string;
  filesProcessed: number;
  filesAdded: number;
  filesUpdated: number;
  filesDeleted: number;
  currentPath?: string;
  isIncremental: boolean;
  errors?: string;
}

export interface StartCrawlRequest {
  driveId: string;
  rootPath?: string;
  isIncremental?: boolean;
  extractExif?: boolean;
  generateThumbnails?: boolean;
}

export interface SearchRequest {
  query?: string;
  driveId?: string;
  tagIds?: string[];
  fromDate?: string;
  toDate?: string;
  page?: number;
  size?: number;
  sort?: string;
}

export interface DirectoryTreeNode {
  name: string;
  path: string;
  imageCount: number;
  totalImageCount: number;
  children: DirectoryTreeNode[];
  expanded?: boolean;
}

export interface SystemStatus {
  totalDrives: number;
  connectedDrives: number;
  totalImages: number;
  totalTags: number;
  activeCrawls: number;
}
