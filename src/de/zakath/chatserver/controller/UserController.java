/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.zakath.chatserver.controller;

import de.zakath.chatserver.ServerCryptoProvider;
import de.zakath.chatserver.model.*;
import de.zakath.simplecrypt.*;
import de.zakath.simplecrypt.RSA.VerifyResult;
import de.zakath.simplenetwork.*;
import de.zakath.simplenetwork.eventargs.*;
import de.zakath.simplenetwork.misc.*;
import java.security.*;
import java.util.*;

/**
 * 
 * @author cw
 */
public class UserController extends BaseController
{

	private final SecureRandom sr;
	private final UserModel _model = new UserModel();

	public UserController(BaseServer server, ServerCryptoProvider cryptoprovider)
	{
		super(server, cryptoprovider);
		this.sr = new SecureRandom();
	}

	@Override
	public void OnClientConnected(Object sender, ClientConnectedEventArgs e)
	{
		System.out.println("User connected");

		Message auth = new Message(-1, 0, Message.MessageType.Authinit);
		Message innerauth = new Message(-1, 0, Message.MessageType.Authinit);

		byte[] secret = new byte[128];

		sr.nextBytes(secret);
		innerauth.setPayload(secret);

		auth.setPayload(_servercrypto.signwithServer(innerauth.toByteArray()));

		_model.addSecretEntry(e.getClient(), secret);
		_server.sendToClient(e.getClient(), auth);
	}

	@Override
	public void OnNewClientMessage(Object sender, NewClientMessageEventArgs e)
	{
		if (e.getMessage().getType() == Message.MessageType.Auth)
		{
			byte[] secret = _model.getSecret(e.getClient());
			int id = e.getMessage().getSenderID();
			RSA _rsa = new RSA(KeyModel.getPublicKeyforUID(id));

			if (!e.getMessage().hasPlayload())
			{
				sendErrorMessage(e.getClient(), "No payload found!");
				return;
			}

			VerifyResult r = _rsa.verify(_servercrypto.decryptwithServer(e
					.getMessage().getPayload()));
			if (r.isVerifyed() && r.Data() == secret)
			{
				// Der Client hat seine Identitaet bestaetigt
				e.getClient().setID(e.getMessage().getSenderID());
				Message m = new Message(-1, e.getClient().getID(),
						Message.MessageType.Authack);
				m.setField("Status", "Success");
				Message innerm = new Message(-1, e.getClient().getID(),
						Message.MessageType.Authack);

				// Der innerm ggf. noch daten hinzufügen
				innerm.setField("Timezone", Calendar.getInstance()
						.getTimeZone().getDisplayName());

				m.setPayload(_servercrypto.signwithServer(ServerCryptoProvider
						.encryptwithUID(innerm.toByteArray(), e.getClient()
								.getID())));

				e.getClient().setLocked(false);
				_server.sendToClient(e.getClient(), m);

			} else
			{
				// Der Client konnte sich nicht ausweisen
				Message m = new Message(-1, 0, Message.MessageType.Authack);
				m.setField("Status", "Failure");

				e.getClient().sendMessage(m);
				e.getClient().shutdown();
			}
		} else if (e.getMessage().getType() == Message.MessageType.NewAccountRequest)
		{
			if (!e.getMessage().hasPlayload())
			{
				sendErrorMessage(e.getClient(), "No payload found!");
				return;
			}

			Message innerm = Message.fromByteArray(_servercrypto.decryptwithServer(e.getMessage().getPayload()));
			String username = innerm.getField("username");
			String password = innerm.getField("password");

			DualByteKey dbk = (DualByteKey) SerializationUtils
					.deserialize(innerm.getPayload());
			if (dbk == null)
			{
				sendErrorMessage(e.getClient(), "Bad payload!");
			}

			int newuid = _model.addNewUser(username, password,
					dbk.getPublicKey(), dbk.getPrivateKey());

			if (newuid != -1)
			{
				Message r = new Message(-1, newuid,
						Message.MessageType.NewAccountResponse);
				Message inner = new Message(-1, newuid,
						Message.MessageType.NewAccountResponse);

				inner.setField("Status", "Success");
				r.setPayload(_servercrypto.signwithServer(ServerCryptoProvider
						.encryptwithUID(inner.toByteArray(), newuid)));
				e.getClient().sendMessage(r);
				e.getClient().shutdown();
			} else
			{
				Message r = new Message(-1, newuid,
						Message.MessageType.NewAccountResponse);
				Message inner = new Message(-1, newuid,
						Message.MessageType.NewAccountResponse);

				inner.setField("Status", "Failure");
				inner.setField("Reason", "Server intern");

				CombiCrypt cc = new CombiCrypt(
						(PublicKey) de.zakath.simplenetwork.SerializationUtils
								.deserialize(dbk.getPublicKey()));

				r.setPayload(_servercrypto.signwithServer(cc.decrypt(inner
						.toByteArray())));
				e.getClient().sendMessage(r);
				e.getClient().shutdown();
			}
		}
	}

	@Override
	public void OnConnectiontoClientLost(Object sender,
			ConnectiontoClientLostEventArgs e)
	{
		// Gaaaaanz viel Mitleid!
		System.out.println("User has lost connection");
	}

}
