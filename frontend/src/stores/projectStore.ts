import { create } from 'zustand';
import { persist } from 'zustand/middleware';

// eslint-disable-next-line import/no-cycle
import { fetchProjects } from '@/apis/project';
import {
  ApplicationWithDefaults,
  ProjectDetailData,
  ProjectInfo as _PI,
  ProjectSummary,
} from '@/types/project';

// ❗️ 원본 _PI 에서 app 필드만 ApplicationWithDefaults[] 로 교체
type ProjectInfo = Omit<_PI, 'app'> & {
  app: ApplicationWithDefaults[];
  ownerId: number;
};

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
  loadProjectInfo: (detail: ProjectDetailData) => void;
}

const initialStatus: ProjectInfo = {
  ownerId: 0,
  gitlab: {
    id: 0,
    repo: '',
    defaultBranch: '',
    structure: '모노',
    directory: {
      client: '',
      server: '',
    },
  },
  server: {
    ip: '',
    pem: false,
    pemName: '',
  },
  app: [],
  env: {
    frontendFramework: 'React',
    frontEnv: false,
    frontEnvName: '',
    backEnv: false,
    backEnvName: '',
    node: '22',
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

      resetProjectStatus: () => {
        set({ stepStatus: initialStatus });
        try {
          localStorage.removeItem('projectInfo');
        } catch (e) {
          console.warn('로컬스토리지 초기화 실패:', e);
        }
      },

      onNextValidate: () => true,
      // 콜백 등록
      setOnNextValidate: (fn) => set(() => ({ onNextValidate: fn })),

      onNextSuccess: undefined,
      setOnNextSuccess: (fn) => set({ onNextSuccess: fn }),

      loadProjectInfo: (detail) =>
        set({
          stepStatus: {
            ownerId: detail.ownerId,
            gitlab: {
              id: 0,
              repo: detail.repositoryUrl,
              defaultBranch:
                detail.structure === 'MONO'
                  ? detail.backendDirectoryName
                  : detail.backendBranchName,
              structure: detail.structure === 'MONO' ? '모노' : '멀티',
              directory: {
                client: detail.frontendDirectoryName,
                server: detail.backendDirectoryName,
              },
            },
            server: {
              ip: detail.serverIP,
              pem: Boolean(detail.pemFilePath),
              pemName: detail.pemFilePath
                ? detail.pemFilePath.split('/').pop()!
                : '',
            },
            app: detail.applicationList.map((app) => ({
              ...app,
              defaultPorts: [app.port],
            })) as ApplicationWithDefaults[],
            env: {
              frontendFramework: detail.frontendFramework,
              frontEnv: Boolean(detail.frontendEnvFilePath),
              frontEnvName: detail.frontendEnvFilePath
                ? detail.frontendEnvFilePath.split('/').pop()!
                : '',
              backEnv: Boolean(detail.backendEnvFilePath),
              backEnvName: detail.backendEnvFilePath
                ? detail.backendEnvFilePath.split('/').pop()!
                : '',
              node: detail.nodejsVersion,
              jdk: detail.jdkVersion,
              buildTool: detail.jdkBuildTool,
            },
          },
        }),
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
  loadProjects: (force?: boolean) => Promise<void>;
}

export const useProjectStore = create<ProjectStore>((set, get) => ({
  projects: [],
  loading: false,
  error: null,

  loadProjects: async (force = false) => {
    const { projects } = get();
    if (!force && projects.length > 0) {
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
