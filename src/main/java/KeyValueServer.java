import de.ruedigermoeller.fastcast.config.FCClusterConfig;
import de.ruedigermoeller.fastcast.remoting.*;
import de.ruedigermoeller.fastcast.util.FCLog;

import java.util.HashMap;

/**
 * Created by ruedi on 10.05.14.
 */
public class KeyValueServer {

    public static class SampleKeyValueService extends FCTopicService {

        HashMap keyVal;
        SampleKeyValueBroadcaster broadCaster;

        @Override
        public void init() {
            keyVal = new HashMap();
            broadCaster = (SampleKeyValueBroadcaster) getRemoting().getRemoteService(ClusterConfig.KEY_VAL_BROADCAST);
//            FCLog.log("initialized service " + keyVal + " " + broadCaster); printing a proxy throws exception with 2.11 !
        }

        @RemoteMethod(1)
        public void get( Object key, FCFutureResultHandler result ) {
            result.sendResult(keyVal.get(key));
        }

        @RemoteMethod(4)
        public void remove( Object key ) {
            Object remove = keyVal.remove(key);
            if ( remove != null ) {
                broadCaster.valueRemoved(key,remove);
            }
        }

        @RemoteMethod(2)
        public void put(Object key, Object value) {
            boolean isnew = !keyVal.containsKey(key);
            Object oldVal = keyVal.get(key);
            keyVal.put(key,value);
            if ( isnew ) {
                broadCaster.valueAdded(key,value);
            } else {
                if ( oldVal == null && value == null ) {
                    return;
                }
                if ( oldVal != null && oldVal.equals(value) ) {
                    return;
                }
                broadCaster.valueChanged(key, value, oldVal);
            }
        }

        /**
         * overkill method in case of large maps
         * @param result
         */
        @RemoteMethod(3)
        public void getMap(FCFutureResultHandler result) {
            result.sendResult(keyVal);
        }

    }

    public static class SampleKeyValueBroadcaster extends FCTopicService {

        @RemoteMethod(1)
        public void valueChanged( Object key, Object value, Object oldValue ) {}
        @RemoteMethod(2)
        public void valueAdded( Object key, Object value ) {}
        @RemoteMethod(3)
        public void valueRemoved( Object key, Object value ) {}

    }


    public static void main( String arg[] ) throws Exception {
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
        // start sending
        remoting.startSending(ClusterConfig.KEY_VAL_BROADCAST, SampleKeyValueBroadcaster.class);
        // start receiving
        remoting.startReceiving(ClusterConfig.KEY_VAL_SERVICE, new SampleKeyValueService());
    }

}