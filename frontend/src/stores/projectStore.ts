import { create } from 'zustand';
import { persist } from 'zustand/middleware';

import { fetchProjects } from '@/apis/project';
import { ProjectInfo, ProjectSummary } from '@/types/project';

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
    frontendFramework: '',
    frontEnv: false,
    backEnv: false,
    node: 'v22',
    jdk: '17',
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

interface ProjectFileState {
  pemFile: File | null;
  frontEnvFile: File | null;
  backEnvFile: File | null;
  setPemFile: (file: File | null) => void;
  setFrontEnvFile: (file: File | null) => void;
  setBackEnvFile: (file: File | null) => void;
  clearAll: () => void;
}

export const useProjectFileStore = create<ProjectFileState>((set) => ({
  pemFile: null,
  frontEnvFile: null,
  backEnvFile: null,

  setPemFile: (file) => set({ pemFile: file }),
  setFrontEnvFile: (file) => set({ frontEnvFile: file }),
  setBackEnvFile: (file) => set({ backEnvFile: file }),

  clearAll: () =>
    set({
      pemFile: null,
      frontEnvFile: null,
      backEnvFile: null,
    }),
}));

interface ProjectStore {
  projects: ProjectSummary[];
  loading: boolean;
  error: string | null;
  loadProjects: () => Promise<void>;
}

export const useProjectStore = create<ProjectStore>((set, get) => ({
  projects: [],
  loading: false,
  error: null,

  loadProjects: async () => {
    const { projects } = get();
    if (projects.length > 0) {
      return;
    }

    set({ loading: true, error: null });
    try {
      const data = await fetchProjects();

      set({ projects: data });
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
    } catch (err: any) {
      set({ error: '프로젝트를 불러오는 데 실패했습니다.' });
    } finally {
      set({ loading: false });
    }
  },
}));
