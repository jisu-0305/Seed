'use client';

import Header from '@/components/Common/Header';
import NewProject from '@/components/NewProject/NewProject';

export default function CreatePage() {
  return (
    <>
      <Header title="새 프로젝트" />
      <NewProject />
    </>
  );
}
