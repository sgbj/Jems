package sgbj;

public class Test {
    public static void main(String[] args) throws Exception {
        String test = "%B5452300551227189^HOGAN/PAUL ^08043210000000725000000?\0\0\0\0";

        // Decrypting
        String bdk = "0123456789ABCDEFFEDCBA9876543210";
        String ksn = "FFFF9876543210E00008";
        String track = "C25C1D1197D31CAA87285D59A892047426D9182EC11353C051ADD6D0F072A6CB3436560B3071FC1FD11D9F7E74886742D9BEE0CFD1EA1064C213BB55278B2F12";
        byte[] decBytes = JDukpt.decrypt(bdk, ksn, track);
        String decrypted = new String(decBytes, "UTF-8");
        System.out.println(decrypted);

        // Encrypting
        byte[] encBytes = JDukpt.encrypt(bdk, ksn, JDukpt.bigIntegerFromBytes(decBytes).toString(16));
        String encrypted = JDukpt.bigIntegerFromBytes(encBytes).toString(16);
        System.out.println(encrypted);
    }
}
