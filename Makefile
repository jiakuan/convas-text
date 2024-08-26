LATEST_TAG?=`git tag|sort -t. -k 1,1n -k 2,2n -k 3,3n -k 4,4n | tail -1`
PROJECT_DIR := $(shell dirname $(realpath $(lastword $(MAKEFILE_LIST))))

help:
	cat Makefile.txt

clean:
	./gradlew clean

scss:
	chmod +x ${PROJECT_DIR}/demo/scss.sh && ${PROJECT_DIR}/demo/scss.sh

gwt:
	./gradlew build

super:
	./gradlew :demo:gwtSuperDev

.PHONY: build
build: gwt

run: build
	chmod +x ${PROJECT_DIR}/demo/start_server.sh && ${PROJECT_DIR}/demo/start_server.sh

site: build
	cp -R ${PROJECT_DIR}/demo/src/main/webapp/ ${PROJECT_DIR}/../dn-client-sites/canvas-text.docstr.org/

release:
	./gradlew release -Prelease.useAutomaticVersion=true

publish-local: build
	rm -rf $$HOME/.m2/repository/org/docstr/canvas-text
	rm -rf $$HOME/.gradle/caches/modules-2/files-2.1/org.docstr/canvas-text
	./gradlew publishMavenJavaPublicationToMavenLocal

clean-s3:
	aws s3 rm s3://maven.docstr.net/releases/org/docstr/canvas-text/ --recursive

publish: publish-local clean-s3
	./gradlew build publishMavenJavaPublicationToMavenRepository
