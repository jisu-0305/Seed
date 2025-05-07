'use client';

import Header from '@/components/Common/Header';
import ProjectEdit from '@/components/Projects/detail/ProjectEdit';

export default function ProjectEditPage() {
  return (
    <>
      <Header title="프로젝트 > 프로젝트 수정" />
      <ProjectEdit />
    </>
  );
}
