document.addEventListener('DOMContentLoaded', function() {
    const usernameInput = document.getElementById('username');
    const passwordInput = document.getElementById('password');
    const editButtons = document.querySelectorAll('.edit-btn');
    const saveButton = document.querySelector('.save-btn');
    const accountForm = document.getElementById('accountForm');
    const togglePasswordButton = document.getElementById('togglePassword');
    const formContainer = document.querySelector('.form-container');

    let originalUsername = '';
    let originalPassword = '';
    let actualPassword = '';

    loadUserInfo();

    editButtons.forEach(button => {
        button.addEventListener('click', function() {
            const targetId = this.getAttribute('data-target');
            const targetInput = document.getElementById(targetId);
            if (targetInput.readOnly) {
                targetInput.readOnly = false;
                this.innerHTML = '<i class="fas fa-times"></i>';
                this.title = 'Abbrechen';
                targetInput.classList.add('editing');
                if (targetId === 'password') {
                    targetInput.value = actualPassword;
                    targetInput.type = 'text';
                    togglePasswordButton.querySelector('i').classList.replace('fa-eye', 'fa-eye-slash');
                }
            } else {
                targetInput.readOnly = true;
                this.innerHTML = '<i class="fas fa-edit"></i>';
                this.title = 'Bearbeiten';
                targetInput.classList.remove('editing');
                if (targetId === 'username') {
                    targetInput.value = originalUsername;
                } else if (targetId === 'password') {
                    targetInput.value = '*'.repeat(actualPassword.length);
                    targetInput.type = 'password';
                    togglePasswordButton.querySelector('i').classList.replace('fa-eye-slash', 'fa-eye');
                }
            }
            updateSaveButtonState();
        });
    });

    function createConfetti() {
        const confettiCount = 300;
        const confettiContainer = document.createElement('div');
        confettiContainer.style.position = 'fixed';
        confettiContainer.style.top = '0';
        confettiContainer.style.left = '0';
        confettiContainer.style.width = '100%';
        confettiContainer.style.height = '100%';
        confettiContainer.style.pointerEvents = 'none';
        confettiContainer.style.zIndex = '9999';

        for (let i = 0; i < confettiCount; i++) {
            const confetti = document.createElement('div');
            confetti.style.position = 'absolute';
            confetti.style.width = '10px';
            confetti.style.height = '10px';
            confetti.style.backgroundColor = getRandomColor();
            confetti.style.left = Math.random() * 100 + 'vw';
            confetti.style.top = '-10px';
            confetti.style.borderRadius = '50%';
            confetti.style.transform = `rotate(${Math.random() * 360}deg)`;
            confetti.style.animation = `fall ${Math.random() * 3 + 2}s linear`;
            confettiContainer.appendChild(confetti);
        }

        document.body.appendChild(confettiContainer);

        setTimeout(() => {
            document.body.removeChild(confettiContainer);
        }, 5000);
    }

    function getRandomColor() {
        const letters = '0123456789ABCDEF';
        let color = '#';
        for (let i = 0; i < 6; i++) {
            color += letters[Math.floor(Math.random() * 16)];
        }
        return color;
    }

    accountForm.addEventListener('submit', function(e) {
        e.preventDefault();
        saveChanges();
    });

    togglePasswordButton.addEventListener('click', function() {
        const type = passwordInput.getAttribute('type') === 'password' ? 'text' : 'password';
        passwordInput.setAttribute('type', type);
        this.querySelector('i').classList.toggle('fa-eye');
        this.querySelector('i').classList.toggle('fa-eye-slash');
        if (type === 'text' && passwordInput.readOnly) {
            passwordInput.value = actualPassword;
        } else if (type === 'password' && passwordInput.readOnly) {
            passwordInput.value = '*'.repeat(actualPassword.length);
        }
    });

    function loadUserInfo() {
        const username = localStorage.getItem('username');
        if (!username) {
            alert('Bitte melden Sie sich an, um Ihre Kontoinformationen zu sehen.');
            window.location.href = 'login.html';
            return;
        }

        fetch('/getUserInfo', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ username: username })
        })
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    usernameInput.value = data.username;
                    originalUsername = data.username;
                    actualPassword = data.password;
                    passwordInput.value = '*'.repeat(actualPassword.length);
                    originalPassword = '*'.repeat(actualPassword.length);
                } else {
                    alert('Fehler beim Laden der Benutzerinformationen.');
                }
            })
            .catch(error => {
                console.error('Error:', error);
                alert('Ein Fehler ist aufgetreten. Bitte versuchen Sie es später erneut.');
            });
    }

    function updateSaveButtonState() {
        const isUsernameChanged = usernameInput.value !== originalUsername && !usernameInput.readOnly;
        const isPasswordChanged = passwordInput.value !== '' && passwordInput.value !== '*'.repeat(actualPassword.length) && !passwordInput.readOnly;
        saveButton.disabled = !(isUsernameChanged || isPasswordChanged);
    }

    function saveChanges() {
        const newUsername = usernameInput.value;
        const newPassword = passwordInput.value;

        if (newUsername === originalUsername && (newPassword === '' || newPassword === '*'.repeat(actualPassword.length))) {
            alert('Keine Änderungen vorgenommen.');
            return;
        }

        const updateData = {
            oldUsername: originalUsername,
            newUsername: newUsername,
            newPassword: (newPassword !== '' && newPassword !== '*'.repeat(actualPassword.length)) ? newPassword : null
        };

        fetch('/updateAccount', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(updateData)
        })
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    createConfetti();

                    originalUsername = newUsername;
                    if (newPassword !== '' && newPassword !== '*'.repeat(actualPassword.length)) {
                        actualPassword = newPassword;
                    }
                    localStorage.setItem('username', newUsername);
                    resetInputs();
                    loadUserInfo();

                    const successMessage = document.createElement('div');
                    successMessage.textContent = 'Änderungen erfolgreich gespeichert!';
                    successMessage.style.position = 'fixed';
                    successMessage.style.top = '20px';
                    successMessage.style.left = '50%';
                    successMessage.style.transform = 'translateX(-50%)';
                    successMessage.style.backgroundColor = '#4CAF50';
                    successMessage.style.color = 'white';
                    successMessage.style.padding = '10px 20px';
                    successMessage.style.borderRadius = '5px';
                    successMessage.style.zIndex = '10000';
                    document.body.appendChild(successMessage);

                    setTimeout(() => {
                        document.body.removeChild(successMessage);
                    }, 3000);
                } else {
                    alert('Fehler beim Aktualisieren der Kontoinformationen: ' + data.message);
                }
            })
            .catch(error => {
                console.error('Error:', error);
                alert('Ein Fehler ist aufgetreten. Bitte versuchen Sie es später erneut.');
            });
    }

    function resetInputs() {
        usernameInput.value = originalUsername;
        passwordInput.value = '*'.repeat(actualPassword.length);
        passwordInput.type = 'password';
        togglePasswordButton.querySelector('i').classList.replace('fa-eye-slash', 'fa-eye');
        editButtons.forEach(button => {
            button.innerHTML = '<i class="fas fa-edit"></i>';
            button.title = 'Bearbeiten';
        });
        usernameInput.readOnly = true;
        passwordInput.readOnly = true;
        updateSaveButtonState();
    }

    usernameInput.addEventListener('input', updateSaveButtonState);
    passwordInput.addEventListener('input', updateSaveButtonState);
});