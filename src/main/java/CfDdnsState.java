import Records.DnsRecord;

import java.util.ArrayList;

public class CfDdnsState {
    public String zoneId = "";
    public String apiToken = "";
    public ArrayList<DnsRecord> dnsRecords = new ArrayList<>();

    public ArrayList<DnsRecord> getV4Records(){
        ArrayList<DnsRecord> v4Records = new ArrayList<>();

        for (DnsRecord record : dnsRecords)
            if (record.recordType() == DnsRecord.RecordType.A)
                v4Records.add(record);

        return v4Records;
    }

    public ArrayList<DnsRecord> getV6Records(){
        ArrayList<DnsRecord> v6Records = new ArrayList<>();

        for (DnsRecord record : dnsRecords)
            if (record.recordType() == DnsRecord.RecordType.AAAA)
                v6Records.add(record);

        return v6Records;
    }

    public void addRecord(DnsRecord.RecordType type, String id) {
        dnsRecords.add(new DnsRecord(type, id));
    }
}