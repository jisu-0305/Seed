from abc import ABC, abstractmethod

class BaseAgent(ABC):
    @abstractmethod
    async def run(self, **kwargs) -> str:
        """
        에이전트 실행을 위한 공통 메서드.
        각각의 에이전트는 이 메서드를 구현해야 함.
        """
        pass
