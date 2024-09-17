document.addEventListener("DOMContentLoaded", function (event) {
  // restart login
  forceRestartLogin();
});

// When the user clicks on the button, open the modal
function showInstructionsModal() {
  var modal = document.getElementById("instructions-modal");
  modal.style.display = "block";
}

function hideInstructionsModal() {
  var modal = document.getElementById("instructions-modal");
  modal.style.display = "none";
}

// When the user clicks anywhere outside of the modal, close it
window.onclick = function (event) {
  var modal = document.getElementById("instructions-modal");
  if (event.target == modal) {
    modal.style.display = "none";
  }
};

function forceRestartLogin() {
  var restartLoginLink = document.getElementById("reset-login");
  var otpInput = document.getElementById("otp");
  if (restartLoginLink && !otpInput) {
    window.location.href = restartLoginLink.href;
  }
}
