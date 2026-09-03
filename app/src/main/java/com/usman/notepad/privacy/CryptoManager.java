package com.usman.notepad.privacy;

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
    private static final String STORE="AndroidKeyStore";
    private static final String ALIAS="usman_notepad_v2_key";
    public static final class EncryptedPayload {
        public final String cipherText;
        public final String iv;
        public EncryptedPayload(String c,String i){cipherText=c;iv=i;}
    }

    private SecretKey key() throws Exception {
        KeyStore ks=KeyStore.getInstance(STORE); ks.load(null);
        if(ks.containsAlias(ALIAS)) return ((KeyStore.SecretKeyEntry)ks.getEntry(ALIAS,null)).getSecretKey();
        KeyGenerator kg=KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES,STORE);
        kg.init(new KeyGenParameterSpec.Builder(ALIAS,
                KeyProperties.PURPOSE_ENCRYPT|KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .build());
        return kg.generateKey();
    }

    public EncryptedPayload encrypt(String text) throws Exception {
        Cipher c=Cipher.getInstance("AES/GCM/NoPadding");
        c.init(Cipher.ENCRYPT_MODE,key());
        byte[] enc=c.doFinal((text==null?"":text).getBytes(StandardCharsets.UTF_8));
        return new EncryptedPayload(Base64.encodeToString(enc,Base64.NO_WRAP),Base64.encodeToString(c.getIV(),Base64.NO_WRAP));
    }

    public String decrypt(String cipherText,String iv) throws Exception {
        Cipher c=Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec spec=new GCMParameterSpec(128,Base64.decode(iv,Base64.NO_WRAP));
        c.init(Cipher.DECRYPT_MODE,key(),spec);
        return new String(c.doFinal(Base64.decode(cipherText,Base64.NO_WRAP)),StandardCharsets.UTF_8);
    }
}
