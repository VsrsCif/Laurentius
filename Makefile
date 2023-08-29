build:
	mvn install
	docker build -t lau .

run:
	docker run -p 8080:8080 -p 9991:9991 -p 9990:9990 -p 8580:8580 --name lau --rm lau

stop:
	docker stop lau

clean:
	mvn clean
