import java.io.*;
import com.sun.net.httpserver.*;
import java.sql.*;
import java.net.URLDecoder;

class LoginHandler implements HttpHandler {
    public void handle(HttpExchange exchange) throws IOException {

        InputStream is = exchange.getRequestBody();
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));

        String data = reader.readLine();

        if (data == null || data.isEmpty()) {
            data = "";
        }

        // String ip = exchange.getRemoteAddress().getAddress().getHostAddress();
        String ip = exchange.getRequestHeaders().getFirst("X-Forwarded-For");

        if (ip == null) {
            ip = exchange.getRemoteAddress().getAddress().getHostAddress();
        }
        String userAgent = exchange.getRequestHeaders().getFirst("User-Agent");

        System.out.println("Raw Data: " + data);

        String username = "unknown";
        String password = "unknown";

        try {
            String[] parts = data.split("&");

            if (parts.length >= 2) {
                username = URLDecoder.decode(parts[0].split("=")[1], "UTF-8");
                password = URLDecoder.decode(parts[1].split("=")[1], "UTF-8");
            }

        } catch (Exception e) {
            System.out.println("⚠ Parsing error");
        }

        // 🔥 ATTACK DETECTION
        String attackType = "NORMAL";

        // SQL Injection
        if (data.contains("'") || data.toLowerCase().contains("or") || data.contains("--")) {
            attackType = "SQL_INJECTION";
            System.out.println("⚠ SQL INJECTION ATTEMPT!");
        }

        // Bot Detection (hidden field)
        if (data.contains("hidden_field=") && !data.endsWith("hidden_field=")) {
            attackType = "BOT";
            System.out.println("🤖 BOT DETECTED!");
        }

        // Brute Force Detection
        if (username.length() > 20 || password.length() > 20) {
            attackType = "BRUTE_FORCE";
            System.out.println("🚨 BRUTE FORCE ATTEMPT!");
        }

        System.out.println("Username: " + username);
        System.out.println("Password: " + password);
        System.out.println("IP: " + ip);
        System.out.println("User-Agent: " + userAgent);

        try {
            System.out.println("👉 Trying DB connection...");

            Class.forName("com.mysql.cj.jdbc.Driver");

            Connection con = DriverManager.getConnection(
                    "jdbc:mysql://127.0.0.1:3306/honeypot_db?useSSL=false&allowPublicKeyRetrieval=true",
                    "root",
                    "kalki21");

            System.out.println("✅ DB Connected");

            String query = "INSERT INTO login_logs (username, password, ip_address, attack_type, user_agent) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement stmt = con.prepareStatement(query);

            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.setString(3, ip);
            stmt.setString(4, attackType);
            stmt.setString(5, userAgent);

            stmt.executeUpdate();

            // 🔥 FILE LOGGING
            FileWriter fw = new FileWriter("logs.txt", true);
            fw.write("IP: " + ip + " | Username: " + username + " | Attack: " + attackType + "\n");
            fw.close();

            System.out.println("🔥 DATA INSERTED SUCCESSFULLY");

            stmt.close();
            con.close();

        } catch (Exception e) {
            System.out.println("❌ DB ERROR:");
            e.printStackTrace();
        }

        // 🔥 FAKE SUCCESS RESPONSE (honeypot trick)
        String response = "<html>" +
                "<head>" +
                "<meta http-equiv='refresh' content='1;url=/dashboard'>" +
                "</head>" +
                "<body style='background:black;color:lime;text-align:center;padding-top:100px;'>" +
                "<h2>✔ Access Granted...</h2>" +
                "<p>Redirecting to dashboard...</p>" +
                "</body></html>";
        byte[] responseBytes = response.getBytes();

        exchange.sendResponseHeaders(200, responseBytes.length);

        OutputStream os = exchange.getResponseBody();
        os.write(responseBytes);
        os.close();
    }
}