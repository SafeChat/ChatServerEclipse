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
public class MessageModel
{

    private final List<MessageandTime> _msgs = new LinkedList<>();

    private boolean _lockcache = false;

    public Message[] getMessagesforUID(int uid)
    {
        Message[] cache = getMessagesforIDfromCache(uid);
        Message[] db = getMessagesforIDfromDB(uid);

        Message[] result = new Message[cache.length + db.length];

        System.arraycopy(cache, 0, result, 0, cache.length);
        System.arraycopy(db, 0, result, cache.length, db.length);
        return result;
    }

    public Message[] getMessagesforIDfromCache(int id)
    {
        List<MessageandTime> msg = new ArrayList<>();
        List<Message> output = new ArrayList<>();
        for (MessageandTime m : _msgs) // Erst alle Nachrichten holen...
        {
            if (m.getMessage().getTargetID() == id)
            {
                msg.add(m);
            }
        }
        for (MessageandTime m : msg) // Dann löschen..
        {
            _msgs.remove(m);
            output.add(m.getMessage()); // Und die Message rausholen
        }
        return output.toArray(new Message[output.size()]);

    }

    public Message[] getMessagesforIDfromDB(int id)
    {
        ResultSet rs = DBModel.executeQuery("SELECT * FROM messagetable WHERE targetid = " + Integer.toString(id) + ";");
        List<Message> output = new ArrayList<>();
        try
        {
            rs.last();
            if (rs.getRow() > 0)
            {
                rs.first();
                do
                {
                    output.add((Message) de.zakath.simplenetwork.SerializationUtils.deserialize(rs.getBytes("message")));
                } while (!rs.isLast());
                DBModel.executeUpdate("DELETE FROM messagetable WHERE targetid = " + Integer.toString(id) + ";");
                return output.toArray(new Message[output.size()]);
            } else
            {
                return new Message[0];
            }
        } catch (SQLException ex)
        {
            Logger.getLogger(MessageModel.class.getName()).log(Level.SEVERE, null, ex);
            return new Message[0];
        }

    }

    public void addMessagetoCache(Message m)
    {
        _msgs.add(new MessageandTime(m));
    }

    public void addMessage(Message m)
    {
        if (!_lockcache)
        {
            addMessagetoCache(m);
        } else
        {
            addMessagetoDB(m);
        }
    }

    public void moveMessagestoDB()
    {
        List<MessageandTime> _todelete = new ArrayList<>();
        for (MessageandTime m : _msgs) // Jeden Eintrag überprüfen...
        {
            if (System.currentTimeMillis() - m.getCreationTime() > 1000 * 15) // ...ob er älter als 15 minuten ist...
            {
                _todelete.add(m); //... ,der Liste zum löschen hinzufügen...
                addMessagetoDB(m.getMessage()); // ... und in die Datenbank schieben.
            }
        }
        for (MessageandTime m : _todelete) // Und alle Nachrichten löschen, die gemovt wurden
        {
            _msgs.remove(m);
        }
    }

    public void addMessagetoDB(Message m)
    {
        PreparedStatement s = DBModel.getPreparedStatement("INSERT INTO messagetable (targetid, message) VALUES("
                + Integer.toString(m.getTargetID()) + ", ?);");
        try
        {
            s.setBytes(1, de.zakath.simplenetwork.SerializationUtils.serialize(m));
            s.execute();
        } catch (SQLException ex)
        {
            Logger.getLogger(MessageModel.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void dumpAllMessagestoDB()
    {
        _lockcache = true;
        for (MessageandTime m : _msgs)
        {
            addMessagetoDB(m.getMessage());
        }
        _msgs.clear();
        _lockcache = false;
    }

    protected class MessageandTime
    {

        private final Message _m;
        private final long _t;

        public Message getMessage()
        {
            return _m;
        }

        public long getCreationTime()
        {
            return _t;
        }

        public MessageandTime(Message m)
        {
            _m = m;
            _t = System.currentTimeMillis();
        }
    }

}
