import de.ruedigermoeller.fastcast.config.FCClusterConfig;
import de.ruedigermoeller.fastcast.config.FCConfigBuilder;
import de.ruedigermoeller.fastcast.util.FCLog;

import java.io.File;
import java.io.IOException;

/**
 * Created by ruedi on 10.05.14.
 */
public class ClusterConfig {

    public static String KEY_VAL_SERVICE = "key_value_service";
    public static String KEY_VAL_BROADCAST = "key_value_bcast";

    public static FCClusterConfig getClusterConfig() {
        // configure a cluster programatically (for larger scale apps one should prefer config files)
        FCClusterConfig conf = FCConfigBuilder.New()
                .socketTransport("default", "127.0.0.1", "229.9.9.9", 44444)
                .topic(KEY_VAL_SERVICE, 0,   2000) // 2000*8 = max 16MB per second
                .topic(KEY_VAL_BROADCAST, 1, 2500)
                .membership("members",99) // built in topic for self monitoring+cluster view
                .end()
                .build();

        conf.setLogLevel(FCLog.INFO);
        try {
            // write out config to enable ClusterView
            new File("/tmp").mkdir(); // windoze ..
            conf.write("/tmp/conf.yaml");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return conf;
    }
}
