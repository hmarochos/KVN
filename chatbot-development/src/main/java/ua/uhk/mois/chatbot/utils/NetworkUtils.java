package ua.uhk.mois.chatbot.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.Enumeration;

@Log4j2
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class NetworkUtils {

    public static String localIPAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        String ipAddress = inetAddress.getHostAddress();
                        int p = ipAddress.indexOf('%');
                        if (p > 0)
                            ipAddress = ipAddress.substring(0, p);
                        log.info("--> localIPAddress = {}", ipAddress);
                        return ipAddress;
                    }
                }
            }
        } catch (SocketException ex) {
            log.error("An error occurred.", ex);
        }
        return "127.0.0.1";
    }

    public static String responseContent(String url) throws IOException, URISyntaxException {
        try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
            HttpGet request = new HttpGet();
            request.setURI(new URI(url));
            InputStream is = client.execute(request).getEntity().getContent();
            try (BufferedReader inb = new BufferedReader(new InputStreamReader(is))) {
                StringBuilder sb = new StringBuilder();
                String line;
                String nl = System.getProperty("line.separator");
                while ((line = inb.readLine()) != null) {
                    sb.append(line).append(nl);
                }
                return sb.toString();
            }
        }
    }

    public static String spec(String host, String botid, String custid, String input) {
        log.trace("--> custid = {}", custid);
        String spec = "";
        try {
            // get custid on first transaction with Pandorabots
            // re-use custid on each subsequent interaction
            spec = "0".equals(custid) ? String.format("%s?botid=%s&input=%s",
                                                      "http://" + host + "/pandora/talk-xml",
                                                      botid,
                                                      URLEncoder.encode(input, "UTF-8")) : String.format("%s?botid=%s&custid=%s&input=%s",
                                                                                                         "http://" + host + "/pandora/talk-xml",
                                                                                                         botid,
                                                                                                         custid,
                                                                                                         URLEncoder.encode(input, "UTF-8"));
        } catch (Exception e) {
            log.error("An error occurred.", e);
        }
        return spec;
    }
}
