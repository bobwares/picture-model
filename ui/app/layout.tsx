/**
 * App: Picture Model
 * Package: ui
 * File: layout.tsx
 * Version: 0.1.0
 * Turns: 1
 * Author: Codex
 * Date: 2026-01-29T22:11:12Z
 * Exports: metadata, RootLayout
 * Description: Root layout configuring global fonts, styles, and providers.
 */

'use client';


import { Fraunces, Manrope } from 'next/font/google';
import './globals.css';
import { QueryProvider } from '@/lib/query-provider';
import React from "react";

const manrope = Manrope({ subsets: ['latin'], variable: '--font-sans' });
const fraunces = Fraunces({ subsets: ['latin'], variable: '--font-display' });


export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="en">
      <body className={`${manrope.variable} ${fraunces.variable} antialiased`}>
        <QueryProvider>{children}</QueryProvider>
      </body>
    </html>
  );
}
