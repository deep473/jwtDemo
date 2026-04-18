function ensureCustomer() {
  const token = localStorage.getItem("token");
  const role = localStorage.getItem("role");
  const username = localStorage.getItem("username");

  if (!token || (role !== "USER" && role !== "ADMIN")) {
    window.location.href = "/login.html";
    return false;
  }

  document.getElementById("welcomeText").innerText = `Welcome, ${username}`;
  return true;
}

async function loadCustomerData() {
  const token = localStorage.getItem("token");

  const response = await fetch("/customer/home", {
    headers: {
      "Authorization": "Bearer " + token
    }
  });

  const text = await response.text();
  document.getElementById("apiResult").innerText = text;
}

function logout() {
  localStorage.removeItem("token");
  localStorage.removeItem("username");
  localStorage.removeItem("role");
  window.location.href = "/index.html";
}

document.addEventListener("DOMContentLoaded", ensureCustomer);