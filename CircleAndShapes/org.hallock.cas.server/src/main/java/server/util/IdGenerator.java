package server.util;

import common.state.EntityId;

import java.util.HashSet;
import java.util.Random;

public class IdGenerator {
    private final HashSet<Integer> generated = new HashSet<>();
    private Random random;

    public IdGenerator(Random random) {
        this.random = random;
        generated.add(0);
    }

    public EntityId generateId() {
        while (true) {
            int newId = random.nextInt();
            if (generated.contains(newId)) {
                continue;
            }

            generated.add(newId);
            return new EntityId(newId);
        }
    }
}
