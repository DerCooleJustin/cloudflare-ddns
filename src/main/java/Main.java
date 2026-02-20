import Records.DnsRecord;
import Records.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public class Main {
    static CfDdnsState state = new CfDdnsState();

    public static void main(String[] args) throws IOException, InterruptedException {
        //region Read Config
        String configPath;

        try {
            configPath = args[0];
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new IllegalArgumentException("Path to configuration file is not given.", e);
        }

        System.out.println("Reading config...");

        JSONObject config = readConfig(configPath);

        setConfig(config);
        //endregion Read Config

        //region Get IPs
        System.out.println("\nRetrieving public IPv4 Address...");
        String v4Address = NetUtils.getPublicV4Address();
        if (v4Address == null)
            System.out.println("This device does not have a working IPv4 connection.");
        else
            System.out.println("IPv4: " + v4Address);

        System.out.println("Retrieving public IPv6 Address...");
        String v6Address = NetUtils.getPublicV6Address();
        if (v6Address == null)
            System.out.println("This device does not have a working IPv6 connection.");
        else
            System.out.println("IPv6: " + v6Address);

        if (v4Address == null && v6Address == null) throw new IllegalStateException("The device does not have any usable IP address right now.");
        //endregion Get IPs

        //region Does Zone exist
        System.out.println("\nChecking if the zone exists...");
        if (!zoneExists()) throw new IllegalArgumentException("The zone to be updated doesn't exist or has been deleted.");
        //endregion Does Zone exist

        //region Update records

        //region Update V4 Records
        System.out.println("\nUpdating IPv4 Records...");
        for (DnsRecord record : state.getV4Records()) {
            if (!recordExists(record.id())) {
                System.err.println("\tRecord " + record + " doesn't exist.");
                continue;
            }
            if (v4Address == null) {
                System.out.println("\tThis device does not have a working IPv4 connection. Skipping v4 entries.");
                break;
            }

            if (updateRecord(record.id(), v4Address)) System.out.println("\tUpdated v4 address for record " + record + " to " + v4Address);
            else System.err.println("\tFailed to update Record " + record);
        }
        //endregion Update V4 Records

        //region Update V6 Records
        System.out.println("\nUpdating IPv6 Entries...");
        for (DnsRecord record : state.getV6Records()) {
            if (!recordExists(record.id())) {
                System.err.println("\tRecord " + record + " doesn't exist.");
                continue;
            }
            if (v6Address == null) {
                System.out.println("\tThis device does not have a working IPv6 connection. Skipping v6 entries.");
                break;
            }

            if (updateRecord(record.id(), v6Address)) System.out.println("\tUpdated v6 adress for record " + record + " to " + v6Address);
            else System.err.println("\tFailed to update Record " + record);
        }
        //endregion Update V6 Records

        //endregion Update Records

        System.out.println("\n\nDone!");
    }

    public static JSONObject readConfig(String configPath) throws IOException {
        String configFile = Files.readString(Paths.get(configPath));
        return new JSONObject(configFile);
    }

    public static void setConfig(JSONObject config) {
        try {
            state.zoneId = config.getString("zoneID");
            state.apiToken = config.getString("apiToken");
        } catch (JSONException e) {
            throw new IllegalArgumentException("Required config not given (\"zoneID\" or \"apiToken\")", e);
        }

        for (int i = 0; i < config.getJSONArray("records").length(); i++) {
            JSONObject record = config.getJSONArray("records").getJSONObject(i);
            if (record.getString("ipv").equals("4"))
                state.addRecord(DnsRecord.RecordType.A, record.getString("recordId"));
            else
                state.addRecord(DnsRecord.RecordType.AAAA, record.getString("recordId"));
        }

        if (state.dnsRecords.isEmpty()) {
            throw new IllegalArgumentException("No DNS records to be updated are given in the config file.");
        }
    }

    public static boolean zoneExists() throws IOException, InterruptedException {
        String response = NetUtils.httpGet(
                "https://api.cloudflare.com/client/v4/zones/" + state.zoneId,
                new Header("Authorization", "Bearer " + state.apiToken)
        );
        JSONObject responseJSON = new JSONObject(response);
        return responseJSON.getBoolean("success");
    }

    public static boolean recordExists(String recordID) throws IOException, InterruptedException {
        String response = NetUtils.httpGet(
                "https://api.cloudflare.com/client/v4/zones/" + state.zoneId + "/dns_records/" + recordID,
                new Header("Authorization", "Bearer " + state.apiToken)
        );
        JSONObject responseJSON = new JSONObject(response);
        if (!responseJSON.getBoolean("success")) System.out.println(response);
        return responseJSON.getBoolean("success");
    }

    public static boolean updateRecord(String recordID, String data) throws IOException, InterruptedException {
        JSONObject bodyJSON = new JSONObject();
        bodyJSON.put("content",data);
        String body = bodyJSON.toString();

        ArrayList<Header> headers = new ArrayList<>();
        headers.add(new Header("Authorization", "Bearer " + state.apiToken));
        headers.add(new Header("Content-Type", "application/json"));

        String response = NetUtils.httpPatch(
                "https://api.cloudflare.com/client/v4/zones/" + state.zoneId + "/dns_records/" + recordID,
                headers,
                body
        );
        JSONObject responseJSON = new JSONObject(response);

        if (!responseJSON.getBoolean("success"))
            System.out.println(response);
        return responseJSON.getBoolean("success");
    }
}