package dev.wolfisberg;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Query;

import java.util.List;

import static org.neo4j.driver.Values.parameters;

public class Main implements AutoCloseable {
    private final Driver driver;

    public Main() {
        driver = GraphDatabase.driver("bolt://localhost:7687", AuthTokens.none());
//        falls mit password
//        driver = GraphDatabase.driver("bolt://localhost:7687", AuthTokens.basic("neo4j", "the password"));
    }

    public static void main(String... args) {
        try (var greeter = new Main()) {
            greeter.createPerson("Peter");
            greeter.createPerson("Paul");
            greeter.listPersons();
        }
    }

    @Override
    public void close() throws RuntimeException {
        driver.close();
    }

    public void createPerson(final String name) {
        try (var session = driver.session()) {
            var createdName = session.executeWrite(tx -> {
                var query = new Query("CREATE (p:Person) SET p.name = $name RETURN p.name", parameters("name", name));
                var result = tx.run(query);
                return result.single().get(0).asString();
            });
            System.out.println("Created person with name: " + createdName);
        }
    }

    public void listPersons() {
        try (var session = driver.session()) {
            List<String> names = session.executeWrite(tx -> {
                var query = new Query("MATCH (p:Person) RETURN p.name");
                var result = tx.run(query);
                return result.list().stream().map(record -> record.get(0).asString()).toList();
            });
            System.out.println("The person names are: " + String.join(", ", names));
        }
    }
}