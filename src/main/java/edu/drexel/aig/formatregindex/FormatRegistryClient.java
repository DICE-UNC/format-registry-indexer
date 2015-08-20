package edu.drexel.aig.formatregindex;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.List;

/**
 * Created by Isaac Simmons on 9/25/2014.
 */
public class FormatRegistryClient {
    public static final String DEFAULT_POST_URL = "http://format-registry.cci.drexel.edu/identify-results?json=true";
    private final String postUrl;
    private final CloseableHttpClient client = HttpClients.createDefault();
    private final Gson gson = new GsonBuilder().create();
    private static final Type LIST_TYPE = new TypeToken<List<String>>(){}.getType();

    public FormatRegistryClient() {
        this(DEFAULT_POST_URL);
    }

    public FormatRegistryClient(String postUrl) {
        this.postUrl = postUrl;
    }

    public List<String> results(String filename, InputStream content) throws IOException {
        final HttpPost post = new HttpPost(postUrl);
        final HttpEntity args = MultipartEntityBuilder.create()
                .addBinaryBody("myfile", content, ContentType.APPLICATION_OCTET_STREAM, filename)
                .build();
        post.setEntity(args);

        CloseableHttpResponse response = client.execute(post);

        HttpEntity resEntity = response.getEntity();

        if (response.getStatusLine().getStatusCode() != 200) {
            throw new IOException("Server responded with error code " + response.getStatusLine().getReasonPhrase() + response.getStatusLine().getStatusCode());
        }
        if (resEntity == null) {
            throw new IOException("Server response empty");
        }
        final String body = EntityUtils.toString(resEntity);
        return gson.fromJson(body, LIST_TYPE);
    }

    public List<String> results(String filename, byte[] content) throws IOException {
        return results(filename, new ByteArrayInputStream(content));
    }
}
