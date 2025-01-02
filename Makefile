##@ Run
.PHONY: etcdup
etcdup: Dockerfile ##Build the etcd cluster then run it.
	docker compose up --build -d

##@ Build
Dockerfile: id_rsa.pub

.PHONY: clean
clean:## Clean up
	rm -f id_rsa id_rsa.pub

id_rsa.pub: id_rsa

id_rsa:
	ssh-keygen -f id_rsa -N ''

##@ Help
.PHONY: help
help: ## Display this help.
	@awk 'BEGIN {FS = ":.*##"; printf "\nUsage:\n  make \033[36m<target>\033[0m\n"} /^[a-zA-Z_0-9-]+:.*?##/ { printf "  \033[36m%-15s\033[0m %s\n", $$1, $$2 } /^##@/ { printf "\n\033[1m%s\033[0m\n", substr($$0, 5) } ' $(MAKEFILE_LIST)

.DEFAULT_GOAL := help
