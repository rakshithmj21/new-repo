package com.demo;

public class App {
    public static void main(String[] args) {
        System.out.println("Hello World from Kubernetes!");
        // Keep the app alive for demonstration (optional)
        try {
            Thread.sleep(600000); // sleeps for 10 minutes
        } catch (InterruptedException e) {
    LOGGER.log(Level.WARN, "Interrupted!", e);
    // Restore interrupted state...
    Thread.currentThread().interrupt();
      }
    }
}

