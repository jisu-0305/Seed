//package org.example.backend.common.initializer;
//
//import org.example.backend.domain.project.entity.Application;
//import org.example.backend.domain.project.repository.ApplicationRepository;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.stereotype.Component;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.List;
//
//@Component
//public class DataInitializer implements CommandLineRunner {
//    private final ApplicationRepository appRepo;
//
//    public DataInitializer(ApplicationRepository appRepo) {
//        this.appRepo = appRepo;
//    }
//
//    @Override
//    @Transactional
//    public void run(String... args) {
//        List<Application> seeds = List.of(
//                /* DATABASE */
//                // MySQL: two ports
//                Application.builder()
//                        .imageName("mysql")
//                        .defaultPort(3306)
//                        .envVariableList(List.of("MYSQL_ROOT_PASSWORD","MYSQL_DATABASE","MYSQL_USER","MYSQL_PASSWORD"))
//                        .description("MySQL is a widely used, open-source relational database management system (RDBMS).")
//                        .build(),
//                Application.builder()
//                        .imageName("mysql")
//                        .defaultPort(33060)
//                        .envVariableList(List.of("MYSQL_ROOT_PASSWORD","MYSQL_DATABASE","MYSQL_USER","MYSQL_PASSWORD"))
//                        .description("MySQL is a widely used, open-source relational database management system (RDBMS).")
//                        .build(),
//
//                // Redis (6379)
//                Application.builder()
//                        .imageName("redis")
//                        .defaultPort(6379)
//                        .envVariableList(List.of())
//                        .description("Redis is the world’s fastest data platform for caching, vector search, and NoSQL databases.")
//                        .build(),
//
//                // Memcached (11211)
//                Application.builder()
//                        .imageName("memcached")
//                        .defaultPort(11211)
//                        .envVariableList(List.of())
//                        .description("Free & open source, high-performance, distributed memory object caching system.")
//                        .build(),
//
//                // MongoDB (27017)
//                Application.builder()
//                        .imageName("mongo")
//                        .defaultPort(27017)
//                        .envVariableList(List.of("MONGO_INITDB_ROOT_USERNAME", "MONGO_INITDB_ROOT_PASSWORD", "MONGO_INITDB_DATABASE"))
//                        .description("MongoDB document databases provide high availability and easy scalability.")
//                        .build(),
//
//                // MariaDB (3306)
//                Application.builder()
//                        .imageName("mariadb")
//                        .defaultPort(3306)
//                        .envVariableList(List.of("MARIADB_ROOT_PASSWORD", "MARIADB_DATABASE"))
//                        .description("MariaDB Server is a high performing open source relational database, forked from MySQL.")
//                        .build(),
//
//                // PostgreSQL (5432)
//                Application.builder()
//                        .imageName("postgres")
//                        .defaultPort(5432)
//                        .envVariableList(List.of("POSTGRES_PASSWORD"))
//                        .description("The PostgreSQL object-relational database system provides reliability and data integrity.")
//                        .build(),
//
//                // Docker Registry (5000)
//                Application.builder()
//                        .imageName("registry")
//                        .defaultPort(5000)
//                        .envVariableList(List.of())
//                        .description("Distribution implementation for storing and distributing of container images and artifacts")
//                        .build(),
//
//                // Elasticsearch (9200)
//                Application.builder()
//                        .imageName("elasticsearch")
//                        .defaultPort(9200)
//                        .envVariableList(List.of())
//                        .description("Elasticsearch is a powerful open source search and analytics engine that makes data easy to explore.")
//                        .build(),
//
//                /* Message Queue */
//                // RabbitMQ
//                Application.builder()
//                        .imageName("rabbitmq")
//                        .defaultPort(5672)
//                        .envVariableList(List.of("RABBITMQ_DEFAULT_USER", "RABBITMQ_DEFAULT_PASS", "RABBITMQ_DEFAULT_VHOST"))
//                        .description("RabbitMQ is an open source multi-protocol messaging broker.")
//                        .build()
//
//                /* Monitoring & Observability */
//                // Kibana
////                Application.builder()
////                        .imageName("kibana")
////                        .defaultPort(5601)
////                        .envVariableList(List.of("ELASTICSEARCH_HOSTS", "ELASTICSEARCH_HOSTS"))
////                        .description("Kibana gives shape to any kind of data — structured and unstructured — indexed in Elasticsearch.")
////                        .build()
//        );
//
//        seeds.forEach(seed -> {
//            boolean exists = appRepo.existsByImageNameAndDefaultPort(
//                    seed.getImageName(), seed.getDefaultPort()
//            );
//            if (!exists) {
//                appRepo.save(seed);
//            }
//        });
//    }
//}
