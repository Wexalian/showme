// package com.wexalian.showme.core.torrent.eztv;
//
// import org.apache.logging.log4j.Level;
// import org.apache.logging.log4j.LogManager;
// import org.apache.logging.log4j.Logger;
//
// import java.io.BufferedReader;
// import java.io.DataOutputStream;
// import java.io.IOException;
// import java.io.InputStreamReader;
// import java.net.HttpURLConnection;
// import java.net.URL;
// import java.util.Map;
//
// public final class HTTPUtils {
//     private static final Logger LOGGER = LogManager.getLogger();
//     private static final int RATE_LIMIT_RESPONSE = 429;
//
//     private HTTPUtils() {}
//
//     public static String get(String url) {
//         return get(url, null);
//     }
//
//     public static String get(String url, Map<String, String> headers) {
//         try {
//             HttpURLConnection con = openGetConnection(url, headers);
//             if (con.getResponseCode() == RATE_LIMIT_RESPONSE) {
//                 int retryAfter = Integer.parseInt(con.getHeaderField("Retry-After"));
//                 con.disconnect();
//                 Thread.sleep(retryAfter * 1000L + 1000);
//                 con = openGetConnection(url, headers);
//             }
//
//             if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
//                 return readData(con);
//             }
//         }
//         catch (IOException | InterruptedException e) {
//             LOGGER.debug("Error GET-ing from '{}'", url);
//             LOGGER.catching(Level.ERROR, e);
//         }
//
//         return "";
//     }
//
//     private static HttpURLConnection openGetConnection(String url, Map<String, String> headers) throws IOException {
//         return openConnection(url, headers, "GET", false);
//     }
//
//     private static String readData(HttpURLConnection con) throws IOException {
//         BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
//         String inputLine;
//         StringBuilder response = new StringBuilder();
//
//         while ((inputLine = in.readLine()) != null) response.append(inputLine);
//         in.close();
//
//         return response.toString();
//     }
//
//     private static HttpURLConnection openConnection(String url, Map<String, String> headers, String method, boolean output) throws IOException {
//         URL obj = new URL(url);
//         HttpURLConnection con = (HttpURLConnection) obj.openConnection();
//         con.setRequestMethod(method);
//         con.setRequestProperty("User-Agent", "Mozilla/5.0");
//         con.setRequestProperty("Content-Type", "application/json");
//         con.setDoOutput(output);
//
//         if (headers != null) headers.forEach(con::setRequestProperty);
//         // con.setConnectTimeout(0);
//         // con.setReadTimeout(0);
//         con.connect();
//         return con;
//     }
//
//     public static String post(String url, String body) {
//         return post(url, null, body);
//     }
//
//     public static String post(String url, Map<String, String> headers, String body) {
//         try {
//             HttpURLConnection con = openPostConnection(url, headers);
//             sendData(con, body);
//
//             if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
//                 return readData(con);
//             }
//         }
//         catch (IOException e) {
//             LOGGER.debug("Error GET-ing from '{}'", url);
//             LOGGER.catching(Level.ERROR, e);
//         }
//
//         return "";
//     }
//
//     private static HttpURLConnection openPostConnection(String url, Map<String, String> headers) throws IOException {
//         return openConnection(url, headers, "POST", true);
//     }
//
//     private static void sendData(HttpURLConnection con, String body) throws IOException {
//         try (DataOutputStream out = new DataOutputStream(con.getOutputStream())) {
//             out.writeBytes(body);
//             out.flush();
//         }
//     }
// }
