pipeline {
	agent any

    environment {
		// KT Cloud Container Registry 설정
        KT_REGISTRY = 'registry.cloud.kt.com'
        KT_PROJECT = '3stxgt9j'
        DOCKER_IMAGE_NAME = 'live-backend'
        DOCKER_IMAGE_TAG = "${env.BUILD_NUMBER}-${env.GIT_COMMIT?.take(7) ?: 'latest'}"
        DOCKER_IMAGE = "${KT_REGISTRY}/${KT_PROJECT}/${DOCKER_IMAGE_NAME}:${DOCKER_IMAGE_TAG}"

        // Kubernetes 설정
        K8S_NAMESPACE = 'live-backend'
        K8S_DEPLOYMENT_NAME = 'live-backend'

        // Gradle 설정
        GRADLE_OPTS = '-Dorg.gradle.daemon=false'
    }

    stages {
		stage('🔍 Checkout') {
			steps {
				script {
					echo "📦 소스코드 체크아웃 중..."
                    checkout scm

                    withCredentials([usernamePassword(
                        credentialsId: 'github-submodule-access-token',
                        usernameVariable: 'GITHUB_USER',
                        passwordVariable: 'GITHUB_TOKEN'
                    )]) {
						sh '''
                            set +x
                            cp .gitmodules .gitmodules.bak
                            sed -i "s|https://github.com/|https://${GITHUB_USER}:${GITHUB_TOKEN}@github.com/|g" .gitmodules
                            git submodule sync
                            git submodule update --init --recursive
                            cd live-config
                            git checkout Master
                            git pull
                            cd ..
                            mv .gitmodules.bak .gitmodules
                            set -x
                            echo "✅ 서브모듈 업데이트 완료!"
                        '''
                    }
                }
            }
        }

        stage('📁 Config Setup') {
			steps {
				script {
					echo "📁 설정 파일 복사 중..."
                    sh '''
                        mkdir -p ./src/main/resources
                        cp live-config/application-dev.yml ./src/main/resources/
                        cp live-config/application-test.yml ./src/main/resources/
                        cp live-config/common.yml ./src/main/resources/
                        mkdir -p ./src/test/resources
                        cp live-config/application-test.yml ./src/test/resources/
                        cp live-config/common.yml ./src/test/resources/
                    '''
                }
            }
        }

        stage('📦 Build JAR') {
			steps {
				script {
					echo "📦 JAR 파일 빌드 중..."
                    sh '''
                        JAVA21_HOME="/var/jenkins_home/tools/jdk-21"

                        if [ ! -d "$JAVA21_HOME" ]; then
                            echo "☕ Java 21 다운로드 중..."
                            mkdir -p /var/jenkins_home/tools
                            (
                                cd /var/jenkins_home/tools
                                curl -L -o jdk-21.tar.gz \
                                    "https://github.com/adoptium/temurin21-binaries/releases/download/jdk-21.0.5%2B11/OpenJDK21U-jdk_x64_linux_hotspot_21.0.5_11.tar.gz"
                                tar -xzf jdk-21.tar.gz
                                mv jdk-21.0.5+11 jdk-21
                                rm jdk-21.tar.gz
                            )
                            echo "✅ Java 21 설치 완료!"
                        fi

                        export JAVA_HOME="$JAVA21_HOME"
                        export PATH="$JAVA_HOME/bin:$PATH"

                        echo "사용 중인 Java 버전:"
                        java -version

                        export GRADLE_OPTS="-Xmx2g -XX:MaxMetaspaceSize=512m -XX:+UseG1GC -XX:MaxGCPauseMillis=200"

                        ./gradlew clean --no-daemon --console=plain

                        ./gradlew bootJar -x test -Pprofile=dev --no-daemon --console=plain \
    						--no-parallel \
    						--max-workers=2

                        echo "✅ 빌드 완료!"
                        ls -lh build/libs/
                    '''
                }
            }
        }

        stage('🐳 Build & Push Docker Image') {
			steps {
				script {
					echo "🐳 이미지 빌드 및 푸시 중..."

                    withCredentials([usernamePassword(
                        credentialsId: 'kt-registry-credentials',
                        usernameVariable: 'KT_USERNAME',
                        passwordVariable: 'KT_PASSWORD'
                    )]) {
						sh """
                            echo "🔐 Docker 레지스트리 로그인..."
                            echo \${KT_PASSWORD} | docker login ${KT_REGISTRY} -u \${KT_USERNAME} --password-stdin

                            echo "🔨 Docker 이미지 빌드 (BuildKit + 캐시 활성화)..."

                            # BuildKit 활성화
                            export DOCKER_BUILDKIT=1

                            # 이전 이미지를 캐시로 사용
                            docker pull ${KT_REGISTRY}/${KT_PROJECT}/${DOCKER_IMAGE_NAME}:latest || true

                            # 빌드 (캐시 활용)
                            docker build \\
                                --cache-from ${KT_REGISTRY}/${KT_PROJECT}/${DOCKER_IMAGE_NAME}:latest \\
                                --build-arg BUILDKIT_INLINE_CACHE=1 \\
                                -t ${DOCKER_IMAGE} \\
                                -t ${KT_REGISTRY}/${KT_PROJECT}/${DOCKER_IMAGE_NAME}:latest \\
                                .

                            echo "📤 Docker 이미지 푸시..."
                            docker push ${DOCKER_IMAGE}
                            docker push ${KT_REGISTRY}/${KT_PROJECT}/${DOCKER_IMAGE_NAME}:latest

                            echo "🧹 로컬 이미지 정리..."
                            docker rmi ${DOCKER_IMAGE} || true

                            echo "🔒 Docker 로그아웃..."
                            docker logout ${KT_REGISTRY}

                            echo "✅ 이미지 푸시 완료!"
                        """
                    }
                }
            }
        }

        stage('🚀 Deploy to Kubernetes') {
			steps {
				script {
					echo "🚀 KT Cloud Kubernetes에 배포 중..."

                    sh """
                        echo "📦 네임스페이스 생성..."
                        kubectl create namespace ${K8S_NAMESPACE} --dry-run=client -o yaml | kubectl apply -f -

                        echo "⚙️  ConfigMap & Secret 적용..."
                        kubectl apply -f k8s/configmap.yaml -n ${K8S_NAMESPACE} || echo "⚠️  ConfigMap 없음"
                        kubectl apply -f k8s/secret.yaml -n ${K8S_NAMESPACE} || echo "⚠️  Secret 없음"
                    """

                    withCredentials([usernamePassword(
                        credentialsId: 'kt-registry-credentials',
                        usernameVariable: 'KT_USERNAME',
                        passwordVariable: 'KT_PASSWORD'
                    )]) {
						sh """
                            echo "🔐 imagePullSecret 생성..."
                            kubectl create secret docker-registry kt-registry-secret \\
                                --docker-server=${KT_REGISTRY} \\
                                --docker-username=\${KT_USERNAME} \\
                                --docker-password=\${KT_PASSWORD} \\
                                --namespace=${K8S_NAMESPACE} \\
                                --dry-run=client -o yaml | kubectl apply -f -
                        """
                    }

                    sh """
                        echo "📦 네임스페이스 생성..."
                        kubectl apply -f k8s/namespace.yaml -n ${K8S_NAMESPACE}

                        echo "⚙️ ConfigMap 적용..."
                        kubectl apply -f k8s/configmap.yaml -n ${K8S_NAMESPACE}

                        echo "🚀 Deployment 적용..."
                        kubectl apply -f k8s/deployment.yaml -n ${K8S_NAMESPACE}

                        echo "🌐 Service 적용..."
                        kubectl apply -f k8s/service.yaml -n ${K8S_NAMESPACE}

                        echo "🔗 Ingress 적용..."
                        kubectl apply -f k8s/ingress.yaml -n ${K8S_NAMESPACE}

                        echo "🔄 이미지 업데이트..."
                        kubectl set image deployment/${K8S_DEPLOYMENT_NAME} \\
                            ${K8S_DEPLOYMENT_NAME}=${DOCKER_IMAGE} \\
                            -n ${K8S_NAMESPACE}

                        echo "⏳ 배포 롤아웃 상태 확인..."
                        kubectl rollout status deployment/${K8S_DEPLOYMENT_NAME} -n ${K8S_NAMESPACE} --timeout=5m

                        echo "✅ 배포 완료!"
                    """
                }
            }
        }

        stage('✅ Health Check') {
			steps {
				script {
					echo "✅ 헬스 체크 중..."

                    sh """
                        echo "📊 Pod 상태:"
                        kubectl get pods -n ${K8S_NAMESPACE} -l app=${K8S_DEPLOYMENT_NAME}

                        echo ""
                        echo "🌐 Service 상태:"
                        kubectl get svc -n ${K8S_NAMESPACE}

                        echo ""
                        echo "📋 최근 이벤트:"
                        kubectl get events -n ${K8S_NAMESPACE} --sort-by='.lastTimestamp' | tail -10
                    """
                }
            }
        }
    }

    post {
		success {
			script {
				echo """
                ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
                ✅ 배포 성공!
                ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
                📦 이미지: ${DOCKER_IMAGE}
                🏷️  태그: ${DOCKER_IMAGE_TAG}
                🎯 네임스페이스: ${K8S_NAMESPACE}
                ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
                """
            }
        }
        failure {
			script {
				echo """
                ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
                ❌ 배포 실패!
                ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
                빌드 번호: ${env.BUILD_NUMBER}
                브랜치: ${env.GIT_BRANCH}
                커밋: ${env.GIT_COMMIT}
                ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
                """
            }
        }

        always {
			script {
				sh """
                    # Docker logout (보안)
                    docker logout ${KT_REGISTRY} || true
                """
            }
        }
    }
}