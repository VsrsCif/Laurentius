VERSION := 2.3.2

build:
	mvn install
	docker build --build-arg VERSION=$(VERSION) -t lau .

run:
	docker run -p 8080:8080 -p 8443:8443 -p 9990:9990 --name lau --rm lau

run-host:
	docker run --network host --name lau --rm lau

stop:
	docker stop lau

clean:
	mvn clean
