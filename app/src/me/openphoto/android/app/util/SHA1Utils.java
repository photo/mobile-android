package me.openphoto.android.app.util;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author Patrick Santana <patrick@openphoto.me>
 */
public class SHA1Utils
{

	private static String convertToHex(byte[] data)
	{
		StringBuilder buf = new StringBuilder();
		for (int i = 0; i < data.length; i++)
		{
			int halfbyte = (data[i] >>> 4) & 0x0F;
			int two_halfs = 0;
			do
			{
				if ((0 <= halfbyte) && (halfbyte <= 9))
					buf.append((char) ('0' + halfbyte));
				else
					buf.append((char) ('a' + (halfbyte - 10)));
				halfbyte = data[i] & 0x0F;
			} while (two_halfs++ < 1);
		}
		return buf.toString();
	}

	public static String SHA1(String text) throws NoSuchAlgorithmException,
			UnsupportedEncodingException
	{
		MessageDigest md = MessageDigest.getInstance("SHA-1");
		md.update(text.getBytes("iso-8859-1"), 0, text.length());
		byte[] sha1hash = md.digest();
		return convertToHex(sha1hash);
	}

	private static String byteArray2Hex(byte[] bytes)
	{
		final char[] hexArray =
		{
				'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b',
				'c', 'd', 'e', 'f'
		};
		char[] hexChars = new char[bytes.length * 2];
		int v;
		for (int j = 0; j < bytes.length; j++)
		{
			v = bytes[j] & 0xFF;
			hexChars[j * 2] = hexArray[v >>> 4];
			hexChars[j * 2 + 1] = hexArray[v & 0x0F];
		}
		return new String(hexChars);
	}

	public static String computeSha1ForFile(String filePath)
			throws IOException, NoSuchAlgorithmException
	{
		MessageDigest md = MessageDigest.getInstance("SHA1");

		// reads the file path & file name As a argument
		FileInputStream fis = new FileInputStream(filePath);
		DigestInputStream dis = new DigestInputStream(fis, md);
		BufferedInputStream bis = new BufferedInputStream(dis);

		try
		{
			// Read the bis so SHA1 is auto calculated at dis
			while (true)
			{
				int b = bis.read();
				if (b == -1)
					break;
			}
		} finally
		{
			bis.close();
		}
		return byteArray2Hex(md.digest());
	}
}
