// utils/parseRepoName.ts

/**
 * GitLab/GitHub repo URL에서 레포지토리 이름만 파싱합니다.
 *
 * @param repoUrl 전체 레포 URL
 * @returns 레포지토리 이름 (확장자 .git 제외)
 */
export function parseRepoName(repoUrl: string): string {
  // URL 끝에서 ".git"을 제거한 뒤, 슬래시로 분리해서 마지막 요소를 반환
  const withoutGit = repoUrl.replace(/\.git(?:\/)?$/, '');
  const parts = withoutGit.split('/');
  return parts[parts.length - 1] || '';
}
