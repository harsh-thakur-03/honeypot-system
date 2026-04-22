import java.io.*;
import java.sql.*;
import com.sun.net.httpserver.*;

class AdminHandler implements HttpHandler {
    public void handle(HttpExchange exchange) throws IOException {

        System.out.println("ADMIN PAGE HIT");

        StringBuilder response = new StringBuilder();

        // 🔥 FULL HACKER UI START
        response.append("""
                <html>
                <head>
                <title>Honeypot Admin</title>

                <meta http-equiv='refresh' content='5'>

                <style>
                body {
                    background: black;
                    color: #00ff9f;
                    font-family: monospace;
                }

                /* GLOW TITLE */
                h2 {
                    text-align: center;
                    text-shadow: 0 0 20px #00ff9f;
                    animation: glow 1.5s infinite alternate;
                }

                @keyframes glow {
                    from { text-shadow: 0 0 10px #00ff9f; }
                    to { text-shadow: 0 0 30px #00ff9f; }
                }

                /* TABLE */
                table {
                    width: 95%;
                    margin: auto;
                    border-collapse: collapse;
                    margin-top: 20px;
                }

                th, td {
                    border: 1px solid #00ff9f;
                    padding: 10px;
                    text-align: center;
                }

                th {
                    background: #001f1f;
                }

                /* HOVER */
                tr:hover {
                    background-color: #003333;
                    transition: 0.3s;
                }

                /* ATTACK BLINK */
                .attack {
                    background-color: rgba(255, 0, 0, 0.3);
                    animation: blink 1s infinite;
                }

                @keyframes blink {
                    50% { background-color: rgba(255, 0, 0, 0.7); }
                }

                /* SCROLL TABLE */
                tbody {
                    display: block;
                    height: 400px;
                    overflow-y: auto;
                }

                thead, tbody tr {
                    display: table;
                    width: 100%;
                    table-layout: fixed;
                }

                /* STATUS BAR */
                .status {
                    text-align: center;
                    margin-top: 10px;
                    color: lime;
                }
                </style>

                </head>

                <body>

                <h2>💀 LIVE HONEYPOT MONITOR 💀</h2>

                <div class="status">System Monitoring Active...</div>

                <table>
                <thead>
                <tr>
                <th>ID</th>
                <th>Username</th>
                <th>Password</th>
                <th>IP</th>
                <th>Attack</th>
                <th>Time</th>
                </tr>
                </thead>

                <tbody>
                """);

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            Connection con = DriverManager.getConnection(
                    "jdbc:mysql://127.0.0.1:3306/honeypot_db?useSSL=false&allowPublicKeyRetrieval=true",
                    "root",
                    "kalki21");

            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM login_logs ORDER BY id DESC");

            while (rs.next()) {

                String attack = rs.getString("attack_type");

                // 🔥 Highlight attacks
                if ("SQL_INJECTION".equals(attack)) {
                    response.append("<tr class='attack'>");
                } else if ("BRUTE_FORCE".equals(attack)) {
                    response.append("<tr style='background-color:#663300;'>");
                } else if ("BOT".equals(attack)) {
                    response.append("<tr style='background-color:#003366;'>");
                } else {
                    response.append("<tr>");
                }

                response.append("<td>" + rs.getInt("id") + "</td>");
                response.append("<td>" + rs.getString("username") + "</td>");

                response.append("<td>" + rs.getString("password") + "</td>");
                response.append("<td>" + rs.getString("ip_address") + "</td>");
                response.append("<td>" + attack + "</td>");
                response.append("<td>" + rs.getString("login_time") + "</td>");

                response.append("</tr>");
            }

            rs.close();
            stmt.close();
            con.close();

        } catch (Exception e) {
            response.append("<tr><td colspan='6'>❌ DB ERROR</td></tr>");
            e.printStackTrace();
        }

        // 🔚 END HTML
        response.append("""
                </tbody>
                </table>

                <script>
                // 🔥 LIVE STATUS TEXT CHANGE
                let text = ["Monitoring...", "Tracking intrusions...", "Analyzing attacks..."];
                let i = 0;

                setInterval(() => {
                    document.querySelector(".status").innerText = text[i % text.length];
                    i++;
                }, 2000);
                </script>

                </body>
                </html>
                """);

        byte[] responseBytes = response.toString().getBytes();

        exchange.sendResponseHeaders(200, responseBytes.length);

        OutputStream os = exchange.getResponseBody();
        os.write(responseBytes);
        os.close();
    }
}