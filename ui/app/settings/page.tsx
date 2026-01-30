/**
 * App: Picture Model
 * Package: ui/app/settings
 * File: page.tsx
 * Version: 0.1.0
 * Turns: 4
 * Author: Claude
 * Date: 2026-01-29
 * Exports: SettingsPage
 * Description: Settings page with display, drives, crawler, and performance configurations
 */
'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import { useQuery } from '@tanstack/react-query';
import { Header } from '@/components/header';
import { systemApi } from '@/lib/api-client';
import { ChevronLeft, Save } from 'lucide-react';

type ThumbnailSize = 'small' | 'medium' | 'large';
type Theme = 'light' | 'dark' | 'auto';
type DefaultView = 'tree' | 'tags' | 'search';
type CrawlSchedule = 'manual' | 'daily' | 'weekly';
type ThumbnailQuality = 'low' | 'medium' | 'high';

export default function SettingsPage() {
  const router = useRouter();

  // Display Settings
  const [thumbnailSize, setThumbnailSize] = useState<ThumbnailSize>('medium');
  const [imagesPerPage, setImagesPerPage] = useState(24);
  const [theme, setTheme] = useState<Theme>('light');
  const [defaultView, setDefaultView] = useState<DefaultView>('tree');

  // Remote Drive Settings
  const [connectionTimeout, setConnectionTimeout] = useState(30);
  const [autoReconnect, setAutoReconnect] = useState(true);
  const [rememberCredentials, setRememberCredentials] = useState(true);

  // Crawler Settings
  const [autoCrawl, setAutoCrawl] = useState(true);
  const [extractExif, setExtractExif] = useState(true);
  const [generateThumbnails, setGenerateThumbnails] = useState(true);
  const [crawlSchedule, setCrawlSchedule] = useState<CrawlSchedule>('daily');
  const [crawlTime, setCrawlTime] = useState('03:00');
  const [crawlDay, setCrawlDay] = useState('sunday');

  // Performance Settings
  const [thumbnailQuality, setThumbnailQuality] = useState<ThumbnailQuality>('high');
  const [maxConcurrentCrawls, setMaxConcurrentCrawls] = useState(4);
  const [cacheThumbnails, setCacheThumbnails] = useState(true);
  const [preloadImages, setPreloadImages] = useState(true);

  const [hasChanges, setHasChanges] = useState(false);

  // Fetch system status
  const { data: systemStatus } = useQuery({
    queryKey: ['system-status'],
    queryFn: async () => {
      const response = await systemApi.getStatus();
      return response.data;
    },
  });

  const handleSaveSettings = () => {
    // TODO: Implement settings save
    console.log('Saving settings...', {
      thumbnailSize,
      imagesPerPage,
      theme,
      defaultView,
      connectionTimeout,
      autoReconnect,
      rememberCredentials,
      autoCrawl,
      extractExif,
      generateThumbnails,
      crawlSchedule,
      crawlTime,
      crawlDay,
      thumbnailQuality,
      maxConcurrentCrawls,
      cacheThumbnails,
      preloadImages,
    });
    setHasChanges(false);
  };

  const markChanged = () => {
    if (!hasChanges) setHasChanges(true);
  };

  return (
    <div className="min-h-screen">
      <Header />

      <main className="mx-auto max-w-4xl px-4 py-6 sm:px-6 lg:px-8">
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
          <span className="text-sm font-semibold text-[var(--text)]">Settings</span>
        </div>

        <h1 className="mb-8 text-3xl font-semibold text-[var(--text)]">Settings</h1>

        <div className="space-y-6">
          {/* Display Settings */}
          <section className="rounded-2xl border border-[var(--border)] bg-white/70 p-6">
            <h2 className="mb-4 text-lg font-semibold text-[var(--text)]">Display</h2>

            <div className="space-y-4">
              {/* Thumbnail Size */}
              <div>
                <label className="mb-2 block text-sm font-medium text-[var(--text)]">
                  Thumbnail Size
                </label>
                <div className="flex gap-3">
                  {(['small', 'medium', 'large'] as ThumbnailSize[]).map((size) => (
                    <label key={size} className="flex items-center gap-2">
                      <input
                        type="radio"
                        name="thumbnailSize"
                        value={size}
                        checked={thumbnailSize === size}
                        onChange={(e) => {
                          setThumbnailSize(e.target.value as ThumbnailSize);
                          markChanged();
                        }}
                        className="text-[var(--accent)] focus:ring-[var(--accent)]"
                      />
                      <span className="text-sm capitalize">{size}</span>
                    </label>
                  ))}
                </div>
              </div>

              {/* Images per Page */}
              <div>
                <label className="mb-2 block text-sm font-medium text-[var(--text)]">
                  Images per page
                </label>
                <select
                  value={imagesPerPage}
                  onChange={(e) => {
                    setImagesPerPage(Number(e.target.value));
                    markChanged();
                  }}
                  className="rounded-lg border border-[var(--border)] bg-white px-3 py-2 text-sm focus:border-[var(--accent)] focus:outline-none"
                >
                  <option value={12}>12</option>
                  <option value={24}>24</option>
                  <option value={48}>48</option>
                  <option value={96}>96</option>
                </select>
              </div>

              {/* Theme */}
              <div>
                <label className="mb-2 block text-sm font-medium text-[var(--text)]">Theme</label>
                <div className="flex gap-3">
                  {(['light', 'dark', 'auto'] as Theme[]).map((t) => (
                    <label key={t} className="flex items-center gap-2">
                      <input
                        type="radio"
                        name="theme"
                        value={t}
                        checked={theme === t}
                        onChange={(e) => {
                          setTheme(e.target.value as Theme);
                          markChanged();
                        }}
                        className="text-[var(--accent)] focus:ring-[var(--accent)]"
                      />
                      <span className="text-sm capitalize">{t}</span>
                    </label>
                  ))}
                </div>
              </div>

              {/* Default View */}
              <div>
                <label className="mb-2 block text-sm font-medium text-[var(--text)]">
                  Default View
                </label>
                <div className="flex gap-3">
                  {(['tree', 'tags', 'search'] as DefaultView[]).map((view) => (
                    <label key={view} className="flex items-center gap-2">
                      <input
                        type="radio"
                        name="defaultView"
                        value={view}
                        checked={defaultView === view}
                        onChange={(e) => {
                          setDefaultView(e.target.value as DefaultView);
                          markChanged();
                        }}
                        className="text-[var(--accent)] focus:ring-[var(--accent)]"
                      />
                      <span className="text-sm capitalize">
                        {view === 'tree' ? 'Directory Tree' : view}
                      </span>
                    </label>
                  ))}
                </div>
              </div>
            </div>
          </section>

          {/* Remote Drives Settings */}
          <section className="rounded-2xl border border-[var(--border)] bg-white/70 p-6">
            <h2 className="mb-4 text-lg font-semibold text-[var(--text)]">Remote Drives</h2>

            <div className="space-y-4">
              {/* Connection Timeout */}
              <div>
                <label className="mb-2 block text-sm font-medium text-[var(--text)]">
                  Connection Timeout
                </label>
                <select
                  value={connectionTimeout}
                  onChange={(e) => {
                    setConnectionTimeout(Number(e.target.value));
                    markChanged();
                  }}
                  className="rounded-lg border border-[var(--border)] bg-white px-3 py-2 text-sm focus:border-[var(--accent)] focus:outline-none"
                >
                  <option value={10}>10 seconds</option>
                  <option value={30}>30 seconds</option>
                  <option value={60}>60 seconds</option>
                  <option value={120}>120 seconds</option>
                </select>
              </div>

              {/* Auto-reconnect */}
              <label className="flex items-center gap-2">
                <input
                  type="checkbox"
                  checked={autoReconnect}
                  onChange={(e) => {
                    setAutoReconnect(e.target.checked);
                    markChanged();
                  }}
                  className="rounded border-[var(--border)] text-[var(--accent)] focus:ring-[var(--accent)]"
                />
                <span className="text-sm">Auto-reconnect on startup</span>
              </label>

              {/* Remember Credentials */}
              <label className="flex items-center gap-2">
                <input
                  type="checkbox"
                  checked={rememberCredentials}
                  onChange={(e) => {
                    setRememberCredentials(e.target.checked);
                    markChanged();
                  }}
                  className="rounded border-[var(--border)] text-[var(--accent)] focus:ring-[var(--accent)]"
                />
                <span className="text-sm">Remember credentials (encrypted)</span>
              </label>

              {/* Manage Drives Link */}
              <button
                onClick={() => router.push('/')}
                className="text-sm font-semibold text-[var(--accent)] hover:underline"
              >
                Manage Remote Drives →
              </button>
            </div>
          </section>

          {/* Crawler Settings */}
          <section className="rounded-2xl border border-[var(--border)] bg-white/70 p-6">
            <h2 className="mb-4 text-lg font-semibold text-[var(--text)]">Crawler</h2>

            <div className="space-y-4">
              <label className="flex items-center gap-2">
                <input
                  type="checkbox"
                  checked={autoCrawl}
                  onChange={(e) => {
                    setAutoCrawl(e.target.checked);
                    markChanged();
                  }}
                  className="rounded border-[var(--border)] text-[var(--accent)] focus:ring-[var(--accent)]"
                />
                <span className="text-sm">Auto-crawl on drive connection</span>
              </label>

              <label className="flex items-center gap-2">
                <input
                  type="checkbox"
                  checked={extractExif}
                  onChange={(e) => {
                    setExtractExif(e.target.checked);
                    markChanged();
                  }}
                  className="rounded border-[var(--border)] text-[var(--accent)] focus:ring-[var(--accent)]"
                />
                <span className="text-sm">Extract EXIF metadata</span>
              </label>

              <label className="flex items-center gap-2">
                <input
                  type="checkbox"
                  checked={generateThumbnails}
                  onChange={(e) => {
                    setGenerateThumbnails(e.target.checked);
                    markChanged();
                  }}
                  className="rounded border-[var(--border)] text-[var(--accent)] focus:ring-[var(--accent)]"
                />
                <span className="text-sm">Generate thumbnails</span>
              </label>

              {/* Crawl Schedule */}
              <div>
                <label className="mb-2 block text-sm font-medium text-[var(--text)]">
                  Crawl Schedule
                </label>
                <div className="space-y-2">
                  <label className="flex items-center gap-2">
                    <input
                      type="radio"
                      name="crawlSchedule"
                      value="manual"
                      checked={crawlSchedule === 'manual'}
                      onChange={(e) => {
                        setCrawlSchedule(e.target.value as CrawlSchedule);
                        markChanged();
                      }}
                      className="text-[var(--accent)] focus:ring-[var(--accent)]"
                    />
                    <span className="text-sm">Manual only</span>
                  </label>
                  <div className="flex items-center gap-2">
                    <input
                      type="radio"
                      name="crawlSchedule"
                      value="daily"
                      checked={crawlSchedule === 'daily'}
                      onChange={(e) => {
                        setCrawlSchedule(e.target.value as CrawlSchedule);
                        markChanged();
                      }}
                      className="text-[var(--accent)] focus:ring-[var(--accent)]"
                    />
                    <span className="text-sm">Daily at</span>
                    <input
                      type="time"
                      value={crawlTime}
                      onChange={(e) => {
                        setCrawlTime(e.target.value);
                        markChanged();
                      }}
                      disabled={crawlSchedule !== 'daily'}
                      className="rounded-lg border border-[var(--border)] bg-white px-2 py-1 text-sm focus:border-[var(--accent)] focus:outline-none disabled:opacity-50"
                    />
                  </div>
                  <div className="flex items-center gap-2">
                    <input
                      type="radio"
                      name="crawlSchedule"
                      value="weekly"
                      checked={crawlSchedule === 'weekly'}
                      onChange={(e) => {
                        setCrawlSchedule(e.target.value as CrawlSchedule);
                        markChanged();
                      }}
                      className="text-[var(--accent)] focus:ring-[var(--accent)]"
                    />
                    <span className="text-sm">Weekly on</span>
                    <select
                      value={crawlDay}
                      onChange={(e) => {
                        setCrawlDay(e.target.value);
                        markChanged();
                      }}
                      disabled={crawlSchedule !== 'weekly'}
                      className="rounded-lg border border-[var(--border)] bg-white px-2 py-1 text-sm focus:border-[var(--accent)] focus:outline-none disabled:opacity-50"
                    >
                      <option value="sunday">Sunday</option>
                      <option value="monday">Monday</option>
                      <option value="tuesday">Tuesday</option>
                      <option value="wednesday">Wednesday</option>
                      <option value="thursday">Thursday</option>
                      <option value="friday">Friday</option>
                      <option value="saturday">Saturday</option>
                    </select>
                  </div>
                </div>
              </div>
            </div>
          </section>

          {/* Performance Settings */}
          <section className="rounded-2xl border border-[var(--border)] bg-white/70 p-6">
            <h2 className="mb-4 text-lg font-semibold text-[var(--text)]">Performance</h2>

            <div className="space-y-4">
              {/* Thumbnail Quality */}
              <div>
                <label className="mb-2 block text-sm font-medium text-[var(--text)]">
                  Thumbnail quality
                </label>
                <select
                  value={thumbnailQuality}
                  onChange={(e) => {
                    setThumbnailQuality(e.target.value as ThumbnailQuality);
                    markChanged();
                  }}
                  className="rounded-lg border border-[var(--border)] bg-white px-3 py-2 text-sm focus:border-[var(--accent)] focus:outline-none"
                >
                  <option value="low">Low</option>
                  <option value="medium">Medium</option>
                  <option value="high">High</option>
                </select>
              </div>

              {/* Max Concurrent Crawls */}
              <div>
                <label className="mb-2 block text-sm font-medium text-[var(--text)]">
                  Maximum concurrent crawls
                </label>
                <select
                  value={maxConcurrentCrawls}
                  onChange={(e) => {
                    setMaxConcurrentCrawls(Number(e.target.value));
                    markChanged();
                  }}
                  className="rounded-lg border border-[var(--border)] bg-white px-3 py-2 text-sm focus:border-[var(--accent)] focus:outline-none"
                >
                  <option value={1}>1</option>
                  <option value={2}>2</option>
                  <option value={4}>4</option>
                  <option value={8}>8</option>
                </select>
              </div>

              <label className="flex items-center gap-2">
                <input
                  type="checkbox"
                  checked={cacheThumbnails}
                  onChange={(e) => {
                    setCacheThumbnails(e.target.checked);
                    markChanged();
                  }}
                  className="rounded border-[var(--border)] text-[var(--accent)] focus:ring-[var(--accent)]"
                />
                <span className="text-sm">Cache thumbnails locally</span>
              </label>

              <label className="flex items-center gap-2">
                <input
                  type="checkbox"
                  checked={preloadImages}
                  onChange={(e) => {
                    setPreloadImages(e.target.checked);
                    markChanged();
                  }}
                  className="rounded border-[var(--border)] text-[var(--accent)] focus:ring-[var(--accent)]"
                />
                <span className="text-sm">Preload next/previous images</span>
              </label>
            </div>
          </section>

          {/* Database Settings */}
          <section className="rounded-2xl border border-[var(--border)] bg-white/70 p-6">
            <h2 className="mb-4 text-lg font-semibold text-[var(--text)]">Database</h2>

            <div className="space-y-4">
              <div className="grid grid-cols-3 gap-4 text-sm">
                <div>
                  <span className="text-[var(--muted)]">Total Images</span>
                  <p className="mt-1 text-lg font-semibold text-[var(--text)]">
                    {systemStatus?.totalImages?.toLocaleString() || '0'}
                  </p>
                </div>
                <div>
                  <span className="text-[var(--muted)]">Total Drives</span>
                  <p className="mt-1 text-lg font-semibold text-[var(--text)]">
                    {systemStatus?.totalDrives || 0}
                  </p>
                </div>
                <div>
                  <span className="text-[var(--muted)]">Database Size</span>
                  <p className="mt-1 text-lg font-semibold text-[var(--text)]">~245 MB</p>
                </div>
              </div>

              <div className="flex flex-wrap gap-2">
                <button className="rounded-lg border border-[var(--border)] bg-white px-4 py-2 text-sm font-semibold text-[var(--text)] transition hover:bg-[var(--surface-muted)]">
                  Clear Thumbnail Cache
                </button>
                <button className="rounded-lg border border-[var(--border)] bg-white px-4 py-2 text-sm font-semibold text-[var(--text)] transition hover:bg-[var(--surface-muted)]">
                  Rebuild Search Index
                </button>
                <button className="rounded-lg border border-[var(--border)] bg-white px-4 py-2 text-sm font-semibold text-[var(--text)] transition hover:bg-[var(--surface-muted)]">
                  Export Database
                </button>
                <button className="rounded-lg border border-[var(--border)] bg-white px-4 py-2 text-sm font-semibold text-[var(--text)] transition hover:bg-[var(--surface-muted)]">
                  Import Database
                </button>
              </div>
            </div>
          </section>

          {/* Save Button */}
          <div className="flex justify-end">
            <button
              onClick={handleSaveSettings}
              disabled={!hasChanges}
              className="flex items-center gap-2 rounded-lg bg-[var(--accent)] px-6 py-3 font-semibold text-white transition hover:bg-[var(--accent-strong)] disabled:opacity-50"
            >
              <Save className="h-4 w-4" />
              <span>Save Settings</span>
            </button>
          </div>
        </div>
      </main>
    </div>
  );
}
