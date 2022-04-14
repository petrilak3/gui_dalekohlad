package fmfi.dalekohlad.Communication;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import fmfi.dalekohlad.InputHandling.InputConfirmation;
import fmfi.dalekohlad.Modules.GUIModule;
import fmfi.dalekohlad.Operations;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class Communication {
    private static final Logger lgr = LogManager.getLogger(Communication.class);
    private static Socket sock = null;
    private static DataOutputStream client_out;
    private static InputStream client_in;
    private static InetSocketAddress host;

    private static ArrayList<GUIModule> modules = null;
    private static boolean run = true;

    private static String readString() throws IOException {
        StringBuilder sb = new StringBuilder();
        int b;
        while ((b = client_in.read()) > 0) {
            sb.append((char)b);
        }
        if (b == -1) {
            // -1 is returned when the other side exits unexpectedly, we'll treat this case in the same way as a timeout
            throw new SocketTimeoutException();
        }
        return sb.toString();
    }

    private static void sleep(int milliseconds) {
        try {
            TimeUnit.MILLISECONDS.sleep(milliseconds);
        }
        catch (Exception e) {
            lgr.debug("Failed to sleep", e);
        }
    }

    private static void informModule(GUIModule mod, JsonObject jo) {
        try {
            mod.update(jo);
        }
        catch (Exception e) {
            lgr.error(String.format("Update of module %s failed:", mod.getClass().getName()));
        }
    }

    private static void processUpdate(String data) {
        if (data.startsWith("{")) {
            JsonReader reader = new JsonReader(new StringReader(data.trim()));
            reader.setLenient(true);
            JsonObject json_object = JsonParser.parseReader(reader).getAsJsonObject();
            modules.forEach(x -> informModule(x, json_object));
        }
        else if (!data.equals("")) {
            Operations.add(data);
        }
    }

    private static void periodicUpdate() {
        connect();
        while (run) {
            try {
                processUpdate(readString());
            }
            catch (SocketTimeoutException|SocketException e) {
                reconnect();
            }
            catch (Exception e) {
                lgr.error("Failed to read data, repeating...", e);
                sleep(1000);
            }
        }
    }

    private static void reconnect() {
        lgr.debug("Attempting to reconnect...");
        closeSock();
        connect();
    }

    private static void sockConnect() throws IOException {
        sock = new Socket();
        sock.connect(host, 2000);
        client_out = new DataOutputStream(sock.getOutputStream());
        client_in = sock.getInputStream();
    }

    private static void connect() {
        lgr.debug(String.format("Connecting to %s:%d", host.getAddress(), host.getPort()));
        while (true) {
            try {
                sockConnect();
                return;
            } catch (Exception e) {
                lgr.error(String.format("Connection attempt to %s:%d failed, repeating...",
                        host.getAddress(), host.getPort()), e);
                sleep(2000);
            }
        }
    }

    private static Thread startPeriodicUpdates() {
        Runnable runnable = Communication::periodicUpdate;
        Thread periodic_thread = new Thread(runnable);
        periodic_thread.start();
        return periodic_thread;
    }

    public static Thread init(InetSocketAddress host, ArrayList<GUIModule> modules) {
        Communication.modules = modules;
        Communication.host = host;
        Runtime.getRuntime().addShutdownHook(new Thread(Communication::close));
        return startPeriodicUpdates();
    }

    public static boolean sendData(String data) {
        try {
            ByteArrayOutputStream out_data = new ByteArrayOutputStream();
            out_data.write(data.getBytes());
            out_data.write(0);
            client_out.write(out_data.toByteArray(), 0, data.length()+1);
            return true;
        } catch (Exception e) {
            lgr.error("Failed to send data:", e);
            InputConfirmation.warn("Data couldn't be sent!");
            return false;
        }
    }

    private static void closeSock() {
        try {
            sock.close();
        }
        catch (Exception e) {
            lgr.error("Failed to close socket:", e);
        }
    }

    public static void close() {
        if (run) {
            run = false;
            closeSock();
        }
    }

}
