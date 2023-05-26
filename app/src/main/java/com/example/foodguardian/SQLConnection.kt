package com.example.foodguardian

import android.os.StrictMode
import android.util.Log
import java.sql.Connection
import java.sql.DriverManager

class SQLConnection {
    private var connection: Connection? = null
    private var username: String = ""
    private var password: String = ""
    private var ip: String = ""
    private var port: String = ""
    private var database: String = ""

    fun connectionClass(): Connection? {
        ip = "ifridge.local"
        database = "ifridge"
        username = "dbuser"
        password = "Foodguardian"
        port = "3306"

        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
        var connection: Connection? = null
        var connectionURL: String? = null

        try {
            Class.forName("net.sourceforge.jtds.jdbc.Driver")
            connectionURL = "jdbc:jtds:sqlserver://$ip:$port;databasename=$database;user=$username;password=$password;"
            connection = DriverManager.getConnection(connectionURL)
        } catch (ex: Exception) {
            Log.e("Error", ex.message!!)
        }

        return connection
    }
}
