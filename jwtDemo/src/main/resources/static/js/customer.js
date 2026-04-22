function ensureCustomer() {
  const token = localStorage.getItem("token");
  const role = localStorage.getItem("role");
  const username = localStorage.getItem("username");

  if (!token || (role !== "USER" && role !== "ADMIN")) {
    window.location.replace("/login.html");
    return false;
  }

  const welcome = document.getElementById("welcomeText");
  if (welcome) {
    welcome.innerText = `Welcome, ${username}`;
  }

  return true;
}

function getToken() {
  return localStorage.getItem("token");
}

function startShopping() {
  loadProducts();
}

function goToCart() {
  window.location.href = "/view-cart.html";
}

async function loadProducts() {
  const response = await fetch("/products", {
    headers: {
      "Authorization": "Bearer " + getToken()
    }
  });

  if (response.status === 401 || response.status === 403) {
    logout();
    return;
  }

  const products = await response.json();
  const shoppingSection = document.getElementById("shoppingSection");
  const productList = document.getElementById("productList");

  shoppingSection.style.display = "block";

  if (!products.length) {
    productList.innerHTML = "<p>No products found</p>";
    return;
  }

  let html = `
    <table border="1" cellpadding="10" cellspacing="0" width="100%">
      <tr>
        <th>ID</th>
        <th>Name</th>
        <th>Price</th>
        <th>Stock</th>
        <th>Category</th>
        <th>Image</th>
        <th>Action</th>
      </tr>
  `;

  products.forEach(product => {
    html += `
      <tr>
        <td>${product.id}</td>
        <td>${product.name}</td>
        <td>${product.price}</td>
        <td>${product.stock}</td>
        <td>${product.category}</td>
        <td>
          <img 
            src="${product.imageUrl || ''}" 
            alt="${product.name}" 
            class="product-thumb"
          />
        </td>
        <td>
          <button onclick="addToCartPlaceholder('${product.name}')">Add to Cart</button>
        </td>
      </tr>
    `;
  });

  html += "</table>";
  productList.innerHTML = html;
}

function addToCartPlaceholder(productName) {
  alert(`${productName} will be added to cart later.`);
}

function logout() {
  localStorage.removeItem("token");
  localStorage.removeItem("username");
  localStorage.removeItem("role");
  window.location.replace("/index.html");
}

document.addEventListener("DOMContentLoaded", () => {
  ensureCustomer();
});