function togglePassword() {
  var x = document.getElementById("password");
  if (x.type === "password") {
    x.type = "text";
  } else {
    x.type = "password";
  }
}

function positionAtEnd() {
	var x = document.getElementById("username");
	x.selectionStart = x.selectionEnd = x.value.length;
}