/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.zakath.chatserver;

import de.zakath.chatserver.model.*;
import de.zakath.simplecrypt.*;
import java.security.*;

/**
 *
 * @author cw
 */
public class ServerCryptoProvider
{

    private KeyPair _serverkey;
    private CombiCrypt _crypt;

    public ServerCryptoProvider(KeyPair keypair)
    {
        _serverkey = keypair;
        _crypt = new CombiCrypt(keypair);
    }

    public byte[] encryptwithServer(byte[] input)
    {
        return _crypt.encrypt(input);
    }

    public byte[] decryptwithServer(byte[] input)
    {
        return _crypt.decrypt(input);
    }

    public byte[] signwithServer(byte[] input)
    {
        return _crypt.getRSA().sign(input);
    }

    public RSA.VerifyResult verifywithServer(byte[] input)
    {
        return _crypt.getRSA().verify(input);
    }

    public static byte[] encryptwithUID(byte[] input, int UID)
    {
        CombiCrypt cc = new CombiCrypt(KeyModel.getPublicKeyforUID(UID));
        return cc.encrypt(input);
    }

    public static byte[] verifywithUID(byte[] input, int UID)
    {
        RSA rsa = new RSA(KeyModel.getPublicKeyforUID(UID));
        return rsa.sign(input);
    }
    
    public KeyPair getServerKeyPair()
    {
    	return _serverkey;
    }

}
