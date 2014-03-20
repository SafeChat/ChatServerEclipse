/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.zakath.chatserver.controller;

import de.zakath.chatserver.ServerCryptoProvider;
import de.zakath.simplenetwork.SerializationUtils;
import de.zakath.chatserver.model.*;
import de.zakath.simplenetwork.*;
import de.zakath.simplenetwork.eventargs.*;
import java.security.*;

/**
 *
 * @author cw
 */
public class KeyController extends BaseController
{
    
    public KeyController(BaseServer server, ServerCryptoProvider cryptoprovider)
    {
        super(server, cryptoprovider);
    }
    
    @Override
    public void OnNewClientMessage(Object sender, NewClientMessageEventArgs e)
    {
        if (!e.getClient().isLocked())
        {
            if (e.getMessage().getType() == Message.MessageType.KeyRequest)
            {
                if (!e.getMessage().hasPlayload())
                {
                    sendErrorMessage(e.getClient(), "No payload found!");
                    return;
                }
                Message m = Message.fromByteArray(_servercrypto.decryptwithServer(e.getMessage().getPayload()));
                if (!m.isFieldSet("RequestID"))
                {
                    sendErrorMessage(e.getClient(), "No request ID set");
                    return;
                }
                KeyPair kp = KeyModel.getPublicKeyforUID(Integer.parseInt(m.getField("RequestID")));
                Message re = new Message(-1, e.getClient().getID(), Message.MessageType.KeyResponse);
                re.setField("RequestID", m.getField("RequestID"));
                re.setPayload(_servercrypto.signwithServer(ServerCryptoProvider.encryptwithUID(SerializationUtils.serialize(kp), e.getClient().getID())));
                e.getClient().sendMessage(re);
            }
        }
    }
    
}
