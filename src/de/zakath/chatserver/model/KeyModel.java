/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.zakath.chatserver.model;

import java.security.*;
import java.security.spec.*;
import java.sql.*;
import java.util.logging.*;
import de.zakath.simplenetwork.*;

/**
 * 
 * @author cw
 */
public class KeyModel
{

	protected static KeyFactory keyFactory;

	static
	{
		try
		{
			keyFactory = KeyFactory.getInstance("RSA");
		} catch (NoSuchAlgorithmException e)
		{
			e.printStackTrace();
		}
	}

	public static KeyPair getPublicKeyforUID(int ID)
	{

		try
		{
			ResultSet rs = DBModel
					.executeQuery("SELECT * FROM keytable WHERE uid = "
							+ Integer.toString(ID) + ";");
			rs.first();

			X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(
					rs.getBytes("publickey"));
			PublicKey pk = null;
			try
			{
				pk = keyFactory.generatePublic(publicKeySpec);
			} catch (InvalidKeySpecException e)
			{
				e.printStackTrace();
			}
			return new KeyPair(pk, null);
		} catch (SQLException ex)
		{
			Logger.getLogger(KeyModel.class.getName()).log(Level.SEVERE, null,
					ex);
			return null;
		}

	}

	public static boolean setKeypairforUID(int UID, KeyPair keypair)
	{
		return setKeypairforUID(UID,
				SerializationUtils.serialize(keypair.getPublic()),
				SerializationUtils.serialize(keypair.getPrivate()));
	}

	public static boolean setKeypairforUID(int UID, byte[] publickey,
			byte[] privatekey)
	{
		// Abfrage ob der User schon exestiert. Wenn ja Update sonst insert!
		ResultSet rs = DBModel
				.executeQuery("SELECT * FROM keytable WHERE uid = "
						+ Integer.toString(UID) + ";");

		if (rs != null)
		{
			try
			{
				rs.last();

				if (rs.getRow() > 0)
				{
					// Gibt es schon
					PreparedStatement s = DBModel
							.getPreparedStatement("UPDATE keytable SET publickey=?, privatekey=? WHERE uid = "
									+ Integer.toString(UID) + ";");
					s.setBytes(1, publickey);
					s.setBytes(2, privatekey);
					s.executeUpdate();
				} else
				{
					// Gibt es nicht
					PreparedStatement s = DBModel
							.getPreparedStatement("INSERT INTO keytable (uid, publickey, privatekey)"
									+ "VALUES ("
									+ Integer.toString(UID)
									+ ", ?,?);");
					s.setBytes(1, publickey);
					s.setBytes(2, privatekey);
					s.execute();
				}
				return true;
			} catch (SQLException ex)
			{
				Logger.getLogger(KeyModel.class.getName()).log(Level.SEVERE,
						null, ex);
				return false;
			}
		}
		return false;
	}

}
