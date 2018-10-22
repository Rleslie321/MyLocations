package com.example.dinklebot.mylocations

/* ********************************* */
/* Program: My Locations */
/* Description: Class MarkerData to store information about markers for displayMarkers function*/
/* Author: Robert Leslie */
/* Notes: */
/* Reference: */
/* Last Modified: 6/8/2018 */
/* ********************************** */

//class to store all required marker data
class MarkerData{
    var location_name: String? = null
    var lat: String? = null
    var ln: String? = null
    var description: String? = null

    constructor(location_name: String, lat: String, ln: String, description: String){
        this.location_name = location_name
        this.lat = lat
        this.ln = ln
        this.description = description
    }
}