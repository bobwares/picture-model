/**
 * App: Picture Model
 * Package: ui/app/image/[driveId]/[imageId]
 * File: page.tsx
 * Version: 0.1.0
 * Turns: 4
 * Author: Claude
 * Date: 2026-01-29
 * Exports: ImageDetailPage
 * Description: Image detail page with right sidebar layout (70/30 split)
 */
'use client';

import { useState } from 'react';
import { useParams, useRouter } from 'next/navigation';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import Image from 'next/image';
import { Header } from '@/components/header';
import { MetadataEditor } from '@/components/metadata-editor';
import { imageApi, tagApi } from '@/lib/api-client';
import {
  ChevronLeft,
  ChevronRight,
  ZoomIn,
  ZoomOut,
  Maximize2,
  ChevronLeftIcon,
} from 'lucide-react';

type ZoomLevel = 'fit' | 'full' | number;

export default function ImageDetailPage() {
  const params = useParams();
  const router = useRouter();
  const queryClient = useQueryClient();

  const driveId = params.driveId as string;
  const imageId = params.imageId as string;

  const [zoomLevel, setZoomLevel] = useState<ZoomLevel>('fit');
  const [imageError, setImageError] = useState(false);

  // Fetch image details
  const { data: image, isLoading } = useQuery({
    queryKey: ['image', imageId],
    queryFn: async () => {
      const response = await imageApi.getById(imageId);
      return response.data;
    },
  });

  // Fetch all tags
  const { data: tags = [] } = useQuery({
    queryKey: ['tags'],
    queryFn: async () => {
      const response = await tagApi.getAll();
      return response.data;
    },
  });

  // Update metadata mutation
  const updateMetadataMutation = useMutation({
    mutationFn: (metadata: Partial<typeof image>) =>
      imageApi.updateMetadata(imageId, metadata as any),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['image', imageId] });
    },
  });

  // Add tag mutation
  const addTagMutation = useMutation({
    mutationFn: (tagId: string) => imageApi.addTags(imageId, [tagId]),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['image', imageId] });
    },
  });

  // Remove tag mutation
  const removeTagMutation = useMutation({
    mutationFn: (tagId: string) => imageApi.removeTag(imageId, tagId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['image', imageId] });
    },
  });

  const handleZoomIn = () => {
    if (zoomLevel === 'fit') {
      setZoomLevel(100);
    } else if (typeof zoomLevel === 'number') {
      setZoomLevel(Math.min(zoomLevel + 25, 200));
    }
  };

  const handleZoomOut = () => {
    if (typeof zoomLevel === 'number') {
      if (zoomLevel <= 100) {
        setZoomLevel('fit');
      } else {
        setZoomLevel(Math.max(zoomLevel - 25, 100));
      }
    }
  };

  const handleZoomFit = () => {
    setZoomLevel('fit');
  };

  const handleSaveMetadata = (metadata: Partial<typeof image>) => {
    updateMetadataMutation.mutate(metadata);
  };

  const handleAddTag = (tagId: string) => {
    addTagMutation.mutate(tagId);
  };

  const handleRemoveTag = (tagId: string) => {
    removeTagMutation.mutate(tagId);
  };

  const handlePrevious = () => {
    // TODO: Implement navigation to previous image
    console.log('Previous image');
  };

  const handleNext = () => {
    // TODO: Implement navigation to next image
    console.log('Next image');
  };

  if (isLoading) {
    return (
      <div className="min-h-screen">
        <Header />
        <div className="flex h-[calc(100vh-64px)] items-center justify-center">
          <div className="text-center">
            <div className="mx-auto h-12 w-12 animate-spin rounded-full border-4 border-[var(--border)] border-t-[var(--accent)]" />
            <p className="mt-4 text-sm text-[var(--muted)]">Loading image...</p>
          </div>
        </div>
      </div>
    );
  }

  if (!image) {
    return (
      <div className="min-h-screen">
        <Header />
        <div className="flex h-[calc(100vh-64px)] items-center justify-center">
          <div className="text-center">
            <p className="text-lg font-semibold text-[var(--text)]">Image not found</p>
            <button
              onClick={() => router.back()}
              className="mt-4 text-sm font-semibold text-[var(--accent)] hover:underline"
            >
              Go back
            </button>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen">
      <Header />

      <div className="flex h-[calc(100vh-64px)]">
        {/* Left Panel - Image Display (70%) */}
        <div className="flex w-[70%] flex-col border-r border-[var(--border)] bg-[var(--surface-muted)]">
          {/* Top Bar - Breadcrumb and Controls */}
          <div className="border-b border-[var(--border)] bg-white/70 px-6 py-3">
            <div className="flex items-center justify-between">
              <button
                onClick={() => router.back()}
                className="flex items-center gap-2 text-sm font-semibold text-[var(--muted)] hover:text-[var(--text)]"
              >
                <ChevronLeftIcon className="h-4 w-4" />
                <span>Back to View</span>
              </button>

              {/* Zoom Controls */}
              <div className="flex items-center gap-2">
                <button
                  onClick={handleZoomOut}
                  className="rounded-lg border border-[var(--border)] bg-white p-2 text-[var(--text)] transition hover:bg-[var(--surface-muted)]"
                  disabled={zoomLevel === 'fit'}
                >
                  <ZoomOut className="h-4 w-4" />
                </button>
                <span className="min-w-[60px] text-center text-sm font-medium text-[var(--text)]">
                  {zoomLevel === 'fit' ? 'Fit' : `${zoomLevel}%`}
                </span>
                <button
                  onClick={handleZoomIn}
                  className="rounded-lg border border-[var(--border)] bg-white p-2 text-[var(--text)] transition hover:bg-[var(--surface-muted)]"
                  disabled={typeof zoomLevel === 'number' && zoomLevel >= 200}
                >
                  <ZoomIn className="h-4 w-4" />
                </button>
                <button
                  onClick={handleZoomFit}
                  className="rounded-lg border border-[var(--border)] bg-white p-2 text-[var(--text)] transition hover:bg-[var(--surface-muted)]"
                >
                  <Maximize2 className="h-4 w-4" />
                </button>
              </div>
            </div>
          </div>

          {/* Image Display Area */}
          <div className="relative flex-1 overflow-auto">
            <div className="flex min-h-full items-center justify-center p-8">
              {!imageError ? (
                <div
                  className={`relative ${
                    zoomLevel === 'fit' ? 'max-h-full max-w-full' : ''
                  }`}
                  style={
                    typeof zoomLevel === 'number'
                      ? { width: `${zoomLevel}%`, height: 'auto' }
                      : undefined
                  }
                >
                  <Image
                    src={image.imageUrl}
                    alt={image.fileName}
                    width={image.width || 1920}
                    height={image.height || 1080}
                    className={`${zoomLevel === 'fit' ? 'object-contain' : 'object-cover'}`}
                    style={{ maxWidth: '100%', height: 'auto' }}
                    onError={() => setImageError(true)}
                    priority
                  />
                </div>
              ) : (
                <div className="text-center">
                  <p className="text-[var(--muted)]">Failed to load image</p>
                </div>
              )}
            </div>
          </div>

          {/* Bottom Bar - Navigation */}
          <div className="border-t border-[var(--border)] bg-white/70 px-6 py-3">
            <div className="flex items-center justify-center gap-4">
              <button
                onClick={handlePrevious}
                className="flex items-center gap-2 rounded-lg bg-[var(--surface-muted)] px-4 py-2 text-sm font-semibold text-[var(--text)] transition hover:bg-[var(--surface-strong)]"
              >
                <ChevronLeft className="h-4 w-4" />
                <span>Previous</span>
              </button>
              <button
                onClick={handleNext}
                className="flex items-center gap-2 rounded-lg bg-[var(--surface-muted)] px-4 py-2 text-sm font-semibold text-[var(--text)] transition hover:bg-[var(--surface-strong)]"
              >
                <span>Next</span>
                <ChevronRight className="h-4 w-4" />
              </button>
            </div>
          </div>
        </div>

        {/* Right Sidebar - Metadata (30%) */}
        <div className="w-[30%] overflow-y-auto bg-white/70">
          <MetadataEditor
            image={image}
            availableTags={tags}
            onSave={handleSaveMetadata}
            onAddTag={handleAddTag}
            onRemoveTag={handleRemoveTag}
          />
        </div>
      </div>
    </div>
  );
}
