package ai.seitok.inoue.dest;

import javax.imageio.ImageIO;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

public class LocalDestination implements Destination {

    private final Path folder;

    public LocalDestination(Path folder){
        this.folder = folder;
        if(!Files.exists(folder)){
            try {
                Files.createDirectory(folder);
            } catch (IOException e){
                // todo: handle
                e.printStackTrace();
            }
        }
    }

    @Override
    public String getName(){
        return folder.toString();
    }

    @Override
    public boolean isLocal(){
        return true;
    }

    @Override
    public URI export(RenderedImage image){
        Path output = folder.resolve(LocalDateTime.now().toString().replace(":"," ") + ".png");
        try {
            ImageIO.write(image, "png", output.toFile());
        } catch (IOException e){
            // TODO: handle better plserino
            e.printStackTrace();
        }
        return output.toUri();
    }

}
