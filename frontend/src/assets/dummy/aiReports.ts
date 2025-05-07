// src/assets/dummy/aiReports.ts
import type { AiReport } from '@/types/aiReport';

export const dummyReports: AiReport[] = [
  {
    id: '119',
    title: '[BE] 순환 참조 에러 해결',
    date: 'Apr 22, 2025',
    status: 'In Progress',
    summary: `BasicServiceImpl과 SecondaryServiceImpl이 서로 생성자 주입(Constructor Injection)으로 참조하며 순환 의존성 문제가 발생했습니다.

이로 인해 애플리케이션 구동 시 BeanCurrentlyInCreationException이 발생합니다. 순환 의존성을 해소하기 위해 한 쪽 빈에 @Lazy 어노테이션을 적용하거나, 생성자 주입 대신 필드(또는 세터) 주입으로 전환하는 방법을 적용했습니다.`,
    files: ['BasicServiceImpl.java', 'SecondaryServiceImpl.java'],
    detail: `• 순환 의존성 발생 원인  
• 두 빈이 서로를 생성자 주입 형태로 의존할 때, A가 생성되는 도중 B를 주입받으면서 순환 참조가 발생합니다.  
• 해결 방법: 한 쪽 빈에 @Lazy 어노테이션을 적용해 지연 초기화하거나, 생성자 주입 대신 세터 주입을 사용해 순환 참조를 끊었습니다.`,
  },
  {
    id: '118',
    title: '[BE] Missing Tag 해결',
    date: 'Apr 22, 2025',
    status: 'Merged',
    summary: `Spring Data JPA에서 @Entity에 @Table(name=...)가 빠져 발생한 매핑 에러를 수정했습니다.`,
    files: ['User.java', 'Order.java'],
    detail: `• @Table 어노테이션 추가  
• 엔티티 필드 매핑 재검토`,
  },
  {
    id: '117',
    title: '[BE] 스프링 빈 순환 의존성',
    date: 'Apr 22, 2025',
    status: 'Merged',
    summary: `스프링 빈 간 순환 의존성 문제를 @Lazy 어노테이션으로 해결했습니다.`,
    files: ['PaymentService.java', 'NotificationService.java'],
    detail: `• @Lazy 적용  
• 테스트 케이스 추가`,
  },

  {
    id: '116',
    title: '[BE] 순환 참조 에러 해결6',
    date: 'Apr 22, 2025',
    status: 'In Progress',
    summary: `BasicServiceImpl과 SecondaryServiceImpl이 서로 생성자 주입(Constructor Injection)으로 참조하며 순환 의존성 문제가 발생했습니다.
    
    이로 인해 애플리케이션 구동 시 BeanCurrentlyInCreationException이 발생합니다. 순환 의존성을 해소하기 위해 한 쪽 빈에 @Lazy 어노테이션을 적용하거나, 생성자 주입 대신 필드(또는 세터) 주입으로 전환하는 방법을 적용했습니다.`,
    files: ['BasicServiceImpl.java', 'SecondaryServiceImpl.java'],
    detail: `• 순환 의존성 발생 원인  
    • 두 빈이 서로를 생성자 주입 형태로 의존할 때, A가 생성되는 도중 B를 주입받으면서 순환 참조가 발생합니다.  
    • 해결 방법: 한 쪽 빈에 @Lazy 어노테이션을 적용해 지연 초기화하거나, 생성자 주입 대신 세터 주입을 사용해 순환 참조를 끊었습니다.`,
  },
  {
    id: '115',
    title: '[BE] 순환 참조 에러 해결5',
    date: 'Apr 22, 2025',
    status: 'In Progress',
    summary: `BasicServiceImpl과 SecondaryServiceImpl이 서로 생성자 주입(Constructor Injection)으로 참조하며 순환 의존성 문제가 발생했습니다.
    
    이로 인해 애플리케이션 구동 시 BeanCurrentlyInCreationException이 발생합니다. 순환 의존성을 해소하기 위해 한 쪽 빈에 @Lazy 어노테이션을 적용하거나, 생성자 주입 대신 필드(또는 세터) 주입으로 전환하는 방법을 적용했습니다.`,
    files: ['BasicServiceImpl.java', 'SecondaryServiceImpl.java'],
    detail: `• 순환 의존성 발생 원인  
    • 두 빈이 서로를 생성자 주입 형태로 의존할 때, A가 생성되는 도중 B를 주입받으면서 순환 참조가 발생합니다.  
    • 해결 방법: 한 쪽 빈에 @Lazy 어노테이션을 적용해 지연 초기화하거나, 생성자 주입 대신 세터 주입을 사용해 순환 참조를 끊었습니다.`,
  },
  {
    id: '114',
    title: '[BE] 순환 참조 에러 해결4',
    date: 'Apr 22, 2025',
    status: 'In Progress',
    summary: `BasicServiceImpl과 SecondaryServiceImpl이 서로 생성자 주입(Constructor Injection)으로 참조하며 순환 의존성 문제가 발생했습니다.
    
    이로 인해 애플리케이션 구동 시 BeanCurrentlyInCreationException이 발생합니다. 순환 의존성을 해소하기 위해 한 쪽 빈에 @Lazy 어노테이션을 적용하거나, 생성자 주입 대신 필드(또는 세터) 주입으로 전환하는 방법을 적용했습니다.`,
    files: ['BasicServiceImpl.java', 'SecondaryServiceImpl.java'],
    detail: `• 순환 의존성 발생 원인  
    • 두 빈이 서로를 생성자 주입 형태로 의존할 때, A가 생성되는 도중 B를 주입받으면서 순환 참조가 발생합니다.  
    • 해결 방법: 한 쪽 빈에 @Lazy 어노테이션을 적용해 지연 초기화하거나, 생성자 주입 대신 세터 주입을 사용해 순환 참조를 끊었습니다.`,
  },
  {
    id: '113',
    title: '[BE] 순환 참조 에러 해결3',
    date: 'Apr 22, 2025',
    status: 'In Progress',
    summary: `BasicServiceImpl과 SecondaryServiceImpl이 서로 생성자 주입(Constructor Injection)으로 참조하며 순환 의존성 문제가 발생했습니다.
    
    이로 인해 애플리케이션 구동 시 BeanCurrentlyInCreationException이 발생합니다. 순환 의존성을 해소하기 위해 한 쪽 빈에 @Lazy 어노테이션을 적용하거나, 생성자 주입 대신 필드(또는 세터) 주입으로 전환하는 방법을 적용했습니다.`,
    files: ['BasicServiceImpl.java', 'SecondaryServiceImpl.java'],
    detail: `• 순환 의존성 발생 원인  
    • 두 빈이 서로를 생성자 주입 형태로 의존할 때, A가 생성되는 도중 B를 주입받으면서 순환 참조가 발생합니다.  
    • 해결 방법: 한 쪽 빈에 @Lazy 어노테이션을 적용해 지연 초기화하거나, 생성자 주입 대신 세터 주입을 사용해 순환 참조를 끊었습니다.`,
  },
  {
    id: '112',
    title: '[BE] 순환 참조 에러 해결2',
    date: 'Apr 22, 2025',
    status: 'In Progress',
    summary: `BasicServiceImpl과 SecondaryServiceImpl이 서로 생성자 주입(Constructor Injection)으로 참조하며 순환 의존성 문제가 발생했습니다.
    
    이로 인해 애플리케이션 구동 시 BeanCurrentlyInCreationException이 발생합니다. 순환 의존성을 해소하기 위해 한 쪽 빈에 @Lazy 어노테이션을 적용하거나, 생성자 주입 대신 필드(또는 세터) 주입으로 전환하는 방법을 적용했습니다.`,
    files: ['BasicServiceImpl.java', 'SecondaryServiceImpl.java'],
    detail: `• 순환 의존성 발생 원인  
    • 두 빈이 서로를 생성자 주입 형태로 의존할 때, A가 생성되는 도중 B를 주입받으면서 순환 참조가 발생합니다.  
    • 해결 방법: 한 쪽 빈에 @Lazy 어노테이션을 적용해 지연 초기화하거나, 생성자 주입 대신 세터 주입을 사용해 순환 참조를 끊었습니다.`,
  },
  {
    id: '111',
    title: '[BE] 순환 참조 에러 해결1',
    date: 'Apr 22, 2025',
    status: 'In Progress',
    summary: `BasicServiceImpl과 SecondaryServiceImpl이 서로 생성자 주입(Constructor Injection)으로 참조하며 순환 의존성 문제가 발생했습니다.
    
    이로 인해 애플리케이션 구동 시 BeanCurrentlyInCreationException이 발생합니다. 순환 의존성을 해소하기 위해 한 쪽 빈에 @Lazy 어노테이션을 적용하거나, 생성자 주입 대신 필드(또는 세터) 주입으로 전환하는 방법을 적용했습니다.`,
    files: ['BasicServiceImpl.java', 'SecondaryServiceImpl.java'],
    detail: `• 순환 의존성 발생 원인  
    • 두 빈이 서로를 생성자 주입 형태로 의존할 때, A가 생성되는 도중 B를 주입받으면서 순환 참조가 발생합니다.  
    • 해결 방법: 한 쪽 빈에 @Lazy 어노테이션을 적용해 지연 초기화하거나, 생성자 주입 대신 세터 주입을 사용해 순환 참조를 끊었습니다.`,
  },
  {
    id: '110',
    title: '[BE] 순환 참조 에러 해결0',
    date: 'Apr 22, 2025',
    status: 'In Progress',
    summary: `BasicServiceImpl과 SecondaryServiceImpl이 서로 생성자 주입(Constructor Injection)으로 참조하며 순환 의존성 문제가 발생했습니다.
    
    이로 인해 애플리케이션 구동 시 BeanCurrentlyInCreationException이 발생합니다. 순환 의존성을 해소하기 위해 한 쪽 빈에 @Lazy 어노테이션을 적용하거나, 생성자 주입 대신 필드(또는 세터) 주입으로 전환하는 방법을 적용했습니다.`,
    files: ['BasicServiceImpl.java', 'SecondaryServiceImpl.java'],
    detail: `• 순환 의존성 발생 원인  
    • 두 빈이 서로를 생성자 주입 형태로 의존할 때, A가 생성되는 도중 B를 주입받으면서 순환 참조가 발생합니다.  
    • 해결 방법: 한 쪽 빈에 @Lazy 어노테이션을 적용해 지연 초기화하거나, 생성자 주입 대신 세터 주입을 사용해 순환 참조를 끊었습니다.`,
  },
];
