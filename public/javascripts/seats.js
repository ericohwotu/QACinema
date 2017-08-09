let host = "";
let callback;
let days;
let hours;

// function for getting class array
function getClassArray(className) {
    return Array.prototype.slice.call(document.getElementsByClassName(className));
}
// functions to increase security and reduce code duplication
function isElemBooked(elem){return elem.classList.contains("booked");}
function makeElemDisabled(elem){elem.setAttribute("disabled", "true");}
function clearElemDisabled(elem){elem.removeAttribute("disabled");}
function clearElemClassDisabled(elem){elem.classList.remove("disabled");}

function makeElemUnavailable(elem){
    elem.classList.remove("available");
    elem.classList.add("unavailable");
}
function makeElemBooked(elem){
    elem.classList.remove("unavailable");
    elem.classList.add("booked");
}
function clearElemBooked(elem){elem.classList.remove("booked");}
function makeElemVip(elem){elem.classList.add("vip");}
function clearElemVip(elem){elem.classList.remove("vip");}
function makeElemStandard(elem){elem.classList.add("standard");}
function clearElemStandard(elem){elem.classList.remove("standard");}
function makeElemEmpty(elem){elem.classList.add("empty");}
function makeElemDisability(elem){elem.classList.add("dis");}
function clearElemDisability(elem){elem.classList.remove("dis");}
function clearElemUnavailable(elem){elem.classList.remove("unavailable");}

function makeElemAvailable(elem){
    elem.classList.add("available");
    elem.classList.remove("unavailable");
}

//multi element functions
function clearElemsDisabled(elems){
    elems.forEach(function(elem){
        clearElemDisabled(elem);
    });
}

function getSelectedText(elementId) {
    let elem = document.getElementById(elementId);

    if (elem.selectedIndex === -1) {
        return null;
    }

    return elem.options[elem.selectedIndex].text;
}

function getDays(){

    let dates = [];
    let months = ["jan","feb","mar","apr","may","jun","jul","aug","sep","oct","nov","dec"];

    for (let i=0; i<7; i++) {
        let date = new Date();
        date.setDate(date.getDate() + i);
        let today = date.getDate();
        let month = date.getMonth();
        let year = date.getYear();
        if(date.getDate()=== new Date().getDate() && date.getHours() > 21) {
            continue;
        } else {
            dates.push(today + " " + months[month].toUpperCase() + " " + (year + 1900));
        }
    }

    if(dates[0] === "blank") {dates.shift();}
    return dates;
}

function getSubTimes(i, timeNow){
    let sub = [];
    for (let j=0; j<24; j += 3){
        if(i === 0 && j < timeNow) {
            continue;
        } else {
            sub.push(j + ":00");
        }
    }
    return sub;
}

function getTimes(){
    let date = new Date();
    let timeNow = date.getHours();
    let times = [];
    let start = 0;

    if (days.length===6) {start = 1;}

    for (let i = start; i<7; i++){
        let sub = getSubTimes(i, timeNow);
        times.push(sub);
    }
    return times;
}

function getStandardTicketCount(){
    let sAdult = +document.getElementById("standard-adult").value;
    let sStudent = +document.getElementById("standard-student").value;
    let sChild = +document.getElementById("standard-child").value;

    return sAdult + sStudent + sChild;
}

function getVipTicketCount(){
    let vAdult = +document.getElementById("vip-adult").value;
    let vStudent = +document.getElementById("vip-student").value;
    let vChild = +document.getElementById("vip-child").value;

    return vAdult + vStudent + vChild;
}

function isSeatLimitReached(){
    let bookedCount = document.getElementsByClassName("booked").length;
    let submitBooking = document.getElementById("submit-booking");

    let total = getStandardTicketCount() + getVipTicketCount();

    if(bookedCount !== total || total === 0) {
        makeElemDisabled(submitBooking);
    } else {
        clearElemDisabled(submitBooking);
    }

    return bookedCount >= total;
}

function isStandardLimitReached(){

    let bookedCount = document.getElementsByClassName("standard booked").length;

    isSeatLimitReached();
    return bookedCount >= getStandardTicketCount();
}

function isVipLimitReached(){
    let bookedCount = document.getElementsByClassName("vip booked").length;
    isSeatLimitReached();
    return bookedCount >= getVipTicketCount();
}

function enableTable(){
    let tallies = getClassArray("tally");

    tallies.forEach(function(tally){
        clearElemDisabled(tally);
        clearElemClassDisabled(tally);
    });
}

function enableScreens(){
    let screens = document.getElementById("screens");
    let total = getStandardTicketCount() + getVipTicketCount();

    clearElemDisabled(screens);
    clearElemClassDisabled(screens);

    refresh();

    enableTable();
}
function popTimes(day){
    let timesOptions = document.getElementById("times");
    let total = getStandardTicketCount() + getVipTicketCount();
    let curHours = hours[day];

    timesOptions.innerHTML = "";
    clearElemDisabled(timesOptions);
    clearElemClassDisabled(timesOptions);

    for(let i = 0; i< curHours.length; i+=1){
        if(curHours[i] !== 0 || curHours[i]){
            let opt = document.createElement("option");
            let text = document.createTextNode(curHours[i]);
            opt.value = i;
            opt.appendChild(text);
            timesOptions.appendChild(opt);
        }
    }

    enableScreens();
}

