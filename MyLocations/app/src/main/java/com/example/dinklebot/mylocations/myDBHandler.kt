package com.example.dinklebot.mylocations

/* ********************************* */
/* Program: My Locations */
/* Description: Class myDBHandler handles creating and querying of database*/
/* Author: Robert Leslie */
/* Notes: */
/* Reference: */
/* Last Modified: 6/8/2018 */
/* ********************************** */

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class myDBHandler(context: Context,
                  name: String?,
                  factory: SQLiteDatabase.CursorFactory?, version: Int) :
        SQLiteOpenHelper(context, DATABASE_NAME, factory, DATABASE_VERSION) {
    //creates the sql tables for the app
    override fun onCreate(db: SQLiteDatabase) {
        val CREATE_USER_TABLE = ("CREATE TABLE "
                + TABLE_USERS
                + "("
                + COLUMN_ID
                + " INTEGER PRIMARY KEY,"
                + COLUMN_USERNAME
                + " TEXT,"
                + COLUMN_PASSWORD
                + " TEXT)")
        val CREATE_LOCATION_TABLE = ("CREATE TABLE " + TABLE_LOCATION
                + "(" + COLUMN_ID + " INTEGER PRIMARY KEY," + COLUMN_USERNAME
                + " TEXT," + COLUMN_LAT + " TEXT," + COLUMN_LONG
                + " TEXT," + COLUMN_LOCATION_NAME + " TEXT," + COLUMN_RATING
                + " TEXT," + COLUMN_TYPE + " TEXT," + COLUMN_DESC + " TEXT)")
        val CREATE_MENU_TABLE = ("CREATE TABLE " + TABLE_MENU + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY," + COLUMN_USERNAME
                + " TEXT," + COLUMN_LOCATION_NAME + " TEXT," + COLUMN_APP + " TEXT,"
                + COLUMN_ENTREE + " TEXT," + COLUMN_DRINK + " TEXT," + COLUMN_DESSERT
                + " TEXT)")
        db.execSQL(CREATE_USER_TABLE)
        db.execSQL(CREATE_LOCATION_TABLE)
        db.execSQL(CREATE_MENU_TABLE)
    }

    //will update the tables if it is given a new value
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_LOCATION")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_MENU")
        onCreate(db)
    }

    //checks if username is in the databse and if not it adds the username and password to the user table
    fun addUser(username: String, password: String): Boolean{
        if(findUser(username) == "") {
            val values = ContentValues()
            values.put(COLUMN_USERNAME, username)
            values.put(COLUMN_PASSWORD, password)
            val db = this.writableDatabase
            db.insert(TABLE_USERS, null, values)
            db.close()
            return true
        }else{
            return false
        }
    }

    //checks if the username is in the username table and returns the password stored
    fun findUser(username: String): String? {
        val query = ("Select * FROM " + TABLE_USERS + " WHERE "
                + COLUMN_USERNAME + " = \"" + username + "\"")
        val db = this.writableDatabase
        val cursor = db.rawQuery(query, null)
        var password = ""
        if(cursor.moveToFirst()){
            password = cursor.getString(2)
            cursor.close()
        }
        db.close()
        return password
    }

    //adds location data to the location table
    fun addLocation(extras: Array<String>){
        val values = ContentValues()
        values.put(COLUMN_USERNAME, extras[0])
        values.put(COLUMN_LAT, extras[1])
        values.put(COLUMN_LONG, extras[2])
        values.put(COLUMN_LOCATION_NAME, extras[3])
        values.put(COLUMN_RATING, extras[4])
        values.put(COLUMN_TYPE, extras[5])
        values.put(COLUMN_DESC, extras[6])
        val db = this.writableDatabase
        db.insert(TABLE_LOCATION, null, values)
        db.close()
    }

    //adds menu data to the menu table
    fun addMenu(extras: Array<String>){
        val values = ContentValues()
        values.put(COLUMN_USERNAME, extras[0])
        values.put(COLUMN_LOCATION_NAME, extras[3])
        values.put(COLUMN_APP, extras[7])
        values.put(COLUMN_ENTREE, extras[8])
        values.put(COLUMN_DRINK, extras[9])
        values.put(COLUMN_DESSERT, extras[10])
        val db = this.writableDatabase
        db.insert(TABLE_MENU, null, values)
        db.close()
    }

    //retrieves information needed for displayMarkers function using class MarkerData as data containers
    //returns an array of those markerData containers
    fun markerData(username: String): ArrayList<MarkerData>{
        val query = ("Select * FROM " + TABLE_LOCATION + " WHERE "
                + COLUMN_USERNAME + " = \"" + username + "\"")
        val db = this.writableDatabase
        val cursor = db.rawQuery(query, null)
        val dataAry = ArrayList<MarkerData>()
        var data: MarkerData? = null
        if(cursor.moveToFirst()){
            for (i in 1..cursor.count) {
                data = MarkerData(cursor.getString(4), cursor.getString(2),
                        cursor.getString(3), cursor.getString(7))
                dataAry.add(data)
                cursor.moveToNext()
            }
        }
        cursor.close()
        db.close()
        return dataAry
    }

    //retrieves the information for restaurants menu items, returns an array of arrays of strings
    //might want to add latlng information to menu table to make sure we are getting this location's menu
    fun getRestInfo(username: String, title: String, lat: String, long: String): Array<Array<String>> {
        var query= ("Select * FROM " + TABLE_LOCATION + " WHERE "
                + COLUMN_USERNAME + " = \"" + username + "\" AND " + COLUMN_LOCATION_NAME
                + " = \"" + title + "\" AND " + COLUMN_LAT + " = \"" + lat + "\" AND " + COLUMN_LONG + " = \"" + long + "\"")
        val db = this.writableDatabase
        var cursor = db.rawQuery(query, null)
        var values: Array<Array<String>>? = null
        if(cursor.moveToFirst()){
            val rating = cursor.getString(5)
            val desc = cursor.getString(7)
            values = arrayOf(arrayOf(rating, desc))
        }
        query = ("Select * FROM " + TABLE_MENU + " WHERE "
                + COLUMN_USERNAME + " = \"" + username + "\" AND " + COLUMN_LOCATION_NAME
                + " = \"" + title + "\"")
        cursor = db.rawQuery(query, null)
        var appetizer: String? = null
        var entree: String? = null
        var drink: String? = null
        var dessert: String? = null
        if(cursor.moveToFirst()){
            for(i in 0 until cursor.count){
                appetizer = cursor.getString(3)
                entree = cursor.getString(4)
                drink = cursor.getString(5)
                dessert = cursor.getString(6)
                values = values!!.plus(arrayOf(appetizer, entree, drink, dessert))
                cursor.moveToNext()
            }
        }
        cursor.close()
        db.close()
        return values!!
    }

    //method to request business information from database
    fun getBusinessInfo(username: String, title: String, lat: String, long: String): Array<String> {
        val query = ("Select * FROM " + TABLE_LOCATION + " WHERE "
                + COLUMN_USERNAME + " = \"" + username + "\" AND " + COLUMN_LOCATION_NAME
                + " = \"" + title + "\" AND " + COLUMN_LAT + " = \"" + lat + "\" AND " + COLUMN_LONG + " = \"" + long + "\"")
        val db = this.writableDatabase
        val cursor = db.rawQuery(query, null)
        var values: Array<String>? = null
        if(cursor.moveToFirst()){
            val rating = cursor.getString(5)
            val desc = cursor.getString(7)
            values = arrayOf(rating, desc)
        }
        cursor.close()
        db.close()
        return values!!
    }

    //function to remove restaurant information from the database
    //might want to add latlng to menu table to make deleting its information easier
    fun removeRest(extras: Array<String>){
        val db = this.writableDatabase
        db.delete(TABLE_LOCATION, COLUMN_LOCATION_NAME + " = ? AND " + COLUMN_USERNAME
                + " = ? AND "  + COLUMN_LAT + " = ? AND " + COLUMN_LONG + " = ?", extras)
        val query = ("Select * FROM " + TABLE_LOCATION + " WHERE " + COLUMN_LOCATION_NAME
                + " = \"" + extras[0] + "\"")
        val cursor = db.rawQuery(query, null)
        if(!cursor.moveToFirst()){
            db.delete(TABLE_MENU, COLUMN_LOCATION_NAME + " = ? AND " + COLUMN_USERNAME
            + "= ?", arrayOf(extras[0], extras[1]))
        }
        cursor.close()
        db.close()
    }

    //funtion to remove business information from the database
    fun removeBusiness(extras: Array<String>){
        val db = this.writableDatabase
        db.delete(TABLE_LOCATION, COLUMN_LOCATION_NAME + " = ? AND " + COLUMN_USERNAME
                + " = ? AND "  + COLUMN_LAT + " = ? AND " + COLUMN_LONG + " = ?", extras)
    }

    //function to retrieve the other user names
    fun retrieveShared(username: String): Array<String>?{
        val db = this.writableDatabase
        val query = ("Select * FROM " + TABLE_USERS + " WHERE " + COLUMN_USERNAME + " != \"" + username + "\"")
        var result: Array<String>? = null
        val cursor = db.rawQuery(query, null)
        if(cursor.moveToFirst()){
            result = Array(cursor.count){""}
            for (i in 0 until cursor.count){
                result[i] = cursor.getString(1)
            }
        }
        cursor.close()
        db.close()
        return result
    }

    //function to find out the location type
    fun getType(username: String, lat: String, long: String): String{
        val db = this.writableDatabase
        val query = ("Select * FROM " + TABLE_LOCATION + " WHERE " + COLUMN_USERNAME + " = \"" + username + "\" AND "
                + COLUMN_LAT + " = \"" + lat + "\" AND " + COLUMN_LONG + " = \"" + long + "\"")
        val cursor = db.rawQuery(query, null)
        var result: String? = null
        if(cursor.moveToFirst()){
            result = cursor.getString(6)
        }
        cursor.close()
        db.close()
        return result!!
    }

    //companion object to store constants about the sql table
    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "user.db"
        const val TABLE_USERS = "users"
        const val COLUMN_ID = "_id"
        const val COLUMN_USERNAME = "userName"
        const val COLUMN_PASSWORD = "password"

        const val TABLE_LOCATION = "location"
        const val COLUMN_LAT = "latitude"
        const val COLUMN_LONG = "longitude"
        const val COLUMN_LOCATION_NAME = "locationName"
        const val COLUMN_RATING = "rating"
        const val COLUMN_TYPE = "type"
        const val COLUMN_DESC = "description"

        const val TABLE_MENU = "menu"
        const val COLUMN_APP = "appetizer"
        const val COLUMN_ENTREE = "entree"
        const val COLUMN_DRINK = "drink"
        const val COLUMN_DESSERT = "dessert"
    }
}