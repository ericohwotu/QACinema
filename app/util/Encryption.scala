package util

import org.apache.commons.codec.binary.Base64
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

object Encryption {

  def encrypt(key: String, value: String): String = {
    val cipher: Cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
    val secretKey: SecretKeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES")
    cipher.init(Cipher.ENCRYPT_MODE, secretKey)
    Base64.encodeBase64String(cipher.doFinal(value.getBytes("UTF-8")))
  }

  def decrypt(key: String, encryptedValue: String): String = {
    val cipher: Cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
    val secretKey: SecretKeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES")
    cipher.init(Cipher.DECRYPT_MODE, secretKey)
    new String(cipher.doFinal(Base64.decodeBase64(encryptedValue)))
  }

  def getKey(key: String)(int: Int): String = {
    def keyHelper(res: String)(cur: Int): String = cur match {
      case 0 => res
      case x => keyHelper(res + key.charAt(cur % key.length))(cur - 1)
    }
    keyHelper("")(int)
  }
}
