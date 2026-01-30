/**
 * App: Picture Model
 * Package: ui/components
 * File: header.tsx
 * Version: 0.2.0
 * Turns: 4
 * Author: Claude
 * Date: 2026-01-29
 * Exports: Header
 * Description: App header with logo, view switcher navigation, and settings access - Fixed styling
 */
'use client';

import Link from 'next/link';
import { usePathname } from 'next/navigation';
import { Search, Settings, FolderTree, Tag, Home } from 'lucide-react';

export function Header() {
  const pathname = usePathname();

  const navItems = [
    { href: '/', label: 'Dashboard', icon: Home },
    { href: '/tree', label: 'Directory Tree', icon: FolderTree },
    { href: '/tags', label: 'Tags', icon: Tag },
    { href: '/search', label: 'Search', icon: Search },
  ];

  return (
    <header className="sticky top-0 z-40 border-b border-gray-200 bg-white/75 shadow-sm backdrop-blur-sm">
      <div className="mx-auto flex h-16 max-w-7xl items-center justify-between px-4 sm:px-6 lg:px-8">
        {/* Logo */}
        <Link href="/" className="flex items-center gap-3">
          <div className="flex h-9 w-9 items-center justify-center rounded-xl bg-gradient-to-br from-amber-400 via-rose-400 to-teal-500 shadow-sm">
            <span className="text-sm font-semibold tracking-wide text-white">PM</span>
          </div>
          <div>
            <span className="text-lg font-semibold text-gray-900">Picture Model</span>
            <div className="text-xs text-gray-500">Multi-drive image studio</div>
          </div>
        </Link>

        {/* Navigation Pills */}
        <nav className="flex max-w-[60vw] items-center gap-1 overflow-x-auto rounded-full border border-gray-200 bg-white/70 p-1 text-sm shadow-sm backdrop-blur-sm sm:max-w-none">
          {navItems.map((item) => {
            const Icon = item.icon;
            const isActive = pathname === item.href;

            return (
              <Link
                key={item.href}
                href={item.href}
                className={`flex items-center gap-2 whitespace-nowrap rounded-full px-3 py-2 font-semibold transition-colors ${
                  isActive
                    ? 'bg-teal-50 text-teal-900 shadow-sm'
                    : 'text-gray-500 hover:bg-gray-50 hover:text-gray-900'
                }`}
              >
                <Icon className="h-4 w-4" />
                <span>{item.label}</span>
              </Link>
            );
          })}
        </nav>

        {/* Settings Button */}
        <Link
          href="/settings"
          className="rounded-full border border-gray-200 bg-white/70 p-2 text-gray-500 shadow-sm transition-colors hover:bg-gray-50 hover:text-gray-900"
        >
          <Settings className="h-5 w-5" />
        </Link>
      </div>
    </header>
  );
}
