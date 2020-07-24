package com.example.test


class GoogleMapDTO {
    var routes=ArrayList<Routes>()
}
class Routes{
    var legs=ArrayList<Legs>()
}
class Legs{
    var distance=Distance()
    var duration=Duration()
    var end_address=""
    var start_address=""
    var end_location=Location()
    var start_location=Location()
    var steps=ArrayList<Steps>()
    var arrival_time=Time()
    var departure_time=Time()
}

class Steps{
    var distance=Distance()
    var duration=Duration()
    var end_address=""
    var start_address=""
    var end_location=Location()
    var start_location= Location()
    var polyline=PolyLine()
    var travel_mode=""
    var maneuver=""
    var html_instructions=""
    var transit_details=Transit()
}
class Transit{
    var arrival_stop=Arrival()
    var arrival_time=Time()
    var departure_stop=Arrival()
    var departure_time=Time()
    var line=Line()
    var num_stops=0
}
class Time{
    var text=""
    var value=0
    var time_zone=""
}
class Line{
    var color=""
    var text_color=""
    var name=""
    var short_name=""
    var vehicle=Vehicle()
}
class Vehicle{
    var icon=""
    var name=""
    var type=""
}
class Arrival{
    var location=Location()
    var name=""
}
class Duration{
    var text=""
    var value=0
}
class Distance{
    var text=""
    var value=0
}
class PolyLine{
    var points=""
}
class Location{
    var lat=0.0
    var lng=0.0
}
