package ai.seitok.inoue.dest;

public interface HttpDestination extends Destination {

    @Override
    public default boolean isLocal(){
        return false;
    }

    public String getHTTPType();

    public boolean isHTTPS();

}
