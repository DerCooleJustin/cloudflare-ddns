package Records;

public record DnsRecord(
        RecordType recordType,
        String id
) {
    public enum RecordType{
        A,
        AAAA
    }
}