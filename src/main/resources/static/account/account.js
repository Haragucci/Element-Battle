document.addEventListener('DOMContentLoaded', function() {
    const usernameInput = document.getElementById('username');
    const passwordInput = document.getElementById('password');
    const editButtons = document.querySelectorAll('.edit-btn');
    const saveButton = document.querySelector('.save-btn');
    const accountForm = document.getElementById('accountForm');
    const togglePasswordButton = document.getElementById('togglePassword');

    loadLeaderboard();

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

    document.getElementById('del-button').addEventListener('click', function (e) {
        e.preventDefault();
        if (confirm('Sind Sie sicher, dass Sie Ihren Account löschen möchten?')) {
            delAcc(localStorage.getItem('username'));
        }
    });

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

    function delAcc(username) {
        fetch('/del-user', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                username: username
            })
        })
            .then(response => response.text())
            .then(data => {
                alert(data);
                if (data === 'User deleted successfully'){
                    window.location.href = 'login.html';
                }
            })
            .catch(error => {
                console.error('Error:', error);
                alert('Ein Fehler ist aufgetreten beim Löschen des Accounts.');
            });
    }

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
                    passwordInput.value = '*'.repeat(data.password.length);
                    originalPassword = passwordInput.value;

                    loadUserStats();
                } else {
                    console.error('Fehler beim Laden der Benutzerinformationen:', data.message);
                }
            })
            .catch(error => {
                console.error('Fehler beim Laden der Benutzerinformationen:', error);
            });
    }

    function loadUserStats() {
        const username = localStorage.getItem('username');
        if (!username) return;

        fetch('/getUserStats', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ username: username })
        })
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    const statsContent = document.getElementById('statsContent');
                    const stats = data.stats;
                    statsContent.innerHTML = `
                    <div class="stat-item">
                        <div class="stat-label">Siege</div>
                        <div class="stat-value">${stats.wins || 0}</div>
                    </div>
                    <div class="stat-item">
                        <div class="stat-label">Niederlagen</div>
                        <div class="stat-value">${stats.lose || 0}</div>
                    </div>
                    <div class="stat-item">
                        <div class="stat-label">Schaden</div>
                        <div class="stat-value">${stats.damage || 0}</div>
                    </div>
                    <div class="stat-item">
                        <div class="stat-label">Direkter Schaden</div>
                        <div class="stat-value">${stats['direkt-damage'] || 0}</div>
                    </div>
                    <div class="stat-item">
                        <div class="stat-label">Coins</div>
                        <div class="stat-value">${stats.coins || 0}</div>
                    </div>
                    <div class="stat-item">
                        <div class="stat-label">Gewinnrate</div>
                        <div class="stat-value">${(stats.winrate || 0).toFixed(2)}%</div>
                    </div>
                `;
                } else {
                    console.error('Fehler beim Laden der Statistiken:', data.message);
                }
            })
            .catch(error => {
                console.error('Fehler beim Laden der Statistiken:', error);
            });
    }

    function loadLeaderboard() {
        fetch('/leaderboard')
            .then(response => response.json())
            .then(data => {
                const leaderboardBody = document.querySelector('#leaderboardTable tbody');
                leaderboardBody.innerHTML = '';
                data.forEach((player, index) => {
                    const row = document.createElement('tr');
                    row.innerHTML = `
                    <td>${index + 1}</td>
                    <td>${player.username}</td>
                    <td>${player.wins || 0}</td>
                    <td>${player.winrate.toFixed(2)}%</td>
                `;
                    leaderboardBody.appendChild(row);
                });
            })
            .catch(error => {
                console.error('Fehler beim Laden des Leaderboards:', error);
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