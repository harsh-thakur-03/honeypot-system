import java.io.*;
import java.net.InetSocketAddress;
import com.sun.net.httpserver.*;
import java.nio.file.*;

public class HoneypotServer {
    public static void main(String[] args) throws Exception {

        HttpServer server = HttpServer.create(new InetSocketAddress("0.0.0.0", 9090), 0);

        // ✅ ROOT route (login page serve karega)
        server.createContext("/", exchange -> {
            System.out.println("ROOT HIT");

            byte[] response = Files.readAllBytes(Paths.get("login.html"));

            exchange.sendResponseHeaders(200, response.length);

            OutputStream os = exchange.getResponseBody();
            os.write(response);
            os.close();
        });

        // ✅ Login route
        server.createContext("/login", new LoginHandler());

        // ✅ Admin route
        server.createContext("/admin", new AdminHandler());

        server.createContext("/dashboard", exchange -> {

            String html = """
                    <!DOCTYPE html>
                    <html>
                    <head>
                    <title>Secure Panel</title>

                    <style>
                    body {
                        margin: 0;
                        background: black;
                        color: #00ff9f;
                        font-family: monospace;
                        text-align: center;
                    }

                    h1 {
                        margin-top: 100px;
                        text-shadow: 0 0 20px #00ff9f;
                        animation: glow 1.5s infinite alternate;
                    }

                    @keyframes glow {
                        from { text-shadow: 0 0 10px #00ff9f; }
                        to { text-shadow: 0 0 30px #00ff9f; }
                    }

                    .loading {
                        margin-top: 30px;
                        font-size: 18px;
                    }
                    </style>

                    </head>

                    <body>

                    <h1>💻 ACCESS GRANTED</h1>
                    <p class="loading">Loading secure modules...</p>
                    <p style="color:red;">⚠ Monitoring activity...</p>

                    </body>
                    </html>
                    """;

            byte[] response = html.getBytes();
            exchange.sendResponseHeaders(200, response.length);

            OutputStream os = exchange.getResponseBody();
            os.write(response);
            os.close();
        });

        /*
         * server.createContext("/dashboard", exchange -> {
         * String html = """
         * <!DOCTYPE html>
         * <html>
         * <head>
         * <title>Secure Panel</title>
         * 
         * <style>
         * body {
         * margin: 0;
         * background: black;
         * color: #00ff9f;
         * font-family: monospace;
         * overflow: hidden;
         * }
         * 
         * // MATRIX BACKGROUND
         * canvas {
         * position: absolute;
         * top: 0;
         * left: 0;
         * }
         * 
         * // MAIN BOX
         * .container {
         * position: relative;
         * z-index: 1;
         * text-align: center;
         * top: 20%;
         * }
         * 
         * h1 {
         * font-size: 40px;
         * text-shadow: 0 0 20px #00ff9f;
         * animation: glow 1.5s infinite alternate;
         * }
         * 
         * @keyframes glow {
         * from { text-shadow: 0 0 10px #00ff9f; }
         * to { text-shadow: 0 0 30px #00ff9f; }
         * }
         * 
         * .typing {
         * margin-top: 20px;
         * font-size: 20px;
         * white-space: nowrap;
         * overflow: hidden;
         * border-right: 2px solid #00ff9f;
         * width: 0;
         * animation: typing 4s steps(40, end) forwards;
         * }
         * 
         * @keyframes typing {
         * from { width: 0 }
         * to { width: 100% }
         * }
         * 
         * // FAKE LOADING BAR
         * .bar {
         * margin: 30px auto;
         * width: 300px;
         * height: 10px;
         * background: #222;
         * border-radius: 5px;
         * }
         * 
         * .progress {
         * height: 100%;
         * width: 0;
         * background: #00ff9f;
         * animation: load 3s forwards;
         * }
         * 
         * @keyframes load {
         * from { width: 0 }
         * to { width: 100% }
         * }
         * </style>
         * 
         * </head>
         * 
         * <body>
         * 
         * <canvas id="matrix"></canvas>
         * 
         * <div class="container">
         * <h1>💻 ACCESS GRANTED</h1>
         * 
         * <div class="typing">
         * Initializing secure system...
         * </div>
         * 
         * <div class="bar">
         * <div class="progress"></div>
         * </div>
         * 
         * <p>Decrypting credentials...</p>
         * <p style="color:red;">⚠ Unauthorized access detected</p>
         * </div>
         * 
         * <script>
         * // MATRIX EFFECT
         * const canvas = document.getElementById("matrix");
         * const ctx = canvas.getContext("2d");
         * 
         * canvas.height = window.innerHeight;
         * canvas.width = window.innerWidth;
         * 
         * const letters = "01";
         * const fontSize = 14;
         * const columns = canvas.width / fontSize;
         * 
         * const drops = [];
         * 
         * for (let i = 0; i < columns; i++) {
         * drops[i] = 1;
         * }
         * 
         * function draw() {
         * ctx.fillStyle = "rgba(0, 0, 0, 0.05)";
         * ctx.fillRect(0, 0, canvas.width, canvas.height);
         * 
         * ctx.fillStyle = "#00ff9f";
         * ctx.font = fontSize + "px monospace";
         * 
         * for (let i = 0; i < drops.length; i++) {
         * const text = letters[Math.floor(Math.random() * letters.length)];
         * ctx.fillText(text, i * fontSize, drops[i] * fontSize);
         * 
         * if (drops[i] * fontSize > canvas.height && Math.random() > 0.975) {
         * drops[i] = 0;
         * }
         * 
         * drops[i]++;
         * }
         * }
         * 
         * setInterval(draw, 33);
         * </script>
         * 
         * </body>
         * </html>
         * """;
         * 
         * byte[] response = html.getBytes();
         * exchange.sendResponseHeaders(200, response.length);
         * OutputStream os = exchange.getResponseBody();
         * os.write(response);
         * os.close();
         * });
         */

        server.setExecutor(null);
        server.start();

        System.out.println("Server started at http://localhost:9090");
    }
}
/*
 * 
 javac -cp ".;lib/mysql-connector-j-9.6.0.jar" *.java
 java -cp ".;lib/mysql-connector-j-9.6.0.jar" HoneypotServer
 * 
 */

// ngrok.exe http 9090