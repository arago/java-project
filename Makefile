PROJECT_VERSION := $(shell cat ./VERSION)
MVN_OPTIONS ?= -T 1C

compile: .version
	mvn $(MVN_OPTIONS) compile

install: .version
	mvn $(MVN_OPTIONS) install

deploy: .version
	mvn $(MVN_OPTIONS) deploy

.version: VERSION
	mvn $(MVN_OPTIONS) versions:set -DallowSnapshots=true -DnewVersion="$(PROJECT_VERSION)" || true
	mvn $(MVN_OPTIONS) versions:commit
	echo "$(PROJECT_VERSION)" > .version

clean:
	mvn $(MVN_OPTIONS) clean
	rm -f .version