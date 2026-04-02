package com.restrunner;

import com.restrunner.core.user.db.ConnectDB;
import com.restrunner.core.user.db.HistoryDB;
import com.restrunner.core.user.db.UserDB;
import com.restrunner.core.user.pojo.History;
import com.restrunner.core.user.pojo.User;

import javax.swing.*;

public class App {
    public static void main(String[] args) {
        try {
            UserDB userDB = UserDB.getInstance();
////            userDB.save(new User("User-123","user@gmail.com","token"));
//
//            System.out.println( UserDB.getInstance().getUser().get().toString());

            userDB.delete("user-123");

            for(History history : HistoryDB.getInstance().getLast30()) {
                System.out.println(history.toString());

            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
