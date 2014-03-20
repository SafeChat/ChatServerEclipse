/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.zakath.chatserver.model;

import de.zakath.simplenetwork.*;
import java.sql.*;
import java.util.*;
import java.util.logging.*;

/**
 *
 * @author cw
 */
public class UserModel
{

    private final HashMap<BaseClient, byte[]> _secretmap = new HashMap<>();

    public UserModel()
    {

    }

    public void addSecretEntry(BaseClient c, byte[] s)
    {
        if (_secretmap.containsKey(c))
        {
            _secretmap.remove(c);
        }
        _secretmap.put(c, s);
    }

    public byte[] getSecret(BaseClient c)
    {
        return _secretmap.get(c);
    }

    public int addNewUser(String username, String password, byte[] publickey, byte[] privatekey)
    {
        Statement s = DBModel.getStatement();
        int uid;
        try
        {
            String querystring = "INSERT INTO usertable (username, password) VALUES ('" + username + "', '" + password + "');";
            s.executeUpdate(querystring, Statement.RETURN_GENERATED_KEYS);
            ResultSet rs = s.getGeneratedKeys();
            rs.next();
            uid = rs.getInt(1);
        } catch (SQLException ex)
        {
            Logger.getLogger(UserModel.class.getName()).log(Level.SEVERE, null, ex);
            return -1;
        }
        if (!KeyModel.setKeypairforUID(uid, publickey, privatekey))
        {
            //Fehler, alles rückabwickeln:
            DBModel.executeUpdate("DELETE FROM usertable WHERE id = " + Integer.toString(uid) + ";");
            return -1;
        }
        return uid;

    }

}
