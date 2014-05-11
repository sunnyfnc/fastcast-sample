/**
 * Created by ruedi on 11.05.14.
 */
public class PureListeningClient extends KeyValueClient {

    public static void main( String arg[] ) throws Exception {
        PureListeningClient client = new PureListeningClient();
        client.connect();
        // just skip mainloop ..
        // client.mainLoop();
    }

}
