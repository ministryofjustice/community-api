.PHONY: all ecr-login gradle-build build tag push clean-remote clean-local

aws_region := eu-west-2
image := hmpps/new-tech-api
gradle_builder_image := gradle:jdk8
# gradle build expects just the PATCH value. MAJOR abd MINOR are hardcoded as 0.1
build_version := $(shell echo ${offenderapi_version} | awk -F . '{print $$3}')
build_dir := $(shell pwd)
gradle_build_file := build.gradle

# offenderapi_version should be passed from command line
all:
	$(MAKE) ecr-login
	$(MAKE) gradle-dependencies
	$(MAKE) gradle-test
	$(MAKE) gradle-assemble
	$(MAKE) ecr-login
	$(MAKE) build
	$(MAKE) tag
	$(MAKE) push
	$(MAKE) clean-remote
	$(MAKE) clean-local

gradle-dependencies:
	$(info Running gradle dependencies task for patch version $(build_version) from tag ${offenderapi_version})
	# Build container runs as root - need to fix up perms at end so jenkins can clear up the workspace
	docker run --rm -v $(build_dir):/delius-offender-api -w /delius-offender-api $(gradle_builder_image) bash -c "./gradlew -b $(gradle_build_file) clean dependencies"

gradle-test:
	$(info Running gradle test task for patch version $(build_version) from tag ${offenderapi_version})
	# Build container runs as root - need to fix up perms at end so jenkins can clear up the workspace
	docker run --rm -v $(build_dir):/delius-offender-api -w /delius-offender-api -e JAVA_TOOL_OPTS="JAVA_TOOL_OPTIONS: -Xmx1024m -XX:ConcGCThreads=2 -XX:ParallelGCThreads=2 -Djava.util.concurrent.ForkJoinPool.common.parallelism=2" $(gradle_builder_image) bash -c "./gradlew -b $(gradle_build_file) test"

gradle-assemble:
	$(info Running gradle assemble task for patch version $(build_version) from tag ${offenderapi_version})
	# Build container runs as root - need to fix up perms at end so jenkins can clear up the workspace
	docker run --rm -v $(build_dir):/delius-offender-api -e CI=true -e CIRCLE_BUILD_NUM=${offenderapi_version} -w /delius-offender-api $(gradle_builder_image) bash -c "gradle assemble; chmod -R 0777 build/ .gradle/"

ecr-login:
	$(shell aws ecr get-login --no-include-email --region ${aws_region})
	aws --region $(aws_region) ecr describe-repositories --repository-names "$(image)" | jq -r .repositories[0].repositoryUri > ecr.repo

build: ecr_repo = $(shell cat ./ecr.repo)
build:
	$(info Build of repo $(ecr_repo))
	docker build -t $(ecr_repo) --build-arg OFFENDERAPI_VERSION=${offenderapi_version}  -f docker/Dockerfile.aws .

tag: ecr_repo = $(shell cat ./ecr.repo)
tag:
	$(info Tag repo $(ecr_repo) $(offenderapi_version))
	docker tag $(ecr_repo) $(ecr_repo):$(offenderapi_version)

push: ecr_repo = $(shell cat ./ecr.repo)
push:
	docker tag  ${ecr_repo} ${ecr_repo}:${offenderapi_version}
	docker push ${ecr_repo}:${offenderapi_version}

clean-remote: untagged_images = $(shell aws ecr list-images --region $(aws_region) --repository-name "$(image)" --filter "tagStatus=UNTAGGED" --query 'imageIds[*]' --output json)
clean-remote:
	if [ "${untagged_images}" != "[]" ]; then aws ecr batch-delete-image --region $(aws_region) --repository-name "$(image)" --image-ids '${untagged_images}' || true; fi

clean-local: ecr_repo = $(shell cat ./ecr.repo)
clean-local:
	-docker rmi ${ecr_repo}:latest
	-docker rmi ${ecr_repo}:${offenderapi_version}
	-rm -f ./ecr.repo
	-rm -f build/libs/delius-offender-api-*.jar