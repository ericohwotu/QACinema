/*global google*/

let Center=new google.maps.LatLng(53.474140,-2.286074);
let directionsDisplay;
let directionsService = new google.maps.DirectionsService();
let distanceService = new google.maps.DistanceMatrixService();

let gMap;
let latitude = 0.0;
let longitude = 0.0;
let markers = [];
let infoWindow;

function distCallback(response, status) {
    if(status === "OK") {
        for(let i = 0; i < response.rows[0].elements.length; i++) {
            let cinema = document.getElementById("cinemaMode").options[i];
            if(cinema.text.includes("|")) {
                cinema.text = cinema.text.substring(0, cinema.text.indexOf("|") -1 );
            }
            cinema.text = cinema.text + " | " + response.rows[0].elements[i].distance.text;
        }
    }
}

function distance(latitude, longitude) {
    let origin = [new google.maps.LatLng(latitude, longitude)];
    let theDestinations = [];
    let selectedMode = document.getElementById("travelMode").value;

    for(let i = 0; i < document.getElementById("cinemaMode").options.length; i++) {
        let coords = document.getElementById("cinemaMode").options[i].value.split(":");
        theDestinations.push(new google.maps.LatLng(coords[0], coords[1]));
    }

    distanceService.getDistanceMatrix(
        {
            origins: origin,
            destinations: theDestinations,
            travelMode: google.maps.TravelMode[selectedMode],
        }, distCallback);
}

function route(latitude, longitude) {
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

function createMarker(place) {
    let markerIcon = {
        url: place.icon,
        scaledSize: new google.maps.Size(25,25),
        origin: new google.maps.Point(0,0),
        anchor: new google.maps.Point(0,0)
    };

    let marker = new google.maps.Marker({
        map: gMap,
        position: place.geometry.location,
        icon: markerIcon
    });
    let zoom = gMap.getZoom();
    marker.setVisible(zoom <= 16);

    markers.push(marker);
    google.maps.event.addListener(marker, "click", function() {
        infoWindow.setContent("<div class=\"text-primary\"><b>" + place.name + "</b>" + "<br>" + place.vicinity + "</div>");
        infoWindow.open(gMap, this);
    });
}

function placeCallback(response, status) {
    if (status === google.maps.places.PlacesServiceStatus.OK) {
        for (let i = 0; i < response.length; i++) {
            createMarker(response[i]);
        }
    }
}

function places() {
    let placeService = new google.maps.places.PlacesService(gMap);
    let endPoint = document.getElementById("cinemaMode").value.split(":");
    let end = new google.maps.LatLng(parseFloat(endPoint[0]),parseFloat(endPoint[1]));

    for(let i = 0; i < markers.length; i++) {
        markers[i].setMap(null);
    }

    placeService.nearbySearch({
        location: end,
        openNow: true,
        rankBy: google.maps.places.RankBy.DISTANCE,
        type: ["store"]
    }, placeCallback);
}

function getPosition(position) {
    latitude = position.coords.latitude;
    longitude = position.coords.longitude;
    route(latitude, longitude);
    distance(latitude, longitude);
    places();
}

function geolocation() {
    if (navigator.geolocation) {
        navigator.geolocation.getCurrentPosition(getPosition);
    } else {
        alert("Geolocation is not supported by this browser\nPlease select your location");
    }
}

function initialize() {
    let input = document.getElementById("searchBar");
    let autocomplete = new google.maps.places.Autocomplete(input);

    infoWindow = new google.maps.InfoWindow();
    directionsDisplay = new google.maps.DirectionsRenderer();

    // Set the values of the map properties
    let properties = {
        center:Center,
        zoom:20,
        mapTypeId:google.maps.MapTypeId.Map
    };

    //create the map with the properties created
    gMap=new google.maps.Map(document.getElementById("map"), properties);

    //Assign the map to a panel to display on
    directionsDisplay.setMap(gMap);
    directionsDisplay.setPanel(document.getElementById("rightPanel"));

    gMap.addListener("click", function(e) {
        latitude = e.latLng.lat();
        longitude = e.latLng.lng();
        route(latitude, longitude);
        distance(latitude, longitude);
        places();
    });

    document.getElementById("travelMode").addEventListener("change", function() {
        route(latitude, longitude);
        distance(latitude, longitude);
    });

    document.getElementById("cinemaMode").addEventListener("change", function() {
       route(latitude, longitude);
       distance(latitude, longitude);
       places();
    });

    autocomplete.addListener("place_changed", function() {
        let place = autocomplete.getPlace();
        let location = place.geometry.location;
        latitude = location.lat();
        longitude = location.lng();
        route(latitude, longitude);
        distance(latitude, longitude);
        places();
    });

    google.maps.event.addListener(gMap, "zoom_changed", function() {
        let zoom = gMap.getZoom();
        for (let i = 0; i < markers.length; i++) {
            markers[i].setVisible(zoom <= 16);
        }
    });

    geolocation();
}

google.maps.event.addDomListener(window,"load",initialize);