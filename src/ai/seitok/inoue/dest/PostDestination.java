package ai.seitok.inoue.dest;

import org.json.JSONObject;

import javax.imageio.ImageIO;
import java.awt.image.RenderedImage;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.time.LocalDateTime;

public class PostDestination implements HttpDestination {

    private final String name;
    private final String urlHost;
    private final URL postUrl;
    private final String param;

    public PostDestination(String name, String urlHost, URL postUrl, String param){
        this.name = name;
        this.urlHost = urlHost;
        this.postUrl = postUrl;
        this.param = param;
    }

    @Override
    public String getName(){
        return name;
    }

    @Override
    public boolean isHTTPS(){
        return postUrl.getProtocol().equals("https");
    }

    @Override
    public String getHTTPType(){
        return "POST";
    }

    @Override
    public URI export(RenderedImage image){
        try {
            HttpURLConnection connection = (HttpURLConnection) postUrl.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("User-Agent", "Inoue");
            connection.setRequestProperty("Connection", "Keep-Alive");
            connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=******");
            connection.setDoInput(true);
            connection.setDoOutput(true);

            try(DataOutputStream dos = new DataOutputStream(connection.getOutputStream())) {
                dos.writeBytes("--******\r\n");
                dos.writeBytes(String.format(
                        "Content-Disposition: form-data; name=\"%s\";filename=\"%s.%s\"\r\nContent-type: %s\r\n",
                        param,
                        LocalDateTime.now().toString(),
                        "png",
                        "image/png"));
                dos.writeBytes("\r\n");
                ImageIO.write(image, "png", dos);
                dos.writeBytes("\r\n");
                dos.writeBytes("--******--\r\n");
                dos.flush();
            }

            try(BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()))){
                JSONObject obj = new JSONObject(br.readLine());
                String url = obj.getJSONArray("files").getJSONObject(0).getString("url");
                if(!url.startsWith(urlHost)){
                    return URI.create(urlHost + "/" + url);
                } else {
                    return URI.create(url);
                }
            }
        } catch (IOException e){
            // todo: handle
            e.printStackTrace();
            return null;
        }
    }

}
