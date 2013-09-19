package sgbj;

import java.math.*;
import java.security.*;
import java.util.*;
import javax.crypto.*;
import javax.crypto.spec.*;
import org.apache.commons.lang3.*;
import org.bouncycastle.jce.provider.*;

public class JDukpt {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    private static final BigInteger 
            reg3Mask = bigIntegerFromHex("1FFFFF"),
            shiftRegMask = bigIntegerFromHex("100000"),
            reg8Mask = bigIntegerFromHex("FFFFFFFFFFE00000"),
            ls16Mask = bigIntegerFromHex("FFFFFFFFFFFFFFFF"),
            ms16Mask = bigIntegerFromHex("FFFFFFFFFFFFFFFF0000000000000000"),
            keyMask = bigIntegerFromHex("C0C0C0C000000000C0C0C0C000000000"),
            pekMask = bigIntegerFromHex("FF00000000000000FF"),
            ksnMask = bigIntegerFromHex("FFFFFFFFFFFFFFE00000");

    public static BigInteger createBdk(BigInteger key1, BigInteger key2) {
        return key1.xor(key2);
    }

    public static BigInteger createIpek(BigInteger ksn, BigInteger bdk) {
        return transform("DESede/CBC/ZeroBytePadding", true, bdk, (ksn.and(ksnMask)).shiftRight(16)).and(ms16Mask)
           .or(transform("DESede/CBC/ZeroBytePadding", true, bdk.xor(keyMask), (ksn.and(ksnMask)).shiftRight(16)).shiftRight(64));
    }

    public static BigInteger createSessionKey(BigInteger ipek, BigInteger ksn) {
        return deriveKey(ipek, ksn).xor(pekMask);
    }

    public static BigInteger deriveKey(BigInteger ipek, BigInteger ksn) {
        BigInteger ksnReg = ksn.and(ls16Mask).and(reg8Mask);
        BigInteger curKey = ipek;
        for (BigInteger shiftReg = shiftRegMask; shiftReg.compareTo(BigInteger.ZERO) > 0; shiftReg = shiftReg.shiftRight(1))
            if ((shiftReg.and(ksn).and(reg3Mask)).compareTo(BigInteger.ZERO) > 0)
                curKey = generateKey(curKey, ksnReg = ksnReg.or(shiftReg));
        return curKey;
    }

    public static BigInteger generateKey(BigInteger key, BigInteger ksn) {
        return encryptRegister(key.xor(keyMask), ksn).shiftLeft(64)
           .or(encryptRegister(key, ksn));
    }

    public static BigInteger encryptRegister(BigInteger curKey, BigInteger reg8) {
        return (curKey.and(ls16Mask)).xor(transform("DES/CBC/ZeroBytePadding", true, 
               (curKey.and(ms16Mask)).shiftRight(64), 
               (curKey.and(ls16Mask).xor(reg8))).shiftRight(64));
    }

    public static BigInteger transform(String name, boolean encrypt, BigInteger key, BigInteger message) {
        try {
            Cipher cipher = Cipher.getInstance(name, "BC");
            cipher.init(encrypt ? Cipher.ENCRYPT_MODE : Cipher.DECRYPT_MODE,
                    new SecretKeySpec(bigIntegerToByteArray(key), name),
                    new IvParameterSpec(new byte[8]));
            return bigIntegerFromBytes(cipher.doFinal(bigIntegerToByteArray(message)));
        } catch (Exception ex) {
            throw new AssertionError(ex);
        }
    }

    public static byte[] encrypt(String bdk, String ksn, String track) {
        return bigIntegerToByteArray(transform("DESede/CBC/ZeroBytePadding", true, createSessionKey(createIpek(
               bigIntegerFromHex(ksn), bigIntegerFromHex(bdk)), bigIntegerFromHex(ksn)), bigIntegerFromHex(track)));
    }

    public static byte[] decrypt(String bdk, String ksn, String track) {
        return bigIntegerToByteArray(transform("DESede/CBC/ZeroBytePadding", false, createSessionKey(createIpek(
               bigIntegerFromHex(ksn), bigIntegerFromHex(bdk)), bigIntegerFromHex(ksn)), bigIntegerFromHex(track)));
    }

    public static BigInteger bigIntegerFromHex(String hex) {
        return new BigInteger("00" + hex, 16);
    }

    public static byte[] bigIntegerToByteArray(BigInteger bi) {
        byte[] b = bi.toByteArray();
        return b[0] == 0 ? Arrays.copyOfRange(b, 1, b.length) : b;
    }

    public static BigInteger bigIntegerFromBytes(byte[] b) {
        return new BigInteger(ArrayUtils.addAll(new byte[1], b));
    }
}
