package semexe.cache;

import semexe.basic.LogInfo;
import semexe.basic.Option;
import semexe.exec.Execution;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Runs a server backed by a file which accepts requests of the following form:
 * get\t|key| put\t|key|\t|value|
 * <p/>
 * You can test it out by doing: telnet <path> <port>
 * <p/>
 * To use it in code, see RemoteStringCache.
 *
 * @author Percy Liang
 */
public class StringCacheServer implements Runnable {
    // Represents the null value to be returned back to the user.
    public static String nullString = "__NULL__";
    @Option(gloss = "Open port here", required = true)
    public int port;
    @Option(gloss = "How much output to print")
    public int verbose = 0;
    @Option(gloss = "Read only")
    public boolean readOnly = false;
    @Option(gloss = "Only allow files in this directory")
    public String basePath;
    // Shared state
    private HashMap<String, FileStringCache> caches = new HashMap<>();
    private boolean terminated = false;

    public static void main(String[] args) throws Exception {
        Execution.run(args,
                new StringCacheServer(),
                "FileStringCache", FileStringCache.opts);
    }

    public void run() {
        LogInfo.logs("[%s] Starting server on port %d", new Date(), port);

        try {
            ServerSocket server = new ServerSocket(port);
            while (!terminated) {
                Socket client = server.accept();
                LogInfo.logs("[%s] Opened connection from %s", new Date(), client);
                Thread t = new Thread(new ClientHandler(client));
                t.start();
            }
            LogInfo.log("Done");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    class ClientHandler implements Runnable {
        Socket client;
        FileStringCache cache;

        public ClientHandler(Socket client) {
            this.client = client;
        }

        public void run() {
            try {
                PrintWriter out = new PrintWriter(client.getOutputStream());
                BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                String line;
                int numGets = 0, numPuts = 0, numErrors = 0;
                while (!terminated && (line = in.readLine()) != null) {
                    // LogInfo.logs("Input: %s", line);
                    String[] tokens = line.split("\t");
                    String response = null;
                    if (tokens[0].equals("open") && tokens.length == 2) {
                        if (basePath != null && tokens[1].contains("/")) {
                            response = "ERROR: only simple file names allowed";
                        } else {
                            String path = tokens[1];
                            if (basePath != null)
                                path = new File(basePath, path).toString();
                            // Create the cache if necessary
                            synchronized (caches) {
                                cache = caches.get(path);
                                if (cache == null) {
                                    cache = new FileStringCache();
                                    caches.put(path, cache);
                                }
                            }
                            response = "OK";
                            synchronized (cache) {
                                if (cache.getPath() == null) {
                                    LogInfo.begin_track("Loading %s", path);
                                    try {
                                        cache.init(path, readOnly);
                                    } catch (Throwable t) {
                                        response = "ERROR: " + t;
                                    }
                                    LogInfo.logs("Response: %s", response);
                                    LogInfo.end_track();
                                }
                            }
                        }
                    } else if (tokens[0].equals("get") && tokens.length == 2) {
                        if (cache == null) {
                            response = "ERROR: no file opened yet";
                        } else {
                            synchronized (cache) {
                                response = cache.get(tokens[1]);
                            }
                            if (response == null) response = nullString;
                            numGets++;
                        }
                    } else if (tokens[0].equals("put") && tokens.length == 3) {
                        if (readOnly) {
                            response = "ERROR: read-only";
                        } else if (cache == null) {
                            response = "ERROR: no file opened yet";
                        } else {
                            synchronized (cache) {
                                cache.put(tokens[1], tokens[2]);
                            }
                            response = "OK";
                            numPuts++;
                        }
                    } else if (tokens[0].equals("stats")) {
                        response = "Caches:";
                        synchronized (caches) {
                            for (Map.Entry<String, FileStringCache> stringFileStringCacheEntry : caches.entrySet())
                                response += "\n  " + stringFileStringCacheEntry.getKey() + " (" + stringFileStringCacheEntry.getValue().size() + " entries)";
                        }
                    } else if (tokens[0].equals("terminate")) {
                        if (readOnly) {
                            response = "ERROR: read-only";
                        } else {
                            response = "OK; telnet to the port again to terminate";
                            terminated = true;
                        }
                    } else if (tokens[0].equals("help")) {
                        response = "Commands (tab-separated):\n  open |path|\n  get |key|\n  put |key| |value|\n  terminate\n  stats\n  help";
                    } else {
                        response = "ERROR: " + line;
                        numErrors++;
                    }
                    // LogInfo.logs("Response: %s", response);
                    out.println(response);
                    out.flush();
                }
                in.close();
                LogInfo.logs("[%s] Closed connection %s: %d gets, %d puts, %d errors", new Date(), client, numGets, numPuts, numErrors);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