function popDates(){
    let daysOptions = document.getElementById("days");
    for(let i = 0; i< days.length; i++){
        let opt = document.createElement("option");
        let text = document.createTextNode(days[i]);
        opt.value = i;
        opt.appendChild(text);
        daysOptions.appendChild(opt);
    }
    popTimes(0);
}

function setVipButton(seat){
    clearElemStandard(seat);
    clearElemVip(seat);
    makeElemVip(seat);
}

function setDisButton(seat){
    clearElemDisability(seat);
    makeElemDisability(seat);
}

function updateButtonHelper(json, elem){
    if (json.available === "true") {
        makeElemAvailable(elem);
    } else if (json.available === "false" && json.bookedBy === "true") {
        makeElemBooked(elem);
    } else {
        makeElemUnavailable(elem);
        makeElemDisabled(elem);
    }
}

function updateButton(json) {
    let elem = document.getElementById("seat-" + json.seatid);
    clearElemStandard(elem);
    makeElemStandard(elem);
    elem.classList.remove("available");
    clearElemUnavailable(elem);
    clearElemBooked(elem);

    updateButtonHelper(json,elem);

    if (json.type === "VIP"){
        setVipButton(elem);
    }
    if (json.type === "DISABLED"){
        setDisButton(elem);
    }
    if (json.type === "EMPTY"){
        clearElemStandard(elem);
        makeElemEmpty(elem);
        makeElemDisabled(elem);
    }
}

function disableHelper(elems){
    elems.forEach(function(elem){
        if(!isElemBooked(elem)) {
            makeElemDisabled(elem);
            makeElemUnavailable(elem);
        }
    });
}

function disableStandard() {
    let elems = getClassArray("standard");
    disableHelper(elems);
    clearInterval(callback);
}

function disableVip(){
    let elems = getClassArray("vip");

    disableHelper(elems);
    clearInterval(callback);
}

function updateButtons(arr) {
    for (let i = 0; i < arr.length; i++) {
        updateButton(arr[i]);
    }

    if(isStandardLimitReached()){
        disableStandard();
    }
    if(isVipLimitReached()){
        disableVip();
    }

}

function refresh() {
    //get an ajax call to book the seat
    let xhttp = new XMLHttpRequest();
    xhttp.onreadystatechange = function () {
        if (this.readyState === 4 && this.status === 200) {
            updateButtons(JSON.parse(this.response));
        }
    };
    xhttp.open("GET", "http://" + host + ":9000/bookings/getseats?date=" +
        getSelectedText("days") + "&time=" + getSelectedText("times"), true);
    xhttp.send();
}

window.onload = function () {
    host = window.location.hostname;
    days = getDays();
    hours = getTimes();
    popDates();
    refresh();
};

function enableStandard() {
    clearInterval(callback);
    callback = setInterval(refresh, 5000);
    let elems = getClassArray("standard");

    clearElemsDisabled(elems);

    refresh();
}

function enableVip() {
    clearInterval(callback);
    callback = setInterval(refresh, 5000);
    let elems = getClassArray("vip");

    clearElemsDisabled(elems);

    refresh();
}

function changeColorHelper(json, elem){
    if (json.outcome === "failure") {
        makeElemUnavailable(elem);
    } else if (json.outcome === "success" && json.message === "seat booked") {
        makeElemBooked(elem);
    } else {
        makeElemAvailable(elem);
    }

}
function changeSeatColor(elem, json) {
    clearElemBooked(elem);
    clearElemDisabled(elem);
    clearElemUnavailable(elem);
    clearElemStandard(elem);

    changeColorHelper(json, elem);

    if(isStandardLimitReached()){disableStandard();}
    else {enableStandard();}
    if(isVipLimitReached()){disableVip();}
    else {enableVip();}
}

function selectSeat(seatId) {
    let xhttp = new XMLHttpRequest();
    xhttp.onreadystatechange = function () {
        if (this.readyState === 4 && this.status === 200) {
            changeSeatColor(document.getElementById("seat-" + seatId), JSON.parse(this.response));
        }
    };
    xhttp.open("POST", "http://" + host + ":9000/bookings/bookseat?id=" + seatId
        + "&date=" + getSelectedText("days") + "&time=" + getSelectedText("times"), true);
    xhttp.send();
}

// ================================ Booking.js functions ============================== //
function getTotal(){
    let vAdult = document.getElementById("vip-adult").value * 18;
    let vStudent = document.getElementById("vip-student").value * 12;
    let vChild = document.getElementById("vip-child").value * 7;

    let sAdult = document.getElementById("standard-adult").value * 8;
    let sStudent = document.getElementById("standard-student").value * 6;
    let sChild = document.getElementById("standard-child").value * 3;

    let multiplier = +document.getElementById("screens").value;

    let total = vAdult + vStudent + vChild + sAdult + sChild + sStudent;

    document.getElementById("total-field").value = total * multiplier;

    if (isVipLimitReached()) {
        disableVip();
    }
    else {
        enableVip();
    }

    if (isStandardLimitReached()) {
        disableStandard();
    }
    else {
        enableStandard();
    }
    isSeatLimitReached();
    return total * multiplier;
}

function submitBookings(){
    alert("Booking has been made, you will now be redirected to the payment");
    window.location.assign("/bookings/topayment?amount=" + getTotal());
}

