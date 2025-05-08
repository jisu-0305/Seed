import { create } from 'zustand';
import { persist } from 'zustand/middleware';

import { ProjectInfo } from '@/types/project';

interface ProjectInfoStore {
  stepStatus: ProjectInfo;
  setGitlabStatus: (gitlab: ProjectInfo['gitlab']) => void;
  setServerStatus: (server: ProjectInfo['server']) => void;
  setAppStatus: (app: ProjectInfo['app']) => void;
  setEnvStatus: (env: ProjectInfo['env']) => void;
  resetProjectStatus: () => void;
  // 다음 단계 유효성 검사
  onNextValidate: () => boolean;
  setOnNextValidate: (fn: () => boolean) => void;
  // 다음 성공시 콜백함수
  onNextSuccess?: () => void;
  setOnNextSuccess: (fn: () => void) => void;
}

const initialStatus: ProjectInfo = {
  gitlab: {
    repo: '',
    structure: '모노',
    directory: {
      client: '',
      server: '',
    },
  },
  server: {
    ip: '',
    pem: false,
  },
  app: [],
  env: {
    frontEnv: false,
    backEnv: false,
    node: 'v22.14.0',
    jdk: 17,
    buildTool: 'Gradle',
  },
};

export const useProjectInfoStore = create<ProjectInfoStore>()(
  persist(
    (set) => ({
      stepStatus: initialStatus,

      setGitlabStatus: (gitlab) =>
        set((state) => ({
          stepStatus: { ...state.stepStatus, gitlab },
        })),

      setServerStatus: (server) =>
        set((state) => ({
          stepStatus: { ...state.stepStatus, server },
        })),

      setAppStatus: (app) =>
        set((state) => ({
          stepStatus: { ...state.stepStatus, app },
        })),

      setEnvStatus: (env) =>
        set((state) => ({
          stepStatus: { ...state.stepStatus, env },
        })),

      resetProjectStatus: () => set({ stepStatus: initialStatus }),

      onNextValidate: () => true,
      // 콜백 등록
      setOnNextValidate: (fn) => set(() => ({ onNextValidate: fn })),

      onNextSuccess: undefined,
      setOnNextSuccess: (fn) => set({ onNextSuccess: fn }),
    }),
    {
      name: 'projectInfo',
    },
  ),
);
