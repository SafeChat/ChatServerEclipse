/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.zakath.chatserver.controller;

import de.zakath.chatserver.*;
import de.zakath.simplenetwork.*;
import de.zakath.simplenetwork.eventargs.*;
import de.zakath.simplenetwork.eventlistener.*;

/**
 *
 * @author cw
 */
public abstract class BaseController implements ClientConnectedListener, ConnectiontoClientLostListener, NewClientMessageListener
{

    protected final BaseServer _server;
    protected final ServerCryptoProvider _servercrypto;

    public BaseController(BaseServer server, ServerCryptoProvider cryptoprovider)
    {
        _server = server;
        _servercrypto = cryptoprovider;
        _server.addClientConnectedListener(this);
        _server.addConnectiontoClientLostListener(this);
        _server.addNewClientMessageListener(this);
    }

    protected void sendErrorMessage(BaseClient c, String msg)
    {
        Message errm = new Message(-1, c.getID(), Message.MessageType.Error);
        errm.setPayload(_servercrypto.signwithServer(msg.getBytes()));
        c.sendMessage(errm);
    }

    protected void sendErrorMessage(BaseClient c, Exception ex)
    {
        sendErrorMessage(c, ex.getMessage());
    }

    @Override
    public void OnConnectiontoClientLost(Object sender, ConnectiontoClientLostEventArgs e)
    {
    }

    @Override
    public void OnClientConnected(Object sender, ClientConnectedEventArgs e)
    {
    }
}
