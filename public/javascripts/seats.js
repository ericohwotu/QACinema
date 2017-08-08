let host = "";
let callback;
let days;
let hours;

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
            dates.push("blank")
        } else {
            dates.push(today + " " + months[month].toUpperCase() + " " + (year + 1900));
        }
    }

    if(dates[0] === "blank") {dates.shift();}
    return dates;
}

function getTimes(){
    let date = new Date();
    let timeNow = date.getHours();
    let times = [];
    let start = 0;

    if (days.length===6) {start = 1;}

    for (let i = start; i<7; i++){
        let sub = [];
        for (let j=0; j<24; j += 3){
            if(i === 0 && j < timeNow) {
                sub.push(0);
            } else {
                sub.push(j + ":00");
            }
        }
        times.push(sub)
    }
    // if(!times[0]) {times.shift();}
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
    let tallies = document.getElementsByClassName("tally");

    for(let i=0; i<tallies.length; i++){
        tallies[i].removeAttribute("disabled");
        tallies[i].classList.remove("disabled");
    }
}

function enableScreens(){
    let screens = document.getElementById("screens");
    let total = getStandardTicketCount() + getVipTicketCount();

    screens.removeAttribute("disabled");
    screens.classList.remove("disabled");

    //if(total>0)refresh();
    enableTable();
}
function popTimes(day){
    let timesOptions = document.getElementById("times");
    let total = getStandardTicketCount() + getVipTicketCount();

    timesOptions.innerHTML = "";
    timesOptions.removeAttribute("disabled");
    timesOptions.classList.remove("disabled");

    for(let i = 0; i< hours[day].length; i+=3){
        if(hours[day][i] !== 0 || hours[day][i]){
            let opt = document.createElement("option");
            opt.value = i;
            opt.innerHTML = hours[day][i];
            timesOptions.appendChild(opt);
        }
    }
    //if(total>0)refresh()
    enableScreens();
}

function popDates(){
    let daysOptions = document.getElementById("days");
    for(let i = 0; i< days.length; i++){
        let opt = document.createElement("option");
        opt.value = i;
        opt.innerHTML = days[i];
        daysOptions.appendChild(opt);
    }
    popTimes(0);
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
    let elems = document.getElementsByClassName("standard");

    for (let i = 0; i < elems.length; i++) {
        elems[i].removeAttribute("disabled");
    }

    refresh();
}

function enableVip() {
    clearInterval(callback);
    callback = setInterval(refresh, 5000);
    let elems = document.getElementsByClassName("vip");

    for (let i = 0; i < elems.length; i++) {
        elems[i].removeAttribute("disabled");
    }

    refresh();
}

function changeSeatColor(elem, json) {
    elem.classList.remove("available");
    elem.classList.remove("booked");

    if (json.outcome === "failure") {
        elem.classList.add("unavailable");
    } else if (json.outcome === "success" && json.message === "seat booked") {
        elem.classList.add("booked");
    } else {
        elem.classList.add("available");
    }

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

function isSeatLimitReached(){
    let bookedCount = document.getElementsByClassName("booked").length;
    let submitBooking = document.getElementById("submit-booking");

    let total = getStandardTicketCount() + getVipTicketCount();


    if(bookedCount !== total || total === 0) {
        submitBooking.setAttribute("disabled", "true");
    } else {
        submitBooking.removeAttribute("disabled");
    }

    return bookedCount >= total;
}



function disableStandard() {
    let elems = document.getElementsByClassName("standard");

    for (let i = 0; i < elems.length; i++) {
        if(!elems[i].classList.contains("booked")) {
            elems[i].setAttribute("disabled", "true");
            elems[i].classList.remove("available");
            elems[i].classList.add("unavailable");
        }
    }
    clearInterval(callback);
}

function disableVip(){
    let elems = document.getElementsByClassName("vip");

    for (let i = 0; i < elems.length; i++) {
        if(!elems[i].classList.contains("booked")) {
            elems[i].setAttribute("disabled", "true");
            elems[i].classList.remove("available");
            elems[i].classList.add("unavailable");
        }
    }
    clearInterval(callback);
}



function setVipButton(seat){
    seat.classList.remove("standard");
    seat.classList.remove("vip");
    seat.classList.add("vip");
}

function setDisButton(seat){
    seat.classList.remove("dis");
    seat.classList.add("dis");
}

function updateButton(json) {
    let elem = document.getElementById("seat-" + json.seatid);
    elem.classList.remove("standard");
    elem.classList.add("standard");
    elem.classList.remove("available");
    elem.classList.remove("booked");
    elem.classList.remove("unavailable");

    if (json.available === "true") {
        elem.classList.add("available");
    } else if (json.available === "false" && json.bookedBy === "true") {
        elem.classList.add("booked");
    } else {
        elem.classList.add("unavailable");
        elem.setAttribute("disabled", "true");
    }

    if (json.type === "VIP"){
        setVipButton(elem);
    }
    if (json.type === "DISABLED"){
        setDisButton(elem);
    }
    if (json.type === "EMPTY"){
        elem.classList.remove("standard");
        elem.classList.add("empty");
        elem.setAttribute("disabled", "true");
    }
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
    window.location.href = "/bookings/topayment?amount=" + getTotal();
}

