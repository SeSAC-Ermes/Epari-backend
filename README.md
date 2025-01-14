    
# Epari - 온라인 교육 플랫폼
<br />

<div align="center">
  <img src="https://github.com/user-attachments/assets/b534bd02-aa78-4ded-b31b-6c1499a0c718" width="100000">
</div>

## 프로젝트 개요
Epari는 효율적인 온라인 교육 관리를 위한 통합 교육 플랫폼 서비스입니다. 강사와 학생 간의 원활한 상호작용을 지원하며, 교육 과정의 전반적인 관리를 제공합니다.
<br>
### 프로젝트 기간
2024년 10월 28일 ~ 2025년 1월 6일 (9주)
<br>
### 프로젝트 목적 및 문제 해결점
Epari는 분산된 교육 플랫폼들을 하나로 통합하여 학습자와 교육자의 불편함을 해소하는 학습 관리 시스템(LMS)입니다. 
다중 플랫폼 사용으로 인한 학습자의 피로도, 교육 자료의 비체계적 관리, 학습 이력 추적의 어려움 등을 해결하고자 개발되었습니다. 
통합 관리 시스템을 통해 교육생별 성취도와 학습 진도를 효율적으로 관리하며, 데이터 기반의 교육 품질 향상을 목표로 합니다.
<br>
### 주요 기능
- **과제 및 평가 :** 과제 출제, 제출 및 평가 시스템
- **시험 관리 :** 온라인 시험 출제, 응시, 자동 채점
- **출석 관리 :** 강의별 출석 현황 관리 및 통계 제공
- **학습 통계 :** 학생별 출석 및 과제/시험 성적 통계 제공
- **사용자 관리 :** 강사 및 학생 계정 관리, 사용자 권한 설정 및 관리
- **운영 통계 :** 사이트 접속 및 강의, 사용자 통계 대시보드
- **파일 관리 :** AWS S3 기반 강의 파일 업로드 및 다운로드 지원
- **커뮤니티 :** 서버리스 기반 게시글 작성, 조회, 댓글 작성 및 좋아요 기능, 태그 및 제목 검색 기능, 게시글 순위 및 태그 기반 인기 글 제공, AI 기반 자동 글감 수집 및 게시글 업로드 자동화
- **AI 챗봇** : 플랫폼 데이터를 활용한 게시글 검색, 요약 및 통계 제공, 학습 자료 추천 및 사용자 문의 응답
<br> 

## 협업 프로세스
### 애자일 스크럼 방법론 일부 차용
    
