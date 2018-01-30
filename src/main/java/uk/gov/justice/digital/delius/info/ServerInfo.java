package uk.gov.justice.digital.delius.info;

import com.google.common.collect.ImmutableMap;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Component
public class ServerInfo implements InfoContributor {

    @Override
    public void contribute(Info.Builder builder) {
        Map<String, Object> serverDetails = new HashMap<>();

        InetAddress localHost = null;
        InetAddress loopbackAddress = null;
        try {
            localHost = InetAddress.getLocalHost();
            loopbackAddress = InetAddress.getLoopbackAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }


        serverDetails.put("localHostAddress", localHost.getHostAddress());
        serverDetails.put("localHostName", localHost.getHostName());
        serverDetails.put("localHostCanonicalHostName", localHost.getCanonicalHostName());
        serverDetails.put("loopbackAddress", loopbackAddress.getHostAddress());
        serverDetails.put("loopbackHostName", loopbackAddress.getHostName());
        serverDetails.put("loopbackCanonicalHostName", loopbackAddress.getCanonicalHostName());
        serverDetails.put("serverTimeMillis", System.currentTimeMillis());
        serverDetails.put("serverDateTime", LocalDateTime.now().toString());

        ImmutableMap<String, Long> mem = ImmutableMap.of("memoryUsedByJVM", Runtime.getRuntime().totalMemory(),
                "remainingMemoryAvailableToJVM", Runtime.getRuntime().freeMemory());

        serverDetails.put("memory", mem);
        builder.withDetail("server", serverDetails);
    }
}