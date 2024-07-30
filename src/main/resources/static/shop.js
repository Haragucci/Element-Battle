document.addEventListener('DOMContentLoaded', function() {
    loadUserInfo();
    loadPurchasedBackgrounds();
    loadCardDesignStatus();
    createPreviewCards();

    const buyAllBackgroundsButton = document.getElementById('buyAllBackgrounds');
    if (buyAllBackgroundsButton) {
        addIconToButton(buyAllBackgroundsButton, 'fas fa-images');
        buyAllBackgroundsButton.addEventListener('click', buyAllBackgrounds);
    }
});

function updateUserDisplay(username, coins) {
    const userInfoElement = document.getElementById('userInfo');
    const authButtonsElement = document.getElementById('authButtons');
    const usernameElement = document.getElementById('username');
    const logoutButton = document.getElementById('logoutButton');

    if (username) {
        usernameElement.innerHTML = `
            <span class="username-text"><i class="fas fa-user"></i> ${username}</span>
            <span class="coins-info"><i class="fas fa-coins"></i> ${coins}</span>
        `;
        userInfoElement.style.display = 'flex';
        userInfoElement.style.alignItems = 'center';
        authButtonsElement.style.display = 'none';
        if (logoutButton) {
            logoutButton.style.display = 'inline-block';
            logoutButton.style.backgroundColor = '#ff4136';
            logoutButton.innerHTML = '<i class="fas fa-sign-out-alt"></i> Abmelden';
            logoutButton.classList.add('icon-button', 'logout-button');
        }
    } else {
        userInfoElement.style.display = 'none';
        authButtonsElement.style.display = 'flex';
        authButtonsElement.style.alignItems = 'center';
        if (logoutButton) {
            logoutButton.style.display = 'none';
        }
    }
}

function loadUserInfo() {
    const username = localStorage.getItem('username');
    if (!username) {
        updateUserDisplay(null, null);
        return;
    }

    fetch('/getUserInfo', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({ username })
    })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                updateUserDisplay(data.username, data.coins);
            } else {
                console.error('Fehler beim Laden der Benutzerinformationen:', data.message);
                updateUserDisplay(null, null);
            }
        })
        .catch(error => {
            console.error('Error:', error);
            updateUserDisplay(null, null);
        });
}

function logout() {
    localStorage.removeItem('username');
    updateUserDisplay(null, null);
    window.location.reload();
}

function loadPurchasedBackgrounds() {
    const username = localStorage.getItem('username');
    if (!username) {
        console.error('Kein Benutzer angemeldet');
        hideAllBackgroundButtons();
        return;
    }

    fetch('/checkUserBackground', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({ username })
    })
        .then(response => response.json())
        .then(data => {
            console.log('Received background data:', data);
            if (data.exists) {
                enableAllBackgrounds();
                const buyButton = document.getElementById('buyAllBackgrounds');
                if (buyButton) {
                    buyButton.style.display = 'none';
                }
                if (data.activeBackground) {
                    updateBackgroundToggleButtons(data.activeBackground);
                    localStorage.setItem('activeBackground', data.activeBackground);
                }
            } else {
                const buyButton = document.getElementById('buyAllBackgrounds');
                if (buyButton) {
                    buyButton.style.display = 'block';
                }
                disableAllBackgrounds();
                hideAllToggleButtons();
            }
        })
        .catch(error => console.error('Error:', error));
}

function hideAllBackgroundButtons() {
    const backgroundButtons = document.querySelectorAll('[id^="background-toggle-button"]');
    backgroundButtons.forEach(button => {
        button.style.display = 'none';
    });
}

function hideAllToggleButtons() {
    const toggleButtons = document.querySelectorAll('.toggle-background');
    toggleButtons.forEach(button => {
        button.style.display = 'none';
    });
}

function disableAllBackgrounds() {
    document.querySelectorAll('[id^="background-toggle-button"]').forEach(button => {
        button.style.display = 'none';
    });
}

function enableAllBackgrounds() {
    document.querySelectorAll('[id^="background-toggle-button"]').forEach(button => {
        button.style.display = 'inline-block';
        button.classList.add('background-toggle-button', 'icon-button');
        button.removeEventListener('click', handleBackgroundToggle);
        button.addEventListener('click', handleBackgroundToggle);
    });
}

function handleBackgroundToggle(event) {
    const backgroundId = event.target.getAttribute('data-background');
    toggleBackground(backgroundId);
}

function updateBackgroundToggleButtons(activeBackground) {
    document.querySelectorAll('[id^="background-toggle-button"]').forEach(button => {
        const backgroundId = button.getAttribute('data-background');
        if (backgroundId === activeBackground) {
            button.innerHTML = '<i class="fas fa-toggle-on"></i> Deaktivieren';
            button.classList.add('active', 'deactivate');
        } else {
            button.innerHTML = '<i class="fas fa-toggle-off"></i> Aktivieren';
            button.classList.remove('active', 'deactivate');
        }
        button.classList.add('icon-button');
    });
}

function toggleBackground(backgroundId) {
    const username = localStorage.getItem('username');
    if (!username) {
        console.error('Kein Benutzer angemeldet');
        return;
    }

    fetch('/toggleBackground', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({ username, background: backgroundId })
    })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                const newActiveBackground = data.isActive ? backgroundId : '';
                updateBackgroundToggleButtons(newActiveBackground);
                localStorage.setItem('activeBackground', newActiveBackground);

                const button = document.querySelector(`[data-background="${backgroundId}"]`);
                if (data.isActive) {
                    button.innerHTML = '<i class="fas fa-toggle-on"></i> Deaktivieren';
                } else {
                    button.innerHTML = '<i class="fas fa-toggle-off"></i> Aktivieren';
                }
            } else {
                console.error('Fehler beim Ändern des Hintergrunds:', data.message);
            }
        })
        .catch(error => console.error('Error:', error));
}

function buyAllBackgrounds() {
    const username = localStorage.getItem('username');
    const buyButton = document.getElementById('buyAllBackgrounds');

    if (!username) {
        flashButtonRed(buyButton);
        console.error('Bitte melden Sie sich an, um Hintergründe zu kaufen.');
        return;
    }

    if (buyButton.disabled) return;
    buyButton.disabled = true;

    const cost = 2;
    const requestBody = { username, background: 'all', cost };
    console.log('Sending buyBackground request:', requestBody);

    fetch('/buyBackground', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(requestBody)
    })
        .then(response => response.json())
        .then(data => {
            console.log('Received buyBackground response:', data);
            if (data.success) {
                console.log('Alle Hintergründe gekauft');
                enableAllBackgrounds();
                if (buyButton) {
                    buyButton.style.display = 'none';
                }
                updateCoins(data.coins);
                loadPurchasedBackgrounds();
            } else {
                throw new Error(data.message || 'Unbekannter Fehler beim Kauf der Hintergründe');
            }
        })
        .catch(error => {
            console.error('Fehler:', error.message);
            flashButtonRed(buyButton);
        });
}

function updateCoins(newCoins) {
    localStorage.setItem('coins', newCoins);
    updateUserDisplay(localStorage.getItem('username'), newCoins);
}

function loadCardDesignStatus() {
    const username = localStorage.getItem('username');
    if (!username) {
        console.error('Kein Benutzer angemeldet');
        return;
    }

    fetch('/checkUserCardDesign', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({ username })
    })
        .then(response => response.json())
        .then(data => {
            const buyButton = document.getElementById('buyCardDesign');
            const togglesContainer = document.getElementById('cardDesignToggles');

            if (data.purchased) {
                buyButton.style.display = 'none';
                togglesContainer.style.display = 'block';
                updateToggleButtons(data.activeDesign);
            } else {
                buyButton.style.display = 'block';
                togglesContainer.style.display = 'none';
            }
        })
        .catch(error => console.error('Error:', error));
}

function updateToggleButtons(activeDesign) {
    const designs = ['default', '1', '2', '3'];
    designs.forEach(designId => {
        const button = document.getElementById(`toggleCardDesign${designId === 'default' ? 'Default' : designId}`);
        if (button) {
            if (designId === activeDesign) {
                button.textContent = ' Deaktivieren';
                button.classList.add('active', 'deactivate');
                addIconToButton(button, 'fas fa-toggle-on');
            } else {
                button.textContent = ' Aktivieren';
                button.classList.remove('active', 'deactivate');
                addIconToButton(button, 'fas fa-toggle-off');
            }
        }
    });
}

function toggleCardDesign(designId) {
    const username = localStorage.getItem('username');
    if (!username) {
        console.error('Kein Benutzer angemeldet');
        return;
    }

    fetch('/toggleCardDesign', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({ username, designId })
    })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                updateToggleButtons(data.activeDesign);
                localStorage.setItem('activeCardDesign', data.activeDesign);
                if (typeof updateCardDesign === 'function') {
                    updateCardDesign();
                }
            } else {
                console.error('Fehler beim Ändern des Kartendesigns:', data.message);
            }
        })
        .catch(error => console.error('Error:', error));
}

function buyCardDesign() {
    const username = localStorage.getItem('username');
    const buyButton = document.getElementById('buyCardDesign');

    if (buyButton.disabled) return;
    buyButton.disabled = true;

    fetch('/buyCardDesign', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({ username })
    })
        .then(response => response.json())
        .then(data => {
            console.log('Server response:', data);
            if (data.success) {
                console.log('Kartendesigns gekauft. Neue Münzanzahl:', data.coins);
                buyButton.style.display = 'none';

                document.querySelectorAll('.toggle-button2').forEach(button => {
                    button.style.display = 'block';
                });

                updateCoins(data.coins);
                updateToggleButtons(data.activeDesign);
                localStorage.setItem('activeCardDesign', data.activeDesign);
            } else {
                throw new Error(data.message || 'Unbekannter Fehler beim Kauf der Kartendesigns');
            }
        })
        .catch(error => {
            console.error('Fehler:', error.message);
            flashButtonRed(buyButton);
        })
        .finally(() => {
            buyButton.disabled = false;
        });
    window.location.reload();
}

function flashButtonRed(button) {
    const originalColor = '#4CAF50';
    const originalTransition = button.style.transition;

    button.style.backgroundColor = 'red';
    button.style.transition = 'background-color 0.5s';

    setTimeout(() => {
        button.style.backgroundColor = originalColor;
        button.style.transition = originalTransition;
    }, 500);
}

function createPreviewCards() {
    const designs = ['default', '1', '2', '3'];
    designs.forEach(design => {
        const previewContainer = document.getElementById(`preview${design === 'default' ? 'Default' : design}`);
        if (previewContainer) {
            const fireCard = createPreviewCard('Feuer', design);
            const iceCard = createPreviewCard('Eis', design);
            previewContainer.appendChild(fireCard);
            previewContainer.appendChild(iceCard);
        }
    });
}

function createPreviewCard(element, design) {
    const card = document.createElement('div');
    card.classList.add('preview-card', 'card', element.toLowerCase());
    if (design !== 'default') {
        card.classList.add(`premium-design-${design}`);
    }

    const elementIcon = element === 'Feuer' ? 'fas fa-fire' : 'fas fa-snowflake';
    const cardName = element === 'Feuer' ? 'Feuersturm' : 'Eisschlag';
    const description = element === 'Feuer' ? 'Verbrennt Gegner' : 'Friert Gegner ein';

    card.innerHTML = `
        <div class="card-header">
            <h3>${cardName}</h3>
            <i class="${elementIcon} element-icon ${element.toLowerCase()} ${design !== 'default' ? `premium-design-${design}` : ''}"></i>
        </div>
        <div class="card-body">
            <p><span class="stat-label">HP:</span> <span class="stat-value">100</span></p>
            <p><span class="stat-label">Damage:</span> <span class="stat-value">50</span></p>
            <p><span class="stat-label">Element:</span> <span>${element}</span></p>
        </div>
        <div class="card-footer">
            <p>${description}</p>
        </div>
    `;
    return card;
}

function addIconToButton(button, iconClass) {
    const icon = document.createElement('i');
    icon.className = iconClass;
    button.prepend(icon);
    button.classList.add('icon-button');
}

function checkPurchaseStatus() {
    const username = localStorage.getItem('username');

    fetch('/checkUserCardDesign', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({ username })
    })
        .then(response => response.json())
        .then(data => {
            if (data.purchased) {

                document.querySelectorAll('.toggle-button2').forEach(button => {
                    button.style.display = 'block';
                });
                document.getElementById('buyCardDesign').style.display = 'none';


                updateToggleButtons(data.activeDesign);
                localStorage.setItem('activeCardDesign', data.activeDesign);
            } else {
                document.getElementById('buyCardDesign').style.display = 'block';
                document.querySelectorAll('.toggle-button2').forEach(button => {
                    button.style.display = 'none';
                });
                localStorage.setItem('activeCardDesign', 'default');
            }
        })
        .catch(error => {
            console.error('Error:', error);
            alert('Ein Fehler ist beim Überprüfen des Kaufstatus aufgetreten: ' + error.message);
        });
}

document.addEventListener('DOMContentLoaded', function() {
    const buyButton = document.getElementById('buyCardDesign');
    if (buyButton) {
        buyButton.addEventListener('click', buyCardDesign);
        addIconToButton(buyButton, 'fas fa-paint-brush');
    }

    const toggleButtons = document.querySelectorAll('.toggle-button2');
    toggleButtons.forEach(button => {
        button.addEventListener('click', function() {
            const designId = this.getAttribute('data-design');
            toggleCardDesign(designId);
        });
    });
    checkPurchaseStatus();
    loadCardDesignStatus();
});
