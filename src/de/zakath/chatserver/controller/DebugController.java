package de.zakath.chatserver.controller;


import de.zakath.chatserver.ServerCryptoProvider;
import de.zakath.simplenetwork.BaseServer;
import de.zakath.simplenetwork.eventargs.NewClientMessageEventArgs;


public class DebugController extends BaseController
{

	public DebugController(BaseServer server,
			ServerCryptoProvider cryptoprovider)
	{
		super(server, cryptoprovider);	
		
	}

	@Override
	public void OnNewClientMessage(Object sender, NewClientMessageEventArgs e)
	{
		// TODO Auto-generated method stub

	}

}
