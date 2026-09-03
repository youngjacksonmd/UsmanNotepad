package com.usman.notepad.security;

import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

public final class CryptoManager {
    private static final String STORE = "AndroidKeyStore";
    private static final String ALIAS = "UsmanNotepadV2ProtectedNotes";
    private CryptoManager() {}

    private static SecretKey key() throws Exception {
        KeyStore ks = KeyStore.getInstance(STORE); ks.load(null);
        if (!ks.containsAlias(ALIAS)) {
            KeyGenerator kg = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, STORE);
            kg.init(new KeyGenParameterSpec.Builder(ALIAS, KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .build());
            kg.generateKey();
        }
        return ((KeyStore.SecretKeyEntry)ks.getEntry(ALIAS, null)).getSecretKey();
    }

    public static String encrypt(String plain) throws Exception {
        Cipher c = Cipher.getInstance("AES/GCM/NoPadding"); c.init(Cipher.ENCRYPT_MODE, key());
        byte[] iv = c.getIV(); byte[] enc = c.doFinal((plain == null ? "" : plain).getBytes(StandardCharsets.UTF_8));
        return "enc2:" + Base64.encodeToString(iv, Base64.NO_WRAP) + ":" + Base64.encodeToString(enc, Base64.NO_WRAP);
    }

    public static String decrypt(String stored) throws Exception {
        if (stored == null || !stored.startsWith("enc2:")) return stored == null ? "" : stored;
        String[] p = stored.split(":", 3);
        byte[] iv = Base64.decode(p[1], Base64.NO_WRAP); byte[] data = Base64.decode(p[2], Base64.NO_WRAP);
        Cipher c = Cipher.getInstance("AES/GCM/NoPadding"); c.init(Cipher.DECRYPT_MODE, key(), new GCMParameterSpec(128, iv));
        return new String(c.doFinal(data), StandardCharsets.UTF_8);
    }

    public static boolean isEncrypted(String s) { return s != null && s.startsWith("enc2:"); }
}
