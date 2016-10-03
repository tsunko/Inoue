package ai.seitok.inoue.dest;

import java.awt.image.RenderedImage;
import java.net.URI;

public interface Destination {

    public String getName();

    public boolean isLocal();

    public URI export(RenderedImage image);

}
