/**
 * App: Picture Model
 * Package: ui/app/search
 * File: page.tsx
 * Version: 0.1.0
 * Turns: 4
 * Author: Claude
 * Date: 2026-01-29
 * Exports: SearchPage
 * Description: Search results view with advanced filtering
 */
'use client';

import { useState, useEffect } from 'react';
import { useRouter, useSearchParams } from 'next/navigation';
import { useQuery } from '@tanstack/react-query';
import { Header } from '@/components/header';
import { ImageGrid } from '@/components/image-grid';
import { FilterPanel } from '@/components/filter-panel';
import { imageApi, driveApi, tagApi } from '@/lib/api-client';
import { ChevronLeft, Search as SearchIcon, ArrowUpDown } from 'lucide-react';

type SortOption = 'relevance' | 'date' | 'name' | 'size';

export default function SearchPage() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const queryParam = searchParams.get('q') || '';

  const [query, setQuery] = useState(queryParam);
  const [searchInput, setSearchInput] = useState(queryParam);
  const [selectedDriveIds, setSelectedDriveIds] = useState<string[]>([]);
  const [selectedTagIds, setSelectedTagIds] = useState<string[]>([]);
  const [dateFrom, setDateFrom] = useState<string>('');
  const [dateTo, setDateTo] = useState<string>('');
  const [selectedFileTypes, setSelectedFileTypes] = useState<string[]>([]);
  const [selectedImageIds, setSelectedImageIds] = useState<Set<string>>(new Set());
  const [sortBy, setSortBy] = useState<SortOption>('relevance');
  const [page, setPage] = useState(0);

  // Fetch drives
  const { data: drives = [] } = useQuery({
    queryKey: ['drives'],
    queryFn: async () => {
      const response = await driveApi.getAll();
      return response.data;
    },
  });

  // Fetch tags
  const { data: tags = [] } = useQuery({
    queryKey: ['tags'],
    queryFn: async () => {
      const response = await tagApi.getAll();
      return response.data;
    },
  });

  // Fetch search results
  const { data: imagesResponse, isLoading: imagesLoading } = useQuery({
    queryKey: [
      'search',
      query,
      selectedDriveIds,
      selectedTagIds,
      dateFrom,
      dateTo,
      selectedFileTypes,
      sortBy,
      page,
    ],
    queryFn: async () => {
      const response = await imageApi.search({
        query: query || undefined,
        driveId: selectedDriveIds.length === 1 ? selectedDriveIds[0] : undefined,
        tagIds: selectedTagIds.length > 0 ? selectedTagIds : undefined,
        fromDate: dateFrom || undefined,
        toDate: dateTo || undefined,
        sort: sortBy,
        page,
        size: 24,
      });
      return response.data;
    },
    enabled: query.length > 0,
  });

  const images = imagesResponse?.content || [];
  const totalImages = imagesResponse?.totalElements || 0;

  useEffect(() => {
    setQuery(queryParam);
    setSearchInput(queryParam);
  }, [queryParam]);

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    if (searchInput.trim()) {
      setQuery(searchInput);
      setPage(0);
      router.push(`/search?q=${encodeURIComponent(searchInput)}`);
    }
  };

  const handleImageSelect = (imageId: string) => {
    setSelectedImageIds((prev) => {
      const next = new Set(prev);
      if (next.has(imageId)) {
        next.delete(imageId);
      } else {
        next.add(imageId);
      }
      return next;
    });
  };

  const handleImageClick = (imageId: string) => {
    const image = images.find((img) => img.id === imageId);
    if (image) {
      router.push(`/image/${image.driveId}/${imageId}`);
    }
  };

  const handleSelectAll = () => {
    if (selectedImageIds.size === images.length) {
      setSelectedImageIds(new Set());
    } else {
      setSelectedImageIds(new Set(images.map((img) => img.id)));
    }
  };

  const handleClearFilters = () => {
    setSelectedDriveIds([]);
    setSelectedTagIds([]);
    setDateFrom('');
    setDateTo('');
    setSelectedFileTypes([]);
    setPage(0);
  };

  const hasActiveFilters =
    selectedDriveIds.length > 0 ||
    selectedTagIds.length > 0 ||
    selectedFileTypes.length > 0 ||
    dateFrom ||
    dateTo;

  return (
    <div className="min-h-screen">
      <Header />

      <main className="mx-auto max-w-[1800px] px-4 py-6 sm:px-6 lg:px-8">
        {/* Breadcrumb */}
        <div className="mb-6 flex items-center gap-3">
          <button
            onClick={() => router.push('/')}
            className="flex items-center gap-2 text-sm font-semibold text-[var(--muted)] hover:text-[var(--text)]"
          >
            <ChevronLeft className="h-4 w-4" />
            <span>Dashboard</span>
          </button>
          <span className="text-[var(--muted)]">/</span>
          <span className="text-sm font-semibold text-[var(--text)]">Search</span>
        </div>

        {/* Search Bar */}
        <form onSubmit={handleSearch} className="mb-6">
          <div className="flex gap-3">
            <div className="relative flex-1">
              <SearchIcon className="absolute left-4 top-1/2 h-5 w-5 -translate-y-1/2 text-[var(--muted)]" />
              <input
                type="text"
                value={searchInput}
                onChange={(e) => setSearchInput(e.target.value)}
                placeholder="Search images by filename, metadata, or tags..."
                className="w-full rounded-xl border border-[var(--border)] bg-white/70 py-3 pl-12 pr-4 text-[var(--text)] placeholder:text-[var(--muted)] focus:border-[var(--accent)] focus:outline-none"
              />
            </div>
            <button
              type="submit"
              className="rounded-xl bg-[var(--accent)] px-6 py-3 font-semibold text-white transition hover:bg-[var(--accent-strong)]"
            >
              Search
            </button>
          </div>
        </form>

        <div className="grid grid-cols-1 gap-6 lg:grid-cols-[320px_1fr]">
          {/* Left Sidebar - Filters */}
          <div className="lg:sticky lg:top-20 lg:h-[calc(100vh-120px)] lg:overflow-y-auto">
            <FilterPanel
              drives={drives}
              tags={tags}
              selectedDriveIds={selectedDriveIds}
              selectedTagIds={selectedTagIds}
              dateFrom={dateFrom}
              dateTo={dateTo}
              selectedFileTypes={selectedFileTypes}
              onDriveChange={setSelectedDriveIds}
              onTagChange={setSelectedTagIds}
              onDateChange={(from, to) => {
                setDateFrom(from);
                setDateTo(to);
              }}
              onFileTypeChange={setSelectedFileTypes}
              onClearFilters={handleClearFilters}
            />
          </div>

          {/* Right Main Area - Search Results */}
          <div className="space-y-4">
            {/* Results Summary */}
            {query && (
              <div className="rounded-xl border border-[var(--border)] bg-white/70 p-4">
                <div className="flex flex-wrap items-center justify-between gap-3">
                  <div>
                    <h2 className="text-lg font-semibold text-[var(--text)]">
                      Search Results for "{query}"
                    </h2>
                    <p className="mt-1 text-sm text-[var(--muted)]">
                      Found {totalImages} image{totalImages !== 1 ? 's' : ''}
                      {hasActiveFilters && ' with active filters'}
                    </p>
                  </div>

                  {/* Sort Dropdown */}
                  {images.length > 0 && (
                    <div className="flex items-center gap-2">
                      <ArrowUpDown className="h-4 w-4 text-[var(--muted)]" />
                      <select
                        value={sortBy}
                        onChange={(e) => setSortBy(e.target.value as SortOption)}
                        className="rounded-lg border border-[var(--border)] bg-white px-3 py-1.5 text-sm font-medium text-[var(--text)] focus:border-[var(--accent)] focus:outline-none"
                      >
                        <option value="relevance">Relevance</option>
                        <option value="date">Date</option>
                        <option value="name">Name</option>
                        <option value="size">Size</option>
                      </select>
                    </div>
                  )}
                </div>
              </div>
            )}

            {/* Batch Actions Bar */}
            {selectedImageIds.size > 0 && (
              <div className="rounded-xl border border-[var(--border)] bg-[var(--accent-soft)] p-4">
                <div className="flex items-center justify-between">
                  <span className="text-sm font-semibold text-[var(--accent-ink)]">
                    {selectedImageIds.size} image{selectedImageIds.size !== 1 ? 's' : ''} selected
                  </span>
                  <div className="flex gap-2">
                    <button
                      onClick={handleSelectAll}
                      className="rounded-lg bg-white/50 px-3 py-1.5 text-sm font-semibold text-[var(--accent-ink)] transition hover:bg-white/70"
                    >
                      {selectedImageIds.size === images.length ? 'Deselect All' : 'Select All'}
                    </button>
                    <button className="rounded-lg bg-[var(--accent)] px-3 py-1.5 text-sm font-semibold text-white transition hover:bg-[var(--accent-strong)]">
                      Batch Tag
                    </button>
                  </div>
                </div>
              </div>
            )}

            {/* Image Grid */}
            <ImageGrid
              images={images}
              loading={imagesLoading}
              selectedImageIds={selectedImageIds}
              onImageSelect={handleImageSelect}
              onImageClick={handleImageClick}
              size="medium"
              emptyMessage={
                !query
                  ? 'Enter a search query to find images'
                  : 'No images found matching your search'
              }
            />
          </div>
        </div>
      </main>
    </div>
  );
}
