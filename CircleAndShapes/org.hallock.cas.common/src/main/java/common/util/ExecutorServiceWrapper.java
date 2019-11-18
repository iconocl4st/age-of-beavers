package common.util;

import java.util.concurrent.ExecutorService;

public class ExecutorServiceWrapper {

    private final ExecutorService service;

    public ExecutorServiceWrapper(ExecutorService service) {
        this.service = service;
    }

    public void submit(Runnable r) {
        service.submit(() -> {
            try {
                r.run();
            } catch (Throwable t) {
                t.printStackTrace();
            }
        });
    }
}
