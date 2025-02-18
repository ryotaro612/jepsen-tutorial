##@ Run
.PHONY: etcdup
etcdup: buildetcd ##Build the etcd cluster then run it.
	docker compose up -d


xa_docker_compose_file = docker-compose-xa.yml
etcddown: ##Down the etcd cluster.
	docker compose down -v

xaup: xadown buildappdb
	docker compose -f $(xa_docker_compose_file) build
	docker compose -f $(xa_docker_compose_file) up -d
xadown:
	docker compose -f $(xa_docker_compose_file) down -v
##@ Build
.PHONY:buildetcd
buildetcd: ##Build the Docker image of etcd.
	docker build --load -t jepsen-tutorial-etcd:latest .

.PHONY:buildappdb
buildappdb: ##Build the Docker image of appdb.
	docker build --load -t jepsen-tutorial-app-db:latest ./app-db

.PHONY: clean
clean:## Clean up
	rm -f id_rsa id_rsa.pub

id_rsa.pub: id_rsa

id_rsa:
	ssh-keygen -f id_rsa -N ''

##@ Fix
.PHONY: format
format: ##Format the clj files in jepsen.etcd.
	cd jepsen.etcd && lein cljfmt fix


##@ Help
.PHONY: help
help: ## Display this help.
	@awk 'BEGIN {FS = ":.*##"; printf "\nUsage:\n  make \033[36m<target>\033[0m\n"} /^[a-zA-Z_0-9-]+:.*?##/ { printf "  \033[36m%-15s\033[0m %s\n", $$1, $$2 } /^##@/ { printf "\n\033[1m%s\033[0m\n", substr($$0, 5) } ' $(MAKEFILE_LIST)

.DEFAULT_GOAL := help
