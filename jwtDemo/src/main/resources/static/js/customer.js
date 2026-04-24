function ensureCustomer() {
  const token = localStorage.getItem("token");
  const role = localStorage.getItem("role");
  const username = localStorage.getItem("username");

  if (!token || role !== "USER") {
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

function goBackToShopping() {
  window.location.href = "/customer-home.html";
}

async function loadProducts() {
  const response = await fetch("/products", {
    headers: {
      "Authorization": "Bearer " + getToken()
    }
  });

  if (response.status === 401) {
    logout();
    return;
  }

  if (response.status === 403) {
    alert("Access denied to products");
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
    <table class="product-table">
      <tr>
        <th>Image</th>
        <th>Name</th>
        <th>Price</th>
        <th>Stock</th>
        <th>Category</th>
        <th>Action</th>
      </tr>
  `;

  products.forEach(product => {
    html += `
      <tr>
        <td>
          <img src="${product.imageUrl || ''}" alt="${product.name}" class="product-thumb" />
        </td>
        <td>${product.name}</td>
        <td>₹${product.price}</td>
        <td>${product.stock}</td>
        <td>${product.category}</td>
        <td>
          <button onclick="addToCart(${product.id})">Add to Cart</button>
        </td>
      </tr>
    `;
  });

  html += "</table>";
  productList.innerHTML = html;
}

async function addToCart(productId) {
  const response = await fetch("/customer/cart", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      "Authorization": "Bearer " + getToken()
    },
    body: JSON.stringify({
      productId: productId,
      quantity: 1
    })
  });

  const result = await response.json().catch(() => ({}));

  if (response.status === 401) {
    logout();
    return;
  }

  if (!response.ok) {
    alert(result.message || "Failed to add product to cart");
    return;
  }

  alert(result.message || "Product added to cart");
}

async function loadCart() {
  const response = await fetch("/customer/cart", {
    headers: {
      "Authorization": "Bearer " + getToken()
    }
  });

  if (response.status === 401) {
    logout();
    return;
  }

  if (response.status === 403) {
    alert("Access denied to cart");
    return;
  }

  const cart = await response.json();
  const cartList = document.getElementById("cartList");
  const cartTotal = document.getElementById("cartTotal");
  const checkoutBtn = document.getElementById("checkoutBtn");

  if (!cartList) return;

  if (!cart.items.length) {
    cartList.innerHTML = "<p class='empty-state'>Your cart is empty</p>";
    cartTotal.innerText = "";
    if (checkoutBtn) checkoutBtn.style.display = "none";
    return;
  }

  let html = `
    <table class="product-table">
      <tr>
        <th>Image</th>
        <th>Name</th>
        <th>Price</th>
        <th>Quantity</th>
        <th>Subtotal</th>
        <th>Actions</th>
      </tr>
  `;

  cart.items.forEach(item => {
    html += `
      <tr>
        <td>
          <img src="${item.imageUrl || ''}" alt="${item.productName}" class="product-thumb" />
        </td>
        <td>${item.productName}</td>
        <td>₹${item.price}</td>
        <td>
          <input type="number" min="1" value="${item.quantity}" id="qty-${item.cartItemId}" style="width: 70px;" />
        </td>
        <td>₹${item.subtotal}</td>
        <td>
          <button onclick="updateCartItem(${item.cartItemId})">Update</button>
          <button onclick="removeCartItem(${item.cartItemId})">Remove</button>
        </td>
      </tr>
    `;
  });

  html += "</table>";

  cartList.innerHTML = html;
  cartTotal.innerText = `Total: ₹${cart.totalAmount}`;
}

async function updateCartItem(cartItemId) {
  const quantity = document.getElementById(`qty-${cartItemId}`).value;

  const response = await fetch(`/customer/cart/${cartItemId}`, {
    method: "PUT",
    headers: {
      "Content-Type": "application/json",
      "Authorization": "Bearer " + getToken()
    },
    body: JSON.stringify({
      quantity: Number(quantity)
    })
  });

  const result = await response.json().catch(() => ({}));

  if (!response.ok) {
    alert(result.message || "Failed to update cart item");
    return;
  }

  alert(result.message || "Cart updated");
  loadCart();
}

async function removeCartItem(cartItemId) {
  const response = await fetch(`/customer/cart/${cartItemId}`, {
    method: "DELETE",
    headers: {
      "Authorization": "Bearer " + getToken()
    }
  });

  const result = await response.json().catch(() => ({}));

  if (!response.ok) {
    alert(result.message || "Failed to remove cart item");
    return;
  }

  alert(result.message || "Item removed from cart");
  loadCart();
}

async function checkout() {
  if (typeof Razorpay === "undefined") {
    alert("Razorpay SDK not loaded");
    return;
  }

  const createOrderResponse = await fetch("/customer/payment/create-order", {
    method: "POST",
    headers: {
      "Authorization": "Bearer " + getToken()
    }
  });

  if (createOrderResponse.status === 401) {
    logout();
    return;
  }

  const orderData = await createOrderResponse.json().catch(() => ({}));

  if (!createOrderResponse.ok) {
    alert(orderData.message || "Failed to create payment order");
    return;
  }

  const username = localStorage.getItem("username") || "Customer";

  const options = {
    key: orderData.keyId,
    amount: orderData.amount,
    currency: orderData.currency,
    name: "JWT Demo Store",
    description: "Cart Payment",
    order_id: orderData.razorpayOrderId,
    handler: async function (response) {
      const verifyResponse = await fetch("/customer/payment/verify", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          "Authorization": "Bearer " + getToken()
        },
        body: JSON.stringify({
          localOrderId: orderData.localOrderId,
          razorpayPaymentId: response.razorpay_payment_id,
          razorpayOrderId: response.razorpay_order_id,
          razorpaySignature: response.razorpay_signature
        })
      });

      const verifyResult = await verifyResponse.json().catch(() => ({}));

      if (!verifyResponse.ok) {
        alert(verifyResult.message || "Payment verification failed");
        return;
      }

      alert(verifyResult.message || "Payment successful");
      window.location.href = "/customer-home.html";
    },
    prefill: {
      name: username
    },
    theme: {
      color: "#2563eb"
    }
  };

  const rzp = new Razorpay(options);

  rzp.on("payment.failed", function () {
    alert("Payment failed");
  });

  rzp.open();
}

function logout() {
  localStorage.removeItem("token");
  localStorage.removeItem("username");
  localStorage.removeItem("role");
  window.location.replace("/index.html");
}

document.addEventListener("DOMContentLoaded", () => {
  if (!ensureCustomer()) return;

  if (document.getElementById("cartList")) {
    loadCart();
  }
});