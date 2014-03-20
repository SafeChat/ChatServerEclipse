/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.zakath.chatserver.controller;

import de.zakath.chatserver.ServerCryptoProvider;
import de.zakath.chatserver.model.*;
import de.zakath.simplenetwork.*;
import de.zakath.simplenetwork.eventargs.*;
import java.util.*;

/**
 * 
 * @author cw
 */
public class MessageController extends BaseController
{

	private final MessageModel _model = new MessageModel();

	private final List<Integer> _conid = new ArrayList<>();

	private final Timer _timer;
	private final TimerTask _timertask;

	public MessageController(BaseServer server,
			ServerCryptoProvider cryptoprovider)
	{
		super(server, cryptoprovider);
		_timer = new Timer();
		_timertask = new TimerTask()
		{

			@Override
			public void run()
			{
				_model.moveMessagestoDB();
			}

		};
		_timer.scheduleAtFixedRate(_timertask, 1000 * 60 * 2, 1000 * 60 * 2); // Und
																				// den
																				// ganzen
																				// Task
																				// alle
																				// zwei
																				// Minuten
																				// durchführen
	}

	@Override
	public void OnClientConnected(Object sender, ClientConnectedEventArgs e)
	{
		// Bringt wenig, da keine ID feststeht zu diesem Zeitpunkt
	}

	@Override
	public void OnNewClientMessage(Object sender, NewClientMessageEventArgs e)
	{
		if (!e.getClient().isLocked())
		{
			if (e.getMessage().getType() == Message.MessageType.MessagePoll)
			{
				if (!_conid.contains(e.getClient().getID()))
				{
					_conid.add(e.getClient().getID());
				}
				// Hier alle Nachrichten die aufgelaufen sein könnten
				// abrufen...
				Message[] msgs = _model
						.getMessagesforUID(e.getClient().getID());
				for (Message m : msgs) // Und versenden
				{
					_server.sendToClient(e.getClient(), m);
				}

			}
			if (e.getMessage().getType() == Message.MessageType.Message)
			{
				e.getMessage().setSenderID(e.getClient().getID());
				int target = e.getMessage().getTargetID();
				if (isClientOnline(target)) // Ist der gewuenshcte Client online
											// die Nachricht direkt zustellen
				{
					_server.sendToClient(target, e.getMessage());
				} else
				// Sonst einreihen
				{
					_model.addMessage(e.getMessage());
				}

			}
		}
	}

	@Override
	public void OnConnectiontoClientLost(Object sender,
			ConnectiontoClientLostEventArgs e)
	{
		if (!e.getClient().isLocked())
		{
			_conid.remove(e.getClient().getID());
		}
	}

	protected boolean isClientOnline(int id)
	{
		for (int i : _conid)
		{
			if (i == id)
			{
				return true;
			}
		}
		return false;
	}

}
