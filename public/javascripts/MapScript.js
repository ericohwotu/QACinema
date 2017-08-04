/**
 * Created by Alex Rimmer on 02/08/2017.
 */

let Center=new google.maps.LatLng(53.474140,-2.286074);
let directionsDisplay;
let directionsService = new google.maps.DirectionsService();
let map;
let latitude = 0.0;
let longitude = 0.0;

function initialize() {
    let input = document.getElementById("searchBar");
    let autocomplete = new google.maps.places.Autocomplete(input);

    directionsDisplay = new google.maps.DirectionsRenderer();

    // Set the values of the map properties
    let properties = {
        center:Center,
        zoom:20,
        mapTypeId:google.maps.MapTypeId.Map
    };

    //create the map with the properties created
    map=new google.maps.Map(document.getElementById("map"), properties);

    //Assign the map to a panel to display on
    directionsDisplay.setMap(map);
    directionsDisplay.setPanel(document.getElementById("rightPanel"));

    getLocation();

    map.addListener("click", function(e) {
        latitude = e.latLng.lat();
        longitude = e.latLng.lng();
        Route(e.latLng.lat(), e.latLng.lng());
    });

    document.getElementById("travelMode").addEventListener("change", function() {
        Route(latitude, longitude);
    });

    document.getElementById("cinemaMode").addEventListener("change", function() {
       Route(latitude, longitude);
    });

    autocomplete.addListener("place_changed", function() {
        let place = autocomplete.getPlace();
        let location = place.geometry.location;
        latitude = location.lat();
        longitude = location.lng();
        Route(latitude, longitude);
    });
}

function getLocation() {
    if (navigator.geolocation) {
        navigator.geolocation.getCurrentPosition(getPosition);
    } else {
        alert("Geolocation is not supported by this browser\nPlease select your location")
    }
}

function getPosition(position) {
    latitude = position.coords.latitude;
    longitude = position.coords.longitude;
    Route(latitude, longitude);
}

function Route(latitude, longitude) {
    let selectedMode = document.getElementById("travelMode").value;
    let start = new google.maps.LatLng(latitude, longitude);
    let endPoint = document.getElementById("cinemaMode").value.split(":");
    let end = new google.maps.LatLng(parseFloat(endPoint[0]),parseFloat(endPoint[1]));

    let request = {
        origin:start,
        destination:end,
        travelMode:google.maps.TravelMode[selectedMode]
    };
    directionsService.route(request, function(result, status) {
        if (status === google.maps.DirectionsStatus.OK) {
            directionsDisplay.setDirections(result);
            document.getElementById("searchBar").value = result.routes[0].legs[0].start_address;
        } else {
            alert("Unable to create a path to the cinema from this location");
        }
    });
}
google.maps.event.addDomListener(window,"load",initialize);