.PHONY: help build start stop restart logs clean dev prod test status

# Variables
APP_NAME=delivery-api
COMPOSE=docker-compose
MAVEN=./mvnw

# Couleurs
GREEN=\033[0;32m
YELLOW=\033[1;33m
RED=\033[0;31m
NC=\033[0m

help: ## Affiche l'aide
	@echo ""
	@echo "$(GREEN)=== Delivery API - Commandes disponibles ===$(NC)"
	@echo ""
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | \
		awk 'BEGIN {FS = ":.*?## "}; {printf "  $(YELLOW)%-20s$(NC) %s\n", $$1, $$2}'
	@echo ""

# ===== BUILD =====
build: ## Build le projet Maven (sans tests)
	@echo "$(GREEN)>>> Build Maven...$(NC)"
	$(MAVEN) clean package -DskipTests

build-tests: ## Build avec les tests
	@echo "$(GREEN)>>> Build Maven avec tests...$(NC)"
	$(MAVEN) clean package

build-docker: ## Build l'image Docker
	@echo "$(GREEN)>>> Build image Docker...$(NC)"
	$(COMPOSE) build

# ===== DÉMARRAGE =====
dev: ## Démarre en mode développement (H2 + pgAdmin)
	@echo "$(GREEN)>>> Démarrage en mode DEV...$(NC)"
	$(COMPOSE) --profile dev up -d postgres redis pgadmin
	@echo "$(YELLOW)>>> En attente de PostgreSQL...$(NC)"
	@sleep 3
	SPRING_PROFILES_ACTIVE=dev $(MAVEN) spring-boot:run

start: ## Démarre tous les services (prod)
	@echo "$(GREEN)>>> Démarrage des services...$(NC)"
	$(COMPOSE) up -d
	@echo "$(GREEN)>>> Services démarrés !$(NC)"
	@make status

start-db: ## Démarre seulement PostgreSQL + Redis
	@echo "$(GREEN)>>> Démarrage BDD + Redis...$(NC)"
	$(COMPOSE) up -d postgres redis

prod: ## Démarre en mode production complet (Docker)
	@echo "$(GREEN)>>> Démarrage PROD...$(NC)"
	$(COMPOSE) up -d --build

# ===== ARRÊT =====
stop: ## Arrête tous les services
	@echo "$(RED)>>> Arrêt des services...$(NC)"
	$(COMPOSE) down

stop-clean: ## Arrête et supprime les volumes
	@echo "$(RED)>>> Arrêt et suppression des volumes...$(NC)"
	$(COMPOSE) down -v

restart: ## Redémarre tous les services
	@echo "$(YELLOW)>>> Redémarrage...$(NC)"
	$(COMPOSE) restart

# ===== LOGS =====
logs: ## Affiche les logs de l'API
	$(COMPOSE) logs -f $(APP_NAME)

logs-all: ## Affiche tous les logs
	$(COMPOSE) logs -f

logs-db: ## Affiche les logs PostgreSQL
	$(COMPOSE) logs -f postgres

logs-redis: ## Affiche les logs Redis
	$(COMPOSE) logs -f redis

# ===== TESTS =====
test: ## Lance les tests unitaires
	@echo "$(GREEN)>>> Lancement des tests...$(NC)"
	$(MAVEN) test -Dspring.profiles.active=test

test-coverage: ## Lance les tests avec rapport de couverture
	$(MAVEN) test jacoco:report -Dspring.profiles.active=test

# ===== BASE DE DONNÉES =====
db-connect: ## Connexion à PostgreSQL
	$(COMPOSE) exec postgres psql -U $${POSTGRES_USER:-delivery_user} -d $${POSTGRES_DB:-delivery_db}

db-backup: ## Sauvegarde la base de données
	@echo "$(GREEN)>>> Backup PostgreSQL...$(NC)"
	@mkdir -p backups
	$(COMPOSE) exec postgres pg_dump -U $${POSTGRES_USER:-delivery_user} $${POSTGRES_DB:-delivery_db} \
		> backups/backup_$$(date +%Y%m%d_%H%M%S).sql
	@echo "$(GREEN)>>> Backup créé dans ./backups/$(NC)"

db-restore: ## Restaure la base (make db-restore FILE=backups/backup.sql)
	@echo "$(YELLOW)>>> Restauration de $(FILE)...$(NC)"
	$(COMPOSE) exec -T postgres psql -U $${POSTGRES_USER:-delivery_user} $${POSTGRES_DB:-delivery_db} < $(FILE)

db-reset: ## Recrée la base de données (ATTENTION : perte de données)
	@echo "$(RED)>>> Reset de la base de données...$(NC)"
	$(COMPOSE) down -v
	$(COMPOSE) up -d postgres
	@sleep 5
	@echo "$(GREEN)>>> Base recréée$(NC)"

# ===== STATUT =====
status: ## Affiche l'état des services
	@echo ""
	@echo "$(GREEN)=== État des services ===$(NC)"
	$(COMPOSE) ps
	@echo ""
	@echo "$(GREEN)=== URLs disponibles ===$(NC)"
	@echo "  API          : http://localhost:8080/api"
	@echo "  Swagger      : http://localhost:8080/api/swagger-ui.html"
	@echo "  Actuator     : http://localhost:8080/api/actuator/health"
	@echo "  H2 Console   : http://localhost:8080/api/h2-console (dev only)"
	@echo "  PgAdmin      : http://localhost:5050 (dev only)"
	@echo ""

health: ## Vérifie la santé de l'API
	@curl -s http://localhost:8080/api/actuator/health | python3 -m json.tool || \
		echo "$(RED)>>> API non disponible$(NC)"

# ===== NETTOYAGE =====
clean: ## Nettoie le build Maven
	$(MAVEN) clean

clean-all: ## Nettoie tout (Maven + Docker)
	$(MAVEN) clean
	$(COMPOSE) down -v --rmi local
	@rm -rf logs/*

# ===== INFO =====
info: ## Informations sur le projet
	@echo ""
	@echo "$(GREEN)=== Delivery API ===$(NC)"
	@echo "  Nom        : $(APP_NAME)"
	@echo "  Java       : 17"
	@echo "  Spring     : 3.5.0"
	@echo "  Profils    : dev | prod | test"
	@echo ""