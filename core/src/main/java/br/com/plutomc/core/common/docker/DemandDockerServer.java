package br.com.plutomc.core.common.docker;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.Ports;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;

public class DemandDockerServer {

    private final DockerClient dockerClient;

    public DemandDockerServer() {
        DefaultDockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withDockerHost("npipe:////./pipe/docker_engine")
                .build();

        ApacheDockerHttpClient dockerHttpClient = new ApacheDockerHttpClient.Builder()
                .dockerHost(config.getDockerHost())
                .maxConnections(100)
                .connectionTimeout(Duration.ofSeconds(30))
                .responseTimeout(Duration.ofSeconds(45))
                .build();

        this.dockerClient = DockerClientBuilder.getInstance(config)
                .withDockerHttpClient(dockerHttpClient)
                .build();
    }

    public void startServer(String serverName, int p, String id) {
        int port;
        if (p == 0) {
            port = ThreadLocalRandom.current().nextInt(20000, 65535 + 1);
        } else {
            port = p;
        }
        try {
            ExposedPort tcp = ExposedPort.tcp(port);
            Ports portBindings = new Ports();
            portBindings.bind(tcp, Ports.Binding.bindPort(port));

            CreateContainerResponse container = dockerClient.createContainerCmd("lobby-server")
                    .withName(serverName)
                    .withNetworkMode("bridge")
                    .withEnv("SERVER_PORT=" + port, "CANSEI=" + id)
                    .withExposedPorts(tcp)
                    .withPortBindings(portBindings)
                    .exec();

            dockerClient.startContainerCmd(container.getId()).exec();
            System.out.println("Server " + serverName + " started on port " + port + " (" + id + ")");
        } catch (Exception e) {
            System.err.println("Failed to start server " + serverName + " on port " + port + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void stopServer(String serverName) {
        try {
            dockerClient.stopContainerCmd(serverName).exec();
            dockerClient.removeContainerCmd(serverName).exec();
            System.out.println("Server " + serverName + " stopped and removed.");
        } catch (Exception e) {
            System.err.println("Failed to stop and remove server " + serverName + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
}
