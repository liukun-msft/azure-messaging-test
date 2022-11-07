package com.azure.eventhubs.specific.blob;

import com.azure.eventhubs.util.Credentials;
import com.azure.storage.blob.BlobAsyncClient;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerAsyncClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import reactor.core.publisher.Flux;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.azure.messaging.eventhubs.checkpointstore.blob.BlobCheckpointStore.EMPTY_STRING;
import static java.nio.charset.StandardCharsets.UTF_8;

public class BlobGetProperties {

    public static void main(String[] args) throws InterruptedException {
        BlobContainerAsyncClient blobContainerAsyncClient = new BlobContainerClientBuilder()
                .connectionString(Credentials.storageConnectionString)
                .containerName(Credentials.storageContainer)
                .buildAsyncClient();


        BlobAsyncClient client = blobContainerAsyncClient.getBlobAsyncClient("test1");
        ByteBuffer data = ByteBuffer.wrap("".getBytes(UTF_8));
        Map<String, String> metadata = new HashMap<>();
        metadata.put("number", "1");
        client.getBlockBlobAsyncClient().uploadWithResponse(Flux.just(data), 0, null,
                 metadata, null, null, null).subscribe(response -> {
            System.out.println(response.getHeaders());
        });

        blobContainerAsyncClient.getProperties().subscribe();
        client.getProperties().subscribe();

        TimeUnit.SECONDS.sleep(10);
    }
}
