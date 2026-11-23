package com.example.tcltvpin;

import android.app.Activity;
import android.os.Bundle;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Locale;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/** Demo PIN screen. Not a system-wide kiosk or secure TV lock. */
public class MainActivity extends Activity {
    private SharedPreferences store;
    private LinearLayout layout;
    private EditText pinEntry;
    private boolean locked = true;
    private int failures = 0;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        store = getSharedPreferences("local_lock", MODE_PRIVATE);
        getWindow().getDecorView().setSystemUiVisibility(5894 | 1024 | 512);
        render();
    }

    private void render() {
        layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        layout.setPadding(60, 30, 60, 30);
        layout.setBackgroundColor(Color.rgb(17, 27, 45));
        setContentView(layout);
        boolean setup = !store.contains("hash");
        boolean showLock = locked || setup;
        label(showLock ? (setup ? "THIET LAP MA PIN TV" : "TV PIN - NHAP MA DE MO") : "DA MO KHOA", 28, Color.WHITE);
        label("Ban thu nghiem: khong chan nut HOME, HDMI hoac khoi dong lai TV.", 16, 0xFFFFCC99);
        if (showLock) {
            pinEntry = new EditText(this);
            pinEntry.setSingleLine(true);
            pinEntry.setHint(setup ? "Tao PIN 4-8 chu so" : "PIN 4-8 chu so");
            pinEntry.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
            pinEntry.setTextColor(Color.WHITE);
            pinEntry.setHintTextColor(0xFFAAAAAA);
            pinEntry.setGravity(Gravity.CENTER);
            pinEntry.setWidth(440);
            layout.addView(pinEntry);
            button(setup ? "LUU PIN" : "MO KHOA", () -> {
                String p = pinEntry.getText().toString();
                if (setup) {
                    if (!p.matches("[0-9]{4,8}")) { toast("PIN phai gom 4-8 chu so"); return; }
                    byte[] salt = new byte[16]; new SecureRandom().nextBytes(salt);
                    store.edit().putString("salt", hex(salt)).putString("hash", compute(p, salt)).apply();
                    locked = false; render();
                } else {
                    if (verify(p)) { failures = 0; locked = false; render(); }
                    else { failures++; toast("Sai PIN"); pinEntry.setText(""); }
                }
            });
        } else {
            button("KHOA LAI (trong ung dung)", () -> { locked = true; render(); });
            label("Khong phai khoa he thong. Nguoi dung van co the thoat ung dung.", 16, 0xFFFFCC99);
        }
    }

    private boolean verify(String p) {
        String saltHex = store.getString("salt", "");
        if (saltHex.length() != 32) return false;
        byte[] salt = new byte[16];
        for (int i = 0; i < 16; i++) salt[i] = (byte)Integer.parseInt(saltHex.substring(i*2, i*2+2), 16);
        return MessageDigest.isEqual(compute(p, salt).getBytes(java.nio.charset.StandardCharsets.UTF_8),
            store.getString("hash", "").getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    private String compute(String p, byte[] salt) {
        try {
            PBEKeySpec spec = new PBEKeySpec(p.toCharArray(), salt, 120000, 256);
            byte[] hash = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
            spec.clearPassword();
            return hex(hash);
        } catch (Exception e) { throw new IllegalStateException("PIN hash failed", e); }
    }
    private String hex(byte[] b) {
        StringBuilder out = new StringBuilder();
        for (byte v : b) out.append(String.format(Locale.US, "%02x", v & 255));
        return out.toString();
    }
    private void label(String s, int size, int color) {
        TextView t = new TextView(this); t.setText(s); t.setTextSize(size); t.setTextColor(color);
        t.setGravity(Gravity.CENTER); t.setPadding(0, 14, 0, 20); layout.addView(t);
    }
    private void button(String text, Runnable action) {
        Button b = new Button(this); b.setText(text); b.setFocusable(true); b.setOnClickListener(v -> action.run());
        layout.addView(b); b.requestFocus();
    }
    private void toast(String s) { Toast.makeText(this, s, Toast.LENGTH_SHORT).show(); }
}
