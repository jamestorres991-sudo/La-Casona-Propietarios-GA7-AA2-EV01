package lacasona.servicio;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/** Transforma la contraseña antes de guardarla en USUARIO. */
public final class ClaveSegura {
    private static final int ITERACIONES = 600000;

    private ClaveSegura() {
    }

    public static String crear(char[] clave) throws GeneralSecurityException {
        byte[] sal = new byte[16];
        new SecureRandom().nextBytes(sal);
        Base64.Encoder codificador = Base64.getEncoder();
        return "pbkdf2$" + ITERACIONES + "$" + codificador.encodeToString(sal)
                + "$" + codificador.encodeToString(derivar(clave, sal, ITERACIONES));
    }

    public static boolean verificar(char[] clave, String guardada)
            throws GeneralSecurityException {
        try {
            String[] partes = guardada.split("\\$");
            if (partes.length != 4 || !partes[0].equals("pbkdf2")) {
                return false;
            }
            int vueltas = Integer.parseInt(partes[1]);
            if (vueltas < 100000 || vueltas > 2000000) {
                return false;
            }
            Base64.Decoder decodificador = Base64.getDecoder();
            return MessageDigest.isEqual(decodificador.decode(partes[3]),
                    derivar(clave, decodificador.decode(partes[2]), vueltas));
        } catch (IllegalArgumentException error) {
            return false;
        }
    }

    private static byte[] derivar(char[] clave, byte[] sal, int vueltas)
            throws GeneralSecurityException {
        PBEKeySpec especificacion = new PBEKeySpec(clave, sal, vueltas, 256);
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                    .generateSecret(especificacion).getEncoded();
        } finally {
            especificacion.clearPassword();
        }
    }
}
