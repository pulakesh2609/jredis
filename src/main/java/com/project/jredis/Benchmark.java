package com.project.jredis;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.CopyOnWriteArrayList;

public class Benchmark {

    private static final String HOST = "localhost";
    private static final int PORT = 6380;

    public static void main(String[] args) throws InterruptedException {
        int clientCount = 100;
        int opsPerClient = 1000;

        System.out.println("Benchmarking SET: " + clientCount + " clients x " + opsPerClient + " ops each");
        runBenchmark("SET", clientCount, opsPerClient,
                i -> "*3\r\n$3\r\nSET\r\n$5\r\nkey" + (i % 1000) + "\r\n$5\r\nvalue\r\n");

        System.out.println();
        System.out.println("Benchmarking GET: " + clientCount + " clients x " + opsPerClient + " ops each");
        runBenchmark("GET", clientCount, opsPerClient,
                i -> "*2\r\n$3\r\nGET\r\n$5\r\nkey" + (i % 1000) + "\r\n");
    }

    private interface CommandBuilder {
        String build(int opIndex);
    }

    private static void runBenchmark(String label, int clientCount, int opsPerClient, CommandBuilder builder)
            throws InterruptedException {
        AtomicLong totalOps = new AtomicLong();
        List<Long> latenciesNanos = new CopyOnWriteArrayList<>();

        ExecutorService pool = Executors.newFixedThreadPool(clientCount);
        long benchmarkStart = System.nanoTime();

        for (int c = 0; c < clientCount; c++) {
            pool.submit(() -> {
                try (
                        Socket socket = new Socket(HOST, PORT);
                        PrintWriter out = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8);
                        BufferedReader in = new BufferedReader(
                                new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8))
                ) {
                    for (int i = 0; i < opsPerClient; i++) {
                        long opStart = System.nanoTime();
                        out.print(builder.build(i));
                        out.flush();
                        in.readLine(); // consume exactly one reply line — enough for our simple reply types
                        long opEnd = System.nanoTime();

                        latenciesNanos.add(opEnd - opStart);
                        totalOps.incrementAndGet();
                    }
                } catch (IOException e) {
                    System.err.println("Client error: " + e.getMessage());
                }
            });
        }

        pool.shutdown();
        pool.awaitTermination(60, TimeUnit.SECONDS);
        long benchmarkEnd = System.nanoTime();

        double elapsedSeconds = (benchmarkEnd - benchmarkStart) / 1_000_000_000.0;
        double opsPerSecond = totalOps.get() / elapsedSeconds;

        long[] sorted = latenciesNanos.stream().mapToLong(Long::longValue).sorted().toArray();
        double p50Ms = percentile(sorted, 50) / 1_000_000.0;
        double p95Ms = percentile(sorted, 95) / 1_000_000.0;
        double p99Ms = percentile(sorted, 99) / 1_000_000.0;

        System.out.printf("%s results:%n", label);
        System.out.printf("  Total ops: %d in %.2fs%n", totalOps.get(), elapsedSeconds);
        System.out.printf("  Throughput: %.0f ops/sec%n", opsPerSecond);
        System.out.printf("  Latency p50: %.3fms  p95: %.3fms  p99: %.3fms%n", p50Ms, p95Ms, p99Ms);
    }

    private static long percentile(long[] sortedNanos, int percentile) {
        if (sortedNanos.length == 0) {
            return 0;
        }
        int index = (int) Math.ceil(percentile / 100.0 * sortedNanos.length) - 1;
        return sortedNanos[Math.max(0, Math.min(index, sortedNanos.length - 1))];
    }
}