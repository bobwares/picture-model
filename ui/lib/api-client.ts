import axios from 'axios';
import type {
  RemoteFileDrive,
  CreateDriveRequest,
  Image,
  Tag,
  CrawlJob,
  StartCrawlRequest,
  SearchRequest,
  DirectoryTreeNode,
  SystemStatus,
} from '@/types';

const API_BASE = process.env.NEXT_PUBLIC_API_BASE || 'http://localhost:8080';

export const apiClient = axios.create({
  baseURL: `${API_BASE}/api`,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Drive Management API
export const driveApi = {
  getAll: () => apiClient.get<RemoteFileDrive[]>('/drives'),
  getById: (id: string) => apiClient.get<RemoteFileDrive>(`/drives/${id}`),
  create: (data: CreateDriveRequest) => apiClient.post<RemoteFileDrive>('/drives', data),
  update: (id: string, data: Partial<CreateDriveRequest>) =>
    apiClient.put<RemoteFileDrive>(`/drives/${id}`, data),
  delete: (id: string) => apiClient.delete(`/drives/${id}`),
  connect: (id: string) => apiClient.post<RemoteFileDrive>(`/drives/${id}/connect`),
  disconnect: (id: string) => apiClient.post<RemoteFileDrive>(`/drives/${id}/disconnect`),
  getStatus: (id: string) => apiClient.get<RemoteFileDrive>(`/drives/${id}/status`),
  testConnection: (id: string) => apiClient.post(`/drives/${id}/test`),
  getDirectoryTree: (id: string, path?: string) =>
    apiClient.get<DirectoryTreeNode>(`/drives/${id}/tree`, { params: { path } }),
  getTree: (id: string) => apiClient.get<DirectoryTreeNode>(`/drives/${id}/tree`),
  getImages: (
    id: string,
    params: { path?: string; sort?: string; page?: number; size?: number }
  ) =>
    apiClient.get<{ content: Image[]; totalElements: number; last: boolean }>(
      `/drives/${id}/images`,
      { params }
    ),
};

// Image Management API
export const imageApi = {
  search: (params: SearchRequest) => apiClient.get<{ content: Image[]; totalElements: number }>('/images', { params }),
  getById: (id: string) => apiClient.get<Image>(`/images/${id}`),
  updateMetadata: (id: string, metadata: Record<string, string>) =>
    apiClient.put<Image>(`/images/${id}/metadata`, metadata),
  addTags: (id: string, tagIds: string[]) =>
    apiClient.post<Image>(`/images/${id}/tags`, { tagIds }),
  removeTag: (id: string, tagId: string) =>
    apiClient.delete(`/images/${id}/tags/${tagId}`),
  delete: (id: string) => apiClient.delete(`/images/${id}`),
};

// Crawler API
export const crawlerApi = {
  startCrawl: (data: StartCrawlRequest) => apiClient.post<CrawlJob>('/crawler/start', data),
  getJob: (id: string) => apiClient.get<CrawlJob>(`/crawler/jobs/${id}`),
  listJobs: (page?: number, size?: number) =>
    apiClient.get<{ content: CrawlJob[]; totalElements: number }>('/crawler/jobs', {
      params: { page, size },
    }),
  cancelJob: (id: string) => apiClient.post<CrawlJob>(`/crawler/jobs/${id}/cancel`),
};

// Tag Management API
export const tagApi = {
  getAll: () => apiClient.get<Tag[]>('/tags'),
  create: (data: { name: string; color?: string }) => apiClient.post<Tag>('/tags', data),
  update: (id: string, data: { name?: string; color?: string }) =>
    apiClient.put<Tag>(`/tags/${id}`, data),
  delete: (id: string) => apiClient.delete(`/tags/${id}`),
};

// System API
export const systemApi = {
  getStatus: () => apiClient.get<SystemStatus>('/system/status'),
};

// File URLs
export const getImageUrl = (imageId: string) => `${API_BASE}/api/files/${imageId}`;
export const getThumbnailUrl = (imageId: string, size: 'small' | 'medium' | 'large' = 'medium') =>
  `${API_BASE}/api/files/${imageId}/thumbnail?size=${size}`;
