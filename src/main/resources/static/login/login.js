document.addEventListener('DOMContentLoaded', () => {
    const loginForm = document.getElementById('loginForm');
    const registerForm = document.getElementById('registerForm');
    const showRegister = document.getElementById('showRegister');
    const showLogin = document.getElementById('showLogin');
    const loginContainer = document.querySelector('.login-container');
    const registerContainer = document.querySelector('.register-container');
    const togglePassword = document.getElementById('togglePassword');
    const toggleNewPassword = document.getElementById('toggleNewPassword');
    const password = document.getElementById('password');
    const newPassword = document.getElementById('newPassword');

    function toggleForms() {
        loginContainer.style.display = loginContainer.style.display === 'none' ? 'block' : 'none';
        registerContainer.style.display = registerContainer.style.display === 'none' ? 'block' : 'none';
    }

    showRegister.addEventListener('click', (e) => {
        e.preventDefault();
        toggleForms();
        window.location.hash = 'register';
    });

    showLogin.addEventListener('click', (e) => {
        e.preventDefault();
        toggleForms();
        window.location.hash = 'login';
    });

    loginForm.addEventListener('submit', (e) => {
        e.preventDefault();
        const username = document.getElementById('username').value;
        const password = document.getElementById('password').value;
        login(username, password);
    });

    registerForm.addEventListener('submit', (e) => {
        e.preventDefault();
        const username = document.getElementById('newUsername').value;
        const password = document.getElementById('newPassword').value;
        register(username, password);
    });

    function togglePasswordVisibility(passwordField, toggleButton) {
        const type = passwordField.getAttribute('type') === 'password' ? 'text' : 'password';
        passwordField.setAttribute('type', type);
        toggleButton.querySelector('i').classList.toggle('fa-eye');
        toggleButton.querySelector('i').classList.toggle('fa-eye-slash');
    }

    togglePassword.addEventListener('click', () => {
        togglePasswordVisibility(password, togglePassword);
    });

    toggleNewPassword.addEventListener('click', () => {
        togglePasswordVisibility(newPassword, toggleNewPassword);
    });

    function showMessage(message, isError = false, container) {
        const messageElement = document.createElement('div');
        messageElement.textContent = message;
        messageElement.style.padding = '10px';
        messageElement.style.marginTop = '10px';
        messageElement.style.borderRadius = '4px';
        messageElement.style.backgroundColor = isError ? '#ffcccb' : '#90ee90';
        messageElement.style.color = isError ? '#d8000c' : '#4F8A10';
        container.innerHTML = '';
        container.appendChild(messageElement);
        setTimeout(() => messageElement.remove(), 5000);
    }

    function login(username, password) {
        fetch('/login', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ username, password }),
        })
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    showMessage('Erfolgreich angemeldet!', false, document.getElementById('loginMessage'));
                    localStorage.setItem('username', username);
                    localStorage.setItem('coins', data.coins);
                    setTimeout(() => window.location.href = '../game/game.html', 1500);
                } else {
                    showMessage('Anmeldung fehlgeschlagen. Bitte überprüfen Sie Ihre Eingaben.', true, document.getElementById('loginMessage'));
                }
            })
            .catch(error => {
                console.error('Error:', error);
                showMessage('Ein Fehler ist aufgetreten. Bitte versuchen Sie es später erneut.', true, document.getElementById('loginMessage'));
            });
    }

    function register(username, password) {
        fetch('/register', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ username, password }),
        })
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    showMessage('Registrierung erfolgreich! Sie werden nun angemeldet.', false, document.getElementById('registerMessage'));
                    localStorage.setItem('username', username);
                    localStorage.setItem('coins', '0');
                    setTimeout(() => window.location.href = '../game/game.html', 1500);
                } else {
                    showMessage('Registrierung fehlgeschlagen. Möglicherweise existiert der Benutzername bereits.', true, document.getElementById('registerMessage'));
                }
            })
            .catch(error => {
                console.error('Error:', error);
                showMessage('Ein Fehler ist aufgetreten. Bitte versuchen Sie es später erneut.', true, document.getElementById('registerMessage'));
            });
    }

    if (window.location.hash === '#register') {
        showRegister.click();
    }
});