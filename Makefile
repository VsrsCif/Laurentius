VERSION := 2.3.2

build:
	mvn install
	docker build --build-arg VERSION=$(VERSION) -t lau .

run:
	docker run --network host --name lau --rm lau

stop:
	docker stop lau

clean:
	mvn clean
