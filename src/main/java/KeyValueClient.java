import de.ruedigermoeller.fastcast.config.FCClusterConfig;
import de.ruedigermoeller.fastcast.remoting.FCRemoting;
import de.ruedigermoeller.fastcast.remoting.FCTopicService;
import de.ruedigermoeller.fastcast.remoting.FastCast;
import de.ruedigermoeller.fastcast.remoting.RemoteMethod;

/**
 * Created by ruedi on 10.05.14.
 */
public class KeyValueClient {

    KeyValueServer.SampleKeyValueService kvService;
    SampleKeyValueBroadcastListener listener;

    public KeyValueServer.SampleKeyValueService getKvService() {
        return kvService;
    }

    public void setKvService(KeyValueServer.SampleKeyValueService kvService) {
        this.kvService = kvService;
    }

    public SampleKeyValueBroadcastListener getListener() {
        return listener;
    }

    public void setListener(SampleKeyValueBroadcastListener listener) {
        this.listener = listener;
    }

    public class SampleKeyValueBroadcastListener extends KeyValueServer.SampleKeyValueBroadcaster {

        @RemoteMethod(1)
        public void valueChanged( Object key, Object value, Object oldValue ) {
            System.out.println("received broadcast VALUE_CHANGED: k:"+key+" v:"+value+" old:"+oldValue);
        }
        @RemoteMethod(2)
        public void valueAdded( Object key, Object value ) {
            System.out.println("received broadcast VALUE_ADDED: k:"+key+" v:"+value);

        }
        @RemoteMethod(3)
        public void valueRemoved( Object key, Object value ) {

        }
    }

    // just put some random stuff
    public void mainLoop() {
        int count = 0;
        while( true ) {
            try {
                count = (count+1) % 1000;
                Thread.sleep(1000);
                kvService.put("hallo"+count, System.currentTimeMillis());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void connect() throws Exception {
        FCRemoting remoting = FastCast.getRemoting();
        // always share same clusterconfig amongst all nodes
        FCClusterConfig conf = ClusterConfig.getClusterConfig();

//        FCLog.setInstance( new FCLog() {
//                @Override
//                protected void out(int level, String msg, Throwable th) {
//                    // plug in your logger here
//                }
//            }
//        );

        // wire sockets/queues etc.
        remoting.joinCluster(conf, "Receiver", null);

        // start listening
        listener = new SampleKeyValueBroadcastListener();
        remoting.startReceiving(ClusterConfig.KEY_VAL_BROADCAST, listener);
        // start sending
        kvService = remoting.startSending(ClusterConfig.KEY_VAL_SERVICE, KeyValueServer.SampleKeyValueService.class);
    }

    public static void main( String arg[] ) throws Exception {
        KeyValueClient client = new KeyValueClient();
        client.connect();
        client.mainLoop();
    }

}