![image](https://github.com/user-attachments/assets/f7e0ed39-6f1e-4e7d-b224-615940cd7bbc)

- **짧은 스프린트:** 2~3일 주기로 스프린트 진행
- **일일 스크럼:** 매일 오전 10시에 진행 상황, 이슈 공유 및 조율
- **GitHub Project 활용**
    - **제품 백로그:** 개발해야 할 기능들을 GitHub Project에 등록하여 관리
    - **스프린트 백로그:** 각 스프린트에서 개발할 기능들을 제품 백로그에서 스프린트 백로그로 이동
    - **이슈 트래킹:** 스프린트 백로그의 각 기능은 GitHub Issue로 발행하여 개발 진행 상황 추적
- **CI/CD 파이프라인:** Jenkins를 활용하여 지속적인 통합 및 배포 자동화
    - **자동 빌드 및 테스트:** 코드 변경 시 자동으로 빌드 및 테스트 수행
    - **자동 배포:** 테스트 통과 시, 자동으로 배포 환경에 반영
- **스프린트 리뷰 및 회고**
    - **스프린트 리뷰:** 각 스프린트 종료 시, 배포 환경에서 동작하는 산출물을 기반으로 데모 및 피드백 공유
    - **스프린트 회고:** 스프린트 리뷰 후, 팀원들과 함께 스크럼 수행 과정을 되돌아보고 개선점을 도출하여 다음 스프린트에 반영

### 협업 도구

- **GitHub:** 코드 버전 관리, 이슈 트래킹, 프로젝트 관리
- **Slack:** CI/CD 파이프라인 실행 결과 및 PR Request 알림
- **Notion:** 팀 커뮤니케이션 및 일정 관리

### 협업 특징 및 강점

- 2개월이라는 짧은 기간 동안 애자일 스크럼 방법론을 효과적으로 적용하여, 빠른 개발 주기와 지속적인 피드백 반영을 통해 사용자 요구에 민첩하게 대응하였습니다.
- GitHub Project와 Jenkins를 활용한 체계적인 협업 프로세스를 구축하여, 투명한 개발 진행 상황 공유와 효율적인 협업을 가능하게 했습니다.
- CI/CD 파이프라인을 통해 자동화된 빌드, 테스트, 배포 프로세스를 구축하여, 개발 생산성을 높이고 안정적인 서비스를 제공했습니다.
- 정기적인 스프린트 리뷰와 회고를 통해, 지속적으로 협업 프로세스를 개선하고 팀의 역량을 강화했습니다.

<br>

## 기술 스택

**Backend**
<br>
![Java](https://img.shields.io/badge/Java-ED8B00?style=flat-square&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=flat-square&logo=spring-boot&logoColor=white)
![Spring Security](https://img.shields.io/badge/Spring_Security-6DB33F?style=flat-square&logo=spring-security&logoColor=white)
![JPA](https://img.shields.io/badge/JPA-6DB33F?style=flat-square&logo=spring&logoColor=white)
![QueryDSL](https://img.shields.io/badge/QueryDSL-4479A1?style=flat-square&logo=hibernate&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-4479A1?style=flat-square&logo=mysql&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-DC382D?style=flat-square&logo=redis&logoColor=white)
![WebSocket](https://img.shields.io/badge/WebSocket-010101?style=flat-square&logo=socket.io&logoColor=white)
![Gradle](https://img.shields.io/badge/Gradle-02303A?style=flat-square&logo=gradle&logoColor=white)
![Serverless](https://img.shields.io/badge/Serverless-FD5750?style=flat-square&logo=serverless&logoColor=white)


**Frontend**
<br>
![React](https://img.shields.io/badge/React-20232A?style=flat-square&logo=react&logoColor=61DAFB)
![TailwindCSS](https://img.shields.io/badge/Tailwind_CSS-38B2AC?style=flat-square&logo=tailwind-css&logoColor=white)
![AG Grid](https://img.shields.io/badge/AG_Grid-027FFB?style=flat-square&logo=aggrid&logoColor=white)
![Apache ECharts](https://img.shields.io/badge/Apache_ECharts-AA344D?style=flat-square&logo=apache&logoColor=white)
![Axios](https://img.shields.io/badge/Axios-5A29E4?style=flat-square&logo=axios&logoColor=white)
![SockJS](https://img.shields.io/badge/SockJS-010101?style=flat-square&logo=socket.io&logoColor=white)
![Vite](https://img.shields.io/badge/Vite-646CFF?style=flat-square&logo=vite&logoColor=white)


**AWS Services**
<br>
![Lambda](https://img.shields.io/badge/AWS_Lambda-FF9900?style=flat-square&logo=aws-lambda&logoColor=white)
![DynamoDB](https://img.shields.io/badge/Amazon%20DynamoDB-4053D6?style=flat-square&logo=Amazon%20DynamoDB&logoColor=white)
![API Gateway](https://img.shields.io/badge/API_Gateway-FF4F8B?style=flat-square&logo=amazon-aws&logoColor=white)
![CloudWatch](https://img.shields.io/badge/CloudWatch-FF4F8B?style=flat-square&logo=amazon-cloudwatch&logoColor=white)
![AWS Bedrock](https://img.shields.io/badge/AWS_Bedrock-232F3E?style=flat-square&logo=amazon-aws&logoColor=white)
![AWS Lex](https://img.shields.io/badge/AWS_Lex-232F3E?style=flat-square&logo=amazon-aws&logoColor=white)
![Amazon ElastiCache](https://img.shields.io/badge/Amazon_ElastiCache-232F3E?style=flat-square&logo=amazon-aws&logoColor=white)
![Amazon SES](https://img.shields.io/badge/Amazon_SES-DD344C?style=flat-square&logo=amazon-aws&logoColor=white)
![Amazon Cognito](https://img.shields.io/badge/Amazon_Cognito-FF9900?style=flat-square&logo=amazon-aws&logoColor=white)
![Amazon EventBridge](https://img.shields.io/badge/EventBridge-FF4F8B?style=flat-square&logo=amazon-aws&logoColor=white)
![AWS IAM](https://img.shields.io/badge/IAM-DD344C?style=flat-square&logo=amazon-aws&logoColor=white)
![AWS CloudFormation](https://img.shields.io/badge/CloudFormation-FF9900?style=flat-square&logo=amazon-aws&logoColor=white)

**CI/CD**
<br>
![GitHub](https://img.shields.io/badge/GitHub-181717?style=flat-square&logo=github&logoColor=white)
![Jenkins](https://img.shields.io/badge/Jenkins-D24939?style=flat-square&logo=Jenkins&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-2496ED?style=flat-square&logo=docker&logoColor=white)
![Docker Compose](https://img.shields.io/badge/Docker_Compose-2496ED?style=flat-square&logo=docker&logoColor=white)

**Infra**
<br>
![AWS](https://img.shields.io/badge/AWS-232F3E?style=flat-square&logo=amazon-aws&logoColor=white)
![VPC](https://img.shields.io/badge/VPC-FF9900?style=flat-square&logo=amazon-aws&logoColor=white)
![Route 53](https://img.shields.io/badge/Route_53-5C4EE5?style=flat-square&logo=amazon-aws&logoColor=white)
![CloudFront](https://img.shields.io/badge/CloudFront-FF9900?style=flat-square&logo=amazon-aws&logoColor=white)
![S3](https://img.shields.io/badge/S3-569A31?style=flat-square&logo=amazon-s3&logoColor=white)
![ELB](https://img.shields.io/badge/ELB-FF9900?style=flat-square&logo=amazon-aws&logoColor=white)
![Auto Scaling](https://img.shields.io/badge/Auto_Scaling-FF9900?style=flat-square&logo=amazon-aws&logoColor=white)
![Amazon ECS](https://img.shields.io/badge/ECS_(Fargate)-FF9900?style=flat-square&logo=amazon-ecs&logoColor=white)
![Amazon ECR](https://img.shields.io/badge/ECR-232F3E?style=flat-square&logo=amazon-ecr&logoColor=white)
![Amazon RDS](https://img.shields.io/badge/RDS-527FFF?style=flat-square&logo=amazon-rds&logoColor=white)

**Tools**
<br>
![Slack](https://img.shields.io/badge/Slack-4A154B?style=flat-square&logo=slack&logoColor=white)
![Notion](https://img.shields.io/badge/Notion-000000?style=flat-square&logo=notion&logoColor=white)
![n8n](https://img.shields.io/badge/n8n-34495E?style=flat-square&logo=n8n&logoColor=white)

<br>

## 인프라 아키텍처

### 특징

<br>

## ERD

<details>
<summary><strong>ERD 보기</strong></summary>

![image](https://github.com/user-attachments/assets/088d43c7-2d15-4d33-8a9f-4e065b024876)<br />
### 특징
사용자(BaseUser)는 강사와 학생으로 구분되며, 강의(Course)를 중심으로 커리큘럼, 강의자료, 과제, 시험, 출석 등의 교육 관리 기능이 연결됩니다. 공지사항과 Q&A 게시판을 통해 구성원 간 소통을 지원하며, 각 도메인별 독립적인 파일 관리 시스템을 통해 효율적인 파일 관리가 가능합니다.
</details>



<br>

## 프로젝트 핵심 기술

<details>
<summary> <strong>인증/인가 (RBAC)</strong> </summary>  <br />
본 프로젝트는 <strong>Amazon Cognito</strong>와 <strong>Spring Security</strong>를 결합하여 안전하고 효율적인 <strong>역할 기반 접근 제어 (RBAC) 시스템</strong>을 구현했습니다. 사용자 인증 및 권한 관리는 <strong>Amazon Cognito User Pool 및 Group</strong>을 통해 처리하고, 백엔드 애플리케이션은 <strong>Spring Security</strong>를 사용하여 <strong>OAuth 2.0 Resource Server</strong>를 구축했습니다.

  &nbsp;

**주요 구현 고려사항**    
- Cognito Group을 활용하여 `관리자`, `강사`, `수강생` 역할을 사용자에게 할당했습니다.
- **URL 기반 접근 제어**와 **메서드 기반 보안 (`@PreAuthorize`)** 을 결합하여, 각 역할에 따른 정교한 권한 제어를 구현했습니다. 이를 통해 역할 별로 접근 가능한 API 엔드포인트와 기능을 세밀하게 제어합니다.

<br>

**주요 이점**

- **Cognito**를 통해 안전하고 확장 가능한 인증/인가 프로세스를 구축하여 민감한 데이터를 보호합니다.
- **RBAC**를 통해 사용자 역할에 따라 세분화된 권한 제어가 가능하며, 시스템 요구사항 변화에 따라 유연하게 권한을 조정할 수 있습니다.
- **Spring Security**를 활용하여 백엔드 리소스에 대한 보안을 강화하고, 개발 효율성을 향상시켰습니다.
</details>
<details>
<summary> <strong>AI 챗봇 (RAG 기반)</strong> </summary><br />

<div align="center">
  <img src="https://github.com/user-attachments/assets/a4e18724-7084-4b0c-8947-09f5b7029f5b" width="400">
</div>
<br />
    
본 프로젝트는 사용자와 자연어로 소통하며 **커뮤니티 게시글에 대한 질문에 답변을 제공**하는 **RAG(Retrieval-Augmented Generation) 기반 AI 챗봇**을 구현했습니다.  **Amazon Lex**와 **Amazon Bedrock Knowledge Bases**를 활용하여, 사용자의 질문 의도를 정확하게 파악하고, 관련 게시글 정보를 검색하여, 문맥에 맞는 답변을 생성합니다.

&nbsp;
    
**주요 구현 고려사항**
- **RAG (Retrieval-Augmented Generation) 모델 기반 챗봇**
    - 단순 키워드 매칭을 넘어, **검색(Retrieval)과 생성(Generation)을 결합**한 RAG 모델을 통해, 보다 정확하고 자연스러운 답변을 제공합니다.
    - 사용자의 질문과 관련된 게시글을 검색하여, 이를 바탕으로 답변을 생성함으로써, **문맥에 맞는 정보를 제공**합니다.
- **Amazon Lex를 활용한 자연어 처리**
    - **Amazon Lex**를 통해 사용자의 자연어 입력을 분석하고, **질문의 의도(Intent)와 슬롯(Slot)**을 파악합니다.
    - 대화 흐름을 정의하여, 사용자와의 **자연스러운 대화형 인터페이스**를 제공합니다.
- **Amazon Bedrock Knowledge Bases를 활용한 지식 기반 구축**
    - **Amazon Bedrock Knowledge Bases**에 커뮤니티 게시글 데이터를 저장하여, 챗봇이 답변 생성에 활용할 수 있는 **지식 기반을 구축**했습니다.
    - Knowledge Bases는 게시글 데이터를 **벡터 임베딩으로 변환**하여 저장하고, **의미 기반 검색**을 통해 관련 정보를 빠르게 찾을 수 있도록 지원합니다.

**주요 이점**
- **정확하고 자연스러운 답변:** RAG 모델을 통해, 단순 챗봇을 넘어서 사용자의 질문에 정확하고 자연스러운 답변을 제공합니다.
- **커뮤니티 활성화:** 사용자가 게시글에 대한 궁금증을 빠르게 해결하고, 더 쉽게 정보에 접근할 수 있도록 돕습니다. 이를 통해 커뮤니티 참여와 활동을 촉진합니다.
</details>

<details>
<summary> <strong>서버리스 아키텍처</strong> </summary><br />
    본 프로젝트는 <strong>서버리스 아키텍처</strong>를 적극 활용하여 인프라 관리 부담을 줄이고 확장성과 비용 효율성을 높였습니다. 특히, <strong>Amazon ECS Fargate</strong>, <strong>Amazon Cognito</strong>, <strong>서버리스 프레임워크</strong>를 활용한 커뮤니티 기능 구현을 통해 서버리스의 이점을 극대화했습니다.

&nbsp;
    
**주요 구현 고려사항**
- **Amazon ECS Fargate를 활용한 컨테이너 오케스트레이션**
    - 애플리케이션의 핵심 기능을 **Amazon ECS Fargate**를 사용하여 컨테이너 기반으로 배포했습니다.
    - Fargate를 통해 서버 관리에 대한 부담 없이 **자동 확장(Auto-scaling)** 및 **고가용성**을 확보했습니다.
- **Amazon Cognito를 통한 서버리스 인증/인가**
    - **Amazon Cognito**를 사용하여 사용자 인증 및 권한 관리 시스템을 서버리스로 구축했습니다.
    - Cognito를 통해 사용자 풀 관리, 인증, 권한 부여 등의 기능을 **완전 관리형 서비스**로 활용하여, 보안 및 운영 효율성을 높였습니다.
- **서버리스 프레임워크를 활용한 커뮤니티 기능 구현**
    - 커뮤니티 기능의 백엔드 로직(게시글 작성, 댓글 등록 등)을 **Serverless Framework**(Node.js)를 사용하여 구현했습니다.
    - **AWS Lambda**에 함수 형태로 배포하여 이벤트 기반으로 코드를 실행하고 사용된 리소스에 대해서만 비용을 지불하는 **비용 최적화**를 달성했습니다.
    - **Serverless Framework**를 통해 개발자는 인프라 관리에 대한 부담 없이 비즈니스 로직 구현에 집중할 수 있었습니다.
    
**주요 이점**
- **운영 효율성 향상:** 서버리스 아키텍처를 적용하여 서버 관리 및 운영에 드는 시간과 노력을 최소화했습니다.
- **자동 확장성:** Fargate와 Lambda를 활용하여, 트래픽 증가에 따라 자동으로 리소스를 확장하여 안정적인 서비스를 제공합니다.
- **비용 최적화:** 사용한 리소스에 대해서만 비용을 지불하는 서버리스 모델을 통해, 비용 효율성을 극대화했습니다.
- **개발 생산성 향상:** 서버리스 프레임워크를 활용하여, 개발자는 인프라 설정에 대한 부담 없이 빠르게 개발하고 배포할 수 있습니다.
</details>
<br />

## 트러블 슈팅

### 1. Cognito 사용자 그룹 할당 개선
    
**문제 상황**

- Google 계정 최초 로그인 시 Cognito 사용자 등록과 그룹 할당 과정에서 비동기 처리로 인한 그룹 할당 누락
- API 호출 방식으로 인한 긴 처리 시간(1.22초)과 네트워크 오버헤드 발생

**해결 방안**

- Cognito Post Confirmation Lambda Trigger 도입
- 사용자 생성 직후 자동으로 그룹 할당하는 방식으로 변경

**개선 효과**

- 처리 시간 65.4% 단축 (1.22초 → 421.58ms)
- 사용자 생성과 그룹 할당의 일관성 보장
- API 호출 제거로 프로세스 단순화
  
2. 시험 웹 소켓(추후에 고려)
3. Jenkins, S3 CORS, …

## 위키 : 서비스 아키텍쳐

![image.png](https://prod-files-secure.s3.us-west-2.amazonaws.com/4d06cb3b-8521-46a7-92e9-c7f551a248e0/fdbcfc64-8420-4bed-b285-c7039f30fdab/image.png)

- 학습 관리 기능
    
    ![image.png](https://prod-files-secure.s3.us-west-2.amazonaws.com/4d06cb3b-8521-46a7-92e9-c7f551a248e0/9bc7693e-2c6c-4ed6-ace2-7d4d77cbd0c8/image.png)
    
    ```markdown
    # 학습 관리 서비스 아키텍처
    
    ## 개요
    AWS 클라우드 기반의 학습 관리 서비스로, 사용자에게 효율적인 학습 콘텐츠 전달과 관리를 제공합니다.
    
    ## 주요 컴포넌트
    - **Amazon CloudFront**: 웹 콘텐츠 전송
    - **Amazon ECS**: 애플리케이션 서버 운영
    - **Amazon RDS**: 데이터베이스 관리
    - **Amazon S3**: 
      - Web UI assets 저장
      - 문서 저장소로 활용
    
    ## 데이터 흐름
    1. 사용자 요청 → CloudFront를 통한 콘텐츠 전송
    2. CloudFront → ECS로 동적 요청 처리
    3. ECS ↔ RDS 데이터 처리
    4. ECS ↔ S3 문서 저장/조회
    
    ## 특징
    - CloudFront를 통한 글로벌 콘텐츠 전송
    - 컨테이너 기반 확장 가능한 아키텍처
    - 이중화된 데이터 저장소 (RDS + S3)
    ```
    
- 챗봇
    
    ![image.png](https://prod-files-secure.s3.us-west-2.amazonaws.com/4d06cb3b-8521-46a7-92e9-c7f551a248e0/d6f7fd27-97fe-4cfa-8cae-6cb19efcaf01/image.png)
    
    ```markdown
    # 챗봇 서비스 아키텍처
    
    ## 개요
    AWS 서버리스 기반의 AI 챗봇 서비스로, 사용자 질문에 대한 지능형 응답을 제공합니다.
    
    ## 주요 컴포넌트
    - **Chat UI**: 사용자 인터페이스
    - **Amazon Lex**: 챗봇 엔진
    - **AWS Lambda**: 비즈니스 로직 처리
    - **Knowledge Bases**: AI 기반 지식 처리
    - **Amazon S3**: 문서 저장소
    
    ## 데이터 흐름
    1. 사용자 입력 → Lex 자연어 처리
    2. Lex → Lambda 로직 처리
    3. Lambda → Knowledge Bases 정보 검색
    4. 결과값 사용자에게 전달
    
    ## 특징
    - 서버리스 아키텍처
    - AI/ML 기반 응답
    - 글로벌 콘텐츠 전송
    - 확장 가능한 구조
    ```
    
- 인증/인가
    
    ![image.png](https://prod-files-secure.s3.us-west-2.amazonaws.com/4d06cb3b-8521-46a7-92e9-c7f551a248e0/7e00e42f-443a-400b-8ef0-bb4c4e08eac4/image.png)
    
    ```markdown
    # 인증/인가 서비스 아키텍처
    
    ## 개요
    AWS Cognito 기반의 통합 인증 서비스로, 소셜 로그인과 사용자 권한 관리를 제공합니다.
    
    ## 주요 컴포넌트
    - **AWS Cognito**: 사용자 인증 관리
    - **Google Identity Provider**: 소셜 로그인
    - **AWS Lambda**: 사용자 그룹 할당 및 JWT 토큰 관리
    - **Cognito Groups**: 사용자 그룹 관리
    
    ## 데이터 흐름
    1. 사용자 → Cognito/Google 인증
    2. Cognito → Lambda 사용자 정보 전달
    3. Lambda → Cognito Groups 사용자 할당
    4. JWT 토큰 발급 및 커스터마이징
    
    ## 특징
    - 소셜 로그인 지원
    - 서버리스 아키텍처
    - 유연한 사용자 그룹 관리
    - JWT 기반 인증
    ```
    
- 대시 보드
    
    ![image.png](https://prod-files-secure.s3.us-west-2.amazonaws.com/4d06cb3b-8521-46a7-92e9-c7f551a248e0/7c0b4ff1-696d-43e5-b90e-c3695d6cb5d2/image.png)
    
    ```markdown
    # 관리자 대시보드 서비스 아키텍처
    
    ## 개요
    AWS 서버리스 기반의 관리자 대시보드로, 학습 플랫폼의 주요 지표들을 모니터링합니다.
    
    ## 주요 컴포넌트
    - **관리자 대시보드 UI**: 사용자 인터페이스
    - **Amazon API Gateway**: REST API 엔드포인트 관리
    - **AWS Lambda**: 6개의 주요 기능별 데이터 처리
    - **DynamoDB**: 데이터 저장소
    
    ## API 엔드포인트
    - 강의 선호도 분석
    - 수강 현황 모니터링
    - 방문자 통계
    - 페이지 랭킹
    - 취업/이직률 분석
    - 수강생 현황 조회
    
    ## 특징
    - 서버리스 아키텍처
    - REST API 기반 통신
    - 실시간 데이터 처리
    - 확장 가능한 구조
    ```
    
- n8n 아키텍쳐
    
    ![image.png](https://prod-files-secure.s3.us-west-2.amazonaws.com/4d06cb3b-8521-46a7-92e9-c7f551a248e0/64c465ce-beef-4c84-9aeb-818ab64347e1/image.png)
    
    ```markdown
    # n8n 자동화 서비스 아키텍처
    
    ## 개요
    n8n 워크플로우 기반의 IT 운영 자동화 시스템으로, 데이터 수집부터 분석, 알림까지 자동화합니다.
    
    ## 주요 컴포넌트
    - **n8n**: 워크플로우 자동화 엔진
    - **AWS 서비스**: API Gateway, Lambda, S3, DynamoDB
    - **외부 서비스**: Perplexity AI, Gmail, Slack
    
    ## 데이터 흐름
    1. 일일 스케줄 트리거 → n8n 워크플로우 실행
    2. AWS 서비스를 통한 데이터 수집/저장
    3. Perplexity AI를 통한 데이터 분석
    4. Gmail/Slack을 통한 결과 알림
    
    ## 특징
    - 정기적 자동 실행
    - 다중 서비스 통합
    - AI 기반 분석
    - 멀티 채널 알림
    ```
    
- 커뮤니티
    
    ![image.png](https://prod-files-secure.s3.us-west-2.amazonaws.com/4d06cb3b-8521-46a7-92e9-c7f551a248e0/9a21c227-ae3b-49ba-8e14-978454874889/image.png)
    
    ```markdown
    # 커뮤니티 서비스 아키텍처
    
    ## 개요
    RESTful API 기반의 커뮤니티 서비스로, 게시글, 댓글, 태그 기능을 제공합니다.
    
    ## 주요 컴포넌트
    - **API Gateway**: RESTful API 엔드포인트 관리
    - **Lambda**: 각 기능별 비즈니스 로직 처리
    - **DynamoDB**: 데이터 저장소
    - **S3**: 미디어 파일 저장소
    
    ## API 구조
    1. Posts API: 게시글 CRUD, 좋아요, 검색, 트렌딩
    2. Comments API: 댓글 CRUD
    3. Tags API: 인기 태그 조회
    
    ## 특징
    - RESTful API 설계
    - 서버리스 아키텍처
    - 확장 가능한 데이터 구조
    - 미디어 처리 지원
    ```


## 팀원 소개 
| 이름 | 역할 | 기여 내용 | GitHub |
| --- | --- | --- | --- |
| 오찬근 | **팀 리더** <br> 풀스택 <br> 인프라 | • 인프라 구축 <br> • 관리자 기능 구현 <br> • 출결, 커리큘럼 관리 기능 구현 <br> • 자동화 워크플로우 구현 <br> • AI 챗봇 구현 | https://github.com/Chan-GN |
    | 박종호 | 풀스택 | • 엔티티 설계
    • 강의 기능 구현
    • 시험 화면 구현
    • 커뮤니티 기능 구현
    • 관리자 대시보드 구현 | https://github.com/cuteJJong |
    | 임진희 | 풀스택
    인증/인가 | • 사용자 인증/인가
    • 마이페이지 구현 | https://github.com/liimjiin |
    | 임혜린 | 풀스택
    인프라 | • 인프라 구축
    • 시험 기능 구현
    • 커뮤니티 기능 구현
    • 관리자 대시보드 구현
    • AI 챗봇 구현 | https://github.com/hyerin315 |
    | 오승찬 | 풀스택 | • 과제 기능 구현 | https://github.com/sseung519 |
    | 이한나 | 풀스택 | • 공지사항 구현 | https://github.com/12ka39 |
