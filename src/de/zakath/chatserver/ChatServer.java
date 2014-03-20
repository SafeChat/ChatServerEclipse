/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.zakath.chatserver;

import de.zakath.chatserver.controller.*;
import de.zakath.simplecrypt.*;
import de.zakath.simplenetwork.*;

import java.io.*;
import java.security.*;
import java.security.spec.*;
import java.sql.*;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

/**
 * 
 * @author cw
 */
public class ChatServer
{

	/**
	 * @param args
	 *            the command line arguments
	 * @throws java.sql.SQLException
	 * @throws java.io.IOException
	 * @throws java.lang.ClassNotFoundException
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeySpecException
	 */
	public static void main(String[] args) throws SQLException, IOException,
			ClassNotFoundException
	{

		Security.addProvider(new BouncyCastleProvider());
		
		System.out.println("ChatServer has been started!");
		BaseServer server = new BaseServer(8000);

		System.out.println("Server initiated!");
		// Den Server Key hier laden!!
		InputStream publicis = ChatServer.class.getClassLoader()
				.getResourceAsStream("public.key");

		byte[] _publicbuffer = new byte[publicis.available()];
		publicis.read(_publicbuffer);

		InputStream privateis = ChatServer.class.getClassLoader()
				.getResourceAsStream("private.key");

		byte[] _privatebuffer = new byte[privateis.available()];

		privateis.read(_privatebuffer);

		KeyPair kp = null;
		try
		{
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");

			X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(
					_publicbuffer);
			PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);

			System.out.println("Key MD5: "
					+ Hex.encode(MD5.computeHash(publicKey.getEncoded())));
			System.out.println("Type: " + publicKey.getAlgorithm());

			PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(
					_privatebuffer);
			PrivateKey privateKey = keyFactory.generatePrivate(privateKeySpec);

			kp = new KeyPair(publicKey, privateKey);
		} catch (Exception ex)
		{
			System.err.println(ex.getLocalizedMessage());
			ex.printStackTrace();
		}
		System.out.println("Server key loaded!");

		ServerCryptoProvider scp = new ServerCryptoProvider(kp);

		System.out.println("Crypto provider initiated!");

		new DebugController(server, scp);

		new UserController(server, scp);
		System.out.println("UserController initiated und hooked up!");
		new KeyController(server, scp);
		System.out.println("KeyController initiated und hooked up!");
		new MessageController(server, scp);
		System.out.println("MessageController initiated und hooked up!");

		System.out.println("Start listening...");
		server.listen(false);

	}
}
