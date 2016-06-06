// Functions to help interactions between javascript and PHP

// will format a PHP array's JSON encoding so that it can be parsed by JS
function addEscapes(jsonString) {
    // 'for' will skip the first and last quotations
    for (var i = 1; i < jsonString.length - 1; i++) {
        if (jsonString[i] == "\"") {
            jsonString = jsonString.slice(0, i) +"\\"+ jsonString.slice(i, jsonString.length-1);
        }
    }
    return jsonString;
}
