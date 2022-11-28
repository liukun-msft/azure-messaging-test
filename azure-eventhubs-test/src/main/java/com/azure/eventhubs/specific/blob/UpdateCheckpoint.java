package com.azure.eventhubs.specific.blob;

import com.azure.eventhubs.util.Credentials;
import com.azure.storage.blob.BlobAsyncClient;
import com.azure.storage.blob.BlobContainerAsyncClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import reactor.core.publisher.Flux;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static java.nio.charset.StandardCharsets.UTF_8;

public class UpdateCheckpoint {

    public static void main(String[] args) throws InterruptedException {
        BlobContainerAsyncClient blobContainerAsyncClient = new BlobContainerClientBuilder()
                .connectionString(Credentials.storageConnectionString)
                .containerName(Credentials.storageContainer)
                .buildAsyncClient();


        BlobAsyncClient client = blobContainerAsyncClient.getBlobAsyncClient("test1");
        ByteBuffer data = ByteBuffer.wrap("".getBytes(UTF_8));
        Map<String, String> metadata = new HashMap<>();
        metadata.put("number", "1");

        for(int i = 0; i < 10; i++) {
            client.getBlockBlobAsyncClient().uploadWithResponse(Flux.just(data), 0, null,
                            metadata, null, null, null)
                    .block();
            System.out.println("Sleep 3");
            TimeUnit.SECONDS.sleep(3);
        }
    }
}
