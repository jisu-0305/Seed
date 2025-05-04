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
}

const initialStatus: ProjectInfo = {
  gitlab: {
    repo: '',
    structure: '모노',
    directory: false,
  },
  server: {
    ip: '',
    pem: false,
  },
  app: [],
  env: {
    env: false,
    node: '',
    jdk: 17,
    buildTool: '',
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
    }),
    {
      name: 'projectInfo',
    },
  ),
);
