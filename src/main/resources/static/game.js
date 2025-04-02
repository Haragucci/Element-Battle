document.addEventListener('DOMContentLoaded', function() {
    updateCardDesign();
    updateElementRelationships();
    checkAndUpdateUserCardDesign();
    const playerField = document.querySelector('.player-field');
    const computerField = document.querySelector('.computer-field');
    const playerCardsContainer = document.querySelector('.player-cards');
    const computerCardsContainer = document.querySelector('.computer-cards');
    const turnInfo = document.getElementById('turn-info');
    const playerHpValue = document.getElementById('player-hp-value');
    const computerHpValue = document.getElementById('computer-hp-value');
    const playerHpBar = document.getElementById('player-hp-bar');
    const computerHpBar = document.getElementById('computer-hp-bar');
    const coinsSpan = document.getElementById('coins');
    let totalDamageDealt = 0;
    let totalDirectDamageDealt = 0;
    let firstAttacker;

    let playerHP = 25;
    let computerHP = 25;
    let allHeroes = [];
    let playerHand = [];
    let computerHand = [];
    let isPlayerFirstAttacker = true;
    let roundCounter = 1;

    const battleLog = document.getElementById('battle-log');

    let isBattleInProgress = false;

    function loadCoins() {
        const coins = parseInt(localStorage.getItem('coins')) || 0;
        coinsSpan.textContent = coins.toString();
    }

    window.onload = function() {
        loadCoins();
        updateUserInfo();
    };

    async function delPlayerGame() {
        const username = localStorage.getItem('username');

        try {
            const response = await fetch(`/game/${username}`, {
                method: 'DELETE',
            });

            if (response.ok) {
                console.log('Spielstand gelöscht!');
            } else {
                console.error('Fehler beim Löschen des Spielstands:', response.status);
            }
        } catch (error) {
            console.error('Netzwerkfehler:', error);
        }
    }

    function savePlayerGame() {
        const username = localStorage.getItem('username');

        if (!username || playerHand.length === 0 || computerHand.length === 0) {
            console.error('Fehlende Daten für das Speichern des Spiels!');
            return;
        }

        const requestData = {
            username: username,
            PHP:playerHP,
            CHP:computerHP,
            playerCards: playerHand.map(hero => ({
                id: hero.id,
                name: hero.name,
                HP: hero.HP,
                Damage: hero.Damage,
                type: hero.type,
                extra: hero.extra
            })),
            computerCards: computerHand.map(hero => ({
                id: hero.id,
                name: hero.name,
                HP: hero.HP,
                Damage: hero.Damage,
                type: hero.type,
                extra: hero.extra
            }))
        };

        console.log("Sending data:", JSON.stringify(requestData));

        fetch('/saveGame', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(requestData)
        })
            .then(response => response.json())
            .then(data => {
                console.log('Spiel gespeichert:', data);
            })
            .catch(error => {
                console.error('Fehler beim Speichern des Spiels:', error);
            });
    }
/*
    function checkPlayerGame() {
        const username = localStorage.getItem('username');
        if (!username) {
            return;
        }

        fetch('/checkGame', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ username: username })
        })
            .then(response => response.json())
            .then(data => {
                if (data.Playercards && data.Computercards) {
                    console.log('Gespeichertes Spiel gefunden:', data);
                    playerHand = data.Playercards;
                    computerHand = data.Computercards;
                } else {
                    console.log('Kein gespeichertes Spiel gefunden.');
                }
            })
            .catch(error => {
                console.error('Error:', error);
            });
    }*/

    function checkAndUpdateUserCardDesign() {
        const username = localStorage.getItem('username');
        if (!username) {
            localStorage.setItem('activeCardDesign', 'default');
            updateCardDesign();
            return;
        }

        fetch('/checkUserCardDesign', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ username: username })
        })
            .then(response => response.json())
            .then(data => {
                let design = 'default';
                if (data.purchased) {
                    design = data.activeDesign || 'default';
                }
                localStorage.setItem('activeCardDesign', design);
                updateCardDesign();
            })
            .catch(error => {
                console.error('Error:', error);
                localStorage.setItem('activeCardDesign', 'default');
                updateCardDesign();
            });
    }

    function updateCoins(amount) {
        const currentCoins = parseInt(localStorage.getItem('coins')) || 0;
        const newCoins = currentCoins + amount;

        localStorage.setItem('coins', newCoins);
        if (coinsSpan) {
            coinsSpan.textContent = newCoins;
        }

        const username = localStorage.getItem('username');
        if (username) {
            fetch('/updateCoins', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    username: username,
                    coins: newCoins
                })
            })
                .then(response => response.json())
                .then(data => {
                    if (data.success) {
                        console.log('Münzen erfolgreich aktualisiert');
                        updateUserInfo();
                    } else {
                        console.error('Fehler beim Aktualisieren der Münzen');
                    }
                })
                .catch(error => {
                    console.error('Fehler beim Senden der Anfrage:', error);
                });
        }
    }

    function updateUserInfo() {
        const username = localStorage.getItem('username');
        const coins = localStorage.getItem('coins');
        const userInfo = document.getElementById('userInfo');
        const authButtons = document.getElementById('authButtons');
        const usernameSpan = document.getElementById('username');
        const loginButton = document.getElementById('loginButton');
        const registerButton = document.getElementById('registerButton');
        const navButtons = document.querySelector('.nav-buttons');

        if (username && coins !== null) {
            userInfo.style.display = 'flex';
            usernameSpan.innerHTML = `
            <span class="username-text"><i class="fas fa-user"></i> ${username}</span>
            <span class="coins-info"><i class="fas fa-coins"></i> ${coins}</span>
        `;

            if (loginButton) loginButton.style.display = 'none';
            if (registerButton) registerButton.style.display = 'none';

            let logoutButton = document.getElementById('logoutButton');
            if (!logoutButton) {
                logoutButton = document.createElement('button');
                logoutButton.id = 'logoutButton';
                logoutButton.className = 'but logout-button';
                logoutButton.innerHTML = '<i class="fas fa-sign-out-alt"></i> Abmelden';
                logoutButton.style.backgroundColor = '#ff4136';
                logoutButton.addEventListener('click', logout);
                authButtons.insertBefore(logoutButton, authButtons.firstChild);
            }
            logoutButton.style.display = 'inline-block';

            if (registerButton) {
                registerButton.innerHTML = '<i class="fas fa-user"></i> Account';
                registerButton.onclick = function() {
                    window.location.href = 'account.html';
                };
                registerButton.style.display = 'inline-block';
            }

            if (navButtons) {
                navButtons.style.position = 'absolute';
                navButtons.style.left = '50%';
                navButtons.style.transform = 'translateX(-50%)';
            }
        } else {
            userInfo.style.display = 'none';

            if (loginButton) {
                const allCards = document.querySelectorAll('.card');
                const allElementRelationships = document.querySelectorAll('.element-relationship');

                allCards.forEach(card => {
                    card.classList.remove('premium-design-1', 'premium-design-2', 'premium-design-3');
                });

                allElementRelationships.forEach(element => {
                    element.classList.remove('premium-design-1', 'premium-design-2', 'premium-design-3');
                });
                loginButton.style.display = 'inline-block';
                loginButton.innerHTML = '<i class="fas fa-sign-in-alt"></i> Anmelden';
                loginButton.onclick = function() {
                    window.location.href = 'login.html#login';
                };
            }
            if (registerButton) {
                registerButton.style.display = 'inline-block';
                registerButton.innerHTML = '<i class="fas fa-user-plus"></i> Registrieren';
                registerButton.onclick = function() {
                    window.location.href = 'login.html#register';
                };
            }

            const logoutButton = document.getElementById('logoutButton');
            if (logoutButton) logoutButton.style.display = 'none';

            if (navButtons) {
                navButtons.style.position = '';
                navButtons.style.left = '';
                navButtons.style.transform = '';
            }
        }
    }

    updateUserInfo();

    function logout() {
        localStorage.removeItem('username');
        localStorage.removeItem('coins');
        localStorage.setItem('activeCardDesign', 'default');
        updateUserInfo();
        window.location.reload();

    }

    function selectCard(card, field, nextTurn) {
        if (isBattleInProgress) return;
        isBattleInProgress = true;


        field.innerHTML = '';
        const selectedCard = createCard(card.name, card.HP, card.Damage, card.type, card.extra);
        selectedCard.classList.add('selected');
        field.appendChild(selectedCard);

        if (field === playerField) {
            playerHand = playerHand.filter(c => c.id !== card.id);
            displayPlayerCards();
        } else {
            computerHand = computerHand.filter(c => c.id !== card.id);
            displayComputerCards();
        }

        selectedCard.style.animation = 'none';
        selectedCard.offsetHeight;
        selectedCard.style.animation = null;
        selectedCard.classList.add('attack-animation');

        setTimeout(async () => {
            selectedCard.classList.remove('attack-animation');
            if (playerField.querySelector('.card') && computerField.querySelector('.card')) {
                await battle();
            } else {
                isBattleInProgress = false;
                nextTurn();
            }
        }, 500);
    }

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

    async function battle() {
        const playerCard = playerField.querySelector('.card');
        const computerCard = computerField.querySelector('.card');

        if (!playerCard || !computerCard) {
            console.error('Cards not found in the fields');
            isBattleInProgress = false;
            endTurn();
            return;
        }

        const playerCardName = playerCard.querySelector('h3').textContent;
        const computerCardName = computerCard.querySelector('h3').textContent;

        const playerCardData = allHeroes.find(card => card.name === playerCardName);
        const computerCardData = allHeroes.find(card => card.name === computerCardName);
        if (!playerCardData || !computerCardData) {
            isBattleInProgress = false;
            endTurn();
            return;
        }

        let firstAttacker, secondAttacker;
        if (isPlayerFirstAttacker) {
            firstAttacker = { card: { ...playerCardData }, name: 'Spieler', field: playerField };
            secondAttacker = { card: { ...computerCardData }, name: 'Computer', field: computerField };
        } else {
            firstAttacker = { card: { ...computerCardData }, name: 'Computer', field: computerField };
            secondAttacker = { card: { ...playerCardData }, name: 'Spieler', field: playerField };
        }

        console.log('Battle started between:', firstAttacker, secondAttacker);

        firstAttacker.field.querySelector('.card').style.zIndex = '10';
        secondAttacker.field.querySelector('.card').style.zIndex = '5';

        const firstDamage = calculateDamage(firstAttacker.card, secondAttacker.card);
        const firstDirectDamage = Math.max(0, firstDamage - secondAttacker.card.HP);
        const isFirstAttackEffective = calculateDamageMultiplier(firstAttacker.card.type, secondAttacker.card.type) > 1;
        const isFirstAttackEffective2 = calculateDamageMultiplier(firstAttacker.card.type, secondAttacker.card.type);
        displayBattleResult(firstAttacker.card, secondAttacker.card, firstDamage, firstDirectDamage, firstAttacker.name, roundCounter, isFirstAttackEffective2);

        if (firstAttacker.name === 'Spieler') {
            totalDamageDealt += firstDamage;
            totalDirectDamageDealt += firstDirectDamage;
        }

        await showDamage(
            firstAttacker.field,
            secondAttacker.field,
            firstDamage,
            isFirstAttackEffective,
            (damage) => {
                secondAttacker.card.HP = Math.max(0, secondAttacker.card.HP - damage);
                updateCardHP(secondAttacker.field, secondAttacker.card.HP);

                if (firstDirectDamage > 0) {
                    if (secondAttacker.name === 'Spieler') {
                        playerHP = Math.max(0, playerHP - firstDirectDamage);
                        showDirectDamageMessage('Spieler', firstDirectDamage);
                    } else {
                        computerHP = Math.max(0, computerHP - firstDirectDamage);
                        showDirectDamageMessage('Computer', firstDirectDamage);
                    }
                    updateHP();
                }
            }
        );

        await new Promise(resolve => setTimeout(resolve, 800));

        if (playerHP <= 0 || computerHP <= 0) {
            await endBattleRound(true);
            return;
        }

        if (secondAttacker.card.HP > 0) {
            secondAttacker.field.querySelector('.card').style.zIndex = '10';
            firstAttacker.field.querySelector('.card').style.zIndex = '5';

            const secondDamage = calculateDamage(secondAttacker.card, firstAttacker.card);
            const secondDirectDamage = Math.max(0, secondDamage - firstAttacker.card.HP);
            const isSecondAttackEffective = calculateDamageMultiplier(secondAttacker.card.type, firstAttacker.card.type) > 1;
            const isSecondAttackEffective2 = calculateDamageMultiplier(secondAttacker.card.type, firstAttacker.card.type);
            displayBattleResult(secondAttacker.card, firstAttacker.card, secondDamage, secondDirectDamage, secondAttacker.name, roundCounter, isSecondAttackEffective2);

            if (secondAttacker.name === 'Spieler') {
                totalDamageDealt += secondDamage;
                totalDirectDamageDealt += secondDirectDamage;
            }

            await showDamage(
                secondAttacker.field,
                firstAttacker.field,
                secondDamage,
                isSecondAttackEffective,
                (damage) => {
                    firstAttacker.card.HP = Math.max(0, firstAttacker.card.HP - damage);
                    updateCardHP(firstAttacker.field, firstAttacker.card.HP);

                    if (secondDirectDamage > 0) {
                        if (firstAttacker.name === 'Spieler') {
                            playerHP = Math.max(0, playerHP - secondDirectDamage);
                            showDirectDamageMessage('Spieler', secondDirectDamage);
                        } else {
                            computerHP = Math.max(0, computerHP - secondDirectDamage);
                            showDirectDamageMessage('Computer', secondDirectDamage);
                        }
                        updateHP();
                    }
                }
            );
            await new Promise(resolve => setTimeout(resolve, 500));
        }
        await endBattleRound(false);
    }

    async function endBattleRound(isQuickEnd) {
        const fadeOutDuration = isQuickEnd ? 1000 : 1000;

        await Promise.all([
            anime({
                targets: playerField.querySelector('.card'),
                opacity: 0,
                scale: 0.8,
                duration: fadeOutDuration,
                easing: 'easeOutQuad'
            }).finished,
            anime({
                targets: computerField.querySelector('.card'),
                opacity: 0,
                scale: 0.8,
                duration: fadeOutDuration,
                easing: 'easeOutQuad'
            }).finished
        ]);

        playerField.innerHTML = '';
        computerField.innerHTML = '';

        isPlayerFirstAttacker = !isPlayerFirstAttacker;
        roundCounter++;

        isBattleInProgress = false;
        if (localStorage.getItem('username') !== null && localStorage.getItem('username') !== "") {savePlayerGame();}
        console.log('Battle ended, starting next turn.');
        endTurn();
    }


    function endTurn() {
        if (playerHP <= 0 || computerHP <= 0) {
            endGame();
            return;
        }

        if (playerHand.length === 0) {
            refillPlayerHand();
        }
        if (computerHand.length === 0) {
            refillComputerHand();
        }
        displayPlayerCards();
        displayComputerCards();
        if (localStorage.getItem('username') !== null && localStorage.getItem('username') !== "") {savePlayerGame();}

        firstAttacker = isPlayerFirstAttacker ? "Spieler" : "Computer";
        turnInfo.textContent = `Wähle deine nächste Karte. ${firstAttacker} greift zuerst an.`;

        playerCardsContainer.style.pointerEvents = 'auto';
        computerCardsContainer.style.pointerEvents = 'auto';
    }

    function showDirectDamageMessage(player, damage) {
        const message = `${player} hat ${damage} HP verloren!`;
        const color = player === 'Spieler' ? 'red' : 'blue';

        const damageMessage = document.createElement('div');
        damageMessage.textContent = message;
        damageMessage.style.position = 'absolute';
        damageMessage.style.top = '50%';
        damageMessage.style.left = '50%';
        damageMessage.style.transform = 'translate(-50%, -50%)';
        damageMessage.style.fontSize = '20px';
        damageMessage.style.fontWeight = 'bold';
        damageMessage.style.color = color;
        damageMessage.style.backgroundColor = 'rgba(255, 255, 255, 0.8)';
        damageMessage.style.padding = '10px 20px';
        damageMessage.style.borderRadius = '10px';
        damageMessage.style.opacity = '0';
        damageMessage.style.transition = 'opacity 0.5s ease-in-out';
        damageMessage.style.zIndex = '1000';

        document.body.appendChild(damageMessage);
        setTimeout(() => {
            damageMessage.style.opacity = '1';
        }, 100);

        setTimeout(() => {
            damageMessage.style.opacity = '0';
        }, 1500);

        setTimeout(() => {
            document.body.removeChild(damageMessage);
        }, 2000);
    }

    function calculateDamage(attacker, defender) {
        const damageMultiplier = calculateDamageMultiplier(attacker.type, defender.type);
        return Math.round(attacker.Damage * damageMultiplier);
    }

    function displayBattleResult(attackerCard, defenderCard, damage, directDamage, attacker, roundNumber, isEffective) {
        const resultElement = document.createElement('div');
        resultElement.className = 'battle-log-entry';
        let resultText = '';

        resultText += `Runde ${roundNumber}: ${attacker} ist am Zug\n\n`;

        resultText += `${attackerCard.name} \n(${attackerCard.HP} HP) / (${attackerCard.Damage} Damage)
        \ngreift\n${defenderCard.name}\n(${defenderCard.HP} HP) / (${defenderCard.Damage} Damage) an.\n\n`;

        let effectivenessText = '';
        if (isEffective > 1) {
            effectivenessText = 'Der Angriff ist sehr effektiv!';
        } else if (isEffective < 1) {
            effectivenessText = 'Der Angriff ist nicht sehr effektiv...';
        }
        if (effectivenessText) {
            resultText += effectivenessText + '\n\n';
        }

        resultText += `${defenderCard.name} erleidet ${damage} Schaden.\n\n`;

        const remainingHP = Math.max(0, defenderCard.HP - damage);
        if (remainingHP > 0) {
            resultText += `${defenderCard.name} hat noch ${remainingHP} HP übrig.\n`;
        } else {
            resultText += `${defenderCard.name} wurde besiegt!\n`;
        }

        if (directDamage > 0) {
            resultText += `\nZusätzlich erleidet der ${attacker === 'Spieler' ? 'Computer' : 'Spieler'}\n${directDamage} direkten Schaden!\n`;
        }

        resultElement.innerText = resultText;
        battleLog.appendChild(resultElement);
        battleLog.scrollTop = battleLog.scrollHeight;
    }

    function createCard(name, hp, damage, element, description) {
        const card = document.createElement('div');
        card.classList.add('card', element.toLowerCase());

        const activeDesign = localStorage.getItem('activeCardDesign');
        if (activeDesign && activeDesign !== 'default') {
            card.classList.add(`premium-design-${activeDesign}`);
        }

        const elementIcon = getElementIcon(element);

        card.innerHTML = `
            <div class="card-header">
                <h3 class="card-name">${name}</h3>
                <i class="${elementIcon} element-icon ${element.toLowerCase()} ${activeDesign ? `premium-design-${activeDesign}` : ''}"></i>
            </div>
            <div class="card-body">
                <p class="stat"><span class="stat-label">HP:</span> <span class="stat-value">${hp}</span></p>
                <p class="stat"><span class="stat-label">Damage:</span> <span class="stat-value">${damage}</span></p>
                <p class="stat"><span class="stat-label">Element:</span> <span class="stat-value">${element}</span></p>
            </div>
            <div class="card-footer">
                <p class="description">${description}</p>
            </div>
            `;
        card.draggable = true;
        return card;
    }

    function getElementIcon(element) {
        const icons = {
            'Feuer': 'fas fa-fire',
            'Wasser': 'fas fa-tint',
            'Pflanze': 'fas fa-leaf',
            'Elektro': 'fas fa-bolt',
            'Erde': 'fas fa-mountain',
            'Luft': 'fas fa-wind',
            'Eis': 'far fa-snowflake'
        };
        return icons[element] || 'fas fa-question';
    }

    function displayPlayerCards() {
        playerCardsContainer.innerHTML = '';
        playerHand.forEach(card => {
            const cardElement = createCard(card.name, card.HP, card.Damage, card.type, card.extra);
            cardElement.addEventListener('click', () => selectCard(card, playerField, computerTurn));

            cardElement.addEventListener('dragstart', dragStart);
            cardElement.addEventListener('dragend', dragEnd);

            playerCardsContainer.appendChild(cardElement);
        });
        updateCardDesign();
    }

    function displayComputerCards() {
        computerCardsContainer.innerHTML = '';
        computerHand.forEach(card => {
            const cardElement = createCard(card.name, card.HP, card.Damage, card.type, card.extra);
            //cardElement.style.backgroundImage = 'url("Kartenrückseite.png")';
            cardElement.style.backgroundSize = 'cover';
            computerCardsContainer.appendChild(cardElement);
        });
        updateCardDesign();
    }

    function updateCardDesign() {
        const activeDesign = localStorage.getItem('activeCardDesign');
        const allCards = document.querySelectorAll('.card');
        const allElementRelationships = document.querySelectorAll('.element-relationship');

        allCards.forEach(card => {
            card.classList.remove('premium-design-1', 'premium-design-2', 'premium-design-3');
            if (activeDesign && activeDesign !== 'default') {
                card.classList.add(`premium-design-${activeDesign}`);
            }
        });

        allElementRelationships.forEach(element => {
            element.classList.remove('premium-design-1', 'premium-design-2', 'premium-design-3');
            if (activeDesign && activeDesign !== 'default') {
                element.classList.add(`premium-design-${activeDesign}`);
            }
        });
    }


    function updateElementRelationships() {
        const elementInfo = document.querySelector('.element-info');
        if (elementInfo) {
            const elements = elementInfo.querySelectorAll('span');
            elements.forEach(element => {
                const elementType = element.textContent.toLowerCase();
                element.className = `element-relationship ${elementType}`;
                if (localStorage.getItem('premiumCardDesign') === 'active') {
                    element.classList.add('premium-card-design');
                } else {
                    element.classList.remove('premium-card-design');
                }
            });
        }
    }

    function computerTurn() {
        if (computerHand.length === 0) {
            refillComputerHand();
        }
        function waehleBesteKarte(computerHand, firstAttacker) {
            let besteKarte = null;

            if (firstAttacker === 'Computer') {
                let maxDamage = -1;
                for (const karte of computerHand) {
                    if (karte.Damage > maxDamage) {
                        maxDamage = karte.Damage;
                        besteKarte = karte;
                    }
                }
            } else {
                let maxHP = -1;
                for (const karte of computerHand) {
                    if (karte.HP > maxHP) {
                        maxHP = karte.HP;
                        besteKarte = karte;
                    }
                }
            }

            return besteKarte;
        }

        const besteKarte = waehleBesteKarte(computerHand, firstAttacker);

        if (besteKarte) {
            selectCard(besteKarte, computerField, () => {
                turnInfo.textContent = 'Wähle deine nächste Karte.';
            });
        } else {
            console.log('Keine passende Karte gefunden.');
            const randomCard = computerHand[Math.floor(Math.random() * computerHand.length)];
            selectCard(randomCard, computerField, () => {
                turnInfo.textContent = 'Wähle deine nächste Karte.';
            });
        }
    }

    function calculateDamageMultiplier(attackerElement, defenderElement) {
        const strengths = {
            'Feuer': 'Pflanze',
            'Wasser': 'Feuer',
            'Pflanze': 'Wasser',
            'Elektro': 'Wasser',
            'Erde': 'Elektro',
            'Luft': 'Erde',
            'Eis' : 'Luft'
        };

        if (strengths[attackerElement] === defenderElement) {
            return 1.5;
        } else if (strengths[defenderElement] === attackerElement) {
            return 0.5;
        }
        return 1;
    }


    function updateHP() {
        playerHP = Math.max(0, playerHP);
        computerHP = Math.max(0, computerHP);
        playerHpValue.textContent = String(playerHP);
        computerHpValue.textContent = String(computerHP);
        playerHpBar.style.width = `${(playerHP / 25) * 100}%`;
        computerHpBar.style.width = `${(computerHP / 25) * 100}%`;
    }
    function updateCardHP(field, newHP) {
        const hpElement = field.querySelector('.card-body .stat:nth-child(1) .stat-value');
        if (hpElement) {
            hpElement.textContent = newHP;
        }
    }

    async function updateStats(username, isWinner, damageDealt, directDamageDealt) {
        try {
            const response = await fetch('/updateStats', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    username: username,
                    win: isWinner ? 1 : 0,
                    lose: isWinner ? 0 : 1,
                    damage: damageDealt,
                    directDamage: directDamageDealt
                }),
            });

            if (!response.ok) {
                console.log('Fehler beim Aktualisieren der Statistiken');
            }

            const result = await response.json();
            console.log('Statistiken aktualisiert:', result);
        } catch (error) {
            console.error('Fehler beim Aktualisieren der Statistiken:', error);
        }
    }

    async function endGame() {
        let message;
        let color;
        let isPlayerWinner = false;

        if (playerHP <= 0 && computerHP <= 0) {
            message = 'Unentschieden!';
            color = 'blue';
        } else if (playerHP <= 0) {
            message = 'Computer gewinnt!';
            color = 'red';
            isPlayerWinner = false;
        } else {
            message = 'Spieler gewinnt!';
            color = 'green';
            isPlayerWinner = true;
            setTimeout(() => {
                createConfetti();
                updateCoins(1);
            }, 0);
        }

        const playerUsername = localStorage.getItem('username');

        if (playerUsername !== '' && playerUsername !== null) {
            await updateStats(
                playerUsername,
                isPlayerWinner,
                totalDamageDealt,
                totalDirectDamageDealt
            );
            await delPlayerGame();
        }

        turnInfo.textContent = message;
        playerCardsContainer.innerHTML = '';
        computerCardsContainer.innerHTML = '';
        document.getElementById('computer-field').innerHTML = '';
        document.getElementById('player-field').innerHTML = '';

        const winMessage = document.createElement('div');
        winMessage.textContent = message;
        winMessage.style.position = 'absolute';
        winMessage.style.top = '50%';
        winMessage.style.left = '50%';
        winMessage.style.transform = 'translate(-50%, -50%)';
        winMessage.style.fontSize = '24px';
        winMessage.style.fontWeight = 'bold';
        winMessage.style.color = color;
        winMessage.style.backgroundColor = 'rgba(255, 255, 255, 0.8)';
        winMessage.style.padding = '10px 20px';
        winMessage.style.borderRadius = '10px';
        winMessage.style.opacity = '0';
        winMessage.style.transition = 'opacity 0.5s ease-in-out';
        winMessage.style.zIndex = '1000';

        document.body.appendChild(winMessage);
        setTimeout(() => {
            winMessage.style.opacity = '1';
        }, 100);

        setTimeout(() => {
            winMessage.style.opacity = '0';
        }, 2000);

        setTimeout(() => {
            document.body.removeChild(winMessage);
        }, 2500);

        playerCardsContainer.style.pointerEvents = 'none';
        computerCardsContainer.style.pointerEvents = 'none';

        const restartButton = document.createElement('button');
        restartButton.innerHTML = '<i class="fas fa-redo"></i> Neu starten';
        restartButton.style.margin = '10px 0';
        restartButton.style.padding = '10px 20px';
        restartButton.style.backgroundColor = '#4CAF50';
        restartButton.style.color = '#fff';
        restartButton.style.border = 'none';
        restartButton.style.borderRadius = '25px';
        restartButton.style.cursor = 'pointer';
        restartButton.style.transition = 'background-color 0.3s ease, transform 0.3s ease';
        restartButton.style.boxShadow = '0 4px 6px rgba(0, 0, 0, 0.1)';
        restartButton.style.display = 'inline-flex';
        restartButton.style.alignItems = 'center';
        restartButton.style.justifyContent = 'center';
        restartButton.style.fontSize = '16px';
        restartButton.style.fontWeight = 'bold';

        restartButton.addEventListener('mouseover', function() {
            this.style.backgroundColor = '#45a049';
            this.style.transform = 'scale(1.05)';
        });
        restartButton.addEventListener('mouseout', function() {
            this.style.backgroundColor = '#4CAF50';
            this.style.transform = 'scale(1)';
        });
        restartButton.addEventListener('click', restartGame);

        const icon = restartButton.querySelector('i');
        icon.style.marginRight = '8px';

        turnInfo.appendChild(restartButton);
    }

    function restartGame() {
        playerHP = 25;
        computerHP = 25;
        updateHP();
        playerHand = [];
        computerHand = [];
        playerCardsContainer.style.pointerEvents = 'auto';
        computerCardsContainer.style.pointerEvents = 'auto';
        initializeGame();
        turnInfo.textContent = 'Wähle deine erste Karte. Spieler greift als erstes an!';
        roundCounter = 1;
        battleLog.innerHTML = '';
    }

    function refillPlayerHand() {
        const remainingHeroes = allHeroes.filter(hero => !playerHand.includes(hero) && !computerHand.includes(hero));
        shuffleArray(remainingHeroes);
        playerHand = remainingHeroes.slice(0, 5)
    }

    function refillComputerHand() {
        const remainingHeroes = allHeroes.filter(hero => !playerHand.includes(hero) && !computerHand.includes(hero));
        shuffleArray(remainingHeroes);
        computerHand = remainingHeroes.slice(0, 5);
    }

    function shuffleArray(array) {
        for (let i = array.length - 1; i > 0; i--) {
            const j = Math.floor(Math.random() * (i + 1));
            [array[i], array[j]] = [array[j], array[i]];
        }
    }

    async function showDamage(attackerField, defenderField, damage, isEffective, updateHP) {
        const attackerCard = attackerField.querySelector('.card');
        const defenderCard = defenderField.querySelector('.card');
        if (!attackerCard || !defenderCard) return;

        const defenderCardBody = defenderCard.querySelector('.card-body');
        if (!defenderCardBody) return;

        attackerCard.style.zIndex = '10';
        defenderCard.style.zIndex = '5';

        await anime({
            targets: attackerCard,
            scale: 1.2,
            duration: 300,
            easing: 'easeOutQuad'
        }).finished;

        const originalPosition = attackerCard.getBoundingClientRect();
        const targetPosition = defenderCard.getBoundingClientRect();

        const moveX = targetPosition.left - originalPosition.left;
        const moveY = targetPosition.top - originalPosition.top;

        await anime({
            targets: attackerCard,
            translateX: moveX,
            translateY: moveY,
            duration: 500,
            easing: 'easeInOutQuad'
        }).finished;


        if (isEffective) {
            await showElementalExplosion(attackerCard, defenderCard, 0.5);
        }

        updateHP(damage);

        for (let i = 0; i < 5; i++) {
            const damageElement = document.createElement('div');
            damageElement.classList.add('damage-indicator');
            damageElement.textContent = `-${damage}`;

            const randomX = Math.random() * 100 - 50;
            const randomY = Math.random() * 100 - 50;

            damageElement.style.position = 'absolute';
            damageElement.style.top = `calc(50% + ${randomY}px)`;
            damageElement.style.left = `calc(50% + ${randomX}px)`;
            damageElement.style.transform = 'translate(-50%, -50%)';
            damageElement.style.opacity = '0';
            damageElement.style.fontSize = '24px';
            damageElement.style.fontWeight = 'bold';
            damageElement.style.color = isEffective ? 'yellow' : 'red';
            damageElement.style.textShadow = '2px 2px 4px rgba(0,0,0,0.5)';
            damageElement.style.zIndex = '15';

            defenderCardBody.appendChild(damageElement);

            anime({
                targets: damageElement,
                opacity: [0, 1, 0],
                translateY: '-=30',
                duration: 2000,
                easing: 'easeOutQuad',
                delay: i * 100,
                complete: () => damageElement.remove()
            });
        }

        await anime({
            targets: document.body,
            translateX: [
                {value: -10},
                {value: 10},
                {value: -10},
                {value: 10},
                {value: 0}
            ],
            duration: 500,
            easing: 'easeInOutQuad'
        }).finished;

        await anime({
            targets: attackerCard,
            translateX: 0,
            translateY: 0,
            scale: 1,
            duration: 500,
            easing: 'easeOutQuad'
        }).finished;

        attackerCard.style.zIndex = '';
        defenderCard.style.zIndex = '';
    }

    function showElementalExplosion(attackerCard, defenderCard, speedFactor = 1) {
        return new Promise((resolve) => {
            const container = document.createElement('div');
            container.style.position = 'fixed';
            container.style.top = '0';
            container.style.left = '0';
            container.style.width = '100%';
            container.style.height = '100%';
            container.style.pointerEvents = 'none';
            container.style.zIndex = '1000';
            document.body.appendChild(container);

            const adjustDuration = (duration) => duration * speedFactor;

            anime({
                targets: attackerCard,
                scale: 1.2,
                duration: adjustDuration(300),
                easing: 'easeOutQuad',
                complete: () => {
                    const flash = document.createElement('div');
                    flash.style.position = 'absolute';
                    flash.style.top = '0';
                    flash.style.left = '0';
                    flash.style.width = '100%';
                    flash.style.height = '100%';
                    flash.style.backgroundColor = 'white';
                    flash.style.opacity = '0';
                    attackerCard.appendChild(flash);

                    anime({
                        targets: flash,
                        opacity: [0, 1, 0],
                        duration: adjustDuration(300),
                        easing: 'easeOutQuad',
                        complete: () => {
                            flash.remove();
                            createElementalSpheres();
                        }
                    });
                }
            });

            function createElementalSpheres() {
                const elements = ['fire', 'water', 'lightning', 'earth', 'air'];
                const spheres = elements.map(element => {
                    const sphere = document.createElement('div');
                    sphere.classList.add('elemental-sphere', element);
                    sphere.style.position = 'absolute';
                    sphere.style.width = '20px';
                    sphere.style.height = '20px';
                    sphere.style.borderRadius = '50%';
                    sphere.style.opacity = '0';
                    container.appendChild(sphere);
                    return sphere;
                });

                const attackerRect = attackerCard.getBoundingClientRect();
                const centerX = attackerRect.left + attackerRect.width / 2;
                const centerY = attackerRect.top + attackerRect.height / 2;

                anime({
                    targets: spheres,
                    translateX: (el, i) => [
                        {value: centerX + Math.cos(i * Math.PI * 2 / 5) * 50},
                        {value: centerX}
                    ],
                    translateY: (el, i) => [
                        {value: centerY + Math.sin(i * Math.PI * 2 / 5) * 50},
                        {value: centerY}
                    ],
                    scale: [0, 1],
                    opacity: [0, 1],
                    duration: adjustDuration(100),
                    easing: 'easeOutQuad',
                    complete: chargeSpheres
                });
            }

            function chargeSpheres() {
                const spheres = container.querySelectorAll('.elemental-sphere');
                anime({
                    targets: spheres,
                    scale: [1, 1.2, 1],
                    opacity: [1, 0.8, 1],
                    duration: adjustDuration(1000),
                    easing: 'easeInOutQuad',
                    loop: 2,
                    complete: triggerExplosion
                });
            }

            function triggerExplosion() {
                const explosion = document.createElement('div');
                explosion.style.position = 'absolute';
                explosion.style.top = '50%';
                explosion.style.left = '50%';
                explosion.style.width = '0';
                explosion.style.height = '0';
                explosion.style.borderRadius = '50%';
                explosion.style.backgroundColor = 'white';
                explosion.style.transform = 'translate(-50%, -50%)';
                container.appendChild(explosion);

                anime({
                    targets: explosion,
                    width: '300%',
                    height: '300%',
                    opacity: [1, 0],
                    duration: adjustDuration(1000),
                    easing: 'easeOutQuad',
                    complete: () => {
                        container.remove();
                        resolve();
                    }
                });
            }
        });
    }

    function dragStart(e) {
        e.dataTransfer.setData('text/plain', e.target.querySelector('h3').textContent);
        setTimeout(() => {
            e.target.style.opacity = '0.5';
        }, 0);
    }

    function dragEnd(e) {
        e.target.style.opacity = '1';
    }

    playerField.addEventListener('dragover', dragOver);
    playerField.addEventListener('dragenter', dragEnter);
    playerField.addEventListener('dragleave', dragLeave);
    playerField.addEventListener('drop', drop);

    function dragOver(e) {
        e.preventDefault();
    }

    function dragEnter(e) {
        e.preventDefault();
        playerField.classList.add('drag-over');
    }

    function dragLeave() {
        playerField.classList.remove('drag-over');
    }

    function drop(e) {
        e.preventDefault();
        playerField.classList.remove('drag-over');
        const cardName = e.dataTransfer.getData('text');
        const card = playerHand.find(c => c.name === cardName);
        if (card) {
            selectCard(card, playerField, computerTurn);
        }
    }

    function initializeDragAndDrop() {
        const playerCards = document.querySelectorAll('.player-card');
        playerCards.forEach(card => {
            card.setAttribute('draggable', true);
            card.addEventListener('dragstart', dragStart);
            card.addEventListener('dragend', dragEnd);
        });

        playerField.addEventListener('dragover', dragOver);
        playerField.addEventListener('dragenter', dragEnter);
        playerField.addEventListener('dragleave', dragLeave);
        playerField.addEventListener('drop', drop);
    }

    let generatedGame = true;

    function initializeGame() {
        const username = localStorage.getItem('username');
        if (!username) {
            fetch('/heroshow')
                .then(response => response.json())
                .then(heroes => {
                    allHeroes = heroes;
                    playerHand = allHeroes.sort(() => 0.5 - Math.random()).slice(0, 5);
                    computerHand = allHeroes.sort(() => 0.5 - Math.random()).slice(0, 5);
                    if (!username){
                        savePlayerGame();
                    }
                    startGame();
                })
                .catch(error => console.error('Fehler beim Laden der Helden:', error));
            return;
        }

        fetch('/heroshow')
            .then(response => response.json())
            .then(heroes => {
                allHeroes = heroes;
            })
            .catch(error => console.error('Fehler beim Laden der Helden:', error));

        fetch('/checkGame', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ username: username })
        })
            .then(response => response.json())
            .then(data => {
                console.log('Antwort vom Server:', data);

                if (data.Playercards && data.Computercards) {
                    console.log('Gespeichertes Spiel gefunden.');
                    playerHand = data.Playercards;
                    computerHand = data.Computercards;
                    playerHP = data.PHP;
                    computerHP = data.CHP;
                    generatedGame = false;
                    savePlayerGame();
                    startGame();
                } else {
                    console.log('Kein gespeichertes Spiel gefunden. Generiere neue Karten.');
                    fetch('/heroshow')
                        .then(response => response.json())
                        .then(heroes => {
                            allHeroes = heroes;
                            playerHand = allHeroes.sort(() => 0.5 - Math.random()).slice(0, 5);
                            computerHand = allHeroes.sort(() => 0.5 - Math.random()).slice(0, 5);
                            if (username){
                                savePlayerGame();
                            }
                            startGame();
                        })
                        .catch(error => console.error('Fehler beim Laden der Helden:', error));
                }
            })
            .catch(error => console.error('Fehler beim Überprüfen des gespeicherten Spiels:', error));
    }

    function startGame() {
        displayPlayerCards();
        displayComputerCards();
        updateHP();
        turnInfo.textContent = `Wähle deine erste Karte. Spieler greift als erstes an!`;
        updateUserInfo();
        initializeDragAndDrop();
    }

    const style = document.createElement('style');
    style.textContent = `
    .drag-over {
        border: 2px dashed #000;
        background-color: grey;
    }
`;
    document.head.appendChild(style);

    const backButton = document.getElementById('backbutton');
    backButton.addEventListener('click', function() {
        window.location.href = 'index.html';
    });

    initializeGame();
});

