#!/usr/bin/env node
// 백엔드(Gradle · JUnit 5)와 프론트엔드(Vitest) 테스트를 차례로 돌리고
// JUnit XML 보고서를 build/test-reports/ 한 곳에 모은다.
// 둘 중 하나라도 실패하면 종료 코드 1 로 끝난다.
import { spawnSync } from 'node:child_process';
import { cpSync, existsSync, mkdirSync, readdirSync, rmSync } from 'node:fs';
import { dirname, join, resolve } from 'node:path';
import { fileURLToPath } from 'node:url';

const root = resolve(dirname(fileURLToPath(import.meta.url)), '..', '..');
const reports = join(root, 'build', 'test-reports');
rmSync(reports, { recursive: true, force: true });
mkdirSync(reports, { recursive: true });

function run(cmd, args, cwd) {
  console.log(`\n$ ${cmd} ${args.join(' ')}  (${cwd})`);
  const r = spawnSync(cmd, args, { cwd, stdio: 'inherit' });
  return r.status ?? 1;
}

const api = join(root, 'apps', 'api');
const gradle = existsSync(join(api, 'gradlew')) ? './gradlew' : 'gradle';
const apiResults = join(api, 'build', 'test-results', 'test');
rmSync(apiResults, { recursive: true, force: true });
const apiStatus = run(gradle, ['test', '--console=plain', '--continue'], api);
if (existsSync(apiResults)) {
  for (const f of readdirSync(apiResults)) {
    if (f.endsWith('.xml')) cpSync(join(apiResults, f), join(reports, `api-${f}`));
  }
}

const web = join(root, 'apps', 'web');
if (!existsSync(join(web, 'node_modules'))) run('npm', ['install', '--no-audit', '--no-fund'], web);
const webStatus = run('npx', ['vitest', 'run', '--reporter=default', '--reporter=junit', `--outputFile.junit=${join(reports, 'web-vitest.xml')}`], web);

console.log(`\napi: ${apiStatus === 0 ? 'pass' : 'fail'} / web: ${webStatus === 0 ? 'pass' : 'fail'}`);
process.exit(apiStatus === 0 && webStatus === 0 ? 0 : 1);
