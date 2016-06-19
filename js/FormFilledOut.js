function formFilledOut() {
    var elements = document.getElementById("f").elements;
    var formFilled = true;
    var missingString = "";

    // go through all form elements EXCEPT the button elements
    for (var i = 0, element; i < elements.length - 1; i++) {
        element = elements[i].value;
        if ((element === "" || element === null) 
        	&& elements[i].type.localeCompare("submit")) {
            missingString += "Missing entry at box" + (i + 1) + "\n";
            formFilled = false;
        }
    }
    if (!formFilled) {
        window.alert(missingString);
    }
    return formFilled;
}
