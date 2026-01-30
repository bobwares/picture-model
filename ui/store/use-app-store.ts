import { create } from 'zustand';

interface AppState {
  // View preferences
  viewMode: 'tree' | 'tags' | 'search';
  setViewMode: (mode: 'tree' | 'tags' | 'search') => void;

  // Selected drive
  selectedDriveId: string | null;
  setSelectedDriveId: (id: string | null) => void;

  // Search state
  searchQuery: string;
  setSearchQuery: (query: string) => void;

  // Sidebar collapsed state
  sidebarCollapsed: boolean;
  toggleSidebar: () => void;
}

export const useAppStore = create<AppState>((set) => ({
  viewMode: 'tree',
  setViewMode: (mode) => set({ viewMode: mode }),

  selectedDriveId: null,
  setSelectedDriveId: (id) => set({ selectedDriveId: id }),

  searchQuery: '',
  setSearchQuery: (query) => set({ searchQuery: query }),

  sidebarCollapsed: false,
  toggleSidebar: () => set((state) => ({ sidebarCollapsed: !state.sidebarCollapsed })),
}));
