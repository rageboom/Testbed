import react from '@vitejs/plugin-react';
import type { UserConfig } from 'vite';
import type { InlineConfig } from 'vitest/node';

/** 빌드 · 개발 서버 · 테스트 설정(ADR-RESERVATION-004). */
export default function viteConfig(): UserConfig & { test: InlineConfig } {
  return {
    plugins: [react()],
    server: {
      // 개발 서버는 /api 를 백엔드로 넘긴다.
      proxy: { '/api': 'http://localhost:8080' },
    },
    test: {
      environment: 'jsdom',
      globals: true,
      setupFiles: ['./src/testing/setup.ts'],
      include: ['src/**/*.test.{ts,tsx}'],
    },
  };
}
