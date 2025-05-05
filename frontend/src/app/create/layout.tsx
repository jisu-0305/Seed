import ProjectCreateLayout from '@/components/NewProject/layout/ProjectCreateLayout';

export default function CreateLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return <ProjectCreateLayout>{children}</ProjectCreateLayout>;
}
