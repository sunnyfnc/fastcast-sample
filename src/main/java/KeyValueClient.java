import de.ruedigermoeller.fastcast.config.FCClusterConfig;
import de.ruedigermoeller.fastcast.remoting.*;
import de.ruedigermoeller.fastcast.service.FCMembership;

import java.util.List;

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
            kvService.get(key, new FCFutureResultHandler() {
                @Override
                public void resultReceived(Object obj, String sender) {
                    System.out.println("get returned:"+obj);
                    // important =>
                    done(); // <==== important to free callback id immediately. If several keyval servers run this would ensure you get the answer of the fastest.
                    // ^important, else callback id will be freed by timeout (=>slow throughput)
                }
            });
        }
        @RemoteMethod(2)
        public void valueAdded( Object key, Object value ) {
            System.out.println("received broadcast VALUE_ADDED: k:"+key+" v:"+value);

        }
        @RemoteMethod(3)
        public void valueRemoved( Object key, Object value ) {
            System.out.println("received broadcast VALUE_REMOVED: k:"+key+" v:"+value);
            // just demonstrate how to discover cluster members
            FCMembership memberShipLocal = getRemoting().getMemberShipLocal();
            if ( memberShipLocal != null ) // in case its not configured (see ClusterConfig)
            {
                System.out.println("------------- dumping members -------------------------------");
                List<FCMembership.NodePingInfo> activeNodes = memberShipLocal.getActiveNodes();
                for (int i = 0; i < activeNodes.size(); i++) {
                    FCMembership.NodePingInfo nodePingInfo = activeNodes.get(i);
                    System.out.println(nodePingInfo);
                }
                System.out.println("-------------------------------------------------------------");
            }
        }
    }

    // just put some random stuff
    public void mainLoop() {
        int count = 0;
        while( true ) {
            try {
                count = (count+1) % 100;
                Thread.sleep(1000);
                long now = System.currentTimeMillis();
                kvService.put("hallo" + count, now);
                if ( (count%10) == 0 ) //  throw in a remove op
                {
                    kvService.remove("hallo"+count);
                }
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
        remoting.joinCluster(conf, "KVClient", null);

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
