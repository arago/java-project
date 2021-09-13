VERSION := $(shell cat ./VERSION)

compile: set_version
	mvn compile

install: set_version
	mvn install

deploy: set_version
	mvn --batch-mode deploy

set_version:
	mvn versions:set -DallowSnapshots=true -DnewVersion="$(VERSION)" || true
	mvn versions:commit

clean:
	mvn clean